package com.allrightsms.server;

import java.util.Date;
import java.util.List;

import com.allrightsms.annotation.ServiceMethod;
import com.allrightsms.shared.RequestSource;
import com.allrightsms.shared.SmsChange;

public class AllRightSMSService {

	static DataStore db = new DataStore();

	@ServiceMethod
	public Sms createSms() {
		//TODO creare l'sms con già la data e l'ora in modo da averne una univoca... tipo il codice sotto
		Sms s = new Sms();
		s.setDueDate(new Date());
		s.setName("Unknown");
		return db.update(s);
	}

	@ServiceMethod
	public Sms readSms(Long id) {
		return db.find(id);
	}

	@ServiceMethod
	public Sms updateSms(Sms sms) {
		sms.setEmailAddress(DataStore.getUserEmail());
		sms.setDueDate(new Date());
		sms = db.update(sms);
		if (!sms.getSync())// se non è ancora stato inviato manda il C2DM, altrimenti no
			DataStore.sendC2DMUpdate(SmsChange.NEWSMS + SmsChange.SEPARATOR
					+ sms.getId());
		return sms;
	}

	@ServiceMethod
	public void deleteSms(Sms sms) {
		db.delete(sms.getId());
	}

	@ServiceMethod
	public List<Sms> querySms() { //usata via web per recuperare tutti i messaggi... -- SENT_WEB
		return db.findAll(RequestSource.SENT_WEB);
	}
	
	@ServiceMethod
	public List<Sms> queryUnSentSms() { //usata via telefono per recuperare i messaggi da inviare
		List<Sms> sms = db.findAll(RequestSource.SENT_MOBILE); // certo tutti gli sms non inviati   -- 
		for (Sms s : sms) {
			s.setSync(true);
			db.update(s);
		}
		return sms;
	}
	
	@ServiceMethod
	public List<Sms> queryUnReadSms(){ //usata via web per recuperare gli sms in arrivo
		List<Sms> sms = db.findAll(RequestSource.SENT_WEB_RECEIVED);
		for (Sms s : sms) {
			s.setRead(true);
			db.update(s);
		}
		
		return sms;
	}

	@ServiceMethod
	public void sentC2DM()
	{
		DataStore.sendC2DMUpdate(SmsChange.UPDATE + SmsChange.SEPARATOR
				+ "TestC2DM");
	}
}
