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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.omg.CORBA.PUBLIC_MEMBER;

import com.allrightsms.client.MyRequestFactory.HelloWorldRequest;
import com.allrightsms.shared.AllRightSMSRequest;
import com.allrightsms.shared.SmsProxy;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

public class AllRightSMSWidget extends Composite {

	private static final int ARRAY_LENGHT = 3;
	private static final int STATUS_DELAY = 4000;
	private static final String STATUS_ERROR = "status error";
	private static final String STATUS_NONE = "status none";
	private static final String STATUS_SUCCESS = "status success";
	private static final int DELAY_MS = 1000; // delay of research incoming, 1
												// 000
												// minute

	private static final String BUTTON_STYLE = "send centerbtn";
	private SmsProxy smsProxy;
	final MyRequestFactory RequestFactory = GWT.create(MyRequestFactory.class);
	final EventBus eventBus = new SimpleEventBus();
	private List<List<SmsProxy>> ThreadSmsReceived = new LinkedList<List<SmsProxy>>();
	
	interface AllRightSMSUiBinder extends UiBinder<Widget, AllRightSMSWidget> {
	}

	private static AllRightSMSUiBinder uiBinder = GWT
			.create(AllRightSMSUiBinder.class);

	@UiField
	TextAreaElement messageArea;

	@UiField
	Label signin;

	@UiField
	InputElement recipientNumber;

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
	SmsUserPicture smsUserPictureOne; //, smsUserPictureTwo, smsUserPictureThree;
	
	@UiField 
	SmsUserName smsUserNameOne; //, smsUserNameTwo, smsUserNameThree;
	
	@UiField 
	SmsUserNumber smsUserNumberOne; //, smsUserNumberTwo, smsUserNumberThree;
	
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
			// recipientArea.setValue("");
			messageArea.setValue("");
			recipientNumber.setValue("");
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

	public static class SmsTable extends CellTable<SmsProxy> {
		public Column<SmsProxy, String> textColumn;
		public Column<SmsProxy, String> numberColumn;
		public Column<SmsProxy, Date> dateColumn;
		public Column<SmsProxy, String> replyColumn;

		interface SmsTableResources extends CellTable.Resources {
			@Source("SmsTable.css")
			TableStyle cellTableStyle();
		}

		interface TableStyle extends CellTable.Style {
			String columnCheckbox();

			String columnName();

			String columnDate();

			String columnTrash();
		}

		private static SmsTableResources resources = GWT
				.create(SmsTableResources.class);

		public SmsTable() {
			super(20, resources);

			textColumn = new Column<SmsProxy, String>(new TextCell()) {
				@Override
				public String getValue(SmsProxy object) {
					return object.getTextmessage();
				}
			};
			addColumn(textColumn, "Text");
			// addColumnStyleName(1, "columnFill");
			addColumnStyleName(1, resources.cellTableStyle().columnName());

		/*	numberColumn = new Column<SmsProxy, String>(new TextCell()) {
				@Override
				public String getValue(SmsProxy object) {
					return object.getPhoneNumber();
				}
			};
			addColumn(numberColumn, "Number");
			addColumnStyleName(2, resources.cellTableStyle().cellTableEvenRow());
*/
			PredefinedFormat dateFormat = PredefinedFormat.HOUR24_MINUTE;
			// TODO controllo data, se < di un giorno OK, altrimenti la cambi in giorno e mese PredefinedFormat.DATE_SHORT
			dateColumn = new Column<SmsProxy, Date>(new DateCell(  //DatePickerCell - il  DatePickerCell permette di modificare la data
					DateTimeFormat.getFormat(dateFormat))) { // MONTH_ABBR_DAY)))
																					// {
				@Override
				public Date getValue(SmsProxy s) {
					Date dueDate = s.getDueDate();
					return dueDate == null ? new Date() : dueDate;
				}
			};
			addColumn(dateColumn, "Date");
			addColumnStyleName(2, resources.cellTableStyle().columnDate());

			// setColumnWidth(nameColumn, 65.0, Unit.PCT);

	/*		ButtonCell buttonCell = new ButtonCell(
					new SafeHtmlRenderer<String>() {
						public SafeHtml render(String object) {
							return SafeHtmlUtils.fromTrustedString("Rispondi"); // <img
																				// src=\"reply.png\"></img>
						}

						public void render(String object,
								SafeHtmlBuilder builder) {
							builder.append(render(object));
						}
					});

			replyColumn = new Column<SmsProxy, String>(buttonCell) {
				@Override
				public String getValue(SmsProxy object) {
					return "\u2717"; // Ballot "X" mark
				}
			};
			addColumn(replyColumn, "\u2717");
			addColumnStyleName(3, resources.cellTableStyle().columnTrash());
		*/
		}
	}
	
