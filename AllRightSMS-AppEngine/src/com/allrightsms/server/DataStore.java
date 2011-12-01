package com.allrightsms.server;

import java.util.Calendar;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletContext;

import com.allrightsms.shared.RequestSource;
import com.google.android.c2dm.server.PMF;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;

public class DataStore {

	/**
	 * Remove this object from the data store.
	 */
	public void delete(Long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Sms item = pm.getObjectById(Sms.class, id);
			pm.deletePersistent(item);
		} finally {
			pm.close();
		}
	}

	/**
	 * Find a {@link Task} by id.
	 * 
	 * @param id
	 *            the {@link Sms} id
	 * @return the associated {@link Sms}, or null if not found
	 */
	@SuppressWarnings("unchecked")
	public Sms find(Long id) {
		if (id == null) {
			return null;
		}

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery("select from " + Sms.class.getName()
					+ " where id==" + id.toString() + " && emailAddress=='"
					+ getUserEmail() + "'");
			List<Sms> list = (List<Sms>) query.execute();
			return list.size() == 0 ? null : list.get(0);
		} catch (RuntimeException e) {
			System.out.println(e);
			throw e;
		} finally {
			pm.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Sms> findAll(RequestSource sent) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query;
			switch (sent) {
			case SENT_MOBILE: // request from mobile
				query = pm.newQuery("SELECT FROM " + Sms.class.getName()
						+ " WHERE emailAddress=='"+ getUserEmail() + "' && sync==false"); // AND sync==FALSE
				break;

			case SENT_WEB: // request from web app, all the message
				query = pm.newQuery("select from " + Sms.class.getName()
						+ " where emailAddress=='" + getUserEmail() + "'");
				break;

			default: // SENT_WEB_RECEIVED //request from web app only sms
						// receive and unread
				query = pm.newQuery("select from " + Sms.class.getName()
						+ " where emailAddress=='" + getUserEmail()
						+ "' && received==true");

				break;
			}

			// potrei aggiungere and done="false" per avere tutti i messaggi non
			// ancora inviati

			List<Sms> list = (List<Sms>) query.execute();
			if (list.size() == 0) {
				// Workaround for this issue:
				// http://code.google.com/p/datanucleus-appengine/issues/detail?id=24
				list.size();
			}
			return list;

		} catch (RuntimeException e) {
			System.out.println(e);
			throw e;
		} finally {
			pm.close();
		}
	}

	/*
	 * @SuppressWarnings("unchecked") public List<Sms> findAllUnsent() {
	 * PersistenceManager pm = PMF.get().getPersistenceManager(); try { Query
	 * query = pm.newQuery("select from " + Sms.class.getName() +
	 * " where emailAddress=='" + getUserEmail() + "' and sync=='false'");
	 * 
	 * //potrei aggiungere and done="false" per avere tutti i messaggi non
	 * ancora inviati
	 * 
	 * List<Sms> list = (List<Sms>) query.execute(); if (list.size() == 0) {
	 * //Workaround for this issue:
	 * //http://code.google.com/p/datanucleus-appengine/issues/detail?id=24
	 * list.size(); }
	 * 
	 * return list; } catch (RuntimeException e) { System.out.println(e); throw
	 * e; } finally { pm.close(); } }
	 */
	/**
	 * Persist this object in the datastore.
	 */
	public Sms update(Sms item) {
		// set the user id (not sure this is where we should be doing this)
		item.setUserId(getUserId());
		item.setEmailAddress(getUserEmail());
		if (item.getDueDate() == null) {
			Calendar c = Calendar.getInstance();
			c.set(2011, 5, 11);
			item.setDueDate(c.getTime());
		}
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(item);
			return item;
		} finally {
			pm.close();
		}
	}

	public static String getUserId() {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		return user.getUserId();
	}

	public static String getUserEmail() {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		return user.getEmail();
	}

	public static void sendC2DMUpdate(String message) {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		ServletContext context = RequestFactoryServlet.getThreadLocalRequest()
				.getSession().getServletContext();
		SendMessage.sendMessage(context, user.getEmail(), message);
	}

}
