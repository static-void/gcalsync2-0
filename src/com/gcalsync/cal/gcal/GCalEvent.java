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
 */
/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author: batcage $
 * @author Agustin Rivero
 * @version $Rev: 38 $
 * @date $Date: 2006-12-30 03:22:30 -0500 (Sat, 30 Dec 2006) $
 * * Changes:
 *  --/nov/2007 Agustin Rivero
 *      -new properties: uid, phoneCalId, eventIsToBeUpdated and autoSyncCantidate
 *      -asXML: fixed the way the date is printed as to support correctly all day events at google
 *      -toString: prints [UPDATE] before the event name if the event was updated
 */

package com.gcalsync.cal.gcal;

import com.gcalsync.util.DateUtil;
import com.gcalsync.cal.Recurrence;
import com.gcalsync.store.Store;


public class GCalEvent {
    
    public String parentCalendarTitle;
    public String id;
    public String title;
    public String note;
    public String location;
    public String origEventId; /* original recurrence event ID */
    
    public String editLink;    /* URI to post event edits */
    
    public long updated;
    public long published;
    public long startTime;
    public long endTime;
    public boolean cancelled;
    public int reminder;
    public Recurrence recur;
    
    public String uid;  //the UID of the event (at google)
    public String phoneCalId; //teh UID of the event at the phone
    public boolean eventIsToBeUpdated;  //tells if the event was updates
    public boolean autoSyncCantidate;   //tells if event is a cantidate to autosyncronize (an event is not candidate if the
                                        // last update date cannot be verified)
    
    static final int MAX_NOTE_LEN_DISPLAYED = 75;
    
    public GCalEvent() {
        this.id = "";
        this.title = "";
        this.note = "";
        this.location = "";
        this.origEventId = "";
        this.recur = null;
        this.phoneCalId = null;
        this.eventIsToBeUpdated = false;
        this.autoSyncCantidate = true;
    }
    
    public GCalEvent(String title, String from, String to) {
        this();
        this.id = "id_" + title;
        this.uid = "uid_" + title;
        this.title = title;
        this.startTime = DateUtil.isoDateToLong(from);
        this.endTime = DateUtil.isoDateToLong(to);
    }
    
    public String asXML() {
        StringBuffer stringBuffer = new StringBuffer();
        
        //insert header
        stringBuffer.append("<?xml version='1.0'?>\n");
        stringBuffer.append("<entry xmlns='http://www.w3.org/2005/Atom'\n" +
                "xmlns:gCal='http://schemas.google.com/gCal/2005'\n" +
                "xmlns:gd='http://schemas.google.com/g/2005'>");
        
        //insert item ID
        if (this.id != null && this.id.equals("") == false)
            stringBuffer.append("<id>" + this.id + "</id>\n");
        
        //insert timestamp of publish
        if (this.published > 0)
            stringBuffer.append("<published>" + DateUtil.longToIsoDateTimeGMT(this.published) + "</published>\n");
        
        //insert timestamp of update
        if (this.updated > 0)
            stringBuffer.append("<updated>" + DateUtil.longToIsoDateTimeGMT(this.updated) + "</updated>\n");
        
        //categorize item as event
        stringBuffer.append("<category scheme='http://schemas.google.com/g/2005#kind'\n" +
                "term='http://schemas.google.com/g/2005#event'></category>\n");
        
        //insert event title
        stringBuffer.append("<title type='text'>" + encode(this.title) + "</title>\n");
        
        //insert event comments
        if (this.note != null && !this.note.equals(""))
            stringBuffer.append("<content type='text'>" + encode(this.note) + "</content>");
        
        //insert event status
        if (this.cancelled)
            stringBuffer.append("<gd:eventStatus value='http://schemas.google.com/g/2005#event.canceled'/>" );
        
        //exclude time for all-day events
        long offset = Store.getOptions().uploadTimeZoneOffset;

        String startDate = DateUtil.longToIsoDateTimeGMT(this.startTime+offset);
        String endDate = DateUtil.longToIsoDateTimeGMT(this.endTime+offset);
        if(this.isAllDay(offset)) {
            //In Goolge all day events do not have time
            startDate = DateUtil.longToIsoDateGMT(this.startTime);
            endDate = DateUtil.longToIsoDateGMT(this.endTime == this.startTime ? (this.startTime + 24 * 60 * 60 * 1000) : this.endTime);
        }
        
        //insert start and end dates of event
        stringBuffer.append("<gd:when startTime='" + startDate + "' endTime='" + endDate + "'>");
        
        //insert reminder for non-recurring event
        if (this.recur == null) {
            //reminder must be inside of <gd:when> for non-recurring events
            if (this.reminder > 0)
                stringBuffer.append("<gd:reminder minutes='" + this.reminder + "'/>");
        }
        //close <gd:when>
        stringBuffer.append("</gd:when>\n");
        
        //insert repeat rule
        if (this.recur != null) {
            String recurStr = this.recur.toString();
            if (recurStr.equals("") == false) {
                stringBuffer.append("<gd:recurrence>" + recurStr + "</gd:recurrence>\n");
                
                //insert reminder for recurring event
                if (this.reminder > 0)
                    stringBuffer.append("<gd:reminder minutes='" + this.reminder + "'/>\n");
            }
        }
        
        //insert event location
        if (this.location != null && this.location.equals("") == false)
            stringBuffer.append("<gd:where valueString='" + encode(this.location) + "'/>");
        
        
        //insert uid
        stringBuffer.append("<gCal:uid value='" + this.uid + "@google.com'/>");
        
        //TODO: handle attendees in the following format
                /*  <gd:who email='attendeeName@nosuchemail.com'>
                        <gd:attendeeStatus value='http://schemas.google.com/g/2005#event.invited'></gd:attendeeStatus>
                        </gd:who>*/
        
        //close <entry>
        stringBuffer.append("</entry>");

        return stringBuffer.toString();
    }
    
