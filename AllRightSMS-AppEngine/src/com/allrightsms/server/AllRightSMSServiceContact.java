package com.allrightsms.server;

import java.util.List;

import com.allrightsms.annotation.ServiceMethod;


public class AllRightSMSServiceContact {

	static DataStore db = new DataStore();
	
	@ServiceMethod
	public Contact createContact() {
		Contact c = new Contact();
		db.update(c);
		return null;
	}
	
	@ServiceMethod
	public void createContact(String name, String Phone){
		Contact c = new Contact();
		c.setName(name);
		c.setNumber(Phone);
		db.update(c);
	}

	@ServiceMethod
	public Contact readContact(Long id) {
		return db.findContact(id);
	}

	@ServiceMethod
	public Contact updateContact(Contact contact) {
		contact.setEmailAddressOwner(DataStore.getUserEmail());
		return db.update(contact);
	}

	@ServiceMethod
	public void deleteContact(Contact contact) {
		db.deleteContact(contact.getId());
	}
	
	@ServiceMethod
	public void deleteAllContact() {
		// TODO Auto-generated method stub
		//controllare
		for (Contact c : queryContacts()) {
			deleteContact(c);
		}
	}

	@ServiceMethod
	public List<Contact> queryContacts() {
		return db.findAllContacts();
	}
}
