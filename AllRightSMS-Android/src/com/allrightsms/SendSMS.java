package com.allrightsms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.allrightsms.shared.SmsProxy;

import android.R.bool;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;

//implemented by Bruno
public class SendSMS extends Activity {

	AllRightSMSActivity activity;

	public SendSMS(AllRightSMSActivity ctx) {
		activity = ctx;
	}

	public boolean Sent(SmsProxy sms) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		if (Simulator.status)
			return false;

		SmsManager smsMan = SmsManager.getDefault();
		ArrayList<String> messages = smsMan.divideMessage(sms.getTextmessage());
		ArrayList<PendingIntent> sendIntent = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> deliveryIntent = new ArrayList<PendingIntent>();

		for (String s : messages) {

			sendIntent.add(PendingIntent.getBroadcast(activity, 0, new Intent(
					SENT), 0));
			deliveryIntent.add(PendingIntent.getBroadcast(activity, 0,
					new Intent(DELIVERED), 0));
		}
		
		try {
			//supporta anche invio di sms multipli... 
			smsMan.sendMultipartTextMessage(sms.getPhoneNumber(), null, messages, sendIntent, deliveryIntent); 
			
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		// registra il messaggio anche sul cell, in modo da poterlo vedere
		registerToDevice(sms.getPhoneNumber(), sms.getTextmessage(), sms.getDueDate()); //dovrebbe servire per
		

		return true;
	}

	public boolean Send(AllRightSMSActivity ctx, String phoneNumber,
			String message, Date date) {
		activity = ctx;
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		if (Simulator.status)
			return false;

		PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(
				SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0,
				new Intent(DELIVERED), 0);

		try {
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		// registerToDevice(phoneNumber, message, date); //dovrebbe servire per
		// registrare il messaggio anche sul cell

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
