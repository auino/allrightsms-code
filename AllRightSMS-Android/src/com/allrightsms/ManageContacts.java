package com.allrightsms;

import java.io.InputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

//classe creata in data 10/02/2012
public class ManageContacts{
	
	public String retrieveName(Context context,String number) {
	
			Log.v("ffnet", "Started uploadcontactphoto...");

			String name = "";
			String contactId = null;
		//	String input = null;
			InputStream input = null;

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
			    contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
			    name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));

			    // Get photo of contactId as input stream:
			    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
			//    input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);

			    Log.v("ffnet", "Started uploadcontactphoto: Contact Found @ " + number);            
			    Log.v("ffnet", "Started uploadcontactphoto: Contact name  = " + name);
			    Log.v("ffnet", "Started uploadcontactphoto: Contact id    = " + contactId);

			} else {

			    Log.v("ffnet", "Started uploadcontactphoto: Contact Not Found @ " + number);
			    return name; // contact not found

			}

			// Only continue if we found a valid contact photo:
			if (input == null) {
			    Log.v("ffnet", "Started uploadcontactphoto: No photo found, id = " + contactId + " name = " + name);
			    return ""; // no photo
			} else {
			    //this.type = contactId;
			    Log.v("ffnet", "Started uploadcontactphoto: Photo found, id = " + contactId + " name = " + name);
			}
		
		return name;
	}
	
	
		 
		 
		 
	
}
