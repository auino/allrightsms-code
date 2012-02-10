package com.allrightsms.shared;

import java.util.Date;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value = "com.allrightsms.server.Sms", locator = "com.allrightsms.server.SmsLocator")
public interface SmsProxy extends ValueProxy {

	String getName();

	void setName(String name);
	
	Date getDueDate();

	void setDueDate(Date dueDate);

	String getEmailAddress();

	void setEmailAddress(String emailAddress);

	String getUserId();

	void setUserId(String userId);

	String getTextmessage();

	void setTextmessage(String textmessage);

	String getPhoneNumber();

	void setPhoneNumber(String phoneNumber);

	Long getId();

	Boolean getSync();

	void setSync(Boolean done);

	Boolean getRead();

	void setRead(Boolean read);

	Boolean getReceived();

	void setReceived(Boolean received);
}
