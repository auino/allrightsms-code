package com.allrightsms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.allrightsms.client.MyRequestFactory;
import com.allrightsms.shared.AllRightSMSRequest;
import com.allrightsms.shared.SmsProxy;
import com.google.web.bindery.requestfactory.shared.Receiver;

//classe creata in data 10/02/2012
public class ManageContacts{
	
	private boolean modified = false;
	private AsyncContactSync asyncUpdate;
	
	public String retrieveName(Context context,String number) {
	
			Log.v("ffnet", "Started searching contact name...");

			String name = "Unknown";
//			String contactId = null;
		//	String input = null;
//			InputStream input = null;

			// define the columns I want the query to return
			String[] projection = new String[] {
			        ContactsContract.PhoneLookup.DISPLAY_NAME,
			        ContactsContract.PhoneLookup._ID};

			// encode the phone number and build the filter URI
			Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

			// query time
			Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

			if (cursor.moveToFirst()) {

			    // Get values from contacts database:
			 //   contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
			    name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));

			    // Get photo of contactId as input stream:
			  //  Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
			//    input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);

			//    Log.v("ffnet", "Started uploadcontactphoto: Contact Found @ " + number);            
			   Log.v("ffnet", "Contact name  = " + name+ "");
			//    Log.v("ffnet", "Started uploadcontactphoto: Contact id    = " + contactId);
			}
//			else {
//
//			    Log.v("ffnet", "Started uploadcontactphoto: Contact Not Found @ " + number);
//			    return name; // contact not found
//
//			}

			// Only continue if we found a valid contact photo:
//			if (input == null) {
//			    Log.v("ffnet", "Started uploadcontactphoto: No photo found, id = " + contactId + " name = " + name);
//			    retusmsProxy = request.edit(smsProxy);rn ""; // no photo
//			} else {
//			    //this.type = contactId;
//			    Log.v("ffnet", "Started uploadcontactphoto: Photo found, id = " + contactId + " name = " + name);
//			}
		
		return name;
	}

	public boolean addNameToSms(Context ctx,SmsProxy sms) {
		
		MyRequestFactory requestFactory = Util.getRequestFactory(ctx, MyRequestFactory.class);
		AllRightSMSRequest request = requestFactory.allRightSMSRequest();
		sms = request.edit(sms);
		sms.setSync(true); // importante per non farsi rimandare il C2DM
		
		sms.setName(this.retrieveName(ctx, sms.getPhoneNumber()));
		if(sms.getName().equals("Unknown"))
			return false;
				
		request.updateSms(sms).fire(new Receiver<SmsProxy>(){

			@Override
			public void onSuccess(SmsProxy arg0) {
				modified = true;
			}});
		
		return modified;
	}
	
	public void retrieveAllContact(AllRightSMSActivity ctx)
	{	
        asyncUpdate = new AsyncContactSync(ctx);
		asyncUpdate.execute();		
	}
}
