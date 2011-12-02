package com.allrightsms;

import java.util.Date;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;

//implemented by Bruno
public class SendSMS extends Activity {

	AllRightSMSActivity activity;

	public SendSMS() {

	}

	public boolean Send(AllRightSMSActivity ctx, String phoneNumber,
			String message, Date date) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(
				SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0,
				new Intent(DELIVERED), 0);

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		registerToDevice(phoneNumber, message, date);

		return true;
	}

	private void registerToDevice(String to, String text, Date date) {
		String ADDRESS = "address";
		String PERSON = "person";
		String DATE = "date";
		String READ = "read";
		String STATUS = "status";
		String TYPE = "type";
		String BODY = "body";
		int MESSAGE_TYPE_INBOX = 1;
		int MESSAGE_TYPE_SENT = 2;

		ContentValues values = new ContentValues();
		values.put(ADDRESS, to);
		values.put(DATE, date.getTime()); // android prende la data in
											// millisecondi.
		values.put(READ, 1);
		values.put(STATUS, -1);
		values.put(TYPE, 2);
		values.put(BODY, text);
		Uri inserted = getContentResolver().insert(Uri.parse("content://sms"),
				values);
	}

	/*
	 * Codice per monitorare lo stato del SMS da inviare String SENT =
	 * "SMS_SENT";
	 * 
	 * PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this,
	 * SendSMS.class), 0);
	 * 
	 * //---when the SMS has been sent--- registerReceiver(new
	 * BroadcastReceiver(){
	 * 
	 * @Override public void onReceive(Context arg0, Intent arg1) { switch
	 * (getResultCode()) { case Activity.RESULT_OK:
	 * Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
	 * break; case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	 * Toast.makeText(getBaseContext(), "Generic failure",
	 * Toast.LENGTH_SHORT).show(); break; case
	 * SmsManager.RESULT_ERROR_NO_SERVICE: Toast.makeText(getBaseContext(),
	 * "No service", Toast.LENGTH_SHORT).show(); break; case
	 * SmsManager.RESULT_ERROR_NULL_PDU: Toast.makeText(getBaseContext(),
	 * "Null PDU", Toast.LENGTH_SHORT).show(); break; case
	 * SmsManager.RESULT_ERROR_RADIO_OFF: Toast.makeText(getBaseContext(),
	 * "Radio off", Toast.LENGTH_SHORT).show(); break; } } }, new
	 * IntentFilter(SENT));
	 * 
	 * SmsManager sms = SmsManager.getDefault();
	 * sms.sendTextMessage(phoneNumber, null, message, pi, null); return true;
	 */

}
