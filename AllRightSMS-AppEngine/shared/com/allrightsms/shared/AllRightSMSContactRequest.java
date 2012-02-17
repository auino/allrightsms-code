package com.allrightsms.shared;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

@ServiceName(value = "com.allrightsms.server.AllRightSMSServiceContact", locator = "com.allrightsms.server.AllRightSMSServiceContactLocator")
public interface AllRightSMSContactRequest extends RequestContext {

	Request<ContactProxy> createContact();
	
	Request<Void> createContact(String name, String Phone);

	Request<ContactProxy> readContact(Long id);

	Request<ContactProxy> updateContact(ContactProxy contact);

	Request<Void> deleteContact(ContactProxy contact);

	Request<List<ContactProxy>> queryContacts();

}
