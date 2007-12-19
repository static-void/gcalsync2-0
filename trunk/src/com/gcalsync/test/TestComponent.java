/*
   Copyright 2007 *****************
 
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

package com.gcalsync.test;

import com.gcalsync.cal.Merger;
import com.gcalsync.cal.gcal.GCalEvent;
import com.gcalsync.cal.phonecal.PhoneCalClient;
import com.gcalsync.component.Components;
import com.gcalsync.component.MVCComponent;
import com.gcalsync.log.ErrorHandler;
import com.gcalsync.option.Options;
import com.gcalsync.store.Store;
import com.gcalsync.util.DateUtil;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.pim.Event;
import javax.microedition.pim.EventList;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMList;
import javax.microedition.pim.RepeatRule;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.ToDoList;

/**
 *
 * @author Agustin
 * @author Yusuf Abdi
 * @version $Rev: 1 $
 * @date $Date: 2007-12-30 03:22:30 -0500 (Sat, 30 Dec 2007) $
 */
public class TestComponent extends MVCComponent  {
    private static final Command CMD_CANCEL = new Command("Close", Command.CANCEL, 2);
    
    private static final Command CMD_PHONE_EVENTS = new Command("Phone Events", Command.ITEM, 2);
    private static final Command CMD_BB_INFO = new Command("BB Info", Command.ITEM, 2);
    private static final Command CMD_ADD_EVENT = new Command("Add Event", Command.ITEM, 2);
    
    private Form form;
    private Font labelFont = null;
    
    /** Creates a new instance of TestComponent */
    public TestComponent() {
        labelFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
    }
    
    public Displayable getDisplayable() {return this.form;}
    
    /**
     * Updates the view after it is created
     */
    protected void updateView() {}
    
    /**
     * Initializes the model before creating the view
     */
    protected void initModel() {}
    
    /**
     * Creates the view
     */
    protected void createView() {
        this.form = new Form("Test");
        this.form.addCommand(CMD_CANCEL);
        this.form.addCommand(CMD_PHONE_EVENTS);
        this.form.addCommand(CMD_BB_INFO);
        this.form.addCommand(CMD_ADD_EVENT);
        this.form.setCommandListener(this);
        //com.nokia.microedition.pim.EventImpl ei;
        
        addInfo("Test screen  ", "v9");
    }
    
