/*
   Copyright 2007 batcage@gmail.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.gcalsync.component;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;
import com.gcalsync.cal.gcal.*;
import com.gcalsync.store.Store;
import com.gcalsync.option.Options;

/**
 * @author $Author$
 * @version $Rev$
 * @date $Date$
 */
public class LoginComponent extends MVCComponent implements ItemCommandListener, ItemStateListener, Runnable
{
	public boolean returnToMainMenu = false;

	static final Command CMD_SKIP = new Command("Skip", Command.ITEM, 1);
	static final Command CMD_SIGNIN = new Command("Sign in", Command.OK, 2);
	static final Command CMD_OPTIONS = new Command("Options", Command.ITEM, 3);
	static final Command CMD_ABOUT = new Command("About", Command.ITEM, 3);
	static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 4);
	static final String welcomeMsg = "Welcome to GCalSync";
	static final String signInMsg = "Sign in with your Google account";
	static final String inProgressMsg = "Signing in...";

	ChoiceGroup chkSavePasswd = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, new String[]{"Save password"}, null);
	ChoiceGroup chkAutoLogin = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, new String[]{"Auto sign in"}, null);
	StringItem btnLogin = new StringItem("Sign in", null, Item.BUTTON);
	StringItem linkSkip = new StringItem("Skip to public calendars", null, Item.HYPERLINK);
	StringItem lblUsername = new StringItem("Username", null);
	StringItem lblPassword = new StringItem("Password", null);
	StringItem lblSignIn = new StringItem(signInMsg, null);
	TextField txtUsername = new TextField("", Store.getOptions().username, 64, TextField.EMAILADDR);
	TextField txtPassword = new TextField("", Store.getOptions().password, 64, TextField.SENSITIVE | TextField.PASSWORD);

	Form form;
	MIDlet midlet;

    /**
    * Constructor
	*/
	public LoginComponent()
	{
		this(null);
	}

    /**
    * Constructor
	*/
	public LoginComponent(MIDlet m)
	{
		setMidlet(m);
	}

	/**
    * Links this Login Component to the specified MIDlet
	*/
	public void setMidlet(MIDlet m)
	{
		midlet = m;
	}

	/**
    * Gets the form for this component
    *
    * @returns <code>form</code>
	*/
	public Displayable getDisplayable()
	{
		return form;
	}

	/**
	* Initializes the model before creating the view
	*/
	protected void initModel()
	{
	}

	/**
    * Creates the view
	*/
	protected void createView()
	{
		form = new Form(welcomeMsg);

		//add form items
		form.append(new Spacer(getDisplayable().getWidth(), 5));
		addLoginFields();
		addSkipHyperlink();

		//populate form items
		updateView();

		//add commands to form
		form.addCommand(CMD_SIGNIN);
		form.addCommand(CMD_SKIP);
		form.addCommand(CMD_ABOUT);
		form.addCommand(CMD_OPTIONS);
		form.addCommand(CMD_EXIT);
		form.setCommandListener(this);
		form.setItemStateListener(this);
	}

	/**
    * Updates the screen and begins auto sign-in if so configured
	*/
	public void handle()
	{
		super.handle();
		//auto sign in
		if (Store.getOptions().autoLogin) signIn();
	}

	/**
	* Updates the view after it is created
	*/
	protected void updateView()
	{
		Options options = Store.getOptions();
		//update credentials fields
		txtUsername.setString(options.username);
		txtPassword.setString(options.password);

		//The password is only saved if the "Save password" checkbox was
		// checked when the User signed in. So, assume that the User
		// still wants to save his password if the password record is not blank.
		if (!options.password.equals(""))
			chkSavePasswd.setSelectedIndex(0, true);

		chkAutoLogin.setSelectedIndex(0, options.autoLogin);
	}

	/**
    * Processes item commands
    *
    * @param c command to execute
    * @param item the form item from which the command originates
	*/
	public void commandAction(Command c, Item item)
	{
		//sign in
		if (c == CMD_SIGNIN)
			signIn();
		//skip to public calendars
		else if (c == CMD_SKIP)
			Components.pubCal.handle();
	}

	/**
    * Processes menu commands
    *
    * @param c command to execute
    * @param d the form from which the command originates
	*/
	public void commandAction(Command c, Displayable d)
	{
		if (c == CMD_SIGNIN)
			signIn();
		else if (c == CMD_SKIP)
			Components.pubCal.handle();
		else if (c == CMD_OPTIONS)
			Components.options.showScreen(this);
		else if (c == CMD_ABOUT)
			new AboutComponent(midlet).handle();
		else if (c == CMD_EXIT)
			if (midlet != null) midlet.notifyDestroyed();
	}

	/**
    * Processes state changes to form items
    *
    * @param item the form item that has changed
	*/
	public void itemStateChanged(Item item)
	{
		Options options = Store.getOptions();
		String username = txtUsername.getString().trim();
		String password = txtPassword.getString();

		if (item == chkAutoLogin)
		{
			//username/password must be populated and password must be saved
			//for auto sign-in to work
			if (username.length() > 0
				&& password.length() > 0
				&& chkSavePasswd.isSelected(0))
			{
				options.autoLogin = chkAutoLogin.isSelected(0);
				Store.saveOptions();
			}
		}
		else if (item == chkSavePasswd)
		{
			//username and password must be populated
			//in order to save the password
			if (password.length() > 0)
			{
				options.password = password;
				Store.saveOptions();
			}
		}
	}

	/**
    * Entry point for new thread
	*/
	public void run()
	{
		Alert a;
		Options options = Store.getOptions();
		GCalClient gcal = new GCalClient();

		//show sign-in in progress
		lblSignIn.setLabel(inProgressMsg);
		try {
			display.setCurrentItem(lblSignIn);
		} catch (Exception e) {
		}
		
		String loginErr = gcal.login(options.username, txtPassword.getString());

		//login to GCal
		if (loginErr == null)
		{
			//jump to calendar list
			Components.feeds = new CalendarFeedsComponent(gcal);
			Components.feeds.handle();
		}
		else
		{
			if (loginErr.indexOf("BadAuthentication") >= 0) {
				loginErr = "Username and password do not match. (You provided " + options.username + ")";
			}
			
			a = new Alert("Error", loginErr, null, AlertType.ERROR);
			a.setTimeout(Alert.FOREVER);
            display.setCurrent(a);
		}

		//restore sign-in message
		lblSignIn.setLabel(signInMsg);
	}

	/**
    * Saves username (and password) and then signs into Google
    * Calendar
	*/
	void signIn()
	{
		Alert a;

		if (txtUsername.getString().trim().length() > 0
			&& txtPassword.getString().length() > 0)
		{
			//save credentials and then start login on another 
			// thread to prevent phone lock-up
			saveCredentials();
			new Thread(this).start();
		}
		else
		{
			a = new Alert("Error", "Username or password is blank.", null, AlertType.ERROR);
			a.setTimeout(Alert.FOREVER);
            display.setCurrent(a);
		}
	}

	/**
    * Saves username and password. The password is only saved if the
    * "Save password" box is checked. If the box is unchecked, the
    * existing saved password is cleared from memory.
	*/
	void saveCredentials()
	{
		Options options = Store.getOptions();
		String username = txtUsername.getString().trim();

		//exclude @gmail.com if it exists
		int index = username.indexOf("@gmail.com");
		if (index >= 0) username = username.substring(0, index);

		options.username = username;

		//get password only if it's to be saved
		if (chkSavePasswd.isSelected(0))
		{
			options.password = txtPassword.getString();
			options.autoLogin = chkAutoLogin.isSelected(0);
		}
		else
		{
			options.password = "";
			options.autoLogin = false;
		}

		//save credentials
		Store.saveOptions();
	}

	/**
    * Adds the "Skip" hyperlink to the form
	*/
	void addSkipHyperlink()
	{
		Font linkFont = Font.getFont(Font.FACE_PROPORTIONAL, 
									 Font.STYLE_BOLD | Font.STYLE_UNDERLINED,
									 Font.SIZE_SMALL);

		linkSkip.setDefaultCommand(CMD_SKIP);
		linkSkip.setItemCommandListener(this);
		linkSkip.setFont(linkFont);

		form.append(linkSkip);
	}

	/**
    * Adds the Login fields to the form
	*/
	void addLoginFields()
	{
		int screenWidth = getDisplayable().getWidth();

		Font labelFont = Font.getFont(Font.FACE_PROPORTIONAL, 
									  Font.STYLE_BOLD,
									  Font.SIZE_SMALL);

		Font titleFont = Font.getFont(Font.FACE_PROPORTIONAL, 
									  Font.STYLE_PLAIN,
									  Font.SIZE_SMALL);

		Font buttonFont = Font.getFont(Font.FACE_PROPORTIONAL, 
									   Font.STYLE_BOLD | Font.STYLE_UNDERLINED,
									   Font.SIZE_SMALL);

		btnLogin.setDefaultCommand(CMD_SIGNIN);
		btnLogin.setItemCommandListener(this);

		//add username and pwd text fields with spacers in between
		lblSignIn.setFont(titleFont);
		form.append(lblSignIn);
		form.append(new Spacer(screenWidth,5));
		lblUsername.setFont(labelFont);
		form.append(lblUsername);
		form.append(new Spacer(screenWidth,1));
		form.append(txtUsername);
		form.append(new Spacer(screenWidth,2));
		lblPassword.setFont(labelFont);
		form.append(lblPassword);	
		form.append(new Spacer(screenWidth,1));
		form.append(txtPassword);
		form.append(new Spacer(screenWidth,10));

		//add checkbox options and login button with spacers in between
		form.append(chkSavePasswd);
		form.append(new Spacer(screenWidth,2));
		form.append(chkAutoLogin);
		form.append(new Spacer(screenWidth,10));
		btnLogin.setFont(buttonFont);
		form.append(btnLogin);
		form.append(new Spacer(screenWidth,5));
	}

}

