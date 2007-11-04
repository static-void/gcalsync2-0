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
import com.gcalsync.store.Store;
import com.gcalsync.log.*;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 1 $
 * @date $Date$
 */
public class PeriodComponent extends MVCComponent {

    // view
    private Form form;
    private TextField pastDaysField;
    private TextField futureDaysField;
    private Command saveCommand;

    public Displayable getDisplayable() {
        return form;
    }

    protected void initModel() {
        // done in constructor
    }

    protected void createView() {
        form = new Form("Sync period");
        pastDaysField = new TextField("Past days", Integer.toString(Store.getOptions().pastDays), 4, TextField.NUMERIC);
        futureDaysField = new TextField("Future days", Integer.toString(Store.getOptions().futureDays), 4, TextField.NUMERIC);
        form.append(pastDaysField);
        form.append(futureDaysField);
        saveCommand = new Command("Save", Command.OK, 1);
        Command cancelCommand = new Command("Cancel", Command.CANCEL, 2);
        form.addCommand(saveCommand);
        form.addCommand(cancelCommand);
        form.setCommandListener(this);
    }

    protected void updateView() {
        pastDaysField.setString(Integer.toString(Store.getOptions().pastDays));
        futureDaysField.setString(Integer.toString(Store.getOptions().futureDays));
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command.getLabel().equals(saveCommand.getLabel())) {
            if (setOptions()) {
                Store.saveOptions();
                Components.options.showScreen();
            }
        } else if (command.getCommandType() == Command.CANCEL) {
            Components.options.showScreen();
        }
    }

    private boolean setOptions() {
        try {
            Store.getOptions().pastDays = Integer.parseInt(pastDaysField.getString());
        } catch (NumberFormatException e) {
            ErrorHandler.showError("Past days must be a number", e);
            return false;
        }
        try {
            Store.getOptions().futureDays = Integer.parseInt(futureDaysField.getString());
        } catch (NumberFormatException e) {
            ErrorHandler.showError("future days must be a number", e);
            return false;
        }
        return true;
    }

}
