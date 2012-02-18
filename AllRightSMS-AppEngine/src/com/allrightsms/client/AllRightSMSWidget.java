/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.allrightsms.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController;
import com.allrightsms.client.MyRequestFactory.HelloWorldRequest;
import com.allrightsms.shared.AllRightSMSContactRequest;
import com.allrightsms.shared.AllRightSMSRequest;
import com.allrightsms.shared.ContactProxy;
import com.allrightsms.shared.NumberUtility;
import com.allrightsms.shared.SmsProxy;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dev.util.collect.HashMap;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

public class AllRightSMSWidget extends Composite {

	private static final int THREADS_NUMBER = 3;
	private static final int MESSAGES_NUMBER = 4;
	private static final int STATUS_DELAY = 4000;
	private static final String STATUS_ERROR = "status error";
	private static final String STATUS_NONE = "status none";
	private static final String STATUS_SUCCESS = "status success";
	private static final int DELAY_MS = 15000; // delay of research incoming, 1
												// 000
												// minute

	private static final String BUTTON_STYLE = "send centerbtn";
	private SmsProxy smsProxy;
	final MyRequestFactory RequestFactory = GWT.create(MyRequestFactory.class);
	final EventBus eventBus = new SimpleEventBus();
	private final List<List<SmsProxy>> ThreadSmsReceived = new LinkedList<List<SmsProxy>>();
	private final List<SmsProxy> allMessage = new LinkedList<SmsProxy>();
	private final List<String> phoneNumber = new LinkedList<String>();
	private String LoggedMail = "";
	private ArrayList<SmsProxy> sortedTasks;
	private ArrayList<ContactProxy> contact;

	interface AllRightSMSUiBinder extends UiBinder<Widget, AllRightSMSWidget> {
	}

	private static AllRightSMSUiBinder uiBinder = GWT
			.create(AllRightSMSUiBinder.class);

	@UiField
	TextAreaElement messageArea;

	@UiField
	DivElement signout;

	@UiField
	MySuggestBox recipientNumber; // InputElement (provided = true)

	@UiField
	DivElement status;

	@UiField
	Button sayHelloButton;

	@UiField
	Button sendMessageButton;

	@UiField
	SmsTable smsTableOne;

	@UiField
	SmsTable smsTableTwo;

	@UiField
	SmsTable smsTableThree;

	@UiField
	SmsUserPicture smsUserPictureOne;

	@UiField
	SmsUserName smsUserNameOne;

	@UiField
	SmsUserNumber smsUserNumberOne;

	@UiField
	SmsUserPicture smsUserPictureTwo;

	@UiField
	SmsUserName smsUserNameTwo;

	@UiField
	SmsUserNumber smsUserNumberTwo;

	@UiField
	SmsUserPicture smsUserPictureThree;

	@UiField
	SmsUserName smsUserNameThree;

	@UiField
	SmsUserNumber smsUserNumberThree;

	@UiField
	DivElement headerOne; // added to remove list sms header if no one...

	@UiField
	DivElement headerTwo; // added to remove list sms header if no one...

	@UiField
	DivElement headerThree; // added to remove list sms header if no one...

	@UiField
	DivElement messageStatus;

	@UiField
	Button replyButtonOne;

	@UiField
	Button replyButtonTwo;

	@UiField
	Button replyButtonThree;

	/**
	 * Timer to clear the UI.
	 */
	Timer timer = new Timer() {
		@Override
		public void run() {
			status.setInnerText("");
			status.setClassName(STATUS_NONE);
			messageArea.setValue("");
			// recipientNumber.setValue("");
		}
	};

	private void setStatus(String message, boolean error) {
		status.setInnerText(message);
		if (error) {
			status.setClassName(STATUS_ERROR);
		} else {
			if (message.length() == 0) {
				status.setClassName(STATUS_NONE);
			} else {
				status.setClassName(STATUS_SUCCESS);
			}
		}

		timer.schedule(STATUS_DELAY);
	}

	public static class SmsTableOne extends SmsTable {
	}

	public static class SmsTableTwo extends SmsTable {
	}

	public static class SmsTableThree extends SmsTable {
	}

	public static class SmsTable extends HTML {

		public SmsTable() {
			super("");
		}

		public void clear() {
			this.setHTML("");
		}

