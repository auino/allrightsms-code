package com.allrightsms.server;

import com.google.web.bindery.requestfactory.shared.Locator;


public class SmsLocator extends Locator<Sms, Void> {

	@Override
	public Sms create(Class<? extends Sms> clazz) {
		return new Sms();
	}

	@Override
	public Sms find(Class<? extends Sms> clazz, Void id) {
		return create(clazz);
	}

	@Override
	public Class<Sms> getDomainType() {
		return Sms.class;
	}

	@Override
	public Void getId(Sms domainObject) {
		return null;
	}

	@Override
	public Class<Void> getIdType() {
		return Void.class;
	}

	@Override
	public Object getVersion(Sms domainObject) {
		return null;
	}

}
