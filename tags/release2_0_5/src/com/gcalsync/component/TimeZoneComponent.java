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
import javax.microedition.lcdui.*;
import com.gcalsync.store.Store;
import com.gcalsync.option.Options;

/**
 * Leave blank for automatic.
 * +4 means for hours ahead
 * -3:30 means 3.5 hours behind
 * <p/>
 * If wrong format: Show "Illegal value!", treat as automatic
 *
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author: thomasold $
 * @version $Rev: 13 $
 * @date $Date: 2006-09-19 16:52:45 -0400 (Tue, 19 Sep 2006) $
 */
public class TimeZoneComponent extends MVCComponent {

    private static final String VALIDATION_MESSAGE = "Illegal adjustment value!";

    // view
    private Form form;
    //private ChoiceGroup useGCalTimeZone;
    private StringItem downloadValidationMessage;
    private TextField downloadAdjustment;
    private StringItem uploadValidationMessage;
    private TextField uploadAdjustment;
    private Command saveCommand;


    public Displayable getDisplayable() {
        return form;
    }

    protected void initModel() {
    }

    protected void createView() throws Exception {
        try {
            form = new Form("Time zone adjustment");

            /*useGCalTimeZone = new ChoiceGroup("", ChoiceGroup.MULTIPLE);
            useGCalTimeZone.append("Use GCal time zone", null);
            useGCalTimeZone.setSelectedIndex(0, true);
            form.append(useGCalTimeZone);*/

            downloadValidationMessage = new StringItem("", "", StringItem.PLAIN);
            //downloadValidationMessage.setFont(Font.getFont(Font.STYLE_BOLD));
            form.append(downloadValidationMessage);

            downloadAdjustment = new TextField("Download adjustment", "+00:00", 6, TextField.NON_PREDICTIVE);
            downloadAdjustment.setInitialInputMode("IS_FULLWIDTH_DIGITS");
            form.append(downloadAdjustment);

            uploadValidationMessage = new StringItem("", "", StringItem.PLAIN);
            //uploadValidationMessage.setFont(Font.getFont(Font.STYLE_BOLD));
            form.append(uploadValidationMessage);

            uploadAdjustment = new TextField("Upload adjustment", "+00:00", 6, TextField.NON_PREDICTIVE);
            uploadAdjustment.setInitialInputMode("IS_FULLWIDTH_DIGITS");
            form.append(uploadAdjustment);

            saveCommand = new Command("Save", Command.OK, 1);
            form.addCommand(saveCommand);

            Command cancelCommand = new Command("Cancel", Command.CANCEL, 2);
            form.addCommand(cancelCommand);

            form.setCommandListener(this);
            updateView();
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "createView", e);
        }
    }

    protected void updateView() throws Exception {
        try {
            Options options = Store.getOptions();
            downloadAdjustment.setString(longOffsetToString(options.downloadTimeZoneOffset));
            uploadAdjustment.setString(longOffsetToString(options.uploadTimeZoneOffset));
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "updateView", e);
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        try {
            boolean showNext = false;
            if (command.getLabel().equals(saveCommand.getLabel())) {
                if (setOptions()) {
                    Store.saveOptions();
                    showNext = true;
                }
            } else if (command.getCommandType() == Command.CANCEL) {
                showNext = true;
            }
            if (showNext) {
                Components.options.showScreen();
            }
        }catch(Throwable t) {
            ErrorHandler.showError("Error [TimeZoneComponent]", t);
        }
    }

    private boolean setOptions() throws Exception {
        try {
            boolean success = true;
            Options options = Store.getOptions();
            //options.useGCalTimeZone = useGCalTimeZone.isSelected(0);
            try {
                options.downloadTimeZoneOffset = getOffset(downloadAdjustment);
                downloadValidationMessage.setText("");
            } catch (Exception e) {
                success = false;
                downloadValidationMessage.setText(VALIDATION_MESSAGE);
            }
            try {
                options.uploadTimeZoneOffset = getOffset(uploadAdjustment);
                uploadValidationMessage.setText("");
            } catch (Exception e) {
                success = false;
                uploadValidationMessage.setText(VALIDATION_MESSAGE);
            }
            return success;
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "setOptions", e);
        }
    }

    private long getOffset(TextField adjustment) {
        long offset = stringOffsetToLong(adjustment.getString());
        // write back formatted to validate format - may throw exception
        adjustment.setString(longOffsetToString(offset));
        return offset;
    }

    private String longOffsetToString(long offset) {
        String sign = "+";
        if (offset < 0) {
            sign = "-";
        }
        int totalMinutes = (int) (Math.abs(offset) / (60*1000));
        long hours = totalMinutes / 60;
        long minutes = totalMinutes - (60*hours);
        return sign + twoDigit(hours) + ":" + twoDigit(minutes);
    }

    private String twoDigit(long value) {
        if (value < 10) {
            return "0" + value;
        } else if (value < 100) {
            return String.valueOf(value);
        } else {
            throw new IllegalArgumentException(value + " should be less than 100");
        }
    }

    private long stringOffsetToLong(String offset) {
        if ((offset == null) || "".equals(offset)) {
            return 0;
        }

        offset = offset.trim();
        int sign = 1;
        if (offset.startsWith("-")) {
            sign = -1;
            offset = offset.substring(1);
        } else if (offset.startsWith("+")) {
            offset = offset.substring(1);
        }
        int colonPos = offset.indexOf(":");
        int hours = 0;
        int minutes = 0;
        if (colonPos >= 0) {
            String hoursField = offset.substring(0, colonPos);
            String minutesField = offset.substring(colonPos + 1);
            hours = Integer.parseInt(hoursField);
            minutes = Integer.parseInt(minutesField);
        } else {
            hours = Integer.parseInt(offset);
        }
        return sign * ((hours*3600*1000) + (minutes*60*1000));
    }

}
