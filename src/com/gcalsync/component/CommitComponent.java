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
 * * Changes:
 *  --/nov/2007 Agustin 
 *      -processUploadEvents: saves the UID of the event at google in the correlation map
 */
package com.gcalsync.component;


import javax.microedition.lcdui.*;
import com.gcalsync.cal.gcal.GCalClient;
import com.gcalsync.cal.gcal.GCalEvent;
import com.gcalsync.log.*;
import com.gcalsync.cal.*;

/**
 * Public class responsible for drawing the Commit screen and displaying the 
 * status of uploads/downloads.
 * <p>
 * <ol>
 * <li>The Preview screen displays events that are available for upload/download.</li> 
 * <li>The user selects "Commit" from the Preview screen to write events to the 
 * device or to the calendar.</li> 
 * <li>The application draws the Commit screen in response to the user selecting "Commit".</li>
 * </ol>
 * 
 * <p> 
 *
 * This class is also responsible for displaying the results of download/upload events, 
 * initiating upload/download events, and instantiation of a new instance of the 
 * CommitEngine class that handles the download/upload of events. 
 * <p>
 * This class uses the MVCComponent base class to retrieve, draw, and update screens.<br>
 * 
 *  
 * @author
 * @see <code>CommitEngine<code/> for code in this class that was commented out and moved 
 *                                           to com.gcalsync.cal.CommitEngine. 
 */
public class CommitComponent extends MVCComponent implements Runnable, StatusLogger {
    Form form;
    GCalClient gCalClient;
    //PhoneCalClient phoneCalClient;
    GCalEvent[] uploads;
    GCalEvent[] downloads;
    
    /**
     * Constructor
     */
    public CommitComponent(GCalClient gcal) {
        gCalClient = gcal;
    }
    
    /**
     * Gets the <code>Displayable</code> object to be displayed for
     * this component
     *
     * @returns <code>Displayable</code>
     */
    public Displayable getDisplayable() {return this.form;}
    
    /**
     * Initializes the model before creating the view
     */
    protected void initModel() {}
    
    /**
     * Updates the view after it is created
     */
    protected void updateView() {}
    
    /**
     * Creates the view
     */
    protected void createView() {
        this.form = new Form("Commit");
        this.form.addCommand(new Command("OK", Command.EXIT, 2));
    }
    
    /**
     * Appends a message to the current form <br>
     * Displays the statistics (results) of the uploads and 
     * downloads that occurred during the commit event 
     *
     * @param message (results of commit event)to be displayed
     */
    public void update(String message) {
        form.append(message + "\n");
    }
    
    /**
     * Replaces the last message on the form with a new message
     *
     * @param message to be displayed
     */
    public void updateMinor(String message) {
        form.delete(form.size() - 1);
        form.append(message + "\n");
    }
    
