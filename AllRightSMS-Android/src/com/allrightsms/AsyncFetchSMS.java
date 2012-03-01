package com.allrightsms;

import android.os.AsyncTask;
import com.allrightsms.client.MyRequestFactory;
import com.allrightsms.shared.AllRightSMSRequest;
import com.allrightsms.shared.SmsProxy;
import com.allrightsms.shared.Cripto.AES;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

public class AsyncFetchSMS extends AsyncTask<Long, Void, SmsProxy> {

	private final AllRightSMSActivity activity;
	private AES aes;
	private SmsProxy sms;
	
	public AsyncFetchSMS(AllRightSMSActivity activity) {
		super();
		this.activity = activity;
	}
	
	public AsyncFetchSMS(AllRightSMSActivity activity, AES key) {
		super();
		aes = key;
		this.activity = activity;
	}

	@Override
	protected SmsProxy doInBackground(Long... arguments) {
		

		MyRequestFactory requestFactory = Util.getRequestFactory(activity,
				MyRequestFactory.class);
		AllRightSMSRequest request = requestFactory.allRightSMSRequest();

		Long id = arguments[0];
		request.readSms(id).fire(new Receiver<SmsProxy>() {
			
			@Override
			public void onFailure(ServerFailure error) {
				
			}

			@Override
			public void onSuccess(SmsProxy arg0) {				
				sms = arg0;
			}
		});

		return sms;
	}

	@Override
	protected void onPostExecute(SmsProxy sms) {
	//	activity.sendSMS(sms); // al termine, notifico a AllrightSmsActivity del
								// messaggio da inviare...
		SendSMS sendsms = new SendSMS(activity, aes);
		sendsms.Sent(sms);
	}
}
