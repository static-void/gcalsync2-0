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
 * *
 * @author Agustin Rivero
 * @version $Rev: 1 $
 * @date $Date: 2007-12-07 $
 */

package com.gcalsync.component;

import com.gcalsync.log.ErrorHandler;
import com.gcalsync.store.Store;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 * Component to configure the time between auto syncs
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
    
    public Displayable getDisplayable() {
        return form;
    }
    
    protected void initModel() {
        // done in constructor
    }
    
    protected void createView() {
        form = new Form("AutoSync period");
        
        periodField = new TextField("Period (mins)", Integer.toString(Store.getOptions().autosyncTime), 4, TextField.NUMERIC);
        form.append(periodField);
        
        saveCommand = new Command("Save", Command.OK, 1);
        Command cancelCommand = new Command("Cancel", Command.CANCEL, 2);
        
        form.addCommand(saveCommand);
        form.addCommand(cancelCommand);
        
        form.setCommandListener(this);
    }
    
    protected void updateView() {
        periodField.setString(Integer.toString(Store.getOptions().autosyncTime));
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
            Store.getOptions().autosyncTime = Integer.parseInt(periodField.getString());
        } catch (NumberFormatException e) {
            ErrorHandler.showError("The period must be a number", e);
            return false;
        }
        return true;
    }
}