    /**
     * Processes menu commands
     *
     * @param c command to execute
     * @param displayable the form from which <code>command</code>
     *                    originates
     */
    public void commandAction(Command c, Displayable displayable) {
        try {
            if (c.getCommandType() == Command.CANCEL) {
                Components.login.showScreen();
            } else if(c == CMD_PHONE_EVENTS) {
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        phoneEvents();
                    }
                });
                t.start();
            } else if(c == CMD_BB_INFO) {
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        bbInfo();
                    }
                });
                t.start();
            } else if(c == CMD_ADD_EVENT) {
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        addTestEvent();
                    }
                });
                t.start();
            }
        }catch(Throwable t) {
            ErrorHandler.showError(t);
        }
    }
    
    private void phoneEvents() {
        try {
            this.form.deleteAll();
            
            //PhoneCalClient phoneCalClient = new PhoneCalClient();
            
            Options options = Store.getOptions();
            long now = System.currentTimeMillis();
            String startDate = DateUtil.longToIsoDate(now, 0 - options.pastDays);
            String endDate = DateUtil.longToIsoDate(now, options.futureDays);
            long startDateLong = DateUtil.isoDateToLong(startDate);
            long endDateLong = DateUtil.isoDateToLong(endDate) + DateUtil.DAY; // want all events starting before midnigth on endDate
            
           /* printCalendarList(null, startDateLong, endDateLong);
            
            PIM pim = PIM.getInstance();
            String[] ss = pim.listPIMLists(PIM.EVENT_LIST);
            for(int i = 0; i < ss.length; i++) {
                try {
                    printCalendarList(ss[i], startDateLong, endDateLong);
                }catch(Exception e) {
                    ErrorHandler.showError(e);
                }
            }*/
            
            printCalendarList("Entries", startDateLong, endDateLong);
            
            //  this.form.append("\n **TODO**\n");
            //  printTodoData(startDateLong, endDateLong);
            
        }catch(Throwable e) {
            ErrorHandler.showError(e);
        }
    }
    
    private void printCalendarList(String listName, long startDate, long endDate) throws Exception {
        PIM pim = PIM.getInstance();
        
        EventList phoneEventList = null;
        if(listName == null) {
            phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
            this.form.append("\nNO LIST NAME\n");
        } else {
            phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE, listName);
            this.form.append("\nLIST: " + listName + "\n");
        }
        Enumeration phoneEvents =  phoneEventList.items(EventList.OCCURRING, startDate, endDate, false);
        
        //Enumeration phoneEvents = phoneCalClient.getPhoneEvents(startDateLong, endDateLong);
        
        Hashtable phoneEventsByGcalId = new Hashtable();
        PhoneCalClient phoneCalClient = new PhoneCalClient();
        Merger merger = new Merger(phoneCalClient, null);
        
        //enumerate all GCal events in the phone's calendar
        while (phoneEvents.hasMoreElements()) {
            try {
                Event phoneEvent = (Event)phoneEvents.nextElement();
                // String gcalId = phoneCalClient.getGCalId(phoneEvent);
                //  if (gcalId != null) phoneEventsByGcalId.put(gcalId, phoneEvent);
                
                GCalEvent gCalEvent = merger.copyToGCalEvent(phoneEvent);
                
                String extraInfo =  " S" + DateUtil.longToDateTimeGMT(gCalEvent.startTime) + " E" + DateUtil.longToDateTimeGMT(gCalEvent.endTime) +
                        " S2" + DateUtil.longToDateTime(gCalEvent.startTime) + " E2" + DateUtil.longToDateTime(gCalEvent.endTime);
                
                String[] ss = phoneEvent.getCategories();
                for(int i = 0; i < ss.length; i++) {
                    extraInfo += "\n" + ss[i];
                }
                if(ss.length == 0) {
                    extraInfo += " NOC";
                }
                extraInfo += " PAL" + gCalEvent.isPlatformAllday;
                
                this.form.append(gCalEvent.toString() + extraInfo + "\n");
                
                
                //String info = "name=" + phoneEvent.getString(Event.SUMMARY, 0);
                // this.form.append(info + "\n" + gCalEvent.toString() + "\n");
                
            }catch(Exception e) {
                ErrorHandler.showError("printCalendarList_while", e);
            }
        }
        
        phoneEventList.close();
        //phoneCalClient.close();
    }
    
    private void printTodoData(long startDate, long endDate) {
        try {
            PIM pim = PIM.getInstance();
            String[] ss = pim.listPIMLists(PIM.TODO_LIST);
            for(int i = 0; i < ss.length; i++) {
                try {
                    printTodoData(ss[i], startDate, endDate);
                }catch(Exception e) {
                    ErrorHandler.showError(e);
                }
            }
            
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
    }
    
    private void printTodoData(String listName, long startDate, long endDate) throws Exception {
        PIM pim = PIM.getInstance();
        
        ToDoList phoneEventList = null;
        if(listName == null) {
            phoneEventList = (ToDoList) pim.openPIMList(PIM.TODO_LIST, PIM.READ_WRITE);
            this.form.append("\nNO LIST NAME\n");
        } else {
            phoneEventList = (ToDoList) pim.openPIMList(PIM.TODO_LIST, PIM.READ_WRITE, listName);
            this.form.append("\nLIST: " + listName + "\n");
        }
        Enumeration phoneEvents =  phoneEventList.items(ToDo.DUE, startDate, endDate);
        
        //Enumeration phoneEvents = phoneCalClient.getPhoneEvents(startDateLong, endDateLong);
        
       /* Hashtable phoneEventsByGcalId = new Hashtable();
        PhoneCalClient phoneCalClient = new PhoneCalClient();
        Merger merger = new Merger(phoneCalClient, null);*/
        
        //enumerate all GCal events in the phone's calendar
        while (phoneEvents.hasMoreElements()) {
            try {
                ToDo phoneEvent = (ToDo)phoneEvents.nextElement();
                
                String info =
                        "name=" + phoneEvent.getString(ToDo.SUMMARY, 0);
                
                this.form.append(info + "\n");
                
            }catch(Exception e) {
                ErrorHandler.showError("printCalendarList_while", e);
            }
        }
        phoneEventList.close();
    }
    
    private void bbInfo() {
        try {
            this.form.deleteAll();
            
            int allday = 20000928;//net.rim.blackberry.api.pdap.BlackBerryEvent.ALLDAY;
            addInfo("ALLDAY: ", Integer.toString(allday) + " " + (allday == 20000928 ? "OK" : "NOT 20000928"));
            
            PIM pim = PIM.getInstance();
            EventList phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
           /* addInfo("EventList: ", phoneEventList.getClass().toString());
            
            addInfo("ALLDAY supported?: ", "" + phoneEventList.isSupportedField(allday));
            
            String[] cs = phoneEventList.getCategories();
            for(int i = 0; i < cs.length; i++) {
                addInfo("\ncategory", cs[i]);
            }
            
            addInfo("\nSUMMARY", Integer.toString(Event.SUMMARY));
            addInfo("\nSTART", Integer.toString(Event.START));
            addInfo("\nEND", Integer.toString(Event.END));
            addInfo("\nNOTE", Integer.toString(Event.NOTE));
            addInfo("\nLOCATION", Integer.toString(Event.LOCATION));
            addInfo("\nALARM", Integer.toString(Event.ALARM));*/
            
            String s =
                    "COUNT= " + Integer.toString(RepeatRule.COUNT) +
                    " INTERVAL= " + Integer.toString(RepeatRule.INTERVAL) +
                    " END= " + Integer.toString(RepeatRule.END) +
                    " MONTH_IN_YEAR= " + Integer.toString(RepeatRule.MONTH_IN_YEAR) +
                    " DAY_IN_WEEK= " + Integer.toString(RepeatRule.DAY_IN_WEEK) +
                    " WEEK_IN_MONTH= " + Integer.toString(RepeatRule.WEEK_IN_MONTH) +
                    " DAY_IN_MONTH= " + Integer.toString(RepeatRule.DAY_IN_MONTH) +
                    " DAY_IN_YEAR= " + Integer.toString(RepeatRule.DAY_IN_YEAR) +
                    " DAILY= " + Integer.toString(RepeatRule.DAILY) +
                    " WEEKLY= " + Integer.toString(RepeatRule.WEEKLY) +
                    " MONTHLY= " + Integer.toString(RepeatRule.MONTHLY) +
                    " YEARLY= " + Integer.toString(RepeatRule.YEARLY);
            addInfo("\nVALUES", s);
            
            s = "";
            int[] is = phoneEventList.getSupportedRepeatRuleFields(RepeatRule.DAILY);
            for(int i = 0; i < is.length; i++) {
                s += (s.length() == 0 ? "" : ", ") + Integer.toString(is[i]);
            }
            addInfo("\nDAILY", s);
            
            s = "";
            is = phoneEventList.getSupportedRepeatRuleFields(RepeatRule.WEEKLY);
            for(int i = 0; i < is.length; i++) {
                s += (s.length() == 0 ? "" : ", ") + Integer.toString(is[i]);
            }
            addInfo("\nWEEKLY", s);
            
            s = "";
            is = phoneEventList.getSupportedRepeatRuleFields(RepeatRule.MONTHLY);
            for(int i = 0; i < is.length; i++) {
                s += (s.length() == 0 ? "" : ", ") + Integer.toString(is[i]);
            }
            addInfo("\nMONTHLY", s);
            
            s = "";
            is = phoneEventList.getSupportedRepeatRuleFields(RepeatRule.YEARLY);
            for(int i = 0; i < is.length; i++) {
                s += (s.length() == 0 ? "" : ", ") + Integer.toString(is[i]);
            }
            addInfo("\nYEARLY", s);
            
            
            phoneEventList.close();
            
            //Integer.parseInt("+010");
           /* Integer.parseInt("-010");
            System.out.println("2: " + DateUtil.parseSignedInt("-0100"));
            System.out.println("3: " + DateUtil.parseSignedInt("+0100"));
            System.out.println("4: " + DateUtil.parseSignedInt("-0000"));
            System.out.println("4: " + DateUtil.parseSignedInt("+1"));
            System.out.println("4: " + DateUtil.parseSignedInt("8"));*/
            
            //PIM pim = PIM.getInstance();
            /*String[] ss = pim.listPIMLists(PIM.TODO_LIST);
            String all = "";
            for(int i = 0; i < ss.length; i++) {
                all += (all.length() == 0 ? "" : ", ") + ss[i];
            }
            addInfo("\nlistPIMLists", all);*/
            
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
    }
    
    private void addTestEvent() {
        try {
            this.form.deleteAll();
            
            PIM pim = PIM.getInstance();
            //EventList phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE, "cat_test");
            
            /*
             
            GCalEvent gCalEvent = new GCalEvent();*/
            /*gCalEvent.title = "j2me event(nokia)";
            gCalEvent.startTime = DateUtil.dateToLong("20071214");
            gCalEvent.endTime = DateUtil.dateToLong("20071215");*/
            /*
            gCalEvent.title = "ad_1";
            gCalEvent.startTime = DateUtil.isoDateToLong("2007-12-15T00:00:00");
            gCalEvent.endTime = 0;//DateUtil.isoDateToLong("2007-12-14T17:00:00");
             */
            
            EventList phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
            
            //PhoneCalClient phoneCalClient = new PhoneCalClient();
            Event pe = phoneEventList.createEvent();
            
            pe.addDate(Event.START, Event.ATTR_NONE, DateUtil.isoDateToLong("2007-12-15T10:00:00"));
            pe.addDate(Event.END, Event.ATTR_NONE, DateUtil.isoDateToLong("2007-12-15T11:00:00"));
            
            pe.addString(Event.SUMMARY, Event.ATTR_NONE, "test_rec");
            
            
            RepeatRule rr = new RepeatRule();
            
            rr.setInt(RepeatRule.FREQUENCY, RepeatRule.MONTHLY);
            rr.setInt(RepeatRule.WEEK_IN_MONTH, RepeatRule.THIRD);
            rr.setInt(RepeatRule.DAY_IN_WEEK, RepeatRule.SATURDAY);
            rr.setDate(RepeatRule.END, DateUtil.isoDateToLong("2008-08-15T11:00:00"));
            
            addInfo("", "**UNO**");
            printRepeatRule(rr);
            
            pe.setRepeat(rr);
            
            addInfo("", "**DOS**");
            printRepeatRule(rr);
            
            addInfo("", "**TRES**");
            printRepeatRule(pe.getRepeat());
            
            pe.commit();
            
            addInfo("", "**CUATRO**");
            printRepeatRule(pe.getRepeat());

            phoneEventList.close();
            
            /*
             
            Merger merger = new Merger(phoneCalClient, null);
            Event phoneEvent = merger.copyToPhoneEvent(gCalEvent);*/
            
           /* int[] fields = phoneEvent.getFields();
            for(int i = 0; i < fields.length; i++) {
                addInfo("", "\n" + i + " = " + fields[i]);
            }*/
            
            /*for(int i = com.nokia.microedition.pim.EventImpl.EXTENDED_FIELD_MIN_VALUE-10000; i < com.nokia.microedition.pim.EventImpl.EXTENDED_FIELD_MIN_VALUE + 99999; i++) {
             
                if(phoneEvent.getPIMList().isSupportedField(i)) {
                    addInfo("", "\nsupported: " + i);
                }
            }*/
            
            //phoneEvent.addToCategory("cat_test");
            /*
            phoneCalClient.insertEvent(phoneEvent, "1234567891");
             
             
             
            gCalEvent = new GCalEvent();
            gCalEvent.title = "ad_2";
            gCalEvent.startTime = 0;
            gCalEvent.endTime = DateUtil.isoDateToLong("2007-12-15T00:00:00");//DateUtil.isoDateToLong("2007-12-14T17:00:00");
            phoneEvent = merger.copyToPhoneEvent(gCalEvent);
            phoneCalClient.insertEvent(phoneEvent, "1234567892");
             */
            //phoneCalClient.close();
            
            addInfo("", "added correctly");
            
        }catch(Exception e) {
            e.printStackTrace();
            ErrorHandler.showError(e);
        }
    }
    
    private void printRepeatRule(RepeatRule rr) {
        
        int[] setFields = rr.getFields();
        for(int i = 0; i < setFields.length; i++) {
            if(setFields[i] == RepeatRule.END) {
                addInfo(Integer.toString(setFields[i]), Long.toString(rr.getDate(setFields[i])));
            } else {
                addInfo(Integer.toString(setFields[i]), Integer.toString(rr.getInt(setFields[i])));
            }
        }
    }
    
    private void addInfo(String title, String info) {
        StringItem lblInfo = new StringItem(title , null);
        lblInfo.setText(info);
        lblInfo.setFont(labelFont);
        this.form.append(lblInfo);
    }
    
}
