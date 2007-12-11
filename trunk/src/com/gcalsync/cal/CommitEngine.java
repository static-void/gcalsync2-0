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

import com.gcalsync.cal.gcal.GCalClient;
import com.gcalsync.cal.gcal.GCalEvent;
import com.gcalsync.cal.phonecal.PhoneCalClient;
import com.gcalsync.option.Options;
import com.gcalsync.store.Store;
import com.gcalsync.util.DateUtil;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.lcdui.Form;
import javax.microedition.pim.Event;

/**
 * Class to commit events in both Google and the phone. Contains code that previously was in CommitComponent.
 * @author Agustin
 * @author Yusuf Abdi
 * @version $Rev: 1 $
 * @date $Date: 2007-12-30 03:22:30 -0500 (Sat, 30 Dec 2007) $
 */
public class CommitEngine {
    
    /** Creates a new instance of CommitEngine */
    public CommitEngine() {
    }

    public void commitSync(GCalEvent[] uploads, GCalEvent[] downloads, GCalClient gCalClient, PhoneCalClient phoneCalClient, Form form) {
        
        //update Google Calendar with any upload events
        processUploadEvents(uploads, gCalClient, form);
        
        //update phone with downloaded events
        processDownloadEvents(downloads, gCalClient, phoneCalClient, form);
        
        Store.getTimestamps().lastSync = System.currentTimeMillis();
        Store.saveTimestamps();
    }
    
    /**
     * Uploads events to Google Calendar
     */
    void processUploadEvents(GCalEvent[] uploads, GCalClient gCalClient, Form form) {
        String gCalId;
        
        try {
            if (uploads != null && uploads.length > 0) {
                //update("Uploading " + this.uploads.length + " event" + ((this.uploads.length > 1)?"s":"") + " to Google...");
                gCalClient.setForm(form);
                for (int i=0; i<uploads.length; i++) {
                    if(uploads[i].eventIsToBeUpdated) {
                        gCalClient.updateEvent(uploads[i]);
                    } else {
                        gCalId = gCalClient.createEvent(uploads[i]);
                        
                        //update the phone event with the gCalId
                        if(uploads[i].uid == null) {
                            IdCorrelation idCorrelation = new IdCorrelation();
                            idCorrelation.phoneCalId = uploads[i].phoneCalId;
                            idCorrelation.gCalId = gCalId;
                            
                            Store.addCorrelation(idCorrelation);
                        }
                    }
                }
            }
        } catch (Exception e) {
            update("Failed Google update: " + e, form);
//#ifdef DEBUG_ERR
//# 			System.err.println("Failed Google update: " + e);
//#endif
        }
    }
    
    /**
     * Saves events to phone calendar
     */
    void processDownloadEvents(GCalEvent[] downloads, GCalClient gCalClient, PhoneCalClient phoneCalClient, Form form) {
        Options options;
        Merger merger;
        Hashtable phoneEventsByGcalId;
        Enumeration phoneEvents;
        Event phoneEvent;
        String gcalId;
        String startDate;
        String endDate;
        long now;
        long startDateLong;
        long endDateLong;
        
        try {
            if (downloads != null && downloads.length > 0) {
                options = Store.getOptions();
                now = System.currentTimeMillis();
                startDate = DateUtil.longToIsoDate(now, 0 - options.pastDays);
                endDate = DateUtil.longToIsoDate(now, options.futureDays);
                startDateLong = DateUtil.isoDateToLong(startDate);
                endDateLong = DateUtil.isoDateToLong(endDate) + DateUtil.DAY; // want all events starting before midnigth on endDate
                phoneEvents = phoneCalClient.getPhoneEvents(startDateLong, endDateLong);
                phoneEventsByGcalId = new Hashtable();
                merger = new Merger(phoneCalClient, gCalClient);
                
                //enumerate all GCal events in the phone's calendar
                while (phoneEvents.hasMoreElements()) {
                    phoneEvent = (Event)phoneEvents.nextElement();
                    gcalId = phoneCalClient.getGCalId(phoneEvent);
                    if (gcalId != null) phoneEventsByGcalId.put(gcalId, phoneEvent);
                }
                
                for (int i=0; i<downloads.length; i++) {
                    //find the downloaded event in the phone's GCal event list.
                    //if the event doesn't exist, then <phoneEvent> is null and
                    //the event will be added to the phone's calendar
                    phoneEvent = (Event)phoneEventsByGcalId.remove(downloads[i].uid);
                    
                    //update/add the downloaded event
                    merger.mergeEvents(phoneEvent, downloads[i], form);
                }
            }
        } catch (Exception e) {e.printStackTrace();
        update("Failed phone update: " + e, form);
//#ifdef DEBUG_ERR
//# 			System.err.println("Failed phone update: " + e);
//#endif
        }
    }
    
    public void update(String message, Form form) {
        form.append(message + "\n");
    }
    
}