		public void rebuild(SmsProxy s) {
			Date today = new Date(); // data corrente

			double giorniFraDueDate = Math.round((today.getTime() - s
					.getDueDate().getTime()) / 86400000.0);
			// A custom date format
			DateTimeFormat fmt = null;

			// se la differenza è maggiore di un giorno metto Mese e giorno
			if (giorniFraDueDate >= 1)
				fmt = DateTimeFormat.getFormat("MMM dd"); // "EEEE, MMMM dd, yyyy"
			else
				fmt = DateTimeFormat.getFormat("hh:mm aaa"); // Ora e minuti

			if (s.getReceived())
				addSms(false, "Received", s.getTextmessage(),
						fmt.format(s.getDueDate()));
			else
				addSms(true, "Sent", s.getTextmessage(),
						fmt.format(s.getDueDate()));
		}

		public void addSms(boolean fromMe, String from, String msg, String date) {
			String align = "left", bg = "#aaccff";
			if (fromMe) {
				// align = "left";
				bg = "#ffffff";
			}
			String html = ""
					+ "<div class=\"roundRect\" style=\"padding:3px; text-align:"
					+ align + "; background-color:" + bg + ";\">" + "<div>"
					+ "<b>" + from + ":</b> " + msg + "</div>"
					+ "<div style=\"font-size:12px;\">" + date + "</div>"
					+ "</div>";
			this.setHTML(this.getHTML() + html);
		}

		/*
		 * StackPanel (che è una figata) String text1 =
		 * "Lorem ipsum dolor sit amet..."; String text2 =
		 * "Sed egestas, arcu nec accumsan..."; String text3 =
		 * "Proin tristique, elit at blandit...";
		 * 
		 * HTML html = new HTML(
		 * "This is <b>HTML</b>.  It will be interpreted as such if you specify "
		 * + "the asHTML flag.", true);
		 * 
		 * Label label = new Label("A"); this.add(label, "One", false); label =
		 * new Label(text2); this.add(html, "Ciao", true);
		 * 
		 * this. label = new Label(text3); this.add(label, "Three", false);
		 * this.setSize("400px", "200px");
		 * 
		 * }
		 */
	}

