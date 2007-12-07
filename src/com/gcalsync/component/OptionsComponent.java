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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 24 $
 * @date $Date$
 */
public class OptionsComponent extends MVCComponent {

    List menu;
	MVCComponent nextDisplay;


    public Displayable getDisplayable() {
        return menu;
    }

    protected void initModel() {
    }

    protected void createView() {
        menu = new List("Options menu", List.IMPLICIT);
        menu.append("Sync period", null);
        menu.append("Time zone", null);
        menu.append("Upload/download", null);
        menu.append("Autosync period", null);
	menu.append("Reset options", null);
        Command cancelCommand = new Command("Cancel", Command.CANCEL, 2);
        menu.addCommand(cancelCommand);
        menu.setCommandListener(this);
    }

    protected void updateView() {
    }

	public void showScreen(MVCComponent nextDisplayable)
	{
		nextDisplay = nextDisplayable;
		showScreen();
	}

    public void commandAction(Command command, Displayable displayable) {
        if (command.getCommandType() == Command.CANCEL) {

			if (nextDisplay == null) 
				Components.login.showScreen(false);
			else
				nextDisplay.showScreen(false);

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
    }

}