    String encode(String in) {
        StringBuffer sb = new StringBuffer();
        char[] c = in.toCharArray();
        
        for (int i=0; i<c.length; i++) {
            
                        /* Encode special characters in UTF-8. For example, the
                        character 'ä' (ASCII code 0xE4) is represented in UTF-8
                        as two bytes: 0xC3 0xA4, where 0xC3 is the ASCII code
                        for 'Ã' and 0xA4 is the original character's ASCII code
                        adjusted by '@' (ASCII code 0x40). */
            if (c[i] >= '¡' && c[i] <= '¿') {
                sb.append('Â');
                sb.append(c[i]);
            } else if (c[i] >= 'À' && c[i] <= 'ÿ') {
                sb.append('Ã');
                sb.append((char)(c[i] - '@'));
            } else {
                sb.append(c[i]);
            }
        }
        
        return sb.toString();
    }
    
    public boolean isAllDay() {
        return this.isAllDay(0);
    }
    
    public boolean isAllDay(long timeOffset) {
        return DateUtil.isAllDay(this.startTime + timeOffset, this.endTime + timeOffset);
    }
    
    public String toString() {
        return this.toString(0);
    }
    
    public String toString(long timeOffset) {
        StringBuffer sb = new StringBuffer();
        
        if (this.cancelled)
            sb.append("CANCELLED:\n");
        
        if (this.parentCalendarTitle != null && !this.parentCalendarTitle.equals(""))
            sb.append("Calendar: \"" + this.parentCalendarTitle + "\"\n");
        
        String updateString = "";
        if(this.eventIsToBeUpdated) {
            updateString = "[Update" + (this.autoSyncCantidate ? "" : "*") + "]";
        }
        
        sb.append(updateString + "Event: \"" + this.title + "\"\n");
        sb.append("When: " + DateUtil.formatInterval(this.startTime + timeOffset, this.endTime + timeOffset));
        
        if (this.location != null && !this.location.equals(""))
            sb.append("\nWhere: " + this.location);
        
        if (this.reminder > 0)
            sb.append("\nReminder: " + this.reminder + " minutes");
        
        if (this.recur != null)
            sb.append("\nRepeats: " + this.recur.toReadableString());
        
        if (this.note != null && !this.note.equals("")) {
            sb.append("\nNote: \"");
            
            //display up to <MAX_NOTE_LEN_DISPLAYED> characters of the note
            if (this.note.length() > MAX_NOTE_LEN_DISPLAYED)
                sb.append(this.note.substring(0, MAX_NOTE_LEN_DISPLAYED) + "[...]");
            else
                sb.append(this.note);
            
            sb.append("\"");
        }
        
        return sb.toString();
    }
}
