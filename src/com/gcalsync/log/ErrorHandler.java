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
package com.gcalsync.log;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 *
 *
 * Modifications:
 *      dec/2007: Agustin Rivero
 *          showError: changed to lock the thread until the user dismisses the alert
 */
public class ErrorHandler {
    
    public static boolean debugMode = true;
    public static Display display;
//#ifdef DEBUG_LOG
//#     public static StringBuffer log;
//#endif
    
    
    /**
     * Shows an error on the screen, and locks the thread until
     * the user dismisses the alert
     * @param t The exception/throwable to display
     */
    public static void showError(Throwable t) {
        showError("Error", t);
    }
    
    
    /**
     * Shows an error on the screen, and locks the thread until
     * the user dismisses the alert
     * @param message Text to display
     * @param t The exception/throwable to display
     */
    public static void showError(String message, Throwable t) {
        //create the alert
        final Alert alert = getErrorAlert(message, t);
        
        //now add a command listener that will unlock the thread
        //theat I will lock right after showing the alert
        alert.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                synchronized(alert) {
                    alert.notify();
                }
            }
        });
        
        //show the alert and lock the thread
        try {
            synchronized(alert) {
                display.setCurrent(alert);
                alert.wait();
            }
        }catch(Exception e) {
            
        }
    }
    
    public static Alert getErrorAlert(String message, Throwable t) {
//#ifdef DEBUG_ERR
//#         if (t != null) {
//#             t.printStackTrace();
//#         }
//#endif
//#ifdef DEBUG_LOG
//#         log.append(t + "\n");
//#endif
        
        
        if (debugMode) {
            t.printStackTrace();
        }
        
        message += ": " + t.toString() + "\n";
        
        final Alert alert = new Alert("Error", message, null, AlertType.ERROR);
        alert.setTimeout(Alert.FOREVER);
        alert.setType(AlertType.ERROR);

        return alert;
    }
}
