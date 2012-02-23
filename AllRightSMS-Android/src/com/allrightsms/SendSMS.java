package com.allrightsms;

import java.util.ArrayList;
import java.util.Date;

import com.allrightsms.shared.SmsProxy;
import com.allrightsms.shared.Cripto.AES;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;

//implemented by Bruno
public class SendSMS extends Activity {

	private AllRightSMSActivity activity;
	private AES aes;

	public SendSMS(AllRightSMSActivity ctx) {
		activity = ctx;
	}

	public SendSMS(AllRightSMSActivity ctx, AES key) {
		activity = ctx;
		aes = key;
	}

	public boolean Sent(SmsProxy sms) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		SmsManager smsMan = SmsManager.getDefault();
		String smsText = aes.decryptBase64(sms.getTextmessage());

		ArrayList<String> messages = smsMan.divideMessage(smsText);
		ArrayList<PendingIntent> sendIntent = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> deliveryIntent = new ArrayList<PendingIntent>();

		for (String s : messages) {

			sendIntent.add(PendingIntent.getBroadcast(activity, 0, new Intent(
					SENT), 0));
			deliveryIntent.add(PendingIntent.getBroadcast(activity, 0,
					new Intent(DELIVERED), 0));
		}

		try {
			// supporta anche invio di sms multipli...
			smsMan.sendMultipartTextMessage(sms.getPhoneNumber(), null,
					messages, sendIntent, deliveryIntent);

		} catch (Exception e) {
			return false;
		}
		// registra il messaggio anche sul cell, 
		//in modo da poterlo vedere
		registerToDevice(sms.getPhoneNumber(), smsText, sms.getDueDate()); 

		return true;
	}
	
	private void registerToDevice(String to, String text, Date date) {
		String ADDRESS = "address";
		String DATE = "date";
		String READ = "read";
		String STATUS = "status";
		String TYPE = "type";
		String BODY = "body";

		ContentValues values = new ContentValues();
		values.put(ADDRESS, to);
		values.put(DATE, date.getTime()); // android prende la data in
											// millisecondi.
		values.put(READ, 0);
		values.put(STATUS, -1);
		values.put(TYPE, 2);
		values.put(BODY, text);

		Uri inserted = activity.getContentResolver().insert(
				Uri.parse("content://sms/sent"), values);
	}
}
