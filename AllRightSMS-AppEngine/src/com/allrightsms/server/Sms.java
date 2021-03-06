package com.allrightsms.server;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Sms {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name = "Unknown";
	private Date dueDate;
	private String emailAddress;
	private String userId;
	private String textmessage;
	private String phoneNumber;
	private boolean sync = false; // True if sent correctly from mobile,
											// False otherwise
	private boolean read = false; // True if read from web app, False
											// otherwise
	private boolean received = false; // True if received from mobile,
												// False if sent from Web App.

	public Sms() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTextmessage() {
		return textmessage;
	}

	public void setTextmessage(String textmessage) {
		this.textmessage = textmessage;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getSync() {
		return sync;
	}

	public void setSync(Boolean s) {
		this.sync = s;
	}

	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public Boolean getReceived() {
		return received;
	}

	public void setReceived(Boolean received) {
		this.received = received;
	}
}
