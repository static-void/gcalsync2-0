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
        
        addInfo("Test screen  ", "v7");
    }
    
    /**
     * Processes menu commands
     *
     * @param c command to execute
     * @param displayable the form from which <code>command</code>
     *                    originates
     */
    public void commandAction(Command c, Displayable displayable) {
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
    }
    
    private void phoneEvents() {
        try {
            this.form.deleteAll();
            
            PhoneCalClient phoneCalClient = new PhoneCalClient();
            
            Options options = Store.getOptions();
            long now = System.currentTimeMillis();
            String startDate = DateUtil.longToIsoDate(now, 0 - options.pastDays);
            String endDate = DateUtil.longToIsoDate(now, options.futureDays);
            long startDateLong = DateUtil.isoDateToLong(startDate);
            long endDateLong = DateUtil.isoDateToLong(endDate) + DateUtil.DAY; // want all events starting before midnigth on endDate
            Enumeration phoneEvents = phoneCalClient.getPhoneEvents(startDateLong, endDateLong);
            Hashtable phoneEventsByGcalId = new Hashtable();
            Merger merger = new Merger(phoneCalClient, null);
            
            //enumerate all GCal events in the phone's calendar
            while (phoneEvents.hasMoreElements()) {
                Event phoneEvent = (Event)phoneEvents.nextElement();
                String gcalId = phoneCalClient.getGCalId(phoneEvent);
                if (gcalId != null) phoneEventsByGcalId.put(gcalId, phoneEvent);
                
                GCalEvent gCalEvent = merger.copyToGCalEvent(phoneEvent);
                
                String extraInfo = " S" + DateUtil.longToDateTimeGMT(gCalEvent.startTime) + " E" + DateUtil.longToDateTimeGMT(gCalEvent.endTime) +
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
            }
            
            phoneCalClient.close();
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
        
        
    }
    
    private void bbInfo() {
        try {
            this.form.deleteAll();
            
            int allday = 20000928;//net.rim.blackberry.api.pdap.BlackBerryEvent.ALLDAY;
            addInfo("ALLDAY: ", Integer.toString(allday) + " " + (allday == 20000928 ? "OK" : "NOT 20000928"));
          
            PIM pim = PIM.getInstance();
            EventList phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
            addInfo("EventList: ", phoneEventList.getClass().toString());
            
            addInfo("ALLDAY supported?: ", "" + phoneEventList.isSupportedField(allday));
            
            String[] cs = phoneEventList.getCategories();
            for(int i = 0; i < cs.length; i++) {
                addInfo("\ncategory", cs[i]);
            }
            
            //Integer.parseInt("+010");
            Integer.parseInt("-010");
            System.out.println("2: " + DateUtil.parseSignedInt("-0100"));
            System.out.println("3: " + DateUtil.parseSignedInt("+0100"));
            System.out.println("4: " + DateUtil.parseSignedInt("-0000"));
            System.out.println("4: " + DateUtil.parseSignedInt("+1"));
            System.out.println("4: " + DateUtil.parseSignedInt("8"));
            
            //PIM pim = PIM.getInstance();
            String[] ss = pim.listPIMLists(PIM.EVENT_LIST);
            String all = "";
            for(int i = 0; i < ss.length; i++) {
                all += (all.length() == 0 ? "" : ", ") + ss[i];
            }
            addInfo("\nlistPIMLists", all);
            
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
    }
    
    private void addTestEvent() {
        try {
            this.form.deleteAll();
            
            PIM pim = PIM.getInstance();
            EventList phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE, "cat_test");
            
            
            
            GCalEvent gCalEvent = new GCalEvent();
            /*gCalEvent.title = "j2me event(nokia)";
            gCalEvent.startTime = DateUtil.dateToLong("20071214");
            gCalEvent.endTime = DateUtil.dateToLong("20071215");*/
            
            gCalEvent.title = "cat_event";
            gCalEvent.startTime = DateUtil.isoDateToLong("2007-12-14T16:00:00");
            gCalEvent.endTime = DateUtil.isoDateToLong("2007-12-14T17:00:00");
            
            PhoneCalClient phoneCalClient = new PhoneCalClient();
            
            Merger merger = new Merger(phoneCalClient, null);
            Event phoneEvent = merger.copyToPhoneEvent(gCalEvent);
            
            int[] fields = phoneEvent.getFields();
            for(int i = 0; i < fields.length; i++) {
                addInfo("", "\n" + i + " = " + fields[i]);
            }
            
            /*for(int i = com.nokia.microedition.pim.EventImpl.EXTENDED_FIELD_MIN_VALUE-10000; i < com.nokia.microedition.pim.EventImpl.EXTENDED_FIELD_MIN_VALUE + 99999; i++) {
                
                if(phoneEvent.getPIMList().isSupportedField(i)) {
                    addInfo("", "\nsupported: " + i);
                }
            }*/
            
            //phoneEvent.addToCategory("cat_test");
            
            phoneCalClient.insertEvent(phoneEvent, "1234567890");
            phoneCalClient.close();
            
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
    }
    
    private void addInfo(String title, String info) {
        StringItem lblInfo = new StringItem(title , null);
        lblInfo.setText(info);
        lblInfo.setFont(labelFont);
        this.form.append(lblInfo);
    }
    
}
