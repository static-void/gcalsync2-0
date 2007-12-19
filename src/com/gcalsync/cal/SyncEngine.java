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
package com.gcalsync.cal;

import java.util.Enumeration;
import java.util.Vector;

import com.gcalsync.cal.gcal.GCalClient;
import com.gcalsync.cal.gcal.GCalEvent;
import com.gcalsync.cal.gcal.GCalFeed;
import com.gcalsync.cal.phonecal.PhoneCalClient;
import com.gcalsync.component.CommitComponent;
import com.gcalsync.log.ErrorHandler;
import com.gcalsync.log.GCalException;
import com.gcalsync.option.Options;
import com.gcalsync.store.Store;
import com.gcalsync.util.DateUtil;
import javax.microedition.lcdui.Form;
import javax.microedition.pim.Event;

/**
 * Class to obtain which events to sync.  Contains code that was previously on SyncComponent.
 * @author Agustin
 * @author Yusuf Abdi
 * @version $Rev: 1 $
 * @date $Date: 2007-12-30 03:22:30 -0500 (Sat, 30 Dec 2007) $
 */
public class SyncEngine {
    /** Creates a new instance of SyncEngine */
    public SyncEngine() {
    }
    
    public int getEventForAutoSync(Form form, GCalClient gCalClient, GCalFeed[] feedsToSync, GCalEvent[][] result) throws Exception {
        try {
            GCalEvent[][] getEventForSync = getEventForSync(feedsToSync, gCalClient, form);

            result[0] = removeNonAutoSync(getEventForSync[0]);
            result[1] = removeNonAutoSync(getEventForSync[1]);

            int conflicts = 0;
            for(int i = 0; i < getEventForSync[0].length; i++) {
                if(!getEventForSync[0][i].autoSyncCantidate) {
                    conflicts++;
                }
            }

            return conflicts;
        }catch(Exception e) {
            throw new GCalException(SyncEngine.class, "getEventForAutoSync", e);
        }
    }
    
    /**
     * Removes all the non auto sync candidates from the list of events
     * @param events
     * @return List of events with autoSyncCantidate=true
     */
    private GCalEvent[] removeNonAutoSync(GCalEvent[] events) throws Exception {
        try {
            Vector vector = new Vector();
            for(int i = 0; i < events.length; i++) {
                if(events[i].autoSyncCantidate) {
                    vector.addElement(events[i]);
                }
            }
            return GCalClient.eventVectorToArray(vector);
        }catch(Exception e) {
            throw new GCalException("SyncEngine", "removeNonAutoSync", e);
        }
    }
    
