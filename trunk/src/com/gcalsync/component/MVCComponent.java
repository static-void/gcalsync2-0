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
import com.gcalsync.log.*;

/**
 * Public abstract class responsible for creation of a new display for the device. 
 * Each screen in the application is an independent functional unit 
 * with its own event handlers, transparent data models, and UI view generator. 
 * Each screen (component) is represented by a class derived from the 
 * MVCComponent abstract class.
 * <p>
 * - Gets screen object from derived class.<br>
 * - Initializes the model before creating the view.<br>
 * - Creates a view from the current model into the screen object.<br> 
 * - Prepares the screen to display on the device.<br>
 * - Methods check if a previously rendered view needs updated or if 
 *                          a new view needs to be created.<br> 
 * - Sets a listener for Commands on the Displayable screen object.<br>
 * - Set the current screen to the prepared view.<br>
 * - Updates the screen object when the model changes. 
 * <p>
 * Based on com.enterprisej2me.iFeedBack.midp.MVC.MVCComponent written by Michael J. Yuan,
 * from the book "Enterprise J2ME: Developing Mobile Java Applications"
 * (http://www.thinkfreedocs.com/tools/download.php?mode=down&dsn=766975)<br>
 * Save the above mentioned .pdf file to the hard drive to view contents
 * - may not be viewable in all browsers.
 *
 */
public abstract class MVCComponent implements CommandListener {

    public static Display display;

    /**
     * Displayable is an object that can be placed on the display.<br>
     * Returns the screen object from the derived class.
     * 
     */
    public abstract Displayable getDisplayable();
    
    /**
     * Prepares the screen to display on the device
     *
     * @throws GCalException that returns ErrorHandler.getErrorAlert
     *                    ("Error preparing screen", e)
     */
    public Displayable prepareScreen() throws Exception {
        try {
            //if screen object is empty, initialize model and create a new form
            if (getDisplayable() == null) {
                initModel();
                createView();
            //if not empty, update the existing view 
            } else {
                updateView();
            }
            //sets a listener for Commands to the Displayable screen object
            getDisplayable().setCommandListener((CommandListener) this);
            return getDisplayable();
        } catch (Exception e) {
            throw new GCalException("MVCComponent", "prepareScreen", e);
           // return ErrorHandler.getErrorAlert("Error preparing screen", e);
        }
    }
    
    /**
     * Set the current screen to the prepared view
     * 
     * @throws GCalException that returns ErrorHandler.showError
     *               ("Error showing screen", e);   
     */
    public void showScreen() throws Exception{
        try {
            display.setCurrent(prepareScreen());
        } catch (Exception e) {
            throw new GCalException("MVCComponent", "setEvents", e);
            //ErrorHandler.showError("Error showing screen", e);
        }
    }
    
         /**
          * Determines if an existing view needs to be updated or 
          * if a new view needs to be created
          * 
          */
	public void showScreen(boolean update)
	{
		try
		{
			Displayable d = getDisplayable();

			//show last rendered view if update is not required
			if (!update && d != null)
			{
				display.setCurrent(d);
			}
			else
			{
                                //otherwise, prepare and create new view
				showScreen();
			}
		}
		catch (Exception e)
		{
			ErrorHandler.showError("Error showing screen", e);
		}
	}

    public void handle() throws Exception {
        showScreen();
    }

    // Initialize. If a data member is not backed by RMS, make sure
    // it is uninitilzed (null) before you put in values.
    protected abstract void initModel() throws Exception;
    
    /**
     * Creates a view from the current model into the screen object 
     *
     */
    protected abstract void createView() throws Exception;

    /**
     * Updates the screen object when the model changes 
     *
     */
    protected abstract void updateView() throws Exception;
    
    /**
     * Indicates that a command event has occurred on Displayable s
     *
     * @param c command to execute
     * @param s <code>Displayable</code> on which this event has occurred
     */
    public abstract void commandAction(Command c, Displayable s);

}