    /**
     * Processes menu commands <br>
     * If the user presses the Exit button, show the login screen
     *
     * @param c command to execute
     * @param d <code>Displayable</code> from which the command
     *          originates
     */
    public void commandAction(Command c, Displayable d) {
        try {
            if (c.getCommandType() == Command.EXIT) {
                Components.login.showScreen();
            }
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
    }
    
    /**
     * Updates screen and begins processing events to be
     * downloaded/uploaded
     */
    public void handle() throws Exception {
        try {
            showScreen();
            new Thread(this).start();
        }catch(Exception e) {
            throw new GCalException(CommitComponent.class, "handle", e);
        }
    }
    
    /**
     * Entry point for new thread
     */
    public void run() {
        try {
            /*phoneCalClient = new PhoneCalClient();
            
            //update Google Calendar with any upload events
            processUploadEvents();
            
            //update phone with downloaded events
            processDownloadEvents();
            
            Store.getTimestamps().lastSync = System.currentTimeMillis();
            Store.saveTimestamps();
            */
            
            CommitEngine commitEngine = new CommitEngine();
            //<int[]> is called from the CommitEngine class that processes the statistics
            //to display on this screen
            int[] commnitStatistics = commitEngine.commitSync(this.uploads, this.downloads, this.gCalClient, this.form);
            
            this.form.append(new Spacer(getDisplayable().getWidth(), 20));
            //creates the message that displays the results of commit events on the screen 
            //message displayed through the <code>update</code> method
            update("GCal:  " + gCalClient.createdCount + " new, " + gCalClient.updatedCount + " updated, " + gCalClient.removedCount + " removed events");
            update("Phone: " + commnitStatistics[0] + " new, " + commnitStatistics[1] + " updated, " + commnitStatistics[2] + " removed events");
            
            //phoneCalClient.close();
            
        } catch (Exception e) {
            ErrorHandler.showError(e.getMessage(), e);
        }
    }
    
    /**
     * Uploads events to Google Calendar
     */
    /*void processUploadEvents() {
        String gCalId;
        
        try {
            if (this.uploads != null && this.uploads.length > 0) {
                //update("Uploading " + this.uploads.length + " event" + ((this.uploads.length > 1)?"s":"") + " to Google...");
                gCalClient.setForm(this.form);
                for (int i=0; i<this.uploads.length; i++) {
                    if(this.uploads[i].eventIsToBeUpdated) {
                        gCalClient.updateEvent(this.uploads[i]);
                    } else {
                        gCalId = gCalClient.createEvent(this.uploads[i]);
                        
                        //update the phone event with the gCalId
                        if(this.uploads[i].uid == null) {
                            IdCorrelation idCorrelation = new IdCorrelation();
                            idCorrelation.phoneCalId = this.uploads[i].phoneCalId;
                            idCorrelation.gCalId = gCalId;
                            
                            Store.addCorrelation(idCorrelation);
                        }
                    }
                }
            }
        } catch (Exception e) {
            update("Failed Google update: " + e);
//#ifdef DEBUG_ERR
//# 			System.err.println("Failed Google update: " + e);
//#endif
        }
    }*/
    
    /**
     * Saves events to phone calendar
     */
   /* void processDownloadEvents() {
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
            if (this.downloads != null && this.downloads.length > 0) {
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
                
                for (int i=0; i<this.downloads.length; i++) {
                    //find the downloaded event in the phone's GCal event list.
                    //if the event doesn't exist, then <phoneEvent> is null and
                    //the event will be added to the phone's calendar
                    phoneEvent = (Event)phoneEventsByGcalId.remove(this.downloads[i].uid);
                    
                    //update/add the downloaded event
                    merger.mergeEvents(phoneEvent, this.downloads[i], this.form);
                }
            }
        } catch (Exception e) {e.printStackTrace();
            update("Failed phone update: " + e);
//#ifdef DEBUG_ERR
//# 			System.err.println("Failed phone update: " + e);
//#endif
        }
    }*/
    
    /**
     * Sets the events to be uploaded/downloaded.
     * This method copies array values from <GCalEvent[]> source arrays. 
     * The uploads and downloads arrays are called to the CommitEngine.java class
     * by the <code>processUploadEvents</code> and the <code>processDownloadEvents</code> methods 
     * to write records to the Google Calendar or to the Device.
     *
     * @param uploads is the destination array for upload events - 
     *              array values were copied from <GCalEvent[]> source array
     * @param downloads is the destination array for download events - 
     *              array values were copied from <GCalEvent[]> source array
     */
    public void setEvents(GCalEvent[] uploads, GCalEvent[] downloads) throws Exception {
        try {
            if (uploads != null) {
                this.uploads = new GCalEvent[uploads.length];
                System.arraycopy(uploads, 0, this.uploads, 0, uploads.length);
            }

            if (downloads != null) {
                this.downloads = new GCalEvent[downloads.length];
                System.arraycopy(downloads, 0, this.downloads, 0, downloads.length);
            }
        }catch(Exception e) {
            throw new GCalException("CommitComponent", "setEvents", e);
        }
    }
}



