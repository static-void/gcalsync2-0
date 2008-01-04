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
package com.gcalsync;

import java.util.TimeZone;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.gcalsync.component.*;
import com.gcalsync.log.*;


public class GCalSync extends MIDlet {

    private Display display;
    private static boolean started = false;

    protected void startApp() throws MIDletStateChangeException {
        //check if the program has already started, this is needed when the program hides
        //and re appears - if the program is re apearing then do not show the login screen,
        //there is already an screen being shown
        if(started) return;
        started = true;
        
        //do the startup and show the login screen
        display = Display.getDisplay(this);
        if (checkRequirements()) {
            setDependencies();
            Components.login.handle();
        }
    }

    private boolean checkRequirements() {
        String pimVersion = System.getProperty("microedition.pim.version");
        if (pimVersion == null) {
            Form messageForm = new Form("Not supported");
            messageForm.append("Sorry, your phone is not supported");
            Command exitCommand = new Command("Exit", Command.EXIT, 2);
            messageForm.addCommand(exitCommand);
            messageForm.setCommandListener(new CommandListener() {
                public void commandAction(Command command, Displayable displayable) {
                    notifyDestroyed();
                }
            });
            display.setCurrent(messageForm);
            return false;
        }
        return true;
    }

    private void setDependencies() {
//#ifdef DEBUG_LOG
//#         ErrorHandler.log = new StringBuffer();
//#endif
        ErrorHandler.display = display;
        MVCComponent.display = display;
		Components.login.setMidlet(this);
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean b) {
    }

}

