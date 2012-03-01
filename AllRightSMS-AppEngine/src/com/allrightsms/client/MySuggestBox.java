package com.allrightsms.client;

import java.util.List;
import java.util.Map;

import com.allrightsms.shared.ContactProxy;
import com.allrightsms.shared.NumberUtility;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;

public class MySuggestBox extends SuggestBox{

	private String placeHolderText = "";
	private final static MultiWordSuggestOracle mySuggestions = new MultiWordSuggestOracle();
	private Map<String, String> map = new java.util.HashMap<String, String>();

	
	public MySuggestBox() {
		super(mySuggestions);
	}
	
	public String getPlaceHolderText() {
		return placeHolderText;
	}

	public void setPlaceHolderText(String text) {
		placeHolderText = text;
		getTextBox().getElement().getStyle().setProperty("placeholder", placeHolderText);
	}
	
	public void constructSuggestion(List<ContactProxy> contact)
	{
		map.clear();
		for (ContactProxy c : contact) {
			String temp = NumberUtility.createSuggestionString(c.getName(), c.getNumber());
			map.put(c.getName(), c.getNumber());
			mySuggestions.add(temp);
		}
	}
	
	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
}


