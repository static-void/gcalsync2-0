/*
   Copyright 2007 Agustin Rivero

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
/*
 *  *
 * @author Agustin Rivero
 * @version $Rev: 1 $
 * @date $Date: 2007-12-07 $
 */

package com.gcalsync.component;

import com.gcalsync.log.ErrorHandler;
import com.gcalsync.log.GCalException;
import com.gcalsync.store.Store;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 * Public class to configure the time between auto syncs
 * This class uses the MVCComponent base class to retrieve, 
 * draw, and update screens. 
 *
 * This component is accessed in the device through the following steps:
 * <ol>
 * <li>User logs into application</li>
 * <li>On personal calendars screen, user clicks Menu button</li>
 * <li>User selects "Options" from menu</li>
 * <li>User selects "AutoSync Period" from menu</li>
 * </ol> 
 * 
 * @author Agustin
 * @author Yusuf Abdi
 * @version $Rev: 1 $
 * @date $Date: 2007-12-30 03:22:30 -0500 (Sat, 30 Dec 2007) $
 */
public class AutosyncPeriodComponent extends MVCComponent {
    
    // view
    private Form form;
    private TextField periodField;
    private Command saveCommand;
    
    //gets the form for this component
    public Displayable getDisplayable() {
        return form;
    }
    
    //initializes the model before creating the view
    protected void initModel() {
        // done in constructor
    }
    
    //Creates the view
    protected void createView() throws Exception {
        try {
            form = new Form("AutoSync period");
            //textbox for user to enter time between auto syncs
            periodField = new TextField("Period (mins)", Integer.toString(Store.getOptions().autosyncTime), 4, TextField.NUMERIC);
            form.append(periodField);

            saveCommand = new Command("Save", Command.OK, 1);
            Command cancelCommand = new Command("Cancel", Command.CANCEL, 2);

            form.addCommand(saveCommand);
            form.addCommand(cancelCommand);

            form.setCommandListener(this);
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "createView", e);
        }
    }
    //displays the sync period in the form
    protected void updateView() throws Exception {
        try {
            periodField.setString(Integer.toString(Store.getOptions().autosyncTime));
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "updateView", e);
        }
    }
    
    //processes button commands
    public void commandAction(Command command, Displayable displayable) {
        try {
            //if user presses "Save" button device
            if (command.getLabel().equals(saveCommand.getLabel())) {
                //if boolean <setOptions> returned true,
                if (setOptions()) {
                    //save the sync period to the device using 
                    //the Store.java <saveOptions> method
                    Store.saveOptions();
                    //display the Options screen
                    Components.options.showScreen();
                }
              //or, if the user presses the "Cancel" button on the device  
            } else if (command.getCommandType() == Command.CANCEL) {
                //display the Options screen
                Components.options.showScreen();
            }
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
    }
    //verifies the user entered a number in the period field
    //returns true if the user entered a number, returns false if not a number
    private boolean setOptions() throws Exception {
        try {
            Store.getOptions().autosyncTime = Integer.parseInt(periodField.getString());
        } catch (NumberFormatException e) {
            ErrorHandler.showError("The period must be a number", e);
            return false;
        } catch(Exception e) {
            throw new GCalException(this.getClass(), "setOptions", e);
        }
        return true;
    }
}
