/*
 * TestComponent.java
 *
 * Created on 5 de diciembre de 2007, 01:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
 * @author Agustin Rivero (agustin.rivero.work@gmail.com)
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
        
        addInfo("Test screen", "");
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
                
                this.form.append(gCalEvent.toString());
            }
            
            phoneCalClient.close();
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
        
        
    }
    
    private void bbInfo() {
        try {
            this.form.deleteAll();
            
            int allday = 20000928;//net.rim.blackberry.api.pim.BlackBerryEvent.ALLDAY;
            addInfo("ALLDAY: ", Integer.toString(allday) + " " + (allday == 20000928 ? "OK" : "NOT 20000928"));
            
            PIM pim = PIM.getInstance();
            EventList phoneEventList = (EventList) pim.openPIMList(PIM.EVENT_LIST, PIM.READ_WRITE);
            
            addInfo("ALLDAY supported?: ", "" + phoneEventList.isSupportedField(allday));
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
    }
    
    private void addTestEvent() {
        try {
            this.form.deleteAll();
            
            GCalEvent gCalEvent = new GCalEvent();
            gCalEvent.title = "j2me event";
            gCalEvent.startTime = DateUtil.dateToLong("20071207");
            gCalEvent.endTime = DateUtil.dateToLong("20071208");
            
            PhoneCalClient phoneCalClient = new PhoneCalClient();
            
            Merger merger = new Merger(phoneCalClient, null);
            Event phoneEvent = merger.copyToPhoneEvent(gCalEvent);
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
