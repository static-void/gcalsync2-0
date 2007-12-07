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

import com.gcalsync.option.Options;
import com.gcalsync.store.Store;

import javax.microedition.lcdui.*;

/**
 * Set options whether to upload events, download events, or both.
 *
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author: thomasold $
 * @version $Rev: 12 $
 * @date $Date: 2006-09-16 19:52:29 -0400 (Sat, 16 Sep 2006) $
 */
public class UploadDownloadComponent extends MVCComponent {

    private Form form;
    private ChoiceGroup choices;
    private Command saveCommand;

    public Displayable getDisplayable() {
        return form;
    }

    protected void initModel() throws Exception {
    }

    protected void createView() throws Exception {
        form = new Form("Upload/download events");
        choices = new ChoiceGroup("", ChoiceGroup.MULTIPLE);
        choices.append("Upload", null);
        choices.append("Download", null);
		choices.append("Enable previews", null);
		choices.append("Auto-download all calendars", null);
        form.append(choices);

        saveCommand = new Command("Save", Command.OK, 1);
        form.addCommand(saveCommand);

        Command cancelCommand = new Command("Cancel", Command.CANCEL, 2);
        form.addCommand(cancelCommand);

        form.setCommandListener(this);
        updateView();
    }

    protected void updateView() throws Exception {
        Options options = Store.getOptions();
        choices.setSelectedIndex(0, options.upload);
        choices.setSelectedIndex(1, options.download);
		choices.setSelectedIndex(2, options.preview);
		choices.setSelectedIndex(3, options.downloadAllCalendars);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command.getLabel().equals(saveCommand.getLabel())) {
            Options options = Store.getOptions();
            options.upload = choices.isSelected(0);
            options.download = choices.isSelected(1);
			options.preview = choices.isSelected(2);
			options.downloadAllCalendars = choices.isSelected(3);
            Store.saveOptions();
        }
        Components.options.showScreen();
    }
}
