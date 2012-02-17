package com.allrightsms.shared;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value = "com.allrightsms.server.Contact", locator = "com.allrightsms.server.ContactLocator")
public interface ContactProxy extends ValueProxy {

	String getName();

	void setName(String name);

	String getNumber();

	void setNumber(String number);

	String getPictureUrl();

	void setPictureUrl(String pictureUrl);

}
