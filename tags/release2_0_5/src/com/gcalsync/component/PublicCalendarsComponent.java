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

import javax.microedition.lcdui.*;
import java.util.Vector;
import com.gcalsync.cal.gcal.*;
import com.gcalsync.log.ErrorHandler;
import com.gcalsync.log.GCalException;
import com.gcalsync.store.Store;
import com.gcalsync.option.Options;

/**
 * @author $Author$
 * @version $Rev$
 * @date $Date$
 */
public class PublicCalendarsComponent extends MVCComponent
{
	public static final String CAL_URL_STARTER = "www.google.com/calendar/feeds/";
	public static final String FULL_CAL_URL_STARTER = "http://" + CAL_URL_STARTER;

    static final Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, 4);
	static final Command CMD_OPTIONS = new Command("Options", Command.ITEM, 3);
	static final Command CMD_SAVE = new Command("Save", Command.ITEM, 2);
	static final Command CMD_SYNC = new Command("Sync", "Start sync", Command.ITEM, 1);
	static final Command CMD_FULL_SYNC = new Command("Full Sync", "Start full sync", Command.ITEM, 1);

	TextField[] calendarUrls = new TextField[5];
	Form form;

	/**
    * Gets the <code>Displayable</code> object to be displayed for
    * this component
    *
    * @returns <code>Displayable</code>
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
	protected void createView() throws Exception
	{
            try {
		form = new Form("Public Calendars");
		int screenWidth = getDisplayable().getWidth();

		//add note
		form.append(new Spacer(screenWidth, 5));
		form.append("Enter public calendar URLs");

		//add form items
		form.append(new Spacer(screenWidth, 5));
		addPubCalFields();

		//populate form items
		updateView();

		//add commands to form
		form.addCommand(CMD_OPTIONS);
		form.addCommand(CMD_CANCEL);
		form.addCommand(CMD_SAVE);
		form.addCommand(CMD_SYNC);
		form.addCommand(CMD_FULL_SYNC);

		form.setCommandListener(this);
            }catch(Exception e) {
                throw new GCalException(this.getClass(), "createView", e);
            }
	}

	/**
    * Updates the view with the saved calendar URLs
	*/
	protected void updateView() throws Exception
	{
            try {
		Options options = Store.getOptions();

		for (int i=0; i<calendarUrls.length; i++)
			calendarUrls[i].setString(options.calendarUrls[i]);
            }catch(Exception e) {
                throw new GCalException(this.getClass(), "updateView", e);
            }
	}

	/**
    * Processes menu commands
    *
    * @param c command to execute
    * @param d the form from which the command originates
	*/
	public void commandAction(Command command, Displayable d)
	{
            try {
		int index;
		Alert a;
		Options options = Store.getOptions();
		GCalFeed[] feeds;
		Vector vect;
		GCalFeed tempFeed;

		if (command == CMD_CANCEL)
		{
			Components.login.showScreen();
		}
		else if (command == CMD_OPTIONS)
		{
			Components.options.showScreen(this);
		}
		else if (command == CMD_SAVE)
		{
			save();
			a = new Alert("Saved", "Calendar URLs saved", null, AlertType.INFO);
			a.setTimeout(1000);
			display.setCurrent(a);
		}
		else if (command == CMD_SYNC || command == CMD_FULL_SYNC)
		{
			if (command == CMD_FULL_SYNC)
				Store.getTimestamps().lastSync = 0;

			// save calendar URLs and then sync
			save();

			vect = new Vector();
			for (int i=0; i<options.calendarUrls.length; i++)
			{
				if (!options.calendarUrls[i].equals(""))
				{
					//basic feeds not supported...switch to full feed
					if (options.calendarUrls[i].endsWith("/basic"))
					{
						index = options.calendarUrls[i].lastIndexOf('/');
						options.calendarUrls[i] = options.calendarUrls[i].substring(0, index) + "/full";
					}

					tempFeed = new GCalFeed();
					tempFeed.url = FULL_CAL_URL_STARTER + options.calendarUrls[i];
					tempFeed.title = "URL" + (i+1);
					tempFeed.sync = true;
					tempFeed.reminders = true;
					vect.addElement(tempFeed);
				}
			}

			if (vect.size() > 0)
			{
				feeds = new GCalFeed[vect.size()];
				vect.copyInto(feeds);

				//start sync on new thread
				new SyncComponent(feeds).handle();
			}
			else
			{
				a = new Alert("Error", "No calendar URLs entered", null, AlertType.ERROR);
				a.setTimeout(1000);
				display.setCurrent(a);
			}
		}
            }catch(Throwable t) {
                ErrorHandler.showError(t);
            }
	}

	/**
    * Saves the calendar URLs
	*/
	void save() throws Exception
	{
            try {
		Options options = Store.getOptions();

		//save calendar URLs
		for (int i=0; i<calendarUrls.length; i++)
			options.calendarUrls[i] = calendarUrls[i].getString().trim();

		Store.saveOptions();
            }catch(Exception e) {
                throw new GCalException(this.getClass(), "save", e);
            }
	}

	/**
    * Adds the calendar URLs to the form
	*/
	void addPubCalFields()
	{
		StringItem calUrlStarter;
		final Font labelFont = Font.getFont(Font.FACE_PROPORTIONAL, 
											Font.STYLE_BOLD,
											Font.SIZE_SMALL);

		for (int i=0; i<calendarUrls.length; i++)
		{
			calendarUrls[i] = new TextField("",
                                            "",
                                            100,
                                            TextField.URL);

			calUrlStarter = new StringItem((i+1) + ") " + CAL_URL_STARTER, null);
			calUrlStarter.setFont(labelFont);
			form.append(calUrlStarter);
			form.append(calendarUrls[i]);
			form.append(new Spacer(getDisplayable().getWidth(), 5));
		}
	}
}

