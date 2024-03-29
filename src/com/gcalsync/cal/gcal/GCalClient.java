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
/*
 *
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author: batcage $
 * @author Agustin Rivero
 * @version $Rev: 39 $
 * @date $Date: 2007-12-07 $
 * * Changes:
 *  --Agustin Rivero
 *      -new properties: uid, phoneCalId, eventIsToBeUpdated and autoSyncCantidate
 *      -asXML: fixed the way the date is printed as to support correctly all day events at google
 *      -toString: prints [UPDATE] before the event name if the event was updated
 *
 *
 * * * Changes:
 *  --/nov/2007 Agustin 
 *      -updateEvent(GCalEvent gCalEvent): now works using the edit link
 *      -uploadEvent(GCalEvent gCalEvent, String method, String url): url parameter added
 */
package com.gcalsync.cal.gcal;
import com.gcalsync.util.*;
import com.gcalsync.log.*;
import com.gcalsync.option.Options;
import com.gcalsync.store.Store;

import javax.microedition.io.HttpConnection;
import javax.microedition.io.HttpsConnection;
import javax.microedition.lcdui.Form;
import java.io.IOException;
import java.util.Vector;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author: thomasold $
 * @version $Rev: 20 $
 * @date $Date: 2006-12-22 04:07:50 -0500 (Fri, 22 Dec 2006) $
 */
public class GCalClient {
    
    private static final String LOGIN_URL = "https://www.google.com/accounts/ClientLogin";
    private static final String DEFAULT_CALENDAR_URL = "http://www.google.com/calendar/feeds/default/private/full";
    private static final String FEEDS_URL_PREFIX = "http://www.google.com/calendar/feeds/";
    
    String username = "";
    String password = "";
    
    public int createdCount = 0;
    public int updatedCount = 0;
    public int removedCount = 0;
    protected final GCalParser gCalParser = new GCalParser();
    Form form;
    private String authorizationHeader;
    boolean isAuthorized;
    
    boolean forceFullSync = false;
    
    public GCalClient() {
        setForm(null);
    }
    
    public GCalClient(Form form) {
        setForm(form);
    }
    
    public void setForm(Form form) {
        this.form = form;
    }
    
    public GCalEvent[] downloadEvents(long startDate, long endDate) {
        //Options options = Store.getOptions();
        String isoStartDate = DateUtil.longToIsoDate(startDate);
        String isoEndDate = DateUtil.longToIsoDate(endDate);
        if (!isAuthorized) {
            throw new IllegalStateException("Login required");
        } else {
            GCalFeed[] feeds = Store.getFeeds();

            if (feeds.length > 0) {
                // have multiple calendars that should be downloaded.
                return downloadCalendars(isoStartDate, isoEndDate, feeds);
            } else {
                // download just a single default or manually specified calendar.
                return downloadSingleCalendar(isoStartDate, isoEndDate, DEFAULT_CALENDAR_URL, true);
            }
        }
    }
    
    public GCalEvent[] downloadCalendars(String startDate, String endDate, GCalFeed[] feeds) {
        Vector allEvents = new Vector();
        boolean foundFeed = false;
        
        for (int i = 0; i < feeds.length; i++) {
            if (feeds[i].sync) {
                foundFeed = true;
                update("Downloading events from \"" + feeds[i].title + "\"...");
                downloadCalendar(startDate, endDate, feeds[i].url, feeds[i].title, feeds[i].reminders, allEvents);
            }
        }
        if (!foundFeed) {
            update("No feeds to sync");
        }
        
        update("Done downloading");
        
        return eventVectorToArray(allEvents);
    }
    
    public GCalEvent[] downloadSingleCalendar(String startDate, String endDate, String url, boolean reminders) {
        update("Downloading calendar...");
        Vector events = new Vector();
        downloadCalendar(startDate, endDate, url, null, reminders, events);
        return eventVectorToArray(events);
    }
    
    public void downloadCalendar(String startDate, String endDate, String url, String title, boolean reminders, Vector events) {
        byte[] downloadedBytes = null;
        try {
            downloadedBytes = downloadBytes(startDate, endDate, url);
            try {
                gCalParser.parseCalendar(downloadedBytes, title, reminders, events);
            } catch (Exception e) {
                ErrorHandler.showError("Failed to parse calendar (" + title + ")", e);
            }
        } catch (Exception e) {
            ErrorHandler.showError("Failed to download calendar (" + title + ")", e);
        }
    }
    
    public GCalFeed[] downloadFeeds() throws GCalException {
        try {
            byte[] feedsBytes = null;
            GCalFeed[] feeds = null;
            Options options = Store.getOptions();

            //try Google account first and if that fails, try Hosted Domain account
            update("Downloading calendar list...");
            feedsBytes = HttpUtil.sendRequest(FEEDS_URL_PREFIX + options.username + "%40gmail.com", HttpsConnection.GET, null, authorizationHeader);
            if (feedsBytes != null) {
                feeds = gCalParser.parseFeeds(feedsBytes);
            }

            if (feeds != null && feeds.length == 0){
                feedsBytes = HttpUtil.sendRequest(FEEDS_URL_PREFIX + options.username, HttpsConnection.GET, null, authorizationHeader);
                feeds = gCalParser.parseFeeds(feedsBytes);
            }

            return feeds;
        }catch(IOException e) {
            throw new GCalException(this.getClass(), "downloadFeeds", e);
        }
    }
    
