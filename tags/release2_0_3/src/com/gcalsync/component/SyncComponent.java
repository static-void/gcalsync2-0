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
        // TODO: Allow cancel
        if (command.getCommandType() == Command.EXIT) {
            Components.login.showScreen();
        }
    }
    
    public void handle() {
        showScreen();
        new Thread(this).start();
    }
    
    
    public void run() {
        try {
            /*CommitComponent commit;
            GCalEvent[] gCalEvents;
            long now = System.currentTimeMillis();
            Options options = Store.getOptions();
            String startDate = DateUtil.longToIsoDateGMT(now, 0 - options.pastDays);
            String endDate = DateUtil.longToIsoDateGMT(now, options.futureDays);
            long startDateLong = DateUtil.isoDateToLong(startDate);
            long endDateLong = DateUtil.isoDateToLong(endDate) + DateUtil.DAY; // want all events starting before midnigth on endDate
            
            //System.out.println("Syncing from " + startDate + " to " + endDate + " (-" + options.pastDays + " to +" + options.futureDays + ")");
            //update("Reading Google Calendar(s)...");
            gCalClient.setForm(form);
            
            if (feedsToSync == null)
                gCalEvents = gCalClient.downloadEvents(startDateLong, endDateLong);
            else
                gCalEvents = gCalClient.downloadCalendars(startDate, endDate, feedsToSync);
            
            Vector newPhoneEvents = new Vector();
            if (options.upload && gCalClient.isAuthorized()) {
                phoneCalClient = new PhoneCalClient();
                merger = new Merger(phoneCalClient, gCalClient);
                update("Reading phone events...");
                
                try {
                    Enumeration eventEnumeration = phoneCalClient.getPhoneEvents(startDateLong, endDateLong);
                    int i = 0;
                    while (eventEnumeration.hasMoreElements()) {
                        Event phoneEvent = (Event) eventEnumeration.nextElement();
                        String gcalId = phoneCalClient.getGCalId(phoneEvent);
                        
                        //check for new or modified phone events
                        //if (gcalId == null || (gcalId != null && phoneEvent.isModified()))
                        {
                            //missing GCal ID indicates that the event was created on the phone
                            //and not by Google Calendar
                            GCalEvent gCalEvent = merger.copyToGCalEvent(phoneEvent);
                            gCalEvent.uid = gcalId;
                            gCalEvent.phoneCalId = phoneCalClient.getPhoneId(phoneEvent);
                            newPhoneEvents.addElement(gCalEvent);
                        }
                    }
                } catch (Exception e) {
                    ErrorHandler.showError("Failed to open phone calendar", e);
                    return;
                }
            }
            
            //filter the events as not up upload nor download repated events
            GCalEvent[] phoneCalEvents = gCalClient.eventVectorToArray(newPhoneEvents);
            
            GCalEvent[] filteredGCalEvents = filterEvents(gCalEvents, phoneCalEvents);
            GCalEvent[] filteredPhoneCalEvents = filterEvents(phoneCalEvents, gCalEvents);
            */
            
            SyncEngine syncEngine = new SyncEngine();
            GCalEvent[][] eventsForSync = syncEngine.getEventForSync(feedsToSync, gCalClient, form);
            
            if(eventsForSync == null) {
                return;
            }
            
            GCalEvent[] filteredPhoneCalEvents = eventsForSync[0];
            GCalEvent[] filteredGCalEvents = eventsForSync[1];
            
            
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
            
            
            
        } catch (Exception e) {
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
    /*
    private GCalEvent[] filterEvents(GCalEvent[] base, GCalEvent[] others) {
        //the base events are the events to be uploaded or downloaded.
        
        //An event will be uploaded if:
        // -has a null ID
        // -there is no event with the same id on google or if it was modified after the last modification in google
        
        //An event will be downloaded if:
        // --there is no event with the same id in the phone or if it was modified after the last modification in the phone
        
        Vector filteredBase = new Vector();
        
        for (int i = 0; i < base.length; i++) {
            boolean eventAcceptable = true;
            boolean eventFound = false;
            
            GCalEvent be = base[i];
            
            for (int j = 0; j < others.length && !eventFound; j++) {
                GCalEvent oe = others[j];
                
                long beStartDate = be.startTime / (1000 * 60 * 60 * 24);
                long oeStartDate = oe.startTime / (1000 * 60 * 60 * 24);
                
                boolean eventEquals =
                        equalsString(be.title, oe.title) &&
                        equalsString(be.note, oe.note) &&
                        equalsString(be.location, oe.location) &&
                        
                        (
                        ( ( be.startTime == oe.startTime) && ( be.endTime == oe.endTime) ) ||
                        ( DateUtil.isAllDay(be.startTime, be.endTime) && DateUtil.isAllDay(oe.startTime, oe.endTime) && beStartDate == oeStartDate)
                        ) &&
                        
                        (   (be.recur == null && oe.recur == null) ||
                        (be.recur != null && oe.recur != null && be.recur.equals(oe.recur) )
                        );
                
                if( (be.uid != null && oe.uid != null && be.uid.equals(oe.uid) ) ||  eventEquals) {
                    //if(be.uid == null || oe.uid == null || be.uid.equals(oe.uid)) {
                    //events with the same ID found!
                    eventFound = true;
                    
                    if(eventEquals && ( be.updated == 0 || oe.updated == 0 || be.updated <= oe.updated) ) {
                        eventAcceptable = false;
                    }
                    
                    //mark that this event in fact is for update
                    be.eventIsToBeUpdated = true;
                    
                    //pass the editLink from the google's event to the phone's event
                    if(be.editLink == null || be.editLink.length() == 0) {
                        be.editLink = oe.editLink;
                    }
                    
                    //mark if this event should be autoupdated, an event is not autoupdated if it cannot be verified which
                    //event was last updated - in this case the user will have to update manually
                    if(be.updated == 0 || oe.updated == 0) {
                        be.autoSyncCantidate = false;
                    }
                }
            }
            if(eventAcceptable) {
                filteredBase.addElement(base[i]);
            }
        }
        
        return GCalClient.eventVectorToArray(filteredBase);
    }
    */
    /**
     * Compares two string
     * @param s1 
     * @param s2 
     * @return If both string string are null or empty it will return true,
     * else it will do the standard String.equals
     */
   /* private boolean equalsString(String s1, String s2) {
        if( (s1 == null || s1.length() == 0) && (s2 == null || s2.length() == 0) ) {
            return true;
        }
        return s1 != null && s2 != null && s1.equals(s2);
    }*/
}

