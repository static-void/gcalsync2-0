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

import com.gcalsync.log.ErrorHandler;
import javax.microedition.lcdui.*;
import com.gcalsync.store.Store;

/**
 * Public class responsible for drawing the display for the Reset Options screen. 
 * The Reset Options screen provides the ability for a user to delete the 
 * Midlet record store (persistent storage) from the device. This screen is 
 * accessible through the Menu button on the Login screen. Uses the 
 * MVCComponent base class to retrieve, draw, and update screens. 
 *  
 */
public class ResetOptionsComponent extends MVCComponent implements Runnable
{
	static final Command CMD_CANCEL = new Command("Cancel", Command.CANCEL, 4);
	static final Command CMD_OK = new Command("OK", Command.OK, 2);
	
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
	protected void createView()
	{
		form = new Form("Reset options");
		form.append("Delete GCalSync record store?");

		form.addCommand(CMD_OK);
		form.addCommand(CMD_CANCEL);
		form.setCommandListener(this);
	}

	/**
    * Processes menu commands
    *
    * @param c command to execute
    * @param d <code>Displayable</code> from which
    *                    <code>command</code> originates
	*/
	public void commandAction(Command c, Displayable d)
	{
            //start deletion thread
            if (c == CMD_OK) {
                new Thread(this).start();
            }
            try {
		//return to Options screen
		Components.options.showScreen();
            }catch(Throwable t) {
                ErrorHandler.showError(t);
            }
	}

	/**
    * Entry point for new thread
	*/
	public void run()
	{
		Store.deleteRecordStore();
	}
}