	public AllRightSMSWidget() {
		initWidget(uiBinder.createAndBindUi(this));

		messageArea.setTitle("Message");
		recipientNumber.setTitle("Cell number");

		// cerco di simulare focus sul recipient number, per simulare il
		// placeholder

		// recipientNumber.addFocusListener(new FocusListener() {
		//
		// @Override
		// public void onLostFocus(Widget sender) {
		// // TODO Auto-generated method stub
		// recipientNumber.setText("Cell Number");
		// }
		//
		// @Override
		// public void onFocus(Widget sender) {
		// // TODO Auto-generated method stub
		// recipientNumber.setText("");
		// }
		// });

		sayHelloButton.getElement().setClassName("send centerbtn");
		sendMessageButton.getElement().setClassName("send");
		replyButtonOne.getElement().setClassName("send");
		replyButtonTwo.getElement().setClassName("send");
		replyButtonThree.getElement().setClassName("send");

		// inizializzo il bus per le chiamate al server
		RequestFactory.initialize(eventBus);

		// inserire la request che mi da la mail dell'utente e se possibile
		// l'url di logout
		HelloWorldRequest helloWorldRequest = RequestFactory
				.helloWorldRequest();
		helloWorldRequest.getMail().fire(new Receiver<String>() {

			@Override
			public void onSuccess(String response) {
				LoggedMail = response;
				signout.setInnerHTML(response);
			}
		});

		// inizializzo le tre liste di messaggi
		ThreadSmsReceived.add(0, new LinkedList<SmsProxy>());
		ThreadSmsReceived.add(1, new LinkedList<SmsProxy>());
		ThreadSmsReceived.add(2, new LinkedList<SmsProxy>());

		sendMessageButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// creo l'sms sul server
				if (!recipientNumber.getValue().isEmpty() & !messageArea.getValue().isEmpty())
					create();
				else
				 setStatus("Impossibile inviare il messaggio", false);
			}
		});

		// ClickHandler per il bottone reply uno
		replyButtonOne.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ThreadSmsReceived.isEmpty()
						&& !ThreadSmsReceived.get(0).isEmpty()) {
					String num = ThreadSmsReceived.get(0).get(0)
							.getPhoneNumber();
					String name = ThreadSmsReceived.get(0).get(0).getName();
					if (!name.equals("Unknown")) {
						reply(NumberUtility.createSuggestionString(name, num));
					} else
						reply(num);
				}
			}
		});

		replyButtonTwo.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ThreadSmsReceived.isEmpty()
						&& !ThreadSmsReceived.get(1).isEmpty()) {
					String num = ThreadSmsReceived.get(1).get(0)
							.getPhoneNumber();
					String name = ThreadSmsReceived.get(1).get(0).getName();
					if (!name.equals("Unknown")) {
						reply(NumberUtility.createSuggestionString(name, num));
					} else
						reply(num);
				}
			}
		});

		replyButtonThree.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!ThreadSmsReceived.isEmpty()
						&& !ThreadSmsReceived.get(2).isEmpty()) {
					String num = ThreadSmsReceived.get(2).get(0)
							.getPhoneNumber();
					String name = ThreadSmsReceived.get(2).get(0).getName();
					if (!name.equals("Unknown")) {
						reply(NumberUtility.createSuggestionString(name, num));
					} else
						reply(num);
				}
			}
		});

		sayHelloButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sayHelloButton.setEnabled(false);
				HelloWorldRequest helloWorldRequest = RequestFactory
						.helloWorldRequest();
				helloWorldRequest.getMessage().fire(new Receiver<String>() {
					@Override
					public void onFailure(ServerFailure error) {
						sayHelloButton.setEnabled(true);
						setStatus(error.getMessage(), true);
					}

					@Override
					public void onSuccess(String response) {
						sayHelloButton.setEnabled(true);
						setStatus(response, response.startsWith("Failure:"));
					}
				});
			}
		});

		// aggiungo bottoni per testare se funziona...
		FlowPanel panel = new FlowPanel();
		panel.getElement().setId("footer");

		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(10);

		HorizontalPanel wrapper = new HorizontalPanel();
		wrapper.setWidth("100%");
		wrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		wrapper.add(hp);

		// Button createButton = new Button();
		// createButton.setText("Create");
		// createButton.getElement().setClassName(BUTTON_STYLE);
		// hp.add(createButton);
		// Button updateButton = new Button();
		// updateButton.setText("Update");
		// updateButton.getElement().setClassName(BUTTON_STYLE);
		// hp.add(updateButton);
		// Button queryButton = new Button();
		// queryButton.setText("Query");
		// queryButton.getElement().setClassName(BUTTON_STYLE);
		// hp.add(queryButton);
		// Button deleteButton = new Button();
		// deleteButton.setText("Delete");
		// deleteButton.getElement().setClassName(BUTTON_STYLE);
		// hp.add(deleteButton);
		Button deleteAllButton = new Button();
		deleteAllButton.setText("Delete All");
		deleteAllButton.getElement().setClassName(BUTTON_STYLE);
		hp.add(deleteAllButton);

		// Button queryUnreadButton = new Button();
		// queryUnreadButton.setText("Unread");
		// queryUnreadButton.getElement().setClassName(BUTTON_STYLE);
		// hp.add(queryUnreadButton);

		// gestione della visualizzazione dei div dei messaggi
		headerOne.setAttribute("style", "display:none"); // display:block to
															// visibility
		headerTwo.setAttribute("style", "display:none");
		headerThree.setAttribute("style", "display:none");
		messageStatus.setInnerText("Waiting for  synchronization!");
		// inizialmente nessun div viene mostrato

		// queryUnreadButton.addClickHandler(new ClickHandler() {
		// @Override
		// public void onClick(ClickEvent event) {
		// queryUnread();
		// }
		// });
		//
		// createButton.addClickHandler(new ClickHandler() {
		// @Override
		// public void onClick(ClickEvent event) {
		// create();
		// }
		// });
		//
		// updateButton.addClickHandler(new ClickHandler() {
		// @Override
		// public void onClick(ClickEvent event) {
		// update(smsProxy);
		// }
		// });
		//
		// deleteButton.addClickHandler(new ClickHandler() {
		// @Override
		// public void onClick(ClickEvent event) {
		// delete(smsProxy);
		// }
		// });

		deleteAllButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteAll();
			}
		});
		//
		// queryButton.addClickHandler(new ClickHandler() {
		// @Override
		// public void onClick(ClickEvent event) {
		// query();
		// }
		// });

		// carica i suggerimenti per la suggestBox
		getSuggestion();

		panel.add(wrapper);
		RootPanel.get().add(panel);

		// temporizzo l'aggiornamento dei nuovi messaggi in arrivo
		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
			@Override
			public boolean execute() {
				retrieveReceivedSms();
				return true;
			}
		}, DELAY_MS);
	}

	public void getSuggestion() {
		AllRightSMSContactRequest request = RequestFactory
				.allRightSMSContactRequest();
		request.queryContacts().fire(new Receiver<List<ContactProxy>>() {

			@Override
			public void onSuccess(List<ContactProxy> response) {
				contact = new ArrayList<ContactProxy>(response);
				recipientNumber.constructSuggestion(contact);
			}
		});
	}

	public static final Comparator<? super SmsProxy> TASK_COMPARATOR = new Comparator<SmsProxy>() {
		public int compare(SmsProxy t0, SmsProxy t1) {
			// Sort sms by due date within each group
			return compareDueDate(t0, t1);
		}

		int compareDueDate(SmsProxy t0, SmsProxy t1) {
			Date d0 = t0.getDueDate();
			Date d1 = t1.getDueDate();

			if (d0 == null) {
				if (d1 == null) {
					return 0;
				} else {
					return -1;
				}
			} else if (d1 == null) {
				return 1;
			}
			long delta = d0.getTime() - d1.getTime();
			if (delta < 0) {
				return 1;
			} else if (delta > 0) {
				return -1;
			} else {
				return 0;
			}
		}
	};

	private void reply(String number) {
		recipientNumber.setText(number);
		messageArea.focus();
	}

	private void retrieveReceivedSms() {

		AllRightSMSRequest request = RequestFactory.allRightSMSRequest();
		request.querySms().fire(new Receiver<List<SmsProxy>>() {
			@Override
			public void onSuccess(List<SmsProxy> response) {
				if (response.size() > 0) {

					sortedTasks = new ArrayList<SmsProxy>(response);
					showMessages();
				} else {
					clearMessage();
				}
			}
		});
	}

	private void clearMessage() {
		allMessage.clear();
		phoneNumber.clear();
		smsTableOne.clear();
		smsTableTwo.clear();
		smsTableThree.clear();
		smsUserNumberOne.setText("");
		smsUserNumberTwo.setText("");
		smsUserNumberThree.setText("");

		// display:block to visibility
		headerOne.setAttribute("style", "display:none");
		headerTwo.setAttribute("style", "display:none");
		headerThree.setAttribute("style", "display:none");
		messageStatus.setInnerText("No message sent or received!");
	}

	private void showMessages() {

		// sort first the thelephone number
		Collections.sort(sortedTasks, TASK_COMPARATOR);
		// se ci sono nuovi messaggi...
		if (!sortedTasks.equals(allMessage)) {
			allMessage.clear();
			allMessage.addAll(sortedTasks);
			// Collections.reverse(sortedTasks);
			phoneNumber.clear();

			int i = 0;
			for (SmsProxy s : sortedTasks) {
				String number = NumberUtility.purgePrefix(s.getPhoneNumber());
				if (!phoneNumber.contains(number)) {
					phoneNumber.add(number);
					i++;
				}
				if (i >= 3)
					break;
			}

			constructForNumber();

			// emette un suono per segnalare messaggio in arrivo!
			SoundController soundController = new SoundController();
			Sound sound = soundController.createSound(
					Sound.MIME_TYPE_AUDIO_MPEG_MP3, "bells-message.mp3");
			// MIME_TYPE_AUDIO_MPEG
			sound.play();
		}
	}

	private void constructForNumber() {
		smsTableOne.clear();
		smsTableTwo.clear();
		smsTableThree.clear();
		smsUserNameOne.setText("Unknown");
		smsUserNameTwo.setText("Unknown");
		smsUserNameThree.setText("Unknown");
		ThreadSmsReceived.get(0).clear();
		ThreadSmsReceived.get(1).clear();
		ThreadSmsReceived.get(2).clear();
		messageStatus.setInnerText("Received Message:");

		int foundOne = 0, foundTwo = 0, foundThree = 0;

		for (SmsProxy s : allMessage) {
			if (phoneNumber.size() > 0
					&& s.getPhoneNumber().equals(phoneNumber.get(0))
					&& foundOne < MESSAGES_NUMBER) {
				headerOne.setAttribute("style", "display:block");
				ThreadSmsReceived.get(0).add(s);
				smsUserNumberOne.setText(s.getPhoneNumber());
				if (smsUserNameOne.getText().equals("Unknown"))
					smsUserNameOne.setText(s.getName());
				smsTableOne.rebuild(s);
				foundOne++;
			}
			if (phoneNumber.size() > 1
					&& s.getPhoneNumber().equals(phoneNumber.get(1))
					&& foundTwo < MESSAGES_NUMBER) {
				headerTwo.setAttribute("style", "display:block");
				ThreadSmsReceived.get(1).add(s);
				smsUserNumberTwo.setText(s.getPhoneNumber());
				if (smsUserNameTwo.getText().equals("Unknown"))
					smsUserNameTwo.setText(s.getName());
				smsTableTwo.rebuild(s);
				foundTwo++;
			}
			if (phoneNumber.size() > 2
					&& s.getPhoneNumber().equals(phoneNumber.get(2))
					&& foundThree < MESSAGES_NUMBER) {
				headerThree.setAttribute("style", "display:block");
				ThreadSmsReceived.get(2).add(s);
				smsUserNumberThree.setText(s.getPhoneNumber());
				if (smsUserNameThree.getText().equals("Unknown"))
					smsUserNameThree.setText(s.getName());
				smsTableThree.rebuild(s);
				foundThree++;
			}
		}
	}

	private void create() {
		if (recipientNumber.getValue().isEmpty()
				|| messageArea.getValue().isEmpty())
			return;
		AllRightSMSRequest request = RequestFactory.allRightSMSRequest();
		request.createSms().fire(new Receiver<SmsProxy>() {
			@Override
			public void onSuccess(SmsProxy sms) {
				smsProxy = sms;
				// update dell'sms sul server e invia la notifica C2DM al
				// telefono android
				update(smsProxy);
			}

			@Override
			public void onFailure(ServerFailure error) {
				Window.alert("Sms Non Inviato");
			}
		});
	}

	private void update(SmsProxy sms) {
		if (sms == null)
			return;

		AllRightSMSRequest request = RequestFactory.allRightSMSRequest();
		smsProxy = request.edit(smsProxy);

		//se è un valore dell'autocomplete
		if (!NumberUtility.ValidatePhoneNumber(recipientNumber.getValue())) {
			String name = NumberUtility.extractName(recipientNumber.getValue());
			String number = recipientNumber.getMap().get(name);
		
			// se non sono riuscito ad estrarre il numero correttamente dall'autocomplete, elimino sms sul server
			if(!NumberUtility.ValidatePhoneNumber(number)) 
				request.deleteSms(smsProxy).fire(new Receiver<Void>() {

					@Override
					public void onSuccess(Void response) {
						// TODO Auto-generated method stub
						setStatus("Numero di telefono non corretto, sms non inviato", false);
					}
				});
				
			if (name != null) { // num è null se non è presente nella mappa...
								// quindi è un numero inserito a mano
				smsProxy.setPhoneNumber(NumberUtility.purgeNumber(number));
				smsProxy.setName(name);
			} else {
				smsProxy.setPhoneNumber(NumberUtility
						.purgeNumber(recipientNumber.getValue()));
				smsProxy.setName("Unknown");
			}
		} else {
			smsProxy.setPhoneNumber(NumberUtility.purgeNumber(recipientNumber
					.getValue()));
			smsProxy.setName("Unknown");
		}

		smsProxy.setTextmessage(messageArea.getValue());
		request.updateSms(sms).fire(new Receiver<SmsProxy>() {
			@Override
			public void onSuccess(SmsProxy sms) {
				setStatus("Sms Inviato Correttamente", false);
			}

			@Override
			public void onFailure(ServerFailure error) {
				Window.alert("Sms Non Inviato");
			}
		});
	}

	private void query() {
		RequestFactory.allRightSMSRequest().querySms()
				.fire(new Receiver<List<SmsProxy>>() {
					@Override
					public void onSuccess(List<SmsProxy> smsList) {
						String names = "\n";
						for (SmsProxy sms : smsList) {
							names += " (" + sms.getId() + "): "
									+ sms.getEmailAddress() + "\n" + "Number: "
									+ sms.getPhoneNumber() + " Text: "
									+ sms.getTextmessage() + "\n Nome: "
									+ sms.getName() + "\n sync="
									+ sms.getSync() + " received="
									+ sms.getReceived() + " read="
									+ sms.getRead() + "\n";
						}
						Window.alert("QUERY SUCCESS: Count[" + smsList.size()
								+ "] Values:" + names);
					}
				});
	}

	private void deleteAll() {
		RequestFactory.allRightSMSRequest().querySms()
				.fire(new Receiver<List<SmsProxy>>() {
					@Override
					public void onSuccess(List<SmsProxy> smsList) {
						for (SmsProxy sms : smsList) {
							RequestFactory.allRightSMSRequest().deleteSms(sms)
									.fire();
						}
						Window.alert("ALL MESSAGE DELETED SUCCESSFULLY");
					}
				});
	}

}
