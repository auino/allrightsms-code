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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.allrightsms.SmsApplication.SmsListener;
import com.allrightsms.client.MyRequestFactory;
import com.allrightsms.client.MyRequestFactory.HelloWorldRequest;
import com.allrightsms.shared.AllRightSMSRequest;
import com.allrightsms.shared.NumberUtility;
import com.allrightsms.shared.SmsChange;
import com.allrightsms.shared.SmsProxy;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

/**
 * Main activity - requests "Hello, World" messages from the server and provides
 * a menu item to invoke the accounts activity.
 */
public class AllRightSMSActivity extends Activity {
	/**
	 * Tag for logging.
	 */
	private static final String TAG = "AllRightSMSActivity";
	private AsyncFetchSMS asyncFetch;
	private String mex ="";
	private String number = "";
	private Button syncContactButton;
	private TextView synchronizationText;
	
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

			mex = "";
			number ="";
			Object messages[] = (Object[]) bundle.get("pdus");
			SmsMessage smsMessage[] = new SmsMessage[messages.length];
			for (int n = 0; n < messages.length; n++) {
				smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
			}
			// mi segno il numero e il testo del messaggio
			for (SmsMessage s : smsMessage) { //concateno il messaggio anche se è piu lungo del previsto.
				mex += s.getMessageBody();
			}
			
			number = smsMessage[0].getOriginatingAddress();
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... arg0) {

					MyRequestFactory requestFactory = Util.getRequestFactory(
							mContext, MyRequestFactory.class);
					AllRightSMSRequest request = requestFactory
							.allRightSMSRequest();
					SmsProxy sms = request.create(SmsProxy.class);
					sms.setEmailAddress("");
					sms.setPhoneNumber(NumberUtility.purgePrefix(number));
					sms.setTextmessage(mex);
					sms.setRead(false);
					sms.setReceived(true);
					sms.setSync(true); // importante per non farsi rimandare il
										// C2DM
					sms.setName(addContacts.retrieveName(mContext, number));

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
		//se imposto il service, qui devo killarlo
//		Intent svc = new Intent(this, MyService.class);
//		stopService(svc);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		// Invoke the Register activity
		menu.getItem(0).setIntent(new Intent(this, AccountsActivity.class));
		return true;
	}

	private void setAllRightSmsScreen() {
		setContentView(R.layout.hello_world);

		final TextView helloWorld = (TextView) findViewById(R.id.hello_world);
		final Button testConnection = (Button) findViewById(R.id.say_hello);
		syncContactButton = (Button) findViewById(R.id.sync_button);
		synchronizationText = (TextView) findViewById(R.id.sync_text);
		
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
		
		syncContactButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sincronizeContact();
			}});
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
			setAllRightSmsScreen();
			break;
		}
	}

	//metodi per la sincronizzazione dei contatti col server
	private void sincronizeContact()
	{
    	SharedPreferences prefs = Util.getSharedPreferences(mContext);
		String connectionStatus = prefs.getString(Util.CONNECTION_STATUS,
				Util.CONNECTED);
		
		if (Util.CONNECTED.equals(connectionStatus)) {
			synchronizationText.setText(R.string.syncing_contacts);
			ManageContacts man = new ManageContacts();
			syncContactButton.setEnabled(false);
    		man.retrieveAllContact(this);	
		}	
	}
	
	public void ContactSyncronized(){
		syncContactButton.setEnabled(true);
		synchronizationText.setText(R.string.syncing_contacts_empty);
		Util.generateToastNotification(getApplicationContext(), "Contacts Synchronized");
	}
	
	// metodi per recuperare il nuovo SMS dal server
	private void fetchSMS(long id) {
		asyncFetch = new AsyncFetchSMS(this);
		asyncFetch.execute(id);
	}

}
