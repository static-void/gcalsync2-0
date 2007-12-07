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
 * Based on com.enterprisej2me.iFeedBack.midp.MVC.MVCComponent written by Michael J. Yuan,
 * from the book "Enterprise J2ME: Developing Mobile Java Applications"
 * (http://enterprisej2me.com/pages/enterprisej2me/book.php)
 */
public abstract class MVCComponent implements CommandListener {

    public static Display display;

    // Returns the screen object from the derived class
    public abstract Displayable getDisplayable();

    public Displayable prepareScreen() {
        try {
            if (getDisplayable() == null) {
                initModel();
                createView();
            } else {
                updateView();
            }
            getDisplayable().setCommandListener((CommandListener) this);
            return getDisplayable();
        } catch (Exception e) {
            return ErrorHandler.getErrorAlert("Error preparing screen", e);
        }
    }

    public void showScreen() {
        try {
            display.setCurrent(prepareScreen());
        } catch (Exception e) {
            ErrorHandler.showError("Error showing screen", e);
        }
    }

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
				showScreen();
			}
		}
		catch (Exception e)
		{
			ErrorHandler.showError("Error showing screen", e);
		}
	}

    public void handle() {
        showScreen();
    }

    // Initialize. If a data member is not backed by RMS, make sure
    // it is uninitilzed (null) before you put in values.
    protected abstract void initModel() throws Exception;

    protected abstract void createView() throws Exception;

    protected abstract void updateView() throws Exception;

    public abstract void commandAction(Command c, Displayable s);

}
