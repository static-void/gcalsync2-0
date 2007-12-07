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
package com.gcalsync.cal;

import java.util.TimeZone;

import javax.microedition.pim.Event;
import javax.microedition.pim.PIMException;
import javax.microedition.lcdui.Form;

import com.gcalsync.option.Options;
import com.gcalsync.cal.Recurrence;
import com.gcalsync.cal.gcal.GCalClient;
import com.gcalsync.cal.gcal.GCalEvent;
import com.gcalsync.cal.phonecal.PhoneCalClient;
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


    public Merger(PhoneCalClient phoneCalClient, GCalClient gCalClient) {
        this.phoneCalClient = phoneCalClient;
        this.gCalClient = gCalClient;
        this.options = Store.getOptions();
    }

    public Event copyToPhoneEvent(GCalEvent gCalEvent) throws PIMException {
        Event phoneEvent = phoneCalClient.createEvent();
        mergeIntoPhoneEvent(phoneEvent, gCalEvent);
        return phoneEvent;
    }

    public GCalEvent copyToGCalEvent(Event phoneEvent) {
        GCalEvent gCalEvent = new GCalEvent();
        mergeIntoGCalEvent(phoneEvent, gCalEvent);
        return gCalEvent;
    }

        public boolean compareEvents(Event phoneEvent, GCalEvent gCalEvent)
        {
                GCalEvent pe = copyToGCalEvent(phoneEvent);
                return (pe.id.equals(gCalEvent.id)
                                && pe.title.equals(gCalEvent.title)
                                && pe.note.equals(gCalEvent.note)
                                && (pe.recur != null && gCalEvent.recur != null && pe.recur.equals(gCalEvent.recur))
                                && pe.startTime == gCalEvent.startTime
                                && pe.endTime == gCalEvent.endTime);
        }

    public void mergeEvents(Event phoneEvent, GCalEvent gCalEvent, Form form) throws PIMException {
                boolean success;

                if (phoneEvent == null) {
            if (!gCalEvent.cancelled && options.download) {
//#ifdef DEBUG_INFO
//#                 System.out.println("=> Inserting event " + gCalEvent.title + ", not present in phone");
//#endif
                phoneEvent = copyToPhoneEvent(gCalEvent);

                                if (form != null) form.append("Saving \"" + gCalEvent.title + "\"...");
                                success = phoneCalClient.insertEvent(phoneEvent, gCalEvent.id);
                if (form != null) 
                                {
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
//#             //System.out.println("PhoneDT=" + DateUtil.longToIsoDateTime(phoneEventChangeTime) + " GCalDT=" +
//#             //                                 DateUtil.longToIsoDateTime(gCalEvent.updated) + " Upd=" + DateUtil.longToIsoDateTime(timestamps.lastSync));
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
                mergeIntoPhoneEvent(phoneEvent, gCalEvent);
                success = phoneCalClient.updateEvent(phoneEvent);
            }

                        if (form != null) 
                        {
                                form.append(success ? "OK" : "ERR");
                                form.append("\n");
                        }
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

    private void mergeIntoPhoneEvent(Event phoneEvent, GCalEvent gCalEvent) {
        if (isSet(gCalEvent.title)) {
            phoneCalClient.setStringField(phoneEvent, Event.SUMMARY, gCalEvent.title);
        }
        if (isSet(gCalEvent.note)) {
            phoneCalClient.setStringField(phoneEvent, Event.NOTE, gCalEvent.note);
        }
        if (isSet(gCalEvent.location)) {
            phoneCalClient.setStringField(phoneEvent, Event.LOCATION, gCalEvent.location);
        }
                
        if (isSet(gCalEvent.startTime)) {
            phoneCalClient.setDateField(phoneEvent, Event.START, gCalEvent.startTime);
        }
//Yusuf: Remove code
/*
System.out.println("Event ID: "+gCalEvent.id);
System.out.println("Event EventID: "+gCalEvent.origEventId);
System.out.println("Event Title: " + gCalEvent.title);
System.out.println("Event Note: " + gCalEvent.note);
System.out.println("Event Start: " + gCalEvent.startTime);
System.out.println("Event End: " + gCalEvent.endTime);
*/

                if (gCalEvent.isAllDay()) {
                        //all-day PIM events are indicated by matching start and end times
                        //if (isSet(gCalEvent.startTime)) phoneCalClient.setDateField(phoneEvent, Event.END, gCalEvent.startTime);
                        if (isSet(gCalEvent.startTime)){ 
                            
                            phoneCalClient.setDateField(phoneEvent, Event.START, gCalEvent.startTime);
                            phoneCalClient.setDateField(phoneEvent, Event.END, gCalEvent.startTime);
                        }
                }
        else if (isSet(gCalEvent.endTime)) {
            phoneCalClient.setDateField(phoneEvent, Event.END, gCalEvent.endTime);
        }

        if (isSet(gCalEvent.reminder) && (gCalEvent.reminder >= 0)) {
            phoneCalClient.setIntField(phoneEvent, Event.ALARM, gCalEvent.reminder * 60);
        }

                try 
                { 
                        if (gCalEvent.recur != null && gCalEvent.recur.getRepeat() != null) 
                                phoneEvent.setRepeat(gCalEvent.recur.getRepeat()); 
                }
                catch (Exception e) { 
//#ifdef DEBUG_ERR
//#                     System.out.println("Failed to copy repeat rule into phone cal, error=" + e.toString()); 
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

                if (gCalEvent.isAllDay(Store.getOptions().uploadTimeZoneOffset))
                {
                        //TODO: BlackBerry phones start all-day events on previous day (bug in BB OS?)
                        //adjust for this behavior...not sure if other phones follow this behavior
                        //so we'll have to add an option to enable the adjustment
                        //Yusuf: gCalEvent.startTime += 1000*60*60*24;
                        //Yusuf: gCalEvent.endTime += 1000*60*60*24;
                }

        int alarm = phoneCalClient.getIntField(phoneEvent, Event.ALARM);
        if (isSet(alarm)) {
            gCalEvent.reminder = alarm / 60;
        }

                try 
                {
                        if (phoneEvent.getRepeat() != null)
                                gCalEvent.recur = new Recurrence(phoneEvent.getRepeat(), startDate, endDate); 
                }
                catch (Exception e) { 
//#ifdef DEBUG_ERR
//#                     System.out.println("Failed to copy repeat rule into gCal, error=" + e.toString()); 
//#endif
                }
    }

}