	public AllRightSMSWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		sayHelloButton.getElement().setClassName("send centerbtn");
		sendMessageButton.getElement().setClassName("send");
		replyButtonOne.getElement().setClassName("send");
		replyButtonTwo.getElement().setClassName("send");
		replyButtonThree.getElement().setClassName("send");
		
		// recipientArea.setValue("allrightsms@gmail.com");
		// recipientArea.setDisabled(true);

		// inizializzo il bus per le chiamate al server
		RequestFactory.initialize(eventBus);

		// inizio comportamento tabella uno
		ListDataProvider<SmsProxy> listDataProvider = new ListDataProvider<SmsProxy>();
		listDataProvider.addDataDisplay(smsTableOne);
		ThreadSmsReceived.add(0,listDataProvider.getList()); 
		
/*		smsTableOne.replyColumn
				.setFieldUpdater(new FieldUpdater<SmsProxy, String>() {

					@Override
					public void update(int index, SmsProxy object, String value) {
						recipientNumber.setValue(object.getPhoneNumber());
						messageArea.focus();

					}
				});
	*/
		// fine comportamento tabella uno

		//inizio comportamento tabella due
		ListDataProvider<SmsProxy> listDataProvider2 = new ListDataProvider<SmsProxy>();
		listDataProvider2.addDataDisplay(smsTableTwo);
		ThreadSmsReceived.add(1,listDataProvider2.getList()); 

	/*	smsTableTwo.replyColumn
				.setFieldUpdater(new FieldUpdater<SmsProxy, String>() {

					@Override
					public void update(int index, SmsProxy object, String value) {
						recipientNumber.setValue(object.getPhoneNumber());
						messageArea.focus();

					}
				});
		// fine comportamento tabella due
	*/	
		//inizio comportamento tabella tre
		ListDataProvider<SmsProxy> listDataProvider3 = new ListDataProvider<SmsProxy>();
		listDataProvider3.addDataDisplay(smsTableThree);
		ThreadSmsReceived.add(2,listDataProvider3.getList()); 

