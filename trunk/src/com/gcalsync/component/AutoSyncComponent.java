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

import com.gcalsync.cal.CommitEngine;
import com.gcalsync.cal.SyncEngine;
import com.gcalsync.cal.gcal.GCalClient;
import com.gcalsync.cal.gcal.GCalEvent;
import com.gcalsync.cal.gcal.GCalFeed;
import com.gcalsync.log.ErrorHandler;
import com.gcalsync.log.GCalException;
import com.gcalsync.store.Store;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

/**
 * Component that implements the autosync functionality
 * @author Agustin
 * @author Yusuf Abdi
 * @version $Rev: 1 $
 * @date $Date: 2007-12-30 03:22:30 -0500 (Sat, 30 Dec 2007) $
 */
public class AutoSyncComponent extends MVCComponent {
    
    private final Command CMD_STOP = new Command("Stop", Command.STOP, 2);
    private final Command CMD_HIDE = new Command("Hide", "Hide", Command.ITEM, 1);
    
    private Form form;
    private GCalClient gCalClient;
    private GCalFeed[] feeds;
    
    private Timer timer = null;
    private int secondsToNextSync = 5; //how many seconds to the next autosync
    private String lastError = null; //contains any possible error from the previouse autosync
    
    private Font labelFont = null;
    private StringItem lblTimeInfo = null; //the item with the timer
    private boolean showingTimer = false; //tells if the timer is being shown on the screen
    private boolean running = true; //tells if the autosync should be running
    
    //here the statistics are stored
    int[] uploadStatistics = new int[] {0, 0, 0};
    int[] downloadStatistics = new int[] {0, 0, 0};
    int conflictStatistics = 0;
    
    public AutoSyncComponent(GCalClient gCalClient, GCalFeed[] feeds) {
        this.feeds = feeds;
        this.gCalClient = gCalClient;
        this.gCalClient.setForceFullSync(true);
        
        labelFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
        lblTimeInfo = new StringItem("Sync in: ", null);
        lblTimeInfo.setFont(labelFont);
    }
    
    public Displayable getDisplayable() {return this.form;}
    
    protected void updateView() {}
    
    protected void initModel() {}
    
    protected void createView() {
        this.form = new Form("Auto Synchronization");
        this.form.addCommand(CMD_STOP);
        this.form.addCommand(CMD_HIDE);
        this.form.setCommandListener(this);
    }
    
    public void handle() throws Exception {
        try {
            showScreen();
            setupTimer();
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "handle", e);
        }
    }
    
    public void commandAction(Command c, Displayable displayable) {
        try {
            if (c.getCommandType() == Command.STOP) {
                running = false;
                if(timer != null) {
                    timer.cancel();
                }
                Components.login.showScreen();
            } else if(c == CMD_HIDE) {
                this.display.setCurrent(null);
            }
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
    }
    
    /**
     * Starts the timer and updates the screen
     */
    private void setupTimer() {
        if(!running) {
            return;
        }
        
        updateScreenTimer();
        
        if(timer != null) {
            timer.cancel();
        }
        
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                timerTaskRun();
            }
        }, 1 * 1000, 1 * 1000);
    }
    
    /**
     * Executes the auto sync
     * @throws Exception If there is any error
     */
    private void doAutoSync() throws Exception {
        //clean the screen
        this.form.deleteAll();
        showingTimer = false;
        
        //show the synchronizing message
        StringItem lblSomeText = new StringItem("Synchronizing...", null);
        lblSomeText.setFont(labelFont);
        this.form.append(lblSomeText);
        
        //login
        String loginResult = this.gCalClient.login();
        if(loginResult != null) {
            this.lastError = loginResult;
            return;
        }
        
        //get the events to sync
        SyncEngine syncEngine = new SyncEngine();
        GCalEvent[][] eventForSync = new GCalEvent[2][];
        this.conflictStatistics = syncEngine.getEventForAutoSync(this.form, this.gCalClient, this.feeds, eventForSync);
        
        //sync the events
        CommitEngine commitEngine = new CommitEngine();

        int[] commitStatistics = commitEngine.commitSync(eventForSync[0], eventForSync[1], this.gCalClient, this.form);
        
        //update the statistics
        this.uploadStatistics[0] += this.gCalClient.createdCount;
        this.uploadStatistics[1] += this.gCalClient.updatedCount;
        this.uploadStatistics[2] += this.gCalClient.removedCount;
        
        this.downloadStatistics[0] += commitStatistics[0];
        this.downloadStatistics[1] += commitStatistics[1];
        this.downloadStatistics[2] += commitStatistics[2];
        
        this.gCalClient.createdCount = 0;
        this.gCalClient.updatedCount = 0;
        this.gCalClient.removedCount = 0;
    }
    
    /**
     * Executes every one second to update the screen and start the autosync when necessary
     */
    private void timerTaskRun() {
        try {
            if(!running) {
                //if autosync should not be running then stop
                timer.cancel();
            }

            //update the timer
            secondsToNextSync--;
            updateScreenTimer();

            if(secondsToNextSync <= 0) {
                //if it is time to sync

                //stop the timer
                timer.cancel();
                timer = null;

                //reset the last error
                lastError = null;

                //sync
                try {
                    doAutoSync();
                }catch(Exception e) {
                    lastError = e.getMessage();
                }

                //restart the timer
                secondsToNextSync = Store.getOptions().autosyncTime * 60;
                setupTimer();
            }
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
    }
    
    /**
     * Updates the screen timer
     */
    private void updateScreenTimer() {
        
        if(!showingTimer) {
            //if the timer is not being shown, then show it
            
            //clean the screen and add the timer item
            this.form.deleteAll();
            this.form.append(lblTimeInfo);
            
            //add the statistics
            String gcalInfo = uploadStatistics[0] + " new, " + uploadStatistics[1] + " updated, " + uploadStatistics[2] + " removed events";
            String phoneInfo = downloadStatistics[0] + " new, " + downloadStatistics[1] + " updated, " + downloadStatistics[2] + " removed events";
            String conflictInfo = Integer.toString(conflictStatistics) + (conflictStatistics > 0 ? " - sync manually" : "");
            
            addInfo("GCal: ", gcalInfo);
            addInfo("Phone: ", phoneInfo);
            addInfo("Conflicts: ", conflictInfo);
            
            //if there has been an error then show it
            if(lastError != null) {
                addInfo("Error: ", lastError);
            }
            
            showingTimer = true;
        }
        
        //update the timer with the new time to sync
        int minutes = secondsToNextSync / 60;
        int seconds = secondsToNextSync % 60;
        
        lblTimeInfo.setText(
                com.gcalsync.util.DateUtil.twoDigit(minutes) + ":" +
                com.gcalsync.util.DateUtil.twoDigit(seconds));
    }
    
    /**
     * Adds information to the screen
     * @param title The title of the information
     * @param info The information text
     */
    private void addInfo(String title, String info) {
        StringItem lblInfo = new StringItem(title , null);
        lblInfo.setText(info);
        lblInfo.setFont(labelFont);
        this.form.append(lblInfo);
    }
}