    public GCalEvent[][] getEventForSync(GCalFeed[] feedsToSync, GCalClient gCalClient, Form form) throws Exception {
        try {
            CommitComponent commit;
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
                update("Reading phone events...", form);

                //In Nokia allday events would be Memos (in the Entries list) or Anniversaries (in the Anniversaries list)
                boolean entriesRead = readEvents(gCalClient, "Entries", startDateLong, endDateLong, newPhoneEvents, GCalEvent.PLATFORM_ALLDAY_YES);
                //when reading anniversaries it throws an outofmemory exception
                boolean anniversariesRead = false;//readEvents(gCalClient, "Anniversaries", startDateLong, endDateLong, newPhoneEvents, GCalEvent.PLATFORM_ALLDAY_YES, form);

                readEvents(gCalClient, null, startDateLong, endDateLong, newPhoneEvents,
                        (entriesRead || anniversariesRead ? GCalEvent.PLATFORM_ALLDAY_NO : GCalEvent.PLATFORM_ALLDAY_UNKNOWN));

            }

            //filter the events as not up upload nor download repated events
            GCalEvent[] phoneCalEvents = gCalClient.eventVectorToArray(newPhoneEvents);

            GCalEvent[] filteredGCalEvents = filterEvents(gCalEvents, phoneCalEvents, false);
            GCalEvent[] filteredPhoneCalEvents = filterEvents(phoneCalEvents, gCalEvents, true);

            return new GCalEvent[][] {filteredPhoneCalEvents, filteredGCalEvents};
        }catch(Exception e) {
            throw new Exception("Error at SyncEngine.getEventForSync: " + e.getClass().getName() + " - " + e.getMessage());
        }
    }
    
    private boolean readEvents(
            GCalClient gCalClient, String listName,
            long startDateLong, long endDateLong,
            Vector newPhoneEvents, int isPlatformAllday) {

        PhoneCalClient phoneCalClient = null;
        try {
            //open the list of events
            phoneCalClient = PhoneCalClient.createPhoneCalClient(listName, false);
            if(phoneCalClient == null) {
                return false;
            }

            Merger merger = new Merger(phoneCalClient, gCalClient);

            Enumeration eventEnumeration = phoneCalClient.getPhoneEvents(startDateLong, endDateLong);
            //int i = 0;
            while (eventEnumeration.hasMoreElements()) {
                Event phoneEvent = (Event) eventEnumeration.nextElement();
                String gcalId = phoneCalClient.getGCalId(phoneEvent);
                
                //check for new or modified phone events
                /*if (gcalId == null || (gcalId != null && phoneEvent.isModified()))*/
                {
                    //missing GCal ID indicates that the event was created on the phone
                    //and not by Google Calendar
                    GCalEvent gCalEvent = merger.copyToGCalEvent(phoneEvent);
                    gCalEvent.uid = gcalId;
                    gCalEvent.phoneCalId = phoneCalClient.getPhoneId(phoneEvent);
    
                    gCalEvent.isMainCalendarEvent =
                            !Store.getIdCorrelator().phoneIdToGcalId.containsKey(gCalEvent.phoneCalId) ||
                            Store.getIdCorrelator().mainCalendarPhoneIds.contains(gCalEvent.phoneCalId);

                    if(isPlatformAllday == GCalEvent.PLATFORM_ALLDAY_YES || isPlatformAllday == GCalEvent.PLATFORM_ALLDAY_NO) {
                        gCalEvent.isPlatformAllday = isPlatformAllday;
                    }
                    
                    newPhoneEvents.addElement(gCalEvent);
                }
            }
        } catch (Exception e) {
            ErrorHandler.showError("Failed to open phone calendar", e);
            return false;
        } finally {
            if(phoneCalClient != null) {
                try {
                    phoneCalClient.close();
                }catch(Exception e) {}
            }
        }
        
        return true;
    }
    
    public void update(String message, Form form) {
        form.append(message + "\n");
    }
    
    
    private GCalEvent[] filterEvents(GCalEvent[] base, GCalEvent[] others, boolean baseIsUpload) throws Exception {
        //the base events are the events to be uploaded or downloaded.
        
        //An event will be uploaded if:
        // -has a null ID
        // -there is no event with the same id on google or if it was modified after the last modification in google
        
        //An event will be downloaded if:
        // --there is no event with the same id in the phone or if it was modified after the last modification in the phone
        
        try {
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

                        if(eventEquals && ( be.updated == 0 || oe.updated == 0 || be.updated <= oe.updated ) ) {
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
                //if the event is acceptable for update or download, and if it is not a public event to be uploaded
                if(eventAcceptable && !(baseIsUpload && !be.isMainCalendarEvent) ) {
                    filteredBase.addElement(base[i]);
                }
            }

            return GCalClient.eventVectorToArray(filteredBase);
        }catch(Exception e) {
            throw new Exception("Error at SyncEngine.filterEvents [baseIsUpload=" + baseIsUpload+ "]: " + 
                    e.getClass().getName() + " - " + e.getMessage());
        }
    }
    
    /**
     * Compares two string
     * @param s1
     * @param s2
     * @return If both string string are null or empty it will return true,
     * else it will do the standard String.equals
     */
    private boolean equalsString(String s1, String s2) {
        if( (s1 == null || s1.length() == 0) && (s2 == null || s2.length() == 0) ) {
            return true;
        }
        return s1 != null && s2 != null && s1.equals(s2);
    }
}
