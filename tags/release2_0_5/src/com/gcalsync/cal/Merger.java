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
 *
 *
 *
 * * * Changes:
 *  --/nov/2007 Agustin
 *      -mergeIntoGCalEvent: BlackBerry patch for allday events remove - it is no longer needed
 *      -mergeIntoPhoneEvent: support for allday events added BlackBerryes (and non BB)
 *      -the GCalEvent.updated field now is used (get from and set into phone Event)
 *  --/dec/2007 Agustin
 *      -mergeIntoGCalEvent: reads BB allday events using RIM API (RIM's ALLDAY field)
 *      -mergeIntoGCalEvent, mergeIntoPhoneEvent: support for Nokia allday events
 */
package com.gcalsync.cal;


import javax.microedition.pim.Event;
import javax.microedition.pim.PIMException;
import javax.microedition.lcdui.Form;

import com.gcalsync.option.Options;
import com.gcalsync.cal.gcal.GCalClient;
import com.gcalsync.cal.gcal.GCalEvent;
import com.gcalsync.cal.phonecal.PhoneCalClient;
import com.gcalsync.log.GCalException;
import com.gcalsync.store.Store;
import com.gcalsync.util.DateUtil;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author: batcage $
 * @version $Rev: 28 $
 * @date $Date: 2006-12-26 21:49:34 -0500 (Tue, 26 Dec 2006) $
 */
public class Merger {
    
    private PhoneCalClient phoneCalClient;
    private GCalClient gCalClient;
    private Options options;
    
    private static final int BB_ALLDAY_FIELD = 20000928; //net.rim.blackberry.api.pdap.BlackBerryEvent.ALLDAY
    
    public Merger(PhoneCalClient phoneCalClient, GCalClient gCalClient) throws Exception {
        try {
            this.phoneCalClient = phoneCalClient;
            this.gCalClient = gCalClient;
            this.options = Store.getOptions();
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "{init}", e);
        }
    }
    
    public Event copyToPhoneEvent(GCalEvent gCalEvent) throws PIMException, Exception {
        Event phoneEvent = phoneCalClient.createEvent();
        mergeIntoPhoneEvent(phoneEvent, gCalEvent);
        return phoneEvent;
    }
    
    public GCalEvent copyToGCalEvent(Event phoneEvent) {
        GCalEvent gCalEvent = new GCalEvent();
        mergeIntoGCalEvent(phoneEvent, gCalEvent);
        return gCalEvent;
    }
    
    public boolean compareEvents(Event phoneEvent, GCalEvent gCalEvent) {
        GCalEvent pe = copyToGCalEvent(phoneEvent);
        return (/*pe.uid.equals(gCalEvent.uid)
        &&*/ pe.title.equals(gCalEvent.title)
        && pe.note.equals(gCalEvent.note)
        && (pe.recur != null && gCalEvent.recur != null && pe.recur.equals(gCalEvent.recur))
        && pe.startTime == gCalEvent.startTime
                && pe.endTime == gCalEvent.endTime);
    }
    
    public void mergeEvents(Event phoneEvent, GCalEvent gCalEvent, Form form) throws PIMException, Exception {
        try {
        boolean success;
        
        if (phoneEvent == null) {
            if (!gCalEvent.cancelled && options.download) {
//#ifdef DEBUG_INFO
//#                 System.out.println("=> Inserting event " + gCalEvent.title + ", not present in phone");
//#endif
                try {
                    phoneEvent = copyToPhoneEvent(gCalEvent);

                    if (form != null) form.append("Saving \"" + gCalEvent.title + "\"...");
                    success = phoneCalClient.insertEvent(phoneEvent, gCalEvent.uid);
                }catch(PIMException e) {
                    throw e;
                }catch(Exception e) {
                    success = false;
                    form.append(e.getMessage() + "\n");
                }
                
                if (form != null) {
                    form.append(success ? "OK" : "ERR");
                    form.append("\n");
                }
                
            }
            return;
        }
        
        Options options = Store.getOptions();
        Timestamps timestamps = Store.getTimestamps();
        long phoneEventChangeTime = phoneCalClient.getDateField(phoneEvent, Event.REVISION);
        boolean phoneEventHasChanged = phoneEventChangeTime > timestamps.lastSync;
        boolean gCalEventHasChanged = (gCalEvent.updated - timestamps.lastSync) > 2000; // 2 sec slack to allow for rounding etc
//#ifdef DEBUG_INFO
//# 		//System.out.println("PhoneDT=" + DateUtil.longToIsoDateTime(phoneEventChangeTime) + " GCalDT=" +
//# 		//				   DateUtil.longToIsoDateTime(gCalEvent.updated) + " Upd=" + DateUtil.longToIsoDateTime(timestamps.lastSync));
//#endif
        boolean phoneCalWonMerge = phoneEventHasChanged;
        boolean gCalWonMerge = gCalEventHasChanged;
        if (options.mergeStrategy == Options.GCAL_WINS_MERGE) {
            phoneCalWonMerge = false;
        } else if (options.mergeStrategy == Options.PHONE_WINS_MERGE) {
            gCalWonMerge = false;
        } else {
            // LAST_UPDATED_WINS_MERGE or something illega
            if (phoneCalWonMerge && gCalWonMerge) {
                // conflict, the last updated wins
                if (phoneEventChangeTime > gCalEvent.updated) {
                    gCalWonMerge = false;
                } else {
                    phoneCalWonMerge = false;
                }
            }
        }
        if (/*gCalWonMerge &&*/ options.download) {
            if (gCalEvent.cancelled) {
//#ifdef DEBUG_INFO
//#                 System.out.println("=> Removing event " + gCalEvent.title + ", cancelled in GCal");
//#endif
                if (form != null) form.append("Removing \"" + gCalEvent.title + "\" from phone...");
                success = phoneCalClient.removeEvent(phoneEvent);
            } else {
//#ifdef DEBUG_INFO
//#                 System.out.println("=> Updating event " + gCalEvent.title + " in phone");
//#endif
                if (form != null) form.append("Updating \"" + gCalEvent.title + "\" in phone...");
                try {
                    mergeIntoPhoneEvent(phoneEvent, gCalEvent);
                    success = phoneCalClient.updateEvent(phoneEvent);
                }catch(Exception e) {
                    success = false;
                    form.append(e.getMessage() + "\n");
                }
            }
            
            if (form != null) {
                form.append(success ? "OK" : "ERR");
                form.append("\n");
            }
        }
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "mergeEvents", e);
        }
        //bug: if you do a full sync and the same event gets committed, the phone Cal wins
        //merge because it was written at a time after the GCalEvent was updated in Google
        // so, phoneCal tries to upload the event back to Google. I think it's a good idea
        // to let the Gcal win merge but not the other way around
                /*else if (phoneCalWonMerge && options.shouldUpdateGCal()) {
//#ifdef DEBUG_INFO
//#             System.out.println("=> Updating event " + gCalEvent.title + " in GCal");
//#endif
                        if (form != null) form.append("Updating \"" + gCalEvent.title + "\" in GCal...\n");
            mergeIntoGCalEvent(phoneEvent, gCalEvent);
            gCalClient.updateEvent(gCalEvent);
        }*/
    }
    
    private void mergeIntoPhoneEvent(Event phoneEvent, GCalEvent gCalEvent) throws Exception {
        if (isSet(gCalEvent.title)) {
            phoneCalClient.setStringField(phoneEvent, Event.SUMMARY, gCalEvent.title);
        }
        if (isSet(gCalEvent.note)) {
            phoneCalClient.setStringField(phoneEvent, Event.NOTE, gCalEvent.note);
        }
        if (isSet(gCalEvent.location)) {
            phoneCalClient.setStringField(phoneEvent, Event.LOCATION, gCalEvent.location);
        }
        
        boolean fixTimeForAlldayEvent = false;
        boolean useLocalTime = false; //in Nokia allday events are saved at midnight but with
        //local time in stead of GMT
        
        if(gCalEvent.isAllDay()) {
            if(phoneCalClient.setBooleanField(phoneEvent, BB_ALLDAY_FIELD, true)) {
                //if I could set the boolean that means that I am in a BlackBerry and so the event
                //is set as an allday event.
                //if I could not set the boolean that means that I am not in a BlackBerry, so follow
                //the Java documentation that says that allday events are events with the same start
                //and end date/time
                fixTimeForAlldayEvent = false;
            } else if(phoneEvent.getPIMList().getName().equalsIgnoreCase("Entries")) {
                //if we are in the Entries event list that means that we are adding a Memo
                //on Nokia, so do not patch the date
                fixTimeForAlldayEvent = false;
                
                useLocalTime = true;
            } else {
                fixTimeForAlldayEvent = true;
            }
        }
        
        if (isSet(gCalEvent.startTime)) {
            if(fixTimeForAlldayEvent) {
                //TODO: support allday events that last more than one day for non blackberry devices
                phoneCalClient.setDateField(phoneEvent, Event.START, gCalEvent.startTime);
            } else {
                if(useLocalTime) {
                    phoneCalClient.setDateField(phoneEvent, Event.START, DateUtil.gmtTimeToLocalTime(gCalEvent.startTime));
                } else {
                    phoneCalClient.setDateField(phoneEvent, Event.START, gCalEvent.startTime);
                }
            }
        }
        if (isSet(gCalEvent.endTime)) {
            if(fixTimeForAlldayEvent) { //use start as end for allday events
                //TODO: support allday events that last more than one day for non blackberry devices
                phoneCalClient.setDateField(phoneEvent, Event.END, gCalEvent.startTime);
            } else {
                if(useLocalTime) {
                    phoneCalClient.setDateField(phoneEvent, Event.END, DateUtil.gmtTimeToLocalTime(gCalEvent.endTime));
                } else {
                    phoneCalClient.setDateField(phoneEvent, Event.END, gCalEvent.endTime);
                }
            }
        }
        
        if (isSet(gCalEvent.reminder) && (gCalEvent.reminder >= 0)) {
            phoneCalClient.setIntField(phoneEvent, Event.ALARM, gCalEvent.reminder * 60);
        }
        
        try {
            if (gCalEvent.recur != null && gCalEvent.recur.getRepeat() != null)
                phoneEvent.setRepeat(gCalEvent.recur.getRepeat());
        } catch (Exception e) {
            throw new Exception("Recurrence not supported by the phone" + (e.getMessage() != null ? (": " + e.getMessage()) : "" ) );
//#ifdef DEBUG_ERR
//# // 			System.out.println("Failed to copy repeat rule into phone cal, error=" + e.toString());
//#endif
        }
    }
    
    private boolean isSet(String value) {
        return (value != null) && value.length() > 0;
    }
    
    private boolean isSet(long value) {
        return value > 0;
    }
    
    private boolean isSet(int value) {
        return value >= 0;
    }
    
    private void mergeIntoGCalEvent(Event phoneEvent, GCalEvent gCalEvent) {
        String summary = phoneCalClient.getStringField(phoneEvent, Event.SUMMARY);
        if (isSet(summary)) {
            gCalEvent.title = summary;
        }
        String note = phoneCalClient.getStringField(phoneEvent, Event.NOTE);
        if (isSet(note)) {
            gCalEvent.note = note;
        }
        String location = phoneCalClient.getStringField(phoneEvent, Event.LOCATION);
        if (isSet(location)) {
            gCalEvent.location = location;
        }
        long startDate = phoneCalClient.getDateField(phoneEvent, Event.START);
        if (isSet(startDate)) {
            gCalEvent.startTime = startDate;
        }
        
        long endDate = phoneCalClient.getDateField(phoneEvent, Event.END);
        if (isSet(endDate)) {
            gCalEvent.endTime = endDate;
        }
        
        //Read the BlackBerry allday filed to know for sure if the event is an allday event
        long alldayEvent = phoneCalClient.getBooleanField(phoneEvent, BB_ALLDAY_FIELD);
        if(alldayEvent == 0) gCalEvent.isPlatformAllday = GCalEvent.PLATFORM_ALLDAY_NO;
        else if(alldayEvent == 1) gCalEvent.isPlatformAllday = GCalEvent.PLATFORM_ALLDAY_YES;
        //if we are not on a BB
        
        //if we are on Nokia (on Nokia allday events are Memos in the Entries list)
        else if(phoneEvent.getPIMList().getName().equalsIgnoreCase("Entries")) {
            gCalEvent.isPlatformAllday = GCalEvent.PLATFORM_ALLDAY_YES;
            
            //on Nokia Memos start and end time are at midnight, but at midnignt in local time
            //and not in GMT time, so let's patch the start and end time
            gCalEvent.startTime = DateUtil.localTimeToGmtTime(gCalEvent.startTime);
            gCalEvent.endTime = DateUtil.localTimeToGmtTime(gCalEvent.endTime);
        }
        //if we are nither on BB nor on Nokia, but this is an allday event
        else if(gCalEvent.endTime == gCalEvent.startTime) {
            gCalEvent.isPlatformAllday = GCalEvent.PLATFORM_ALLDAY_UNKNOWN; //we cannot tell for sure that this is an allday event
            
            //let's fix the end time as to make it last one whole day
            gCalEvent.endTime += 24 * 60 * 60 * 1000;
        }
        
        
       /* if (gCalEvent.isAllDay(Store.getOptions().uploadTimeZoneOffset)) {
            //TODO: BlackBerry phones start all-day events on previous day (bug in BB OS?)
            //adjust for this behavior...not sure if other phones follow this behavior
            //so we'll have to add an option to enable the adjustment
            gCalEvent.startTime += 1000*60*60*24;
            gCalEvent.endTime += 1000*60*60*24;
        }*/
        
        int alarm = phoneCalClient.getIntField(phoneEvent, Event.ALARM);
        if (isSet(alarm)) {
            gCalEvent.reminder = alarm / 60;
        }
        
        try {
            if (phoneEvent.getRepeat() != null)
                gCalEvent.recur = new Recurrence(phoneEvent.getRepeat(), startDate, endDate);
        } catch (Exception e) {
//#ifdef DEBUG_ERR
//# 			System.out.println("Failed to copy repeat rule into gCal, error=" + e.toString());
//#endif
        }
    }
}
