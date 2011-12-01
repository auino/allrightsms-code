package com.allrightsms.shared;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

@ServiceName(value = "com.allrightsms.server.AllRightSMSService", locator = "com.allrightsms.server.AllRightSMSServiceLocator")
public interface AllRightSMSRequest extends RequestContext {

	Request<SmsProxy> createSms();

	Request<SmsProxy> readSms(Long id);

	Request<SmsProxy> updateSms(SmsProxy sms);

	Request<Void> deleteSms(SmsProxy sms);

	Request<List<SmsProxy>> querySms();
	
	Request<List<SmsProxy>> queryUnSentSms(); //recupera sms da inviare
	
	Request<List<SmsProxy>> queryUnReadSms(); //recupera sms ricevuti
	
	Request<Void> sentC2DM(); //test C2DM connection
}

