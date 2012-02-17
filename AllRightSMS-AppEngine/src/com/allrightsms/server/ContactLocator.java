package com.allrightsms.server;

import com.google.web.bindery.requestfactory.shared.Locator;


public class ContactLocator extends Locator<Contact, Void> {

	@Override
	public Contact create(Class<? extends Contact> clazz) {
		return new Contact();
	}

	@Override
	public Contact find(Class<? extends Contact> clazz, Void id) {
		return create(clazz);
	}

	@Override
	public Class<Contact> getDomainType() {
		return Contact.class;
	}

	@Override
	public Void getId(Contact domainObject) {
		return null;
	}

	@Override
	public Class<Void> getIdType() {
		return Void.class;
	}

	@Override
	public Object getVersion(Contact domainObject) {
		return null;
	}

}