	/*	smsTableThree.replyColumn
				.setFieldUpdater(new FieldUpdater<SmsProxy, String>() {

					@Override
					public void update(int index, SmsProxy object, String value) {
						recipientNumber.setValue(object.getPhoneNumber());
						messageArea.focus();

					}
				});
		// fine comportamento tabella tre
	*/	
		sendMessageButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// creo l'sms sul server
				create();
			}
		});

		//ClickHandler per il bottone reply uno
		replyButtonOne.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(!ThreadSmsReceived.isEmpty() && !ThreadSmsReceived.get(0).isEmpty())
					reply(ThreadSmsReceived.get(0).get(0).getPhoneNumber());
			}
		});
		
		replyButtonTwo.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(!ThreadSmsReceived.isEmpty() && !ThreadSmsReceived.get(1).isEmpty())
					reply(ThreadSmsReceived.get(1).get(0).getPhoneNumber());
			}
		});
		
		replyButtonThree.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(!ThreadSmsReceived.isEmpty() && !ThreadSmsReceived.get(2).isEmpty())
					reply(ThreadSmsReceived.get(2).get(0).getPhoneNumber());
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

		Button createButton = new Button();
		createButton.setText("Create");
		createButton.getElement().setClassName(BUTTON_STYLE);
		hp.add(createButton);
		Button updateButton = new Button();
		updateButton.setText("Update");
		updateButton.getElement().setClassName(BUTTON_STYLE);
		hp.add(updateButton);
		Button queryButton = new Button();
		queryButton.setText("Query");
		queryButton.getElement().setClassName(BUTTON_STYLE);
		hp.add(queryButton);
		Button deleteButton = new Button();
		deleteButton.setText("Delete");
		deleteButton.getElement().setClassName(BUTTON_STYLE);
		hp.add(deleteButton);
		Button deleteAllButton = new Button();
		deleteAllButton.setText("Delete All");
		deleteAllButton.getElement().setClassName(BUTTON_STYLE);
		hp.add(deleteAllButton);

		Button queryUnreadButton = new Button();
		queryUnreadButton.setText("Unread");
		queryUnreadButton.getElement().setClassName(BUTTON_STYLE);
		hp.add(queryUnreadButton);

		queryUnreadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				queryUnread();
			}
		});

		createButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				create();
			}
		});

		updateButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				update(smsProxy);
			}
		});

		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				delete(smsProxy);
			}
		});

		deleteAllButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteAll();
			}
		});

		queryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				query();
			}
		});

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
		// modificato
	}

	private void reply(String number){
		recipientNumber.setValue(number);
		messageArea.focus();
	}

	private void retrieveReceivedSms() {

		AllRightSMSRequest request = RequestFactory.allRightSMSRequest();
		request.queryUnReadSms().fire(new Receiver<List<SmsProxy>>() {
			// request.querySms().fire(new Receiver<List<SmsProxy>>() {
			@Override
			public void onSuccess(List<SmsProxy> response) {
				if (response.size() > 0) {  //&& threadReceived.size() != response.size()
					signin.setText(response.get(0).getEmailAddress());

					ThreadSmsReceived.get(0).clear();
					ThreadSmsReceived.get(1).clear();
					ThreadSmsReceived.get(2).clear();
					for (SmsProxy sms : response) {
						smsUserNumberOne.setText(sms.getPhoneNumber());
						
						ThreadSmsReceived.get(0).add(sms);
						
					
						ThreadSmsReceived.get(1).add(sms);
						ThreadSmsReceived.get(2).add(sms);
					}
					
				
				//	SoundController soundController = new SoundController();
				//	@SuppressWarnings("deprecation")
				//	Sound sound = soundController.createSound(
				//			Sound.MIME_TYPE_AUDIO_MPEG,
				//			"bells-message.mp3");
				//	sound.play();
				}
			}
		});
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
				update(smsProxy); // lo chiamo qui almeno sono sicuro di
									// aggiornare l'sms corrente!
			}

			@Override
			public void onFailure(ServerFailure error) {
				Window.alert("UNABLE TO CREATE SMS");
			}
		});
	}

	private void update(SmsProxy sms) {
		if (sms == null)
			return;

		AllRightSMSRequest request = RequestFactory.allRightSMSRequest();
		smsProxy = request.edit(smsProxy);
		smsProxy.setDueDate(new Date());
		smsProxy.setEmailAddress("allrightsms@gmail.com"); // recipientArea.getValue()
		smsProxy.setPhoneNumber(recipientNumber.getValue());
		smsProxy.setTextmessage(messageArea.getValue());
		request.updateSms(sms).fire(new Receiver<SmsProxy>() {
			@Override
			public void onSuccess(SmsProxy sms) {
				/*
				 * Window.alert("UPDATE SUCCESS:(" + sms.getId() + "): " +
				 * sms.getPhoneNumber() + "\n messaggio: " +
				 * sms.getTextmessage());
				 */
				setStatus("Sms Inviato Correttamente", false);
			}

			@Override
			public void onFailure(ServerFailure error) {
				Window.alert("UNABLE TO UPDATE");
			}
		});
	}

	private void delete(SmsProxy smsProxy) {
		RequestFactory.allRightSMSRequest().deleteSms(smsProxy)
				.fire(new Receiver<Void>() {
					@Override
					public void onSuccess(Void v) {
						Window.alert("DELETE SUCCESS");
					}

					@Override
					public void onFailure(ServerFailure error) {
						Window.alert("UNABLE TO DELETE");
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
									+ sms.getTextmessage() + "\n sync="
									+ sms.getSync() + " received="
									+ sms.getReceived() + " read="
									+ sms.getRead() + "\n";
						}
						Window.alert("QUERY SUCCESS: Count[" + smsList.size()
								+ "] Values:" + names);
					}
				});
	}

	private void queryUnread() {
		RequestFactory.allRightSMSRequest().queryUnReadSms()
				.fire(new Receiver<List<SmsProxy>>() {
					@Override
					public void onSuccess(List<SmsProxy> smsList) {
						String names = "\n";
						for (SmsProxy sms : smsList) {
							names += " (" + sms.getId() + "): "
									+ sms.getEmailAddress() + "\n" + "Number: "
									+ sms.getPhoneNumber() + " Text: "
									+ sms.getTextmessage() + "\n sync="
									+ sms.getSync() + " received="
									+ sms.getReceived() + "\n";
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
