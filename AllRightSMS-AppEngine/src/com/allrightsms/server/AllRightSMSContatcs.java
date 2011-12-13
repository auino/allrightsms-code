package com.allrightsms.server;

import java.io.IOException;
import com.google.gdata.client.*;
import com.google.gdata.client.contacts.*;
import com.google.gdata.data.*;
import com.google.gdata.data.contacts.*;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.*;

import java.io.IOException;
import java.net.URL;

import com.google.gwt.dev.protobuf.ServiceException;

public class AllRightSMSContatcs {

	// Authorize the service object.
	private ContactsService myService = new ContactsService("YOUR_APPLICATION_NAME");
	private static String mail = DataStore.getUserEmail();
	
	
	
	public AllRightSMSContatcs(){
		myService = new ContactsService("exampleCo-exampleApp-1");
	}
	
	
//	public static void printAllContacts(ContactsService myService)
//			throws ServiceException, IOException {
//		// Request the feed
//		URL feedUrl = new URL(
//				"https://www.google.com/m8/feeds/contacts/"+mail+"/full");
////		ContactFeed resultFeed = myService.getFeed(feedUrl, ContactFeed.class);
//		
//		// Print the results
//		System.out.println(resultFeed.getTitle().getPlainText());
//		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
//			ContactEntry entry = resultFeed.getEntries().get(i);
//			System.out.println("\t" + entry.getTitle().getPlainText());
//
//			System.out.println("Email addresses:");
//			for (Email email : entry.getEmailAddresses()) {
//				System.out.print(" " + email.getAddress());
//				if (email.getRel() != null) {
//					System.out.print(" rel:" + email.getRel());
//				}
//				if (email.getLabel() != null) {
//					System.out.print(" label:" + email.getLabel());
//				}
//				if (email.getPrimary()) {
//					System.out.print(" (primary) ");
//				}
//				System.out.print("\n");
//			}
//
//			System.out.println("IM addresses:");
//			for (Im im : entry.getImAddresses()) {
//				System.out.print(" " + im.getAddress());
//				if (im.getLabel() != null) {
//					System.out.print(" label:" + im.getLabel());
//				}
//				if (im.getRel() != null) {
//					System.out.print(" rel:" + im.getRel());
//				}
//				if (im.getProtocol() != null) {
//					System.out.print(" protocol:" + im.getProtocol());
//				}
//				if (im.getPrimary()) {
//					System.out.print(" (primary) ");
//				}
//				System.out.print("\n");
//			}
//
//			System.out.println("Groups:");
//			for (GroupMembershipInfo group : entry.getGroupMembershipInfos()) {
//				String groupHref = group.getHref();
//				System.out.println("  Id: " + groupHref);
//			}
//
//			System.out.println("Extended Properties:");
//			for (ExtendedProperty property : entry.getExtendedProperties()) {
//				if (property.getValue() != null) {
//					System.out.println("  " + property.getName() + "(value) = "
//							+ property.getValue());
//				} else if (property.getXmlBlob() != null) {
//					System.out.println("  " + property.getName()
//							+ "(xmlBlob)= " + property.getXmlBlob().getBlob());
//				}
//			}
//
//			String photoLink = entry.getContactPhotoLink().getHref();
//			System.out.println("Photo Link: " + photoLink);
//
//			if (photoLink.getEtag() != null) {
//				System.out.println("Contact Photo's ETag: "
//						+ photoLink.getEtag());
//			}
//
//			System.out.println("Contact's ETag: " + entry.getEtag());
//		}
//	}
}
