package com.allrightsms;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.allrightsms.client.MyRequestFactory;
import com.allrightsms.shared.AllRightSMSRequest;
import com.allrightsms.shared.RequestSource;
import com.allrightsms.shared.SmsChange;
import com.allrightsms.shared.SmsProxy;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

public class AsyncFetchSMS extends AsyncTask<Long, Void, List<SmsProxy>> {

	private final AllRightSMSActivity activity;

	public AsyncFetchSMS(AllRightSMSActivity activity) {
		super();
		this.activity = activity;
	}

	@Override
	protected List<SmsProxy> doInBackground(Long... arguments) {
		final List<SmsProxy> list = new ArrayList<SmsProxy>();

		MyRequestFactory requestFactory = Util.getRequestFactory(activity,
				MyRequestFactory.class);
		AllRightSMSRequest request = requestFactory.allRightSMSRequest();

		Long id = arguments[0];
		request.readSms(id).fire(new Receiver<SmsProxy>() {
			// request.queryUnSentSms().fire(new Receiver<List<SmsProxy>>() {

			/*
			 * @Override public void onSuccess(List<SmsProxy> arg0) { // TODO
			 * Auto-generated method stub list.addAll(arg0);
			 * 
			 * }
			 */
			@Override
			public void onFailure(ServerFailure error) {
				list.clear();
			}

			@Override
			public void onSuccess(SmsProxy arg0) {
				// TODO Auto-generated method stub
				list.add(arg0);
			}
		});

		return list;
	}

	@Override
	protected void onPostExecute(List<SmsProxy> sms) {
		activity.sendSMS(sms); // al termine, notifico a AllrightSmsActivity del
								// messaggio da inviare...
	}
}
