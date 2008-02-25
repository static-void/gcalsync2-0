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

import java.util.Vector;
import javax.microedition.lcdui.*;
import com.gcalsync.cal.gcal.GCalEvent;
import com.gcalsync.cal.gcal.GCalClient;
import com.gcalsync.log.ErrorHandler;
import com.gcalsync.log.GCalException;
import com.gcalsync.store.Store;

/**
 * Public class responsible for drawing the display and options for the Preview
 * screen. The Preview screen displays events that are available for upload/download.
 * Uses the MVCComponent base class to retrieve, draw, and update screens. 
 * <ol>
 * <li>User signs into the application.</li> 
 * <li>Application displays calendars that are available for syncing.</li> 
 * <li>User selects calendars to sync.</li>
 * <li>User selects sync option through menu button on device.</li>
 * <li>Application fetches events that are available for syncing and displays 
 *     in the Preview screen.</li>
 * </ol>
 *  
 */
public class PreviewComponent extends MVCComponent 
{
	//sets cancel button on device
        static final Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, 4);
	//sets items on menu that display when a user presses the Menu button on the device
        static final Command CMD_COMMIT = new Command("Commit", "Commit selected events", Command.ITEM, 2);
	static final Command CMD_SELECT_DL = new Command("Select DLs", "Select all download events", Command.ITEM, 1);
	static final Command CMD_SELECT_UL = new Command("Select ULs", "Select all upload events", Command.ITEM, 1);
	static final Command CMD_UNSELECT = new Command("Unselect", "Unselect all events", Command.ITEM, 1);

    Form form;
	GCalClient gCalClient;
	ChoiceGroup[] uploadChoices;
	ChoiceGroup[] downloadChoices;
	GCalEvent[] uploads;
	GCalEvent[] downloads;

	/**
    * Constructor
	*/
	public PreviewComponent(GCalClient gcal)
	{
		gCalClient = gcal;
	}

	/**
    * Gets the form for this component
    *
    * @returns <code>form</code>
	*/
    public Displayable getDisplayable() {return this.form;}

	/**
    * Updates the view after it is created
	*/
	protected void updateView() {}

	/**
    * Initializes the model before creating the view
	*/
    protected void initModel() {}

	/**
    * Creates the view
	*/
    protected void createView() throws Exception
	{
            try {
                this.form = new Form("Preview");

                //add upload choices to form
                if (uploads != null) addEvents(true);

		//add download choices to form
		if (downloads != null) addEvents(false);

		//add commit command to menu only if choices exist
		if ((this.downloads != null && this.downloads.length > 0) 
			|| (this.uploads != null && this.uploads.length > 0))
		{
			this.form.addCommand(CMD_COMMIT);
			this.form.addCommand(CMD_UNSELECT);
		}

		//add commands to form
		if (this.downloads != null && this.downloads.length > 0)
			this.form.addCommand(CMD_SELECT_DL);

		if (this.uploads != null && this.uploads.length > 0)
			this.form.addCommand(CMD_SELECT_UL);

            this.form.addCommand(CMD_CANCEL);
            this.form.setCommandListener(this);
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "createView", e);
        }
    }

	/**
    * Processes menu commands
    *
    * @param c command to execute
    * @param displayable <code>Displayable</code> from which
    *                    <code>command</code> originates
	*/
    public void commandAction(Command c, Displayable displayable) 
	{
            try {
		CommitComponent cmt;
		Alert alert;

		if (c == CMD_SELECT_DL)
		{
			selectAll(this.downloadChoices, true);
		}
		else if (c == CMD_SELECT_UL)
		{
			selectAll(this.uploadChoices, true);
		}
		else if (c == CMD_UNSELECT)
		{
			selectAll(this.uploadChoices, false);
			selectAll(this.downloadChoices, false);
		}
		else if (c == CMD_COMMIT) 
		{
			if (isSelected(this.uploadChoices)
				|| isSelected(this.downloadChoices))
			{
				//remove events that are not selected from the uploads/downloads
				//and pass them to the CommitComponent
				cmt = new CommitComponent(gCalClient);
				cmt.setEvents(getSelectedEvents(this.uploadChoices, this.uploads), 
							  getSelectedEvents(this.downloadChoices, this.downloads));

				//start CommitComponent on new thread to prevent phone lockup
				cmt.handle();
			}
			else
			{
				//show error for 2 seconds
				alert = new Alert("Error", "No events selected", null, AlertType.ERROR);
				alert.setTimeout(1500);
				this.display.setCurrent(alert, this.display.getCurrent());
			}
        }
		else if (c == CMD_CANCEL) 
		{
			Components.login.showScreen();
		}
            }catch(Throwable t) {
                ErrorHandler.showError(t);
            }
    }

	/**
    * Sets the upload and download events to be previewed
    *
    * @param uploads events to be uploaded
    * @param downloads events to be downloaded
	*/
	public void setEvents(GCalEvent[] uploads, GCalEvent[] downloads) throws Exception
	{
            try {
		if (uploads != null)
		{
			this.uploads = new GCalEvent[uploads.length];
			System.arraycopy(uploads, 0, this.uploads, 0, uploads.length);
		}

		if (downloads != null)
		{
			this.downloads = new GCalEvent[downloads.length];
			System.arraycopy(downloads, 0, this.downloads, 0, downloads.length);
		}
            }catch(Exception e) {
                throw new GCalException("PreviewComponent", "setEvents", e);
            }
	}

	/*
    * Adds events to the current form
    *
    * @param upload if true, adds the upload events; otherwise, adds the download events
	*/
	void addEvents(boolean upload) throws Exception
	{
            try {
		ChoiceGroup[] choices;
		GCalEvent[] events;
		StringItem title;
		long timeOffset;

		if (upload)
		{
			events = this.uploads;
			this.uploadChoices = new ChoiceGroup[events.length];
			choices = this.uploadChoices;
			title = new StringItem("Uploads", null);
			timeOffset = Store.getOptions().uploadTimeZoneOffset;
		}
		else
		{
			events = this.downloads;
			this.downloadChoices = new ChoiceGroup[events.length];
			choices = this.downloadChoices;
			title = new StringItem("Downloads", null);
			timeOffset = Store.getOptions().downloadTimeZoneOffset;
		}

		//add group
		title.setFont(Font.getFont(Font.FACE_PROPORTIONAL, 
								   Font.STYLE_BOLD,
								   Font.SIZE_SMALL));
		this.form.append(title);

		if (events.length == 0)
		{
			this.form.append(new StringItem("", "*None*"));
		}
		else
		{
			for (int i = 0; i < events.length; i++)
			{
				choices[i] = new ChoiceGroup(null, ChoiceGroup.MULTIPLE, new String[]{events[i].toString(timeOffset)}, null);
                                choices[i].setFitPolicy(ChoiceGroup.TEXT_WRAP_ON);
                                choices[i].setFont(0, Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL));
                      		this.form.append(choices[i]);
				this.form.append(new Spacer(getDisplayable().getWidth(), 8));
			}
		}
            }catch(Exception e) {
                throw new GCalException(this.getClass(), "addEvents", e);
            }
	}

	/**
    * Gets all events that correspond to the selected elements in
    * the specified <code>ChoiceGroup</code> array
    *
    * @param choices ChoiceGroups to search
    * @param events list of events
	*/
	GCalEvent[] getSelectedEvents(ChoiceGroup[] choices, GCalEvent[] events)
	{
		GCalEvent[] rval = null;

		if (choices != null && choices.length > 0 && events != null)
		{
			Vector eventVect = new Vector(choices.length);
			
			//add selected events to the event vector
			for (int i=0; i<choices.length; i++)
			{
				if (choices[i].isSelected(0)) 
					eventVect.addElement(events[i]);
			}

			//replace <events> with events from the event vector
			if (eventVect.size() > 0)
			{
				rval = new GCalEvent[eventVect.size()];
				eventVect.copyInto(rval);
			}
		}

		return rval;
	}

	void selectAll(ChoiceGroup[] choices, boolean selected)
	{
		for (int i=0; i<choices.length; i++)
			choices[i].setSelectedIndex(0, selected);
	}

	/**
    * Determines if the specified ChoicGroup array contains any
    * elements that are selected
    *
    * @param choices ChoiceGroups to search
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

