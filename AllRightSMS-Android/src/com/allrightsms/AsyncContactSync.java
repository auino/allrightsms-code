package com.allrightsms;

import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.allrightsms.client.MyRequestFactory;
import com.allrightsms.shared.AllRightSMSContactRequest;
import com.allrightsms.shared.ContactProxy;
import com.allrightsms.shared.SmsProxy;
import com.google.web.bindery.requestfactory.shared.Receiver;

public class AsyncContactSync extends AsyncTask<Long, Void, List<ContactProxy>> {

	private final AllRightSMSActivity activity;
	MyRequestFactory requestFactory;

	public AsyncContactSync(AllRightSMSActivity act) {
		super();
		activity = act;
	}

	@Override
	protected List<ContactProxy> doInBackground(Long... params) {
	
		// parte di comunicazione con il server per creare e modificare oggetti,
		// potrei farmi un metodo create con i paametri che mi servono in modo
		// da fare una chiamata sola
		
		requestFactory = Util.getRequestFactory(activity,
				MyRequestFactory.class);
		final AllRightSMSContactRequest req = requestFactory.allRightSMSContactRequest();
		
		// per recuperare tutti i nomi e numeri, per ogni contatto, lo creo sul
		// servere e lo aggiorno... se esiste...
		final ContentResolver cr = activity.getContentResolver();
		final Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		
		/*
		 * TODO
		 * faccio MD5 hash di curr e lo contronto con quello sul server,
		 * se sono diversi, cancello tutto sul server e li ricopio scrivendo anche il valore MD5, 
		 * altrimenti non faccio niente... 
		 */
		//se !md5(cur).equals(old.md5)
		req.deleteAllContact().fire(new Receiver<Void>() {

			@Override
			public void onSuccess(Void arg0) {
				// TODO Auto-generated method stub
				uploadNewContact(cur, cr);
			}
		});
		
		
		return null;
	}
	
	@Override
	protected void onPostExecute(List<ContactProxy> cont) {
		activity.ContactSyncronized();
	}
	
	private void uploadNewContact(Cursor cur, ContentResolver cr)
	{
		MyRequestFactory requestFactory = Util.getRequestFactory(activity,
				MyRequestFactory.class);
		final AllRightSMSContactRequest req = requestFactory.allRightSMSContactRequest();
		
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				
				
				String phone = "";
				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					// Query phone here. Covered next
					
						Cursor pCur = cr
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = ?", new String[] { id },
										null);
						while (pCur.moveToNext()) {
							phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							
						}
						pCur.close();
					
				}
				
				sendNewContactToServer(name, phone);
			}
			
			
		}
		cur.close();		
	}
	
	private void sendNewContactToServer(String name, String number) {
	    
		AllRightSMSContactRequest req = requestFactory.allRightSMSContactRequest();
		ContactProxy cont = req.create(ContactProxy.class);
		
		cont.setName(name);
		cont.setNumber(number);
		req.updateContact(cont).fire();
	   
	  }
}
