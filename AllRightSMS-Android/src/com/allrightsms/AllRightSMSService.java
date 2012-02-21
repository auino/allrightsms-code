package com.allrightsms;

import java.util.Date;

import com.allrightsms.client.MyRequestFactory;
import com.allrightsms.shared.AllRightSMSRequest;
import com.allrightsms.shared.SmsProxy;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class AllRightSMSService extends Service {

	private String mex;
	private String number;
	private Context mContext;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.v("test", "Registrazione Service Bind");

		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent != null) {
			mContext = getBaseContext();
			
			Log.v("test", "Registrazione Service");
			Toast.makeText(mContext, "Registrazione Service", Toast.LENGTH_SHORT).show();
			Runnable r = new Runnable() {

				public void run() {
					registerReceiver(smsReceiver, new IntentFilter(
							Util.RECEIVE_SMS));
				}
			};

			new Thread(r).start();
			
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private final BroadcastReceiver smsReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();

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
					// sms = request.edit(smsProxy);
					sms.setDueDate(new Date());
					sms.setEmailAddress("");
					sms.setPhoneNumber(number);
					sms.setTextmessage(mex);
					sms.setReceived(true);
					sms.setSync(true);// importante per non farsi rimandare il
										// C2DM

					request.updateSms(sms).fire();

					return null;
				}

			}.execute();

		}
	};

}