    public void setCred(String usernm, String passwd) {
        username = usernm;
        password = passwd;
    }
    
    public String login(String usernm, String passwd) {
        setCred(usernm, passwd);
        return login();
    }
    
    public String login() {
        
        String rval;
        
        if (username.trim().equals("") || password.trim().equals(""))
            throw new IllegalArgumentException("Username or password is blank");
        
        isAuthorized = false;
        
        // update("Logging in...");
        String parameters = "Email=" + username + "&Passwd=" + password + "&source=Zenior-GCalSync-1&service=cl&accountType=HOSTED_OR_GOOGLE";
        String loginResponse = new String(HttpsUtil.sendRequest(LOGIN_URL, HttpsConnection.POST, parameters, null));
        int lastResponseCode = HttpsUtil.getLastResponseCode();
        if (lastResponseCode == HttpsConnection.HTTP_OK) {
            authorizationHeader = getAuthCode(loginResponse);
            isAuthorized = true;
            //no error
            rval = null;
        } else {
            rval = "ERR: (" + lastResponseCode + ") " + HttpsUtil.getLastResponseMsg();
        }
        
        return rval;
    }
    
    public boolean isAuthorized() {
        return isAuthorized;
    }
    
    public void setForceFullSync(boolean forceFullSync) {
        this.forceFullSync = forceFullSync;
    }
    
    private String getAuthCode(String response) {
        String authCode = null;
        int authPosition = response.indexOf("Auth=");
        if (authPosition > 0) {
            int newLinePosition = response.indexOf('\n', authPosition);
            if (newLinePosition > 0) {
                authCode = "GoogleLogin auth=" + response.substring(authPosition + 5, newLinePosition);
            } else {
                authCode = "GoogleLogin auth=" + response.substring(authPosition + 5);
            }
        }
        return authCode;
    }
    
    private byte[] downloadBytes(String isoStartDate, String isoEndDate, String calendarUrl) throws IOException {
        long lastSync = Store.getTimestamps().lastSync;
        String lastSyncTime;
        String parameters = "start-min=" + isoStartDate + "&start-max=" + isoEndDate;
        
        if (lastSync != 0 && !forceFullSync) {
            lastSyncTime = DateUtil.longToIsoDateTime(lastSync);
            parameters += "&updated-min=" + lastSyncTime;
        }
        
        return HttpUtil.sendRequest(calendarUrl + "?" + encode(parameters), HttpsConnection.GET, null, authorizationHeader);
    }
    
    /**
     * Encodes all colons in given string (with %3a)
     *
     * @param in string to search
     * @returns new string containing encoded colons
     */
    String encode(String in) {
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i< in.length(); i++) {
            char c = in.charAt(i);
            if (c == ':') {
                //found colon...append "%3a"
                sb.append("%3a");
            } else {
                //append any other character
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
    
    private boolean isSet(String value) {
        return (value != null) && !"".equals(value);
    }
    
    private void update(String message) {
        if (form != null) {
            form.append(message);
        }
    }
    
    public String createEvent(GCalEvent gCalEvent) throws Exception {
        try {
            String gCalId = uploadEvent(gCalEvent, "POST", DEFAULT_CALENDAR_URL);
            if (gCalId != null) {
                createdCount++;
            }
            return gCalId;
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "createEvent", e);
        }
    }
    
    public String updateEvent(GCalEvent gCalEvent) throws Exception {
        try {
            String gCalId = uploadEvent(gCalEvent, "PUT", gCalEvent.editLink);
            if (gCalId != null) {
                updatedCount++;
            }
            return gCalId;
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "updateEvent", e);
        }
        
        //note: this request fails because the uploadEvent() is posting to
        //the default calendar URL when it should be posting to the event's
        //edit URI. Google Calendar is not providing the link for some reason.
        //It might be because the authentication is not correct. But why does
        //Google allow every other action (read/write)?
        //return null;
    }
    
    public String removeEvent(GCalEvent gCalEvent) throws Exception {
        try {
            String gCalId = uploadEvent(gCalEvent, "DELETE", gCalEvent.editLink);
            if (gCalId != null) {
                removedCount++;
            }
            return gCalId;
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "removeEvent", e);
        }
    }
    
    private String uploadEvent(GCalEvent gCalEvent, String method, String url) throws Exception {
        try {
            String gCalId = null;
            int result;
            if (gCalEvent != null) {
                String eventAsXml = gCalEvent.asXML();

                update("Uploading \"" + gCalEvent.title + "\"...");

                byte[] uploadResponse = HttpUtil.sendAtomRequest(url, method, eventAsXml, authorizationHeader);
                result = HttpUtil.getLastResponseCode();
                if (result == HttpConnection.HTTP_OK || result == HttpConnection.HTTP_CREATED) update("OK");
                else update("ERR: (" + result + ") " + HttpUtil.getLastResponseMsg());

                if (!"GET".equals(method)) {
                    try {
                        gCalId = gCalParser.parseUploadResponse(uploadResponse);
                    } catch (IOException e) {
                        ErrorHandler.showError("Unexpected reply from Google Calendar, gCalEvent may not have been saved", e);
                    }
                }
            }
            return gCalId;
        }catch(Exception e) {
            throw new GCalException(this.getClass(), "uploadEvent", e);
        }
    }
    
    static public GCalEvent[] eventVectorToArray(Vector vector) {
        GCalEvent[] array = new GCalEvent[vector.size()];
        vector.copyInto(array);
        return array;
    }
    
}
