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
package com.allrightsms.server;

import java.util.Date;
import java.util.logging.Logger;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class HelloWorldService {

  private static final Logger log = Logger.getLogger(HelloWorldService.class.getName());

  public HelloWorldService() {
  }

  public static String getMessage() {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    String message;
    if (user == null) {
      message = "No one is logged in!\nSent from App Engine at " + new Date();
    } else {
      message = "Hello, " + user.getEmail() + "!\nSent from App Engine at " + new Date();
    }
    log.info("Returning message \"" + message + "\"");
    return message;
  }
  
  public static String getMail()
  {
	  UserService userService = UserServiceFactory.getUserService();
	  User user = userService.getCurrentUser();
	  
	  String thisURL = "allrightsms.appspot.com";
	  String retval = "allrightsms.appspot.com";
		
		if (user != null) {
			retval += "<p>Hello, " + user.getEmail()
							+ "!  You can <a href=\""
							+ userService.createLogoutURL(thisURL)
							+ "\">sign out</a>.</p>";
		} else {
			retval += "<p>Please <a href=\""
							+ userService.createLoginURL("")
							+ "\">sign in</a>.</p>";
		}
	  
	  return retval;
  }  
}
