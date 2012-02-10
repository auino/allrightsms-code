/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.allrightsms;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import com.allrightsms.SmsApplication.SmsListener;
import com.allrightsms.client.MyRequestFactory;
import com.allrightsms.client.MyRequestFactory.HelloWorldRequest;
import com.allrightsms.shared.AllRightSMSRequest;
import com.allrightsms.shared.NumberUtility;
import com.allrightsms.shared.SmsChange;
import com.allrightsms.shared.SmsProxy;

import android.R.bool;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.telephony.SmsMessage;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main activity - requests "Hello, World" messages from the server and provides
 * a menu item to invoke the accounts activity.
 */
public class AllRightSMSActivity extends Activity {
	/**
	 * Tag for logging.
	 */
	private static final String TAG = "AllRightSMSActivity";
	private static final int TO_CONNECT = 1;
	private static final int TO_DISCONNECT = 1;
	private static final long STATUS_DELAY = 4000;
	private AsyncFetchSMS asyncFetch;
	private List<SmsProxy> newSms;

	private String mex;
	private String number;
	/**
	 * The current context.
	 */
	private Context mContext = this;

	/**
	 * A {@link BroadcastReceiver} to receive the response from a register or
	 * unregister request, and to update the UI.
	 */
	private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String accountName = intent
					.getStringExtra(DeviceRegistrar.ACCOUNT_NAME_EXTRA);
			int status = intent.getIntExtra(DeviceRegistrar.STATUS_EXTRA,
					DeviceRegistrar.ERROR_STATUS);
			String message = null;
			String connectionStatus = Util.DISCONNECTED;
			if (status == DeviceRegistrar.REGISTERED_STATUS) {
				message = getResources().getString(
						R.string.registration_succeeded);
				connectionStatus = Util.CONNECTED;
			} else if (status == DeviceRegistrar.UNREGISTERED_STATUS) {
				message = getResources().getString(
						R.string.unregistration_succeeded);
			} else {
				message = getResources().getString(R.string.registration_error);
			}

			// Set connection status
			SharedPreferences prefs = Util.getSharedPreferences(mContext);
			prefs.edit().putString(Util.CONNECTION_STATUS, connectionStatus)
					.commit();

