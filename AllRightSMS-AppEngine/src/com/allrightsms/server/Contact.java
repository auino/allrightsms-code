package com.allrightsms.server;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Contact {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String Name;
	private String Number;
	private String PictureUrl ="";
	private String emailAddressOwner;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmailAddressOwner() {
		return emailAddressOwner;
	}

	public void setEmailAddressOwner(String emailAddress) {
		this.emailAddressOwner = emailAddress;
	}

	public String getName() {
		return Name;
	}
	
	public void setName(String name) {
		Name = name;
	}
	
	public String getNumber() {
		return Number;
	}
	
	public void setNumber(String number) {
		Number = number;
	}
	
	public String getPictureUrl() {
		return PictureUrl;
	}
	
	public void setPictureUrl(String pictureUrl) {
		PictureUrl = pictureUrl;
	}
	
}
