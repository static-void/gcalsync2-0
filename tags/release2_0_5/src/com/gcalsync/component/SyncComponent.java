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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.pim.Event;
import javax.microedition.pim.PIMException;

import com.gcalsync.log.*;
import com.gcalsync.cal.gcal.*;
import com.gcalsync.cal.phonecal.PhoneCalClient;
import com.gcalsync.store.Store;
import com.gcalsync.util.*;
import com.gcalsync.cal.*;
import com.gcalsync.option.Options;

/**
 *
 * @author $Author$
 * @version $Rev: 19 $
 * @date $Date$
 *
 * * Changes:
 *  --/nov/2007 Agustin
 *      -filterEvents: added; filters which events should be synchorinzed
 *      -equalsString: added; checks if two string equals (a null string equals a string of lenght 0)
 *      -run: now it uses filterEvents to tell which events should be syncrhonized
 *  --/dec/2007 Agustin
 *      -run: now used SyncEngine
 *      -filterEvents: moved to SyncEngine
 *      -equalsString: moved to SyncEngine
 */
public class SyncComponent extends MVCComponent implements Runnable, StatusLogger {
    private GCalClient gCalClient;
    private PhoneCalClient phoneCalClient;
    private Merger merger;
    private Hashtable phoneEventsByGcalId;
    private Form form;
    GCalFeed[] feedsToSync = null;
    
    public SyncComponent() {
        gCalClient = new GCalClient();
    }
    
    public SyncComponent(GCalClient gcal) {
        gCalClient = gcal;
    }
    
    public SyncComponent(GCalFeed[] feeds) {
        this();
        feedsToSync = feeds;
    }
    
    public SyncComponent(GCalClient gcal, GCalFeed[] feeds) {
        gCalClient = gcal;
        feedsToSync = feeds;
    }
    
    public Displayable getDisplayable() {
        return form;
    }
    
    protected void initModel() throws Exception {
    }
    
    protected void createView() throws Exception {
        form = new Form("Download");
    }
    
    protected void updateView() throws Exception {
    }
    
    public void commandAction(Command command, Displayable screen) {
        try {
            // TODO: Allow cancel
            if (command.getCommandType() == Command.EXIT) {
                Components.login.showScreen();
            }
        }catch(Throwable t) {
            ErrorHandler.showError(t);
        }
    }
    
    public void handle() throws Exception {
        try {
            showScreen();
            new Thread(this).start();
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "handle", e);
        }
    }
    
    
    public void run() {
        try {
            //get the event to sync suing SyncEngine
            SyncEngine syncEngine = new SyncEngine();
            GCalEvent[][] eventsForSync = syncEngine.getEventForSync(feedsToSync, gCalClient, form);
            
            //if there was an error
            if(eventsForSync == null) {
                return;
            }
            
            //let's put down the result from SyncEngine a bit more clear
            GCalEvent[] filteredPhoneCalEvents = eventsForSync[0];
            GCalEvent[] filteredGCalEvents = eventsForSync[1];
            
            //now move to the next screen..
            Options options = Store.getOptions();
            
            if (options.preview) {
                PreviewComponent prvw = new PreviewComponent(gCalClient);
                prvw.setEvents(filteredPhoneCalEvents, filteredGCalEvents);
                prvw.showScreen();
            } else {
                CommitComponent commit = new CommitComponent(gCalClient);
                commit.setEvents(filteredPhoneCalEvents, filteredGCalEvents);
                commit.handle();
            }

        } catch (Throwable e) {
            ErrorHandler.showError("Sync failed", e);
        }
    }
    
    public void update(String message) {
        form.append(message + "\n");
    }
    
    public void updateMinor(String message) {
        form.delete(form.size() - 1);
        form.append(message + "\n");
    }
}

