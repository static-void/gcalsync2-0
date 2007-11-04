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
import javax.microedition.lcdui.Display;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 */
public class ErrorHandler {

    public static boolean debugMode = true;
    public static Display display;
//#ifdef DEBUG_LOG
    public static StringBuffer log;
//#endif

    public static void showError(String message, Throwable t) {
        display.setCurrent(getErrorAlert(message, t));
    }

    public static Alert getErrorAlert(String message, Throwable t) {
//#ifdef DEBUG_ERR
        if (t != null) {
            t.printStackTrace();
        }
//#endif
//#ifdef DEBUG_LOG
        log.append(t + "\n");
//#endif

        if (debugMode) {
            message += ": " + t + "\n";
        }

        Alert alert = new Alert("Error", message, null, AlertType.ERROR);
        alert.setTimeout(Alert.FOREVER);
        return alert;
    }
}
