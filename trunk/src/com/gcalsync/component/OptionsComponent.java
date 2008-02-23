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
import com.gcalsync.log.GCalException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 * Public class responsible for drawing the display and menu choices for the Options
 * screen. Uses the MVCComponent base class to retrieve, draw, and update screens.  
 * Passes control to handlers when a user selects a choice from the Options menu.
 *  
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 24 $
 * @date $Date$
 */
public class OptionsComponent extends MVCComponent {

    List menu;
	MVCComponent nextDisplay;

    /**
     * Displayable is an object that can be placed on the display.<br>
     * Returns the screen object from the derived class.<br>
     *
     * @returns <code>menu</code>
     */ 
    public Displayable getDisplayable() {
        return menu;
    }
    
    /**
     * Initializes the model before creating the view
     *
     */
    protected void initModel() {
    }
    
    /**
     * Creates a view from the current model into the screen object.<br> 
     * Creates the on-screen menu choices for the Options screen.
     * 
     */
    protected void createView() {
        //menu displays on-screen (not a popup)
        menu = new List("Options menu", List.IMPLICIT);
        menu.append("Sync period", null);
        menu.append("Time zone", null);
        menu.append("Upload/download", null);
        menu.append("Autosync period", null);
	menu.append("Reset options", null);
        //the cancel command attached to a device button
        Command cancelCommand = new Command("Cancel", Command.CANCEL, 2);
        menu.addCommand(cancelCommand);
        menu.setCommandListener(this);
    }
    
    /**
     * Updates the screen object when the model changes 
     *
     */
    protected void updateView() {
    }
        /**
         * Gets the next displayable screen and draws it on the display. 
         *
         * @param nextDisplayable uses MVCComponent methods to draw next displayable
         *                   screen based on user interaction with the interface
         *                   through implementation of <code>commandAction</code>
         * @throws GCalException that returns ErrorHandler.showError
         *                               ("Error showing screen", e);
         */
	public void showScreen(MVCComponent nextDisplayable) throws Exception
	{
            try {
		nextDisplay = nextDisplayable;
		showScreen();
            }catch(Exception e) {
                throw new GCalException(this.getClass(), "showScreen", e);
            }
	}

    public void commandAction(Command command, Displayable displayable) {
        try {
            //if user selects the "Cancel" button
            if (command.getCommandType() == Command.CANCEL) {
                            //and focus is on the Options screen
                            if (nextDisplay == null)
                                    //display the login screen
                                    Components.login.showScreen(false);
                            //if focus is a screen selected from the options menu
                            else
                                    //go back and show the Options screen 
                                    nextDisplay.showScreen(false);
              //if user selects a choice from the Options screen menu, 
              //pass control to selected component
            } else if (command == List.SELECT_COMMAND) {
                int choice = menu.getSelectedIndex();
                switch (choice) {
                    case 0:
                        Components.period.handle();
                        break;
                    case 1:
                        Components.timeZone.handle();
                        break;
                    case 2:
                        Components.uploadDownload.handle();
                        break;
                    case 3:
                        Components.autosyncPeriodComponent.handle();
                        break;
                    case 4:
                        Components.resetOptions.handle();
                        break;
                }
            }
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
    }

}
