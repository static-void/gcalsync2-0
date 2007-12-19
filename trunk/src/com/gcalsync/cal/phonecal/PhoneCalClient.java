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
 * * Changes:
 *  --/nov/2007 Agustin
 *      -getGCalId: now it uses the phoneCalId
 *      -setBoolean: added
 *  dec/2007 Agustin
 *      -insertEvent, updateEvent: ignores events which RepeatRule is not supported by the phone
 */
package com.gcalsync.cal.phonecal;

import com.gcalsync.cal.IdCorrelation;
import com.gcalsync.cal.Recurrence;
import com.gcalsync.store.Store;
import com.gcalsync.log.*;

import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.pim.RepeatRule;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author: batcage $
 * @version $Rev: 32 $
 * @date $Date: 2006-12-26 23:43:46 -0500 (Tue, 26 Dec 2006) $
 */
public class PhoneCalClient {
    
    public int createdCount = 0;
    public int updatedCount = 0;
    public int removedCount = 0;
    private EventList phoneEventList;
    
    private static int[][] supportedRecurrenceFields = null;
    
    public PhoneCalClient() {
        if (phoneEventList == null) {
            try {
                PIM pim = PIM.getInstance();
                phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
            } catch (Exception e) {}
        }
    }
    
    private PhoneCalClient(EventList phoneEventList) {
        this.phoneEventList = phoneEventList;
    }
    
    
    /**
     * Creates a new PhoneCalClient for the specified list of events.
     * @param listName Name of a list, or null for no list
     * @return A PhoneCalClient for the specifed list, or null if the
     * list does not exist
     */
    public static PhoneCalClient createPhoneCalClient(String listName, boolean write) {
        try {
            PIM pim = PIM.getInstance();
            
            if(listName == null) {
                EventList phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, write ? PIM.READ_WRITE : PIM.READ_ONLY);
                return new PhoneCalClient(phoneEventList);
            }
            
            String[] ss = pim.listPIMLists(PIM.EVENT_LIST);
            boolean found = false;
            
            for(int i = 0; i < ss.length && !found; i++) {
                if(ss[i].equalsIgnoreCase(listName)) {
                    found = true;
                }
            }
            
            if(!found) {
                return null;
            }
            
            EventList phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, write ? PIM.READ_WRITE : PIM.READ_ONLY, listName);
            return new PhoneCalClient(phoneEventList);
            
        }catch(Exception e) {
            return null;
        }
    }
    
    public void close() {
        try {
            phoneEventList.close();
        } catch (PIMException e) {
            ErrorHandler.showError("Failed to close phone calendar, events may not have been saved", e);
        }
    }
    
    public Enumeration getPhoneEvents(long startDate, long endDate) throws PIMException {
        return getPhoneEventList().items(EventList.OCCURRING, startDate, endDate, false);
    }
    
    private EventList getPhoneEventList() throws PIMException {
        if (phoneEventList == null) {
            PIM pim = PIM.getInstance();
            phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
        }
        return phoneEventList;
    }
    
    public String getStringField(Event phoneEvent, int field) {
        try {
            if (phoneEvent.getPIMList().isSupportedField(field) && (phoneEvent.countValues(field) > 0)) {
                return phoneEvent.getString(field, 0);
            } else {
                // TODO: log all unsupported fields, but only once
                return null;
            }
        }catch(Exception e) {
            return null;
        }
    }
    
    public void setStringField(Event phoneEvent, int field, String value) {
        if (phoneEvent.getPIMList().isSupportedField(field)) {
            if (phoneEvent.countValues(field) == 0) {
                phoneEvent.addString(field, Event.ATTR_NONE, value);
            } else {
                phoneEvent.setString(field, 0, Event.ATTR_NONE, value);
            }
        }
        // TODO: log all unsupported fields, but only once
    }
    
    public long getDateField(Event phoneEvent, int field) {
        if (phoneEvent.getPIMList().isSupportedField(field) && (phoneEvent.countValues(field) > 0)) {
            return phoneEvent.getDate(field, 0);
        } else {
            // TODO: log all unsupported fields, but only once
            return 0;
        }
    }
    
    public void setDateField(Event phoneEvent, int field, long value) {
        if (phoneEvent.getPIMList().isSupportedField(field)) {
            if (phoneEvent.countValues(field) == 0) {
                phoneEvent.addDate(field, Event.ATTR_NONE, value);
            } else {
                phoneEvent.setDate(field, 0, Event.ATTR_NONE, value);
            }
        }
        // TODO: log all unsupported fields, but only once
    }
    
    public int getIntField(Event phoneEvent, int field) {
        try {
            if (phoneEvent.getPIMList().isSupportedField(field) && (phoneEvent.countValues(field) > 0)) {
                return phoneEvent.getInt(field, 0);
            } else {
                // TODO: log all unsupported fields, but only once
                return -1;
            }
        }catch(Exception e) {
            return -1;
        }
    }
    
    public void setIntField(Event phoneEvent, int field, int value) {
        if (phoneEvent.getPIMList().isSupportedField(field)) {
            if (phoneEvent.countValues(field) == 0) {
                phoneEvent.addInt(field, Event.ATTR_NONE, value);
            } else {
                phoneEvent.setInt(field, 0, Event.ATTR_NONE, value);
            }
        }
        // TODO: log all unsupported fields, but only once
    }
    
    
    /**
     * Gets the value of a boolean field
     * @param phoneEvent The phone event to use
     * @param field The field to get the value for
     * @return 0=false 1=true -1=fild unsuported
     */
    public int getBooleanField(Event phoneEvent, int field) {
        if (phoneEvent.getPIMList().isSupportedField(field)) {
            if(phoneEvent.countValues(field) > 0) {
                return phoneEvent.getBoolean(field, 0) == true ? 1 : 0;
            } else {
                return 0;
            }
        } else {
            // TODO: log all unsupported fields, but only once
            return -1;
        }
    }
    
    /**
     * Sets the value of a boolean field
     * @param phoneEvent The Event onto which set the value
     * @param field The field to set
     * @param value The boolean value to set
     * @return True if the field is supported, false otherwise
     */
    public boolean setBooleanField(Event phoneEvent, int field, boolean value) {
        if (phoneEvent.getPIMList().isSupportedField(field)) {
            if (phoneEvent.countValues(field) == 0) {
                phoneEvent.addBoolean(field, Event.ATTR_NONE, value);
            } else {
                phoneEvent.setBoolean(field, 0, Event.ATTR_NONE, value);
            }
            return true;
        } else {
            return false;
        }
    }
    
    
    public String getPhoneId(Event phoneEvent) {
        int idField = findIdField();
        String phoneId = phoneEvent.getString(idField, 0);
        
        return phoneId;
    }
    
    public String getGCalId(Event phoneEvent) {
        String phoneId = getPhoneId(phoneEvent);
        String gcalId = (String) Store.getIdCorrelator().phoneIdToGcalId.get(phoneId);
//#ifdef DEBUG_INFO
//#         System.out.println("Read " + phoneId + " -> " + gcalId);
//#endif
        return gcalId;
    }
    
    public void setGCalId(Event phoneEvent, String gCalId) {
        int idField = findIdField();
        IdCorrelation idCorrelation = new IdCorrelation();
        idCorrelation.phoneCalId = phoneEvent.getString(idField, 0);
        idCorrelation.gCalId = gCalId;
//#ifdef DEBUG_INFO
//#         System.out.println("Storing " + idCorrelation.phoneCalId + " -> " + idCorrelation.gCalId);
//#endif
        Store.addCorrelation(idCorrelation);
    }
    
    private int findIdField() {
        if (phoneEventList.isSupportedField(Event.UID)) {
            return Event.UID;
        } else if (phoneEventList.isSupportedField(Event.LOCATION)) {
            return Event.LOCATION;
        } else if (phoneEventList.isSupportedField(Event.NOTE)) {
            return Event.NOTE;
        } else {
            throw new IllegalStateException("Cannot store ID, neither UID, LOCATION nor NOTE is supported");
        }
    }
    
    public Event createEvent() {
        return phoneEventList.createEvent();
    }
    
    public boolean insertEvent(Event phoneEvent, String gCalId) throws PIMException {
        boolean success;
        try {
            Hashtable correctRR = Recurrence.getRepeatRuleInfo(phoneEvent.getRepeat());
            phoneEvent.commit();
            RepeatRule phoneRR = phoneEvent.getRepeat();
            
            if(!Recurrence.repeatRuleEquals(phoneRR, correctRR)) {
                ((EventList)phoneEvent.getPIMList()).removeEvent(phoneEvent);
                success = false;
            }
            else {
                setGCalId(phoneEvent, gCalId);
                createdCount++;
                success = true;
            }
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        
        return success;
    }
    
    public boolean updateEvent(Event phoneEvent) throws PIMException {
        boolean success;
        try {
            Hashtable correctRR = Recurrence.getRepeatRuleInfo(phoneEvent.getRepeat());
            phoneEvent.commit();
            RepeatRule phoneRR = phoneEvent.getRepeat();
            
            //check if the phone supports the repeat rule (if set)
            if(!Recurrence.repeatRuleEquals(phoneRR, correctRR)) {
                ((EventList)phoneEvent.getPIMList()).removeEvent(phoneEvent);
                success = false;
            } else {
                updatedCount++;
                success = true;
            }
        } catch (Exception e) {
            success = false;
        }
        
        return success;
    }
    
    public boolean removeEvent(Event event) throws PIMException {
        boolean success;
        try {
            phoneEventList.removeEvent(event);
            try {event.commit();} catch (Exception e) {}
            removedCount++;
            success = true;
        } catch (Exception e) {
            success = false;
        }
        
        return success;
    }
    
    public void removeDownloadedEvents() {
        try {
            EventList phoneEventList = getPhoneEventList();
            Hashtable phoneIdToGcalId = Store.getIdCorrelator().phoneIdToGcalId;
            Enumeration allPhoneEventsEnum = phoneEventList.items();
            int idField = findIdField();
            while (allPhoneEventsEnum.hasMoreElements()) {
                Event phoneEvent = (Event) allPhoneEventsEnum.nextElement();
                String phoneId = phoneEvent.getString(idField, 0);
                if (phoneIdToGcalId.get(phoneId) != null) {
                    removeEvent(phoneEvent);
                }
            }
        } catch (PIMException e) {
            ErrorHandler.showError("Failed to delete downloaded events", e);
        }
    }
    
    private boolean shouldDownload() throws Exception {
        try {
            return Store.getOptions().download;
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "shouldDownload", e);
        }
    }
    
    public static int[][] getSupportedRecurrenceFields() {
        if(supportedRecurrenceFields == null) {
            try {
                PIM pim = PIM.getInstance();
                EventList phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
                
                supportedRecurrenceFields = new int[4][];
                
                supportedRecurrenceFields[0] = phoneEventList.getSupportedRepeatRuleFields(RepeatRule.DAILY);
                supportedRecurrenceFields[1] = phoneEventList.getSupportedRepeatRuleFields(RepeatRule.WEEKLY);
                supportedRecurrenceFields[2] = phoneEventList.getSupportedRepeatRuleFields(RepeatRule.MONTHLY);
                supportedRecurrenceFields[3] = phoneEventList.getSupportedRepeatRuleFields(RepeatRule.YEARLY);
                
            } catch (Exception e) {}
        }
        
        return supportedRecurrenceFields;
    }
    
}
