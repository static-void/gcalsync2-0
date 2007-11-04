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

import com.gcalsync.log.*;
import com.gcalsync.cal.gcal.GCalClient;
import com.gcalsync.cal.gcal.GCalFeed;
import com.gcalsync.store.Store;
import com.gcalsync.util.HttpUtil;

import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.*;
import java.util.Vector;

/**
 * @author 
 * @version
 * @date 
 */
public class CalendarFeedsComponent extends MVCComponent implements Runnable
{
	static final Command CMD_OPTIONS = new Command("Options", Command.ITEM, 3);
	static final Command CMD_SYNC = new Command("Sync", "Start sync", Command.ITEM, 1);
	static final Command CMD_FULL_SYNC = new Command("Full Sync", "Start full sync", Command.ITEM, 1);
	static final Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, 4);
	static final Command CMD_DOWNLOAD_LIST = new Command("Download", "Download calendar list", Command.ITEM, 2);

	Form form;
	StringItem title;
	GCalClient gCalClient;
	GCalFeed[] feeds;
	ChoiceGroup[] syncChoices;
	ChoiceGroup[] reminderChoices;

	/**
     * Constructor
	 */
	public CalendarFeedsComponent()
	{
		gCalClient = new GCalClient();
	}

	/**
     * Constructor that allows installation of an existing GCal
     * client
	 */
	public CalendarFeedsComponent(GCalClient gcal)
	{
		gCalClient = gcal;
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
     * Creates a new form and updates the view with a fresh calendar
     * list
	 */
	protected void createView()
	{
		String title = Store.getOptions().username;

		//get username and append "Calendars"
		if (!title.equals("")) title += "'s ";
		title += "Calendars";

		form = new Form(title);
		downloadFeeds();
	}

	/**
     * Updates the view with a fresh calendar list
	 */
	protected void updateView()
	{
		downloadFeeds();
	}

	/**
     * Handles commands from the menu, buttons, and links
     *
     * @param c command to execute
     * @param d <code>Displayable</code> from which the
     *                    command originated
	 */
	public void commandAction(Command c, Displayable d)
	{

		Alert a;

		if (c == CMD_DOWNLOAD_LIST)
			downloadFeeds();
		else if (c == CMD_OPTIONS)
			Components.options.showScreen(this);
		else if (c == CMD_CANCEL)
			Components.login.showScreen();
		else if (c == CMD_SYNC || c == CMD_FULL_SYNC)
		{
			if (isSelected(syncChoices))
			{
				if (c == CMD_FULL_SYNC)
					Store.getTimestamps().lastSync = 0;

				saveCalendarSettings();
				new SyncComponent(gCalClient, feeds).handle();
			}
			else
			{
				a = new Alert("Error", "No calendars selected for Sync", null, AlertType.ERROR);
				a.setTimeout(1000);
				display.setCurrent(a);
			}
		}
	}

	/**
     * Downloads the calendar list for the current User and adds the
     * list to the form
	 */
	public void run()
	{
		Alert a;
		String err;

		try
		{
			//add commands to form
			form.addCommand(CMD_OPTIONS);
			form.addCommand(CMD_CANCEL);

			feeds = gCalClient.downloadFeeds();

			//if no feeds were downloaded, show error
			if (feeds.length == 0) {
				if (HttpUtil.getLastResponseCode() == HttpConnection.HTTP_OK) {
					err = "Calendars unavailable";
				} else {
					//show server error
					err = "ERR (" + HttpUtil.getLastResponseCode() + ") " + HttpUtil.getLastResponseMsg();
				}

				a = new Alert("Error", err, null, AlertType.ERROR);
				a.setTimeout(Alert.FOREVER);
				display.setCurrent(a, Components.login.getDisplayable());

			//otherwise, add feeds to Calendar List
			} else {

				//if we're downloading all calendars, then download the
				//event details and reminders of all calendars (and pass
				//to the Preview component if previews are enabled)
				if (Store.getOptions().downloadAllCalendars) {
					Store.getTimestamps().lastSync = 0;

					for (int i=0; i<feeds.length; i++) {
						feeds[i].sync = true;
						feeds[i].reminders = true;
					}

					new SyncComponent(gCalClient, feeds).handle();
				} else {
					addCalendarList();
					form.addCommand(CMD_DOWNLOAD_LIST);
					form.addCommand(CMD_FULL_SYNC);
					form.addCommand(CMD_SYNC);
				}
			}
		}
		catch (Exception e)
		{
			ErrorHandler.showError("Failed to get calendar list", e);
		}
	}

	/**
     * Initiates new thread to download calendar list
	 */
	void downloadFeeds()
	{
		//clean form
		form.deleteAll();
		form.append(new Spacer(getDisplayable().getWidth(), 5));
		form.append(new StringItem("Downloading calendar list...", null));
		new Thread(this).start();
	}

	/**
     * Populates form with calendar list, including the calendar
     * title and checkboxes that enable syncing and reminders
	 */
	void addCalendarList()
	{
		Font feedTitleFont = Font.getFont(Font.FACE_PROPORTIONAL, 
										  Font.STYLE_BOLD,
										  Font.SIZE_SMALL);

		//clean form
		form.deleteAll();

		//copy saved calendar settings (sync/reminders) to the new calendar list
		copyCalendarSettings();

		//add calendars by title and append checkboxes for selection of 
		//syncing and reminders
		reminderChoices = new ChoiceGroup[feeds.length];
		syncChoices = new ChoiceGroup[feeds.length];
		for (int i = 0; i < feeds.length; ++i)
		{
			title = new StringItem(feeds[i].title, null);
			title.setFont(feedTitleFont);
			syncChoices[i] = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, new String[]{"Sync"}, null);
			reminderChoices[i] = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, new String[]{"Reminders"}, null);
			syncChoices[i].setSelectedIndex(0, feeds[i].sync);
			reminderChoices[i].setSelectedIndex(0, feeds[i].reminders);

			form.append(title);
			form.append(syncChoices[i]);
			form.append(reminderChoices[i]);
		}
	}

	/**
     * Copies saved calendar settings into new calendar list
	 */
	void copyCalendarSettings()
	{
		GCalFeed[] newFeeds = feeds;
		GCalFeed[] oldFeeds = Store.getFeeds();
		Vector newIds = new Vector();
		Vector oldIds = new Vector();
		int feedIndex;

		//compare all the ids from the new feeds and the saved feeds
		for (int i=0; i<newFeeds.length; i++) newIds.addElement(newFeeds[i].id);
		for (int i=0; i<oldFeeds.length; i++) oldIds.addElement(oldFeeds[i].id);

		if (!oldIds.isEmpty() && !newIds.isEmpty())
		{
			//if a saved calendar is found in the new list, then
			//update the new list entry with the calendar settings
			for (int i=0; i<newIds.size(); i++)
			{
				feedIndex = oldIds.indexOf(newIds.elementAt(i));
				if (feedIndex >= 0)
				{
					newFeeds[i].sync = oldFeeds[feedIndex].sync;
					newFeeds[i].reminders = oldFeeds[feedIndex].reminders;
				}
			}
		}
	}

	/**
     * Saves sync and reminder settings for all calendars
	 */
	void saveCalendarSettings()
	{
		for (int i = 0; i < feeds.length; i++)
		{
			feeds[i].sync = syncChoices[i].isSelected(0);
			feeds[i].reminders = reminderChoices[i].isSelected(0);
		}
		Store.deleteFeeds();
		Store.setFeeds(feeds);
		Store.saveFeeds();
	}

	/**
     * Determines if the given <code>ChoiceGroup</code> array
     * contains a selected item
     *
     * @param choices <code>ChoiceGroup</code> array to search
	 */
	boolean isSelected(ChoiceGroup[] choices)
	{
		boolean rval = false;

		for (int i=0; i<choices.length; i++)
		{
			if (choices[i].isSelected(0))
			{
				rval = true;
				break;
			}
		}

		return rval;
	}
}
