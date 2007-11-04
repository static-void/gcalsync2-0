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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.pim.Event;
import javax.microedition.pim.PIMException;

import com.gcalsync.log.*;
import com.gcalsync.cal.gcal.*;
import com.gcalsync.cal.phonecal.PhoneCalClient;
import com.gcalsync.store.Store;
import com.gcalsync.util.*;
import com.gcalsync.cal.*;
import com.gcalsync.option.Options;

/**
 *
 * @author $Author$
 * @version $Rev: 19 $
 * @date $Date$
 */
public class SyncComponent extends MVCComponent implements Runnable, StatusLogger
{
	private GCalClient gCalClient;
	private PhoneCalClient phoneCalClient;
	private Merger merger;
	private Hashtable phoneEventsByGcalId;
	private Form form;
	GCalFeed[] feedsToSync = null;

	public SyncComponent()
	{
		gCalClient = new GCalClient();
	}

	public SyncComponent(GCalClient gcal)
	{
		gCalClient = gcal;
	}

	public SyncComponent(GCalFeed[] feeds)
	{
		this();
		feedsToSync = feeds;
	}

	public SyncComponent(GCalClient gcal, GCalFeed[] feeds)
	{
		gCalClient = gcal;
		feedsToSync = feeds;
	}

	public Displayable getDisplayable()
	{
		return form;
	}

	protected void initModel() throws Exception {
	}

	protected void createView() throws Exception {
		form = new Form("Download");
	}

	protected void updateView() throws Exception {
	}

	public void commandAction(Command command, Displayable screen)
	{
		// TODO: Allow cancel
		if (command.getCommandType() == Command.EXIT)
		{
			Components.login.showScreen();
		}
	}

	public void handle()
	{
		showScreen();
		new Thread(this).start();
	}


	public void run() 
	{
		try
		{
			CommitComponent commit;
			GCalEvent[] gCalEvents;
			long now = System.currentTimeMillis();
			Options options = Store.getOptions();
			String startDate = DateUtil.longToIsoDate(now, 0 - options.pastDays);
			String endDate = DateUtil.longToIsoDate(now, options.futureDays);
			long startDateLong = DateUtil.isoDateToLong(startDate);
			long endDateLong = DateUtil.isoDateToLong(endDate) + DateUtil.DAY; // want all events starting before midnigth on endDate

			//System.out.println("Syncing from " + startDate + " to " + endDate + " (-" + options.pastDays + " to +" + options.futureDays + ")");
			//update("Reading Google Calendar(s)...");
			gCalClient.setForm(form);

			if (feedsToSync == null)
				gCalEvents = gCalClient.downloadEvents(startDateLong, endDateLong);
			else
				gCalEvents = gCalClient.downloadCalendars(startDate, endDate, feedsToSync);

			Vector newPhoneEvents = new Vector();
			if (options.upload && gCalClient.isAuthorized())
			{
				phoneCalClient = new PhoneCalClient();
				merger = new Merger(phoneCalClient, gCalClient);
				update("Reading phone events...");

				try
				{
					Enumeration eventEnumeration = phoneCalClient.getPhoneEvents(startDateLong, endDateLong);
					int i = 0;
					while (eventEnumeration.hasMoreElements())
					{
						Event phoneEvent = (Event) eventEnumeration.nextElement();
						String gcalId = phoneCalClient.getGCalId(phoneEvent);
	
						//check for new or modified phone events
						if (gcalId == null || (gcalId != null && phoneEvent.isModified()))
						{
							//missing GCal ID indicates that the event was created on the phone
							//and not by Google Calendar
							GCalEvent gCalEvent = merger.copyToGCalEvent(phoneEvent);
							newPhoneEvents.addElement(gCalEvent);
						}
					}
				}
				catch (Exception e)
				{
					ErrorHandler.showError("Failed to open phone calendar", e);
					return;
				}
			}

			if (options.preview)
			{
				PreviewComponent prvw = new PreviewComponent(gCalClient);
				prvw.setEvents(gCalClient.eventVectorToArray(newPhoneEvents), gCalEvents);
				prvw.showScreen();
			}
			else
			{
				commit = new CommitComponent(gCalClient);
				commit.setEvents(gCalClient.eventVectorToArray(newPhoneEvents), gCalEvents);
				commit.handle();
			}
		}
		catch (Exception e)
		{
			ErrorHandler.showError("Sync failed", e);
		}
	}

	public void update(String message)
	{
		form.append(message + "\n");
	}

	public void updateMinor(String message)
	{
		form.delete(form.size() - 1);
		form.append(message + "\n");
	}
}

