package com.allrightsms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;

//implemented by Bruno
public class SendSMS extends Activity{

	AllRightSMSActivity activity;
	public SendSMS(){
		
	}
	
	public boolean Send(AllRightSMSActivity ctx, String phoneNumber, String message) {
		               //this.ctx = ctx;    
		               String SENT = "SMS_SENT";
		               String DELIVERED = "SMS_DELIVERED";
		               
		PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0, new Intent(DELIVERED), 0);
		           
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	
		return true;
		       
	}
			
/*		Codice per monitorare lo stato del SMS da inviare
 * 		String SENT = "SMS_SENT";
		
		 PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, SendSMS.class), 0);
		
		//---when the SMS has been sent---
		 registerReceiver(new BroadcastReceiver(){
	            @Override
	            public void onReceive(Context arg0, Intent arg1) {
	                switch (getResultCode())
	                {
	                    case Activity.RESULT_OK:
	                        Toast.makeText(getBaseContext(), "SMS sent", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	                        Toast.makeText(getBaseContext(), "Generic failure", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_NO_SERVICE:
	                        Toast.makeText(getBaseContext(), "No service", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_NULL_PDU:
	                        Toast.makeText(getBaseContext(), "Null PDU", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_RADIO_OFF:
	                        Toast.makeText(getBaseContext(), "Radio off", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                }
	            }
	        }, new IntentFilter(SENT));
		
		 SmsManager sms = SmsManager.getDefault();
		 sms.sendTextMessage(phoneNumber, null, message, pi, null);
		return true;
*/
	
	
}