			// Display a notification
			Util.generateNotification(mContext,
					String.format(message, accountName), false);
		}
	};

	private final BroadcastReceiver smsReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			final ManageContacts addContacts = new ManageContacts();
			
			Object messages[] = (Object[]) bundle.get("pdus");
			SmsMessage smsMessage[] = new SmsMessage[messages.length];
			for (int n = 0; n < messages.length; n++) {
				smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
			}
			// mi segno il numero e il testo del messaggio
			mex = smsMessage[0].getMessageBody();
			number = smsMessage[0].getOriginatingAddress();
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... arg0) {

					MyRequestFactory requestFactory = Util.getRequestFactory(
							mContext, MyRequestFactory.class);
					AllRightSMSRequest request = requestFactory
							.allRightSMSRequest();
					SmsProxy sms = request.create(SmsProxy.class);
					// sms.setDueDate(new Date()); //già creato dal server
					sms.setEmailAddress("");
					sms.setPhoneNumber(NumberUtility.purgePrefix(number));
					sms.setTextmessage(mex);
					sms.setRead(false);
					sms.setReceived(true);
					
					sms.setName(addContacts.retrieveName(mContext, number));
					
					// importante per non farsi rimandare il C2DM
					sms.setSync(true);

					request.updateSms(sms).fire();

					return null;
				}

			}.execute();

		}
	};

	/**
	 * Begins the activity.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		// Register a receiver to provide register/unregister notifications
		registerReceiver(mUpdateUIReceiver, new IntentFilter(
				Util.UPDATE_UI_INTENT));

		// Register a receiver to capture incoming sms
		registerReceiver(smsReceiver, new IntentFilter(Util.RECEIVE_SMS));
	}

	@Override
	public void onResume() {
		super.onResume();

		// inizio modifiche
		SmsApplication smsApplication = (SmsApplication) getApplication();
		smsApplication.setSMSListener(new SmsListener() {
			public void onSmsUpdated(final String message, final long id) {
				runOnUiThread(new Runnable() {
					public void run() {
						if (SmsChange.NEWSMS.equals(message)) {
							fetchSMS(id);
						}
					}

				});
			}
		});
		// fine modifiche

		SharedPreferences prefs = Util.getSharedPreferences(mContext);
		String connectionStatus = prefs.getString(Util.CONNECTION_STATUS,
				Util.DISCONNECTED);
		if (Util.DISCONNECTED.equals(connectionStatus)) {

			startActivity(new Intent(this, AccountsActivity.class));
		}
		
		setScreenContent(R.layout.hello_world);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// DEBUG
		// SmsApplication smsApp = (SmsApplication) getApplication();
		// smsApp.setSMSListener(null);

		// creo un intent per ricevere sms anche dopo aver chiuso
		// l'applicazione...
		// inserito in data 25/11 per ricevere sms, start Service
		// Intent serviceIntent = new
		// Intent(getBaseContext(),AllRightSMSService.class);
		// serviceIntent.putExtra("text", _text); //passaggio di parametri
		// serviceIntent.putExtra("num", _num); //passaggio di parametri
		// Toast.makeText(context, "Invio in Corso...",
		// Toast.LENGTH_SHORT).show(); //messaggio di testo
		// startService(serviceIntent);
	}

	/**
	 * Shuts down the activity.
	 */
	@Override
	public void onDestroy() {
		unregisterReceiver(mUpdateUIReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		// Invoke the Register activity
		menu.getItem(0).setIntent(new Intent(this, AccountsActivity.class));
		return true;
	}

	// Manage UI Screens
	/*
	 * private void setHelloWorldScreenContent() {
	 * setContentView(R.layout.hello_world);
	 * 
	 * final TextView helloWorld = (TextView) findViewById(R.id.hello_world);
	 * final Button sayHelloButton = (Button) findViewById(R.id.say_hello);
	 * sayHelloButton.setOnClickListener(new OnClickListener() { public void
	 * onClick(View v) { sayHelloButton.setEnabled(false);
	 * helloWorld.setText(R.string.contacting_server);
	 * 
	 * // Use an AsyncTask to avoid blocking the UI thread new AsyncTask<Void,
	 * Void, String>() { private String message;
	 * 
	 * @Override protected String doInBackground(Void... arg0) {
	 * 
	 * MyRequestFactory requestFactory = Util .getRequestFactory(mContext,
	 * MyRequestFactory.class); final HelloWorldRequest request = requestFactory
	 * .helloWorldRequest(); Log.i(TAG, "Sending request to server");
	 * request.getMessage().fire(new Receiver<String>() {
	 * 
	 * @Override public void onFailure(ServerFailure error) { message =
	 * "Failure: " + error.getMessage(); }
	 * 
	 * @Override public void onSuccess(String result) { message = result; } });
	 * return message; }
	 * 
	 * @Override protected void onPostExecute(String result) {
	 * helloWorld.setText(result); sayHelloButton.setEnabled(true); }
	 * }.execute(); } }); }
	 */

	private void setAllRightSmsScreen() {
		setContentView(R.layout.hello_world);

		final TextView helloWorld = (TextView) findViewById(R.id.hello_world);
		final Button testConnection = (Button) findViewById(R.id.say_hello);

		testConnection.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				testConnection.setEnabled(false);
				helloWorld.setText(R.string.contacting_server);

				// Use an AsyncTask to avoid blocking the UI thread
				new AsyncTask<Void, Void, String>() {
					private String message;

					@Override
					protected String doInBackground(Void... arg0) {

						MyRequestFactory requestFactory = Util
								.getRequestFactory(mContext,
										MyRequestFactory.class);
						final HelloWorldRequest request = requestFactory
								.helloWorldRequest();
						Log.i(TAG, "Sending request to server");
						request.getMessage().fire(new Receiver<String>() {
							@Override
							public void onFailure(ServerFailure error) {
								message = "Failure: " + error.getMessage();
							}

							@Override
							public void onSuccess(String result) {
								message = result;
							}
						});
						return message;
					}

					@Override
					protected void onPostExecute(String result) {
						helloWorld.setText(result);
						// Timer t = new Timer();
						// t.schedule(AllRightSMSActivity.this.t, 300,
						// STATUS_DELAY);
						testConnection.setEnabled(true);
					}
				}.execute();
			}
		});
		
		
		//startActivity(new Intent(this, AccountsActivity.class));
		
	//	startActivityForResult(new Intent(this,ManageContacts.class), 0);
	}

	// private final TimerTask t = new TimerTask() {
	// final TextView helloWorld = (TextView) findViewById(R.id.hello_world);
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// helloWorld.setText(" ");
	// Log.i(TAG, "Sticazzi come mai va il log e non il settext???");
	// }
	// };

	/**
	 * Sets the screen content based on the screen id.
	 */
	private void setScreenContent(int screenId) {
		setContentView(screenId);
		switch (screenId) {
		case R.layout.hello_world:
			// setHelloWorldScreenContent();
			setAllRightSmsScreen();
			break;
		}
	}

	// metodi per invio SMS
	private void fetchSMS(long id) {
		asyncFetch = new AsyncFetchSMS(this);
		asyncFetch.execute(id);
	}

	public void sendSMS(List<SmsProxy> sms2send) {
		// code to send SMS here
		if (!sms2send.isEmpty()) {
			SendSMS sendsms = new SendSMS();
			ManageContacts addContacts = new ManageContacts();
			boolean success = false;
			boolean successupdate = false;
			// SMSs to send!
			newSms = sms2send;
			int size = newSms.size();
			int i = 1;
			for (SmsProxy sms : newSms) {
				if (size > 1) // se ci sono più messaggi inserisce 1/2
										// davanti
				{
					//Log.i(TAG, "[" + i + "/" + size + "]");
					success = sendsms.Send(this, sms.getPhoneNumber(), "[" + i + "/"
							+ size + "]" + sms.getTextmessage(),
							sms.getDueDate());// ritorna un booleano
					i++;
					if (!success)
						break;
				} else {
					//invio di un messaggio senza problemi
					// Log.i(TAG, "Unico Messaggio");
					success = sendsms.Send(this, sms.getPhoneNumber(),
							sms.getTextmessage(), sms.getDueDate());// ritorna
																	// un booleano
					//aggiunto in data 10/02/2012
			//		successupdate = addContacts.addNameToSms(this, sms);										
				}
			}
			if (success){
				Util.generateNotification(mContext, "New SMS sent correctly!",
						false);
			}	
			else
				Util.generateNotification(mContext,
						"New SMS Not sent correctly!", false);
		} else {
			// nothing to do!
			Util.generateNotification(mContext,
					"Unable to send new SMS or No sms to send!", false);
		}
	}
	
	private void updateSms(SmsProxy sms)
	{
		ManageContacts addContacts = new ManageContacts();
		MyRequestFactory requestFactory = Util.getRequestFactory(mContext, MyRequestFactory.class);
		AllRightSMSRequest request = requestFactory.allRightSMSRequest();
	
		sms.setName(addContacts.retrieveName(mContext, number));
		request.updateSms(sms).fire();
		
	}
}
