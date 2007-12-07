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
 *      -equals: implemented
 *      -parse: now timezone information is parsed and used to transform start and end time to GMT time
 *      -processTZ_START: added; used in parse to parse timezone information
 *      -compareExceptDatesArray: added; used in equals
 */
package com.gcalsync.cal;

import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Vector;
import javax.microedition.pim.RepeatRule;
import harmony.java.util.StringTokenizer;
import com.gcalsync.util.*;
import com.gcalsync.store.Store;
import java.util.Calendar;

/**
 * A crude iCalendar data parser, implemented in the absence of
 * J2ME regular expression engine
 *
 * @author batcage@gmail.com
 * @version 1.0.0
 * @see iCalendar standard RFC 2455
 *      <link>http://www.ietf.org/rfc/rfc2445.txt)</link>
 * @since JDK1.6.0, WTK2.5-Beta2
 */
public class Recurrence {
    int frequency;              //type of repeat rule
    int interval;               //how often rule repeats
    int count;                  //number of occurrences
    int monthInYear;            //month(s) in the year that event occurs
    int dayInWeek;              //day(s) in the week that event occurs
    int weekInMonth;            //week(s) in month that event occurs
    int dayInMonth;             //day(s) in month that event occurs
    int dayInYear;              //day(s) in the year that event occurs
    int tzOffset;               //time zone hour offset from GMT
    long expirationDate;        //date that recurrence ends in milliseconds
    long startTime;             //event start date/time in milliseconds
    long endTime;	            //event end date/time in milliseconds
    long duration;              //event duration
    String tzCodeStd;           //standard time zone name (abbreviation) e.g. "EST"
    String tzCodeDay;           //daylight time zone name (abbreviation) e.g. "EDT"
    String tzName;              //time zone ID (full length) e.g. "America/New_York"
    RepeatRule repeatRule;      //PIM Repeat Rule
    Date[] exceptDates;         //exception dates
    
    /**
     * Constructor
     */
    public Recurrence() {
        this.exceptDates = null;
        this.tzCodeStd = null;
        this.tzCodeDay = null;
        this.tzName = null;
    }
    
    /**
     * Constructor that takes an iCal data string as a param
     *
     * @param rule repeat rule string in iCal format (RFC 2455)
     */
    public Recurrence(String rule) {
        this();
        this.parse(rule);
    }
    
    /**
     * Constructor that takes a PIM repeat rule structure as a param
     *
     * @param rule PIM repeat rule
     */
    public Recurrence(RepeatRule rule) {
        this(rule, 0, 0);
    }
    
    /**
     * Constructor that takes a PIM repeat rule structure, starting
     * and end times of the recurrent event
     *
     * @param rule PIM repeat rule
     * @param startTime starting time of recurrent event
     * @param endTime end time of recurring event
     */
    public Recurrence(RepeatRule rule, long startTime, long endTime) {
        this();
        this.startTime = startTime;
        this.endTime = endTime;
        this.repeatRule = rule;
        this.parse(rule);
    }
    
    /**
     * Gets the PIM repeat rule for this recurrent event
     *
     * @returns PIM repeat rule
     */
    public RepeatRule getRepeat() {
        return this.repeatRule;
    }
    
    /**
     * Adds an exception date to the PIM repeat rule
     *
     * @param date exception date in milliseconds since 1970 January
     *             1
     */
    public void addExceptDate(long date) {
        if (this.repeatRule != null) this.repeatRule.addExceptDate(date);
    }
    
    /**
     * Gets the recurring event's start date/time
     *
     * @returns start date/time in milliseconds since 1970 January 1
     */
    public long getStartDateTime() {
        return this.startTime;
    }
    
    /**
     * Gets the recurring event's end date/time
     *
     * @returns end date/time in milliseconds since 1970 January 1
     */
    public long getEndDateTime() {
        return this.endTime;
    }
    
    /**
     * Gets the recurring event's expiration date/time
     *
     * @returns expiration date/time in milliseconds since 1970
     *        January 1
     */
    public long getExpiration() {
        return this.expirationDate;
    }
    
    /**
     * Gets the name of the time zone at which the recurring event
     * occurs
     *
     * @returns full length name of time zone
     * @example "America/New_York"
     */
    public String getTimeZoneName() {
        return this.tzName;
    }
    
    /**
     * Sets the name of the time zone at which the recurring event
     * occurs
     *
     * @param full length name of time zone
     * @example "America/New_York"
     */
    public void setTimeZoneName(String name) {
        this.tzName = name;
    }
    
    /**
     * Parses all assigned fields, including frequency, interval, and
     * exception dates, from specified PIM repeat rule
     *
     * @param PIM repeat rule to parse
     * @throws IllegalArgumentException if <code>rule</code> is
     *                                  <code>null</code>
     */
    public void parse(RepeatRule rule) {
        int[] fields;
        
        if (rule != null) {
            //get all assigned fields
            fields = rule.getFields();
            
            for (int i=0; i<fields.length; i++) {
                try {
                    switch (fields[i]) {
                        case RepeatRule.FREQUENCY:
                            this.frequency = rule.getInt(RepeatRule.FREQUENCY);
                            break;
                            
                        case RepeatRule.INTERVAL:
                            this.interval = rule.getInt(RepeatRule.INTERVAL);
                            break;
                            
                        case RepeatRule.COUNT:
                            this.count = rule.getInt(RepeatRule.COUNT);
                            break;
                            
                        case RepeatRule.END:
                            this.expirationDate = rule.getDate(RepeatRule.END);
                            break;
                            
                        case RepeatRule.MONTH_IN_YEAR:
                            this.monthInYear = rule.getInt(RepeatRule.MONTH_IN_YEAR);
                            break;
                            
                        case RepeatRule.DAY_IN_WEEK:
                            this.dayInWeek = rule.getInt(RepeatRule.DAY_IN_WEEK);
                            break;
                            
                        case RepeatRule.WEEK_IN_MONTH:
                            this.weekInMonth = rule.getInt(RepeatRule.WEEK_IN_MONTH);
                            break;
                            
                        case RepeatRule.DAY_IN_MONTH:
                            this.dayInMonth = rule.getInt(RepeatRule.DAY_IN_MONTH);
                            break;
                            
                        case RepeatRule.DAY_IN_YEAR:
                            this.dayInYear = rule.getInt(RepeatRule.DAY_IN_YEAR);
                            break;
                            
                        default: break;
                    }
                } catch (Exception e) { }
            }
            
            try {
                //get exception dates
                Enumeration exDates = rule.getExceptDates();
                
                if (exDates.hasMoreElements()) {
                    Vector exDatesVector = new Vector();
                    while (exDates.hasMoreElements()) exDatesVector.addElement(exDates.nextElement());
                    exceptDates = new Date[exDatesVector.size()];
                    exDatesVector.copyInto(exceptDates);
                }
            } catch (Exception e){};
        } else
            throw new IllegalArgumentException("null RepeatRule");
    }
    
    /**
     * Parses specified iCal repeat rule string
     *
     * @param rule repeat rule in iCal format (RFC 2455)
     * @throws IllegalArgumentException if <code>rule</code> is
     *                                  <code>null</code>
     */
    public void parse(String rule) {
        if (rule == null) throw new IllegalArgumentException("null RepeatRule");

        //break string up by newlines or spaces, each line is a field of the rule
        StringTokenizer fields = new StringTokenizer(rule, "\n ");
        StringTokenizer subFields;
        String field;
        boolean stdRule = false;
        boolean dayRule = false;
        int i = 0;
        
        Calendar stdTimezoneStart = null;
        Calendar dayTimezoneStart = null;
        int stdTimezoneOffset = 0;
        int dayTimezoneOffset = 0;

        while (fields.hasMoreTokens()) {
            //break each field into subfields if possible
            field = fields.nextToken();
            subFields = new StringTokenizer(field, ";:");

            //process the main fields
            if (field.startsWith("DTSTART")) {
                if (stdRule == false && dayRule == false)
                    processDTSTART(field, subFields);
                
                //get time zones start time
                if (stdRule) stdTimezoneStart = processTZ_START(field, subFields);
                if (dayRule) dayTimezoneStart = processTZ_START(field, subFields);
            } else if (field.startsWith("DTEND")) {
                if (stdRule == false && dayRule == false)
                    processDTEND(field, subFields);
            } else if (field.startsWith("DURATION:")) {
                processDURATION(field, subFields);
            } else if (field.startsWith("RRULE:")) {
                //do not process the repeat rules from the
                // time zone rules
                if (stdRule == false && dayRule == false)
                    processRRULE(field, subFields);
            } else if (field.startsWith("TZNAME:")) {
                if (stdRule || dayRule)
                    processTZNAME(field, subFields, stdRule);
            } else if (field.startsWith("TZOFFSETTO:")) {
                //get time zone offsets
                if (stdRule) stdTimezoneOffset = processTZOFFSET(field, subFields);
                if (dayRule) dayTimezoneOffset = processTZOFFSET(field, subFields);
            }else if (field.startsWith("BEGIN:STANDARD")) {
                stdRule = true;
                dayRule = false;
            } else if (field.startsWith("BEGIN:DAYLIGHT")) {
                stdRule = false;
                dayRule = true;
            } else if (field.startsWith("END:STANDARD")) {
                stdRule = false;
            } else if (field.startsWith("END:DAYLIGHT")) {
                dayRule = false;
            }
            //else ignore other fields since we don't need them
        }
        
        //get the right time offset depending on if we are in standard time or daylight saving time
        this.tzOffset = stdTimezoneOffset;
        
        //try to identify if we are in daylight saving time
        if(stdTimezoneStart != null && dayTimezoneStart != null) {
            Calendar startCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            startCalendar.setTime(new Date(this.startTime));
            startCalendar.set(Calendar.YEAR, 1970);
            if(stdTimezoneStart.before(dayTimezoneStart) ) {
                if(startCalendar.before(stdTimezoneStart) || startCalendar.after(dayTimezoneStart)) {
                    this.tzOffset = dayTimezoneOffset;
                }
            } else {
                if(startCalendar.after(dayTimezoneStart) && startCalendar.before(stdTimezoneStart)) {
                    this.tzOffset = dayTimezoneOffset;
                }
            }
            
        }
        
        //fix the start time and end time to be in GMT time
        this.startTime -= this.tzOffset;
        this.endTime -= this.tzOffset;
        
        this.repeatRule.setDate(RepeatRule.END, this.expirationDate);
    }
    
    /**
     * Gets the iCal representation of the repeat rule if populated
     *
     * @returns iCal repeat rule string (RFC 2455). String is empty
     *        if repeat rule is not populated.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String rval = "";
        
        try {
            //rule must always start with "DTSTART;"
            getDTSTART(sb, false);//getDTSTART(sb, true);
            
            //DTEND or DURATION required
            getDTEND(sb, false);//getDTEND(sb, true);
            
            //RRULE required
            getRRULE(sb);
            
            //EXDATE required only if exception dates exist
            getEXDATE(sb);
            
            //VTIMEZONE required
            getVTIMEZONE(sb);
            
            rval = sb.toString().replace(' ', '\n');
        } catch (Exception e) {}
        
        return rval;
    }
    
    /**
     * Gets a readable string for the repeat rule, including
     * frequency and exception dates
     *
     * @returns repeat rule in readable format. String is empty if
     *        repeat rule is not populated.
     */
    public String toReadableString() {
        StringBuffer sb = new StringBuffer();
        
        getReadableRule(sb);
        getReadableExceptions(sb);
        
        return sb.toString();
    }
    
    /**
     * Appends specified DateTime value to the given string buffer
     *
     * @param sb string buffer to which the DateTime value is
     *           appended
     * @param includeTZID if true, includes time zone ID
     * @param startEnd if "DTSTART", gets the start time of the
     *                 repeat rule; if "DTEND", gets the end time
     */
    void getDT(StringBuffer sb, boolean includeTZID, String startEnd) {
        long time;
        
        time = startEnd.equals("DTSTART") ? this.startTime : this.endTime;
        
        sb.append(startEnd);
        
        if (time > 0) {
            //include option time zone ID
            if (this.tzName != null && includeTZID) sb.append(";TZID=" + this.tzName);
            else sb.append(";VALUE=DATE-TIME");
            
            //append date time string
            sb.append(":" + DateUtil.longToDateTimeGMT(time) + "\n");
        } else {
            sb.append(";VALUE=DATE:" + DateUtil.longToDateGMT(time) + "\n");
        }
    }
    
    /**
     * Appends start time of repeat rule to the given string buffer
     *
     * @param sb string buffer to which start time is appended
     * @param includeTZID if true, includes time zone ID
     */
    void getDTSTART(StringBuffer sb, boolean includeTZID) {
        getDT(sb, includeTZID, "DTSTART");
    }
    
    /**
     * Appends end time of repeat rule to the given string buffer
     *
     * @param sb string buffer to which end time is appended
     * @param includeTZID if true, includes time zone ID
     */
    void getDTEND(StringBuffer sb, boolean includeTZID) {
        getDT(sb, includeTZID, "DTEND");
    }
    
    /**
     * Appends event duration of repeat rule to the given string
     * buffer
     *
     * @param sb string buffer to which duration is appended
     */
    void getDURATION(StringBuffer sb) {
        //get event duration in seconds
        long time = this.endTime - this.startTime;
        time /= 1000;
        sb.append("DURATION:PT" + time + "S\n");
    }
    
    /**
     * Appends repeat rule info, including frequency and interval, to
     * the given string buffer
     *
     * @param sb string buffer to which rule is appended
     */
    void getRRULE(StringBuffer sb) {
        if (this.frequency != 0) {
            sb.append("RRULE:FREQ=");
            
            //FREQ
            switch (this.frequency) {
                case RepeatRule.YEARLY: sb.append("YEARLY"); break;
                case RepeatRule.MONTHLY: sb.append("MONTHLY"); break;
                case RepeatRule.WEEKLY: sb.append("WEEKLY"); break;
                case RepeatRule.DAILY: sb.append("DAILY"); break;
                default: break;
            }
            
            //INTERVAL
            if (this.interval != 0) sb.append(";INTERVAL=" + this.interval);
            
            //COUNT
            if (this.count != 0) sb.append(";COUNT=" + this.count);
            
            //BYDAY
            if (this.dayInWeek != 0) {
                Vector dayVector = new Vector();
                
                sb.append(";BYDAY=");
                
                //get list of days into vector
                if ((this.dayInWeek & RepeatRule.SUNDAY)!=0) dayVector.addElement("SU");
                if ((this.dayInWeek & RepeatRule.MONDAY)!=0) dayVector.addElement("MO");
                if ((this.dayInWeek & RepeatRule.TUESDAY)!=0) dayVector.addElement("TU");
                if ((this.dayInWeek & RepeatRule.WEDNESDAY)!=0) dayVector.addElement("WE");
                if ((this.dayInWeek & RepeatRule.THURSDAY)!=0) dayVector.addElement("TH");
                if ((this.dayInWeek & RepeatRule.FRIDAY)!=0) dayVector.addElement("FR");
                if ((this.dayInWeek & RepeatRule.SATURDAY)!=0) dayVector.addElement("SA");
                
                //assemble vector elements into comma separated list
                for (int i=0; i<dayVector.size(); i++) {
                    if (i != 0) sb.append(",");
                    sb.append((String)dayVector.elementAt(i));
                }
            }
            
            //BYWEEK
            if (this.weekInMonth != 0) {
                sb.append(";BYWEEK=");
                
                switch (this.weekInMonth) {
                    case RepeatRule.FIRST: sb.append("1"); break;
                    case RepeatRule.SECOND: sb.append("2"); break;
                    case RepeatRule.THIRD: sb.append("3"); break;
                    case RepeatRule.FOURTH: sb.append("4"); break;
                    case RepeatRule.FIFTH: sb.append("5"); break;
                    case RepeatRule.SECONDLAST: sb.append("-1"); break;
                    case RepeatRule.THIRDLAST: sb.append("-2"); break;
                    case RepeatRule.FOURTHLAST: sb.append("-3"); break;
                    case RepeatRule.FIFTHLAST: sb.append("-4"); break;
                    default: break;
                }
            }
            
            //BYMONTH
            if (this.monthInYear != 0) {
                Vector moVector = new Vector();
                
                sb.append(";BYMONTH=");
                
                //get list of months into vector
                if ((this.monthInYear & RepeatRule.JANUARY)!=0) moVector.addElement("1");
                if ((this.monthInYear & RepeatRule.FEBRUARY)!=0) moVector.addElement("2");
                if ((this.monthInYear & RepeatRule.MARCH)!=0) moVector.addElement("3");
                if ((this.monthInYear & RepeatRule.APRIL)!=0) moVector.addElement("4");
                if ((this.monthInYear & RepeatRule.MAY)!=0) moVector.addElement("5");
                if ((this.monthInYear & RepeatRule.JUNE)!=0) moVector.addElement("6");
                if ((this.monthInYear & RepeatRule.JULY)!=0) moVector.addElement("7");
                if ((this.monthInYear & RepeatRule.AUGUST)!=0) moVector.addElement("8");
                if ((this.monthInYear & RepeatRule.SEPTEMBER)!=0) moVector.addElement("9");
                if ((this.monthInYear & RepeatRule.OCTOBER)!=0) moVector.addElement("10");
                if ((this.monthInYear & RepeatRule.NOVEMBER)!=0) moVector.addElement("11");
                if ((this.monthInYear & RepeatRule.DECEMBER)!=0) moVector.addElement("12");
                
                //assemble vector elements into comma separated list
                for (int i=0; i<moVector.size(); i++) {
                    if (i != 0) sb.append(",");
                    sb.append((String)moVector.elementAt(i));
                }
            }
            
            //UNTIL
            //Note: Google Calendar has a bug where the UNTIL date-time must end with UTC indicator
            // or must not contain time or else the recurring event is not created properly; the
            // event gets created on the start date only even though the event details show the repeat rule.
            if (this.expirationDate != 0) sb.append(";UNTIL=" + DateUtil.longToDateTimeGMT(expirationDate));
            
            sb.append("\n");
        }
    }
    
    /**
     * Gets a readable string for the repeat rule
     *
     * @param sb string buffer to which repeat rule is appended
     */
    void getReadableRule(StringBuffer sb) {
        if (this.frequency != 0) {
            //INTERVAL
            sb.append("Every ");
            
            if (this.interval > 1) sb.append(this.interval + " ");
            
            switch (this.frequency) {
                case RepeatRule.YEARLY: sb.append("year"); break;
                case RepeatRule.MONTHLY: sb.append("month"); break;
                case RepeatRule.WEEKLY: sb.append("week"); break;
                case RepeatRule.DAILY: sb.append("day"); break;
                default: sb.append("time"); break;
            }
            
            if (this.interval > 1) sb.append("s");
            
            //COUNT
            if (this.count != 0) {
                sb.append("; " + this.count + " time");
                if (this.count > 1) sb.append("s");
            }
            
            //BYDAY
            if (this.dayInWeek != 0) {
                Vector dayVector = new Vector();
                
                //get list of days into vector
                if ((this.dayInWeek & RepeatRule.SUNDAY)!=0) dayVector.addElement("SU");
                if ((this.dayInWeek & RepeatRule.MONDAY)!=0) dayVector.addElement("MO");
                if ((this.dayInWeek & RepeatRule.TUESDAY)!=0) dayVector.addElement("TU");
                if ((this.dayInWeek & RepeatRule.WEDNESDAY)!=0) dayVector.addElement("WE");
                if ((this.dayInWeek & RepeatRule.THURSDAY)!=0) dayVector.addElement("TH");
                if ((this.dayInWeek & RepeatRule.FRIDAY)!=0) dayVector.addElement("FR");
                if ((this.dayInWeek & RepeatRule.SATURDAY)!=0) dayVector.addElement("SA");
                
                //assemble vector elements into comma separated list
                if (dayVector.size() != 0) sb.append("; on ");
                for (int i=0; i<dayVector.size(); i++) {
                    if (i!=0) sb.append(",");
                    sb.append((String)dayVector.elementAt(i));
                }
            }
            
            //BYWEEK
            if (this.weekInMonth != 0) {
                sb.append("; on the ");
                
                switch (this.weekInMonth) {
                    case RepeatRule.FIRST: sb.append("1st"); break;
                    case RepeatRule.SECOND: sb.append("2nd"); break;
                    case RepeatRule.THIRD: sb.append("3rd"); break;
                    case RepeatRule.FOURTH: sb.append("4th"); break;
                    case RepeatRule.FIFTH: sb.append("5th"); break;
                    case RepeatRule.SECONDLAST: sb.append("2nd to last"); break;
                    case RepeatRule.THIRDLAST: sb.append("3rd to last"); break;
                    case RepeatRule.FOURTHLAST: sb.append("4th to last"); break;
                    case RepeatRule.FIFTHLAST: sb.append("5th to last"); break;
                    default: sb.append("?"); break;
                }
                
                sb.append(" week");
            }
            
            //BYMONTH
            if (this.monthInYear != 0) {
                Vector moVector = new Vector();
                
                //get list of months into vector
                if ((this.monthInYear & RepeatRule.JANUARY)!=0) moVector.addElement("Jan");
                if ((this.monthInYear & RepeatRule.FEBRUARY)!=0) moVector.addElement("Feb");
                if ((this.monthInYear & RepeatRule.MARCH)!=0) moVector.addElement("Mar");
                if ((this.monthInYear & RepeatRule.APRIL)!=0) moVector.addElement("Apr");
                if ((this.monthInYear & RepeatRule.MAY)!=0) moVector.addElement("May");
                if ((this.monthInYear & RepeatRule.JUNE)!=0) moVector.addElement("Jun");
                if ((this.monthInYear & RepeatRule.JULY)!=0) moVector.addElement("Jul");
                if ((this.monthInYear & RepeatRule.AUGUST)!=0) moVector.addElement("Aug");
                if ((this.monthInYear & RepeatRule.SEPTEMBER)!=0) moVector.addElement("Sep");
                if ((this.monthInYear & RepeatRule.OCTOBER)!=0) moVector.addElement("Oct");
                if ((this.monthInYear & RepeatRule.NOVEMBER)!=0) moVector.addElement("Nov");
                if ((this.monthInYear & RepeatRule.DECEMBER)!=0) moVector.addElement("Dec");
                
                //assemble vector elements into comma separated list
                sb.append("; on ");
                for (int i=0; i<moVector.size(); i++) {
                    if (i != 0) sb.append(", ");
                    sb.append((String)moVector.elementAt(i));
                }
            }
            
            //UNTIL
            if (this.expirationDate != 0) {
                sb.append("; until " +
                        DateUtil.formatTimeGMT(this.expirationDate+Store.getOptions().uploadTimeZoneOffset,
                        true,
                        DateUtil.MONTH_MASK | DateUtil.DAY_MASK | DateUtil.YEAR_MASK));
            }
        }
    }
    
    /**
     * Appends exception dates of repeat rule to the given string
     * buffer
     *
     * @param sb string buffer to which exception dates are appended
     */
    void getEXDATE(StringBuffer sb) {
        copyExceptions();
        
        if (this.exceptDates != null && this.exceptDates.length != 0) {
            sb.append("EXDATE:");
            
            for (int i=0; i<this.exceptDates.length; i++) {
                if (i != 0) sb.append(",");
                sb.append(DateUtil.longToDateTime(this.exceptDates[i].getTime()));
            }
            
            sb.append("\n");
        }
    }
    
    /**
     * Copies the exception dates from the repeat rule into local
     * variable. This is necessary for retreiving the ex dates when
     * they are added manually instead of being parsed from the
     * repeat rule.
     */
    void copyExceptions() {
        if (this.repeatRule != null) {
            Vector dates = new Vector();
            Enumeration exDates = this.repeatRule.getExceptDates();
            while (exDates.hasMoreElements())
                dates.addElement(exDates.nextElement());
            
            this.exceptDates = new Date[dates.size()];
            dates.copyInto(this.exceptDates);
        }
    }
    
    /**
     * Gets a readable string for the repeat rule's exception dates
     *
     * @param sb string buffer to which exception dates are appended
     */
    void getReadableExceptions(StringBuffer sb) {
        copyExceptions();
        if (this.exceptDates != null && this.exceptDates.length != 0) {
            sb.append("; except on ");
            
            for (int i=0; i<this.exceptDates.length; i++) {
                if (i != 0) sb.append(", ");
                sb.append(DateUtil.formatTime(this.exceptDates[i].getTime()+Store.getOptions().uploadTimeZoneOffset,
                        true,
                        DateUtil.MONTH_MASK | DateUtil.DAY_MASK | DateUtil.YEAR_MASK));
            }
        }
    }
    
    /**
     * Appends time zone rules of repeat rule to the given string
     * buffer
     *
     * @param sb string buffer to which time zone rules are appended
     */
    void getVTIMEZONE(StringBuffer sb) {
        String tznm;
        sb.append("BEGIN:VTIMEZONE\n");
        
        //TODO: get phone's time zone as default if <this.tzName> is null
        if (this.tzName != null) tznm = this.tzName;
        else tznm = "Universal_Time_Coordinated";
        
        sb.append("TZID:" + tznm + "\nX-LIC-LOCATION:" + tznm + "\n");
        
        //get standard time zone rule
        getVTIME(sb, true);
        //get daylight time zone rule
        getVTIME(sb, false);
        
        sb.append("END:VTIMEZONE\n");
    }
    
    /**
     * Appends specified time zone rule of repeat rule to the given
     * string buffer
     *
     * @param sb string buffer to which time zone is appended
     * @param standard if true, gets the standard time zone rule; if
     *                 false, gets the daylight saving time zone rule
     */
    void getVTIME(StringBuffer sb, boolean standard) {
        if (standard)sb.append("BEGIN:STANDARD\n");
        else sb.append("BEGIN:DAYLIGHT\n");
        
        String tzCode = (standard ? this.tzCodeStd : this.tzCodeDay);
        sb.append("TZOFFSETFROM:");
        getFormattedTime(sb, (standard ? this.tzOffset+100 : this.tzOffset));
        sb.append("\nTZOFFSETTO:");
        //Note: Not sure if this is a Google Calendar bug...
        //Assume time zone is US Eastern and in Daylight Saving Time...
        // If uploading a repeating event with TZOFFSETFROM/TZOFFSETTO different
        // for each zone rule (FROM: -0400 TO: -0500 for standard and FROM: -0500
        // TO: -0400 for daylight), the event's start time is 1 hour early.
        // The repeat rule downloaded from Google Calendar follows this formula
        // for daylight saving time:
        // "TZOFFSETFROM: [STDTIMEOFFSET] TZOFFSETTO: [STDTIMEOFFSET+1hr]".
        // Match this bug's behavior so that our events are uploaded
        // at the right start time.
        //getFormattedTime(sb, (standard ? this.tzOffset : this.tzOffset+100));
        getFormattedTime(sb, (standard ? this.tzOffset+100 : this.tzOffset));
        sb.append("\n");
        if (tzCode != null) sb.append("TZNAME:" + tzCode + "\n");
        
        //don't include time zone info for this DTSTART
        getDTSTART(sb, false);
        getRRULE(sb);
        if (standard) sb.append("END:STANDARD\n");
        else sb.append("END:DAYLIGHT\n");
    }
    
    /**
     * Appends a 4-digit representation of the specified time to the
     * given string buffer
     *
     * @param sb string buffer to which formatted time is appended
     * @param time number of hours * 100 + number of minutes, ex: 1
     *             hour 40 minutes is 140
     * @example -400 -> -0400<br>230 -> +0230
     */
    void getFormattedTime(StringBuffer sb, int time) {
        //pad if shorter than 4 digits
        if (Math.abs(time) < 1000) {
            if (time < 0) sb.append("-");
            else sb.append("+");
            
            if (time == 0)
                sb.append("0000");
            else
                sb.append("0" + Math.abs(time));
        } else
            sb.append(Integer.toString(time));
    }
    
    /**
     * Processes DateTime values (i.e. DTSTART and DTEND) from a iCal
     * repeat rule string
     *
     * @param startEnd if "DTSTART", assigns parsed time to the
     *             starting time of recurring event; if "DTEND",
     *             assigns parsed time to the end time
     * @param field string that contains the DateTime data in iCal
     *              format
     * @param subFields all tokens of <code>field</code>
     */
    void processDT(String startEnd, String field, StringTokenizer subFields) {
        String subField;
        int idx;
        
        while (subFields.hasMoreTokens()) {
            subField = subFields.nextToken();
            
            //Ignore Start/End field identifier and Value field
            if (subField.equals(startEnd) || subField.startsWith("VALUE")) {
            }
            //Time Zone ID
            else if (subField.startsWith("TZID")) {
                this.tzName = getFieldValue(subField, "TZID");
            }
            //Start/End time
            else {
                try {
                    long date = DateUtil.dateToLong(subField);
                    if (startEnd.equals("DTSTART")) {
                        this.startTime = date;
                    } else {
                        //this end time is not the same as the repeat rule's expiration time
                        this.endTime = date;
                    }
                } catch (Exception e){}
            }
        }
    }
    
    /**
     * Processes starting time from given iCal repeat rule string
     *
     * @param field string that contains start time in iCal format
     * @param subFields all tokens of <code>field</code>
     * @example <code>field</code>: <strong>
     * "DTSTART;TZID=America/New_York:20070101T173511"</strong><br>
     * <code>subFields[0]</code>: "DTSTART"<br>
     * <code>subFields[1]</code>: "TZID=America/New_York"<br>
     * <code>subFields[2]</code>: "20070101T173511"
     */
    void processDTSTART(String field, StringTokenizer subFields) {
        processDT("DTSTART", field, subFields);
    }
    
    /**
     * Processes end time from given iCal repeat rule string
     *
     * @param field string that contains end time in iCal format
     * @param subFields all tokens of <code>field</code>
     * @example <code>field</code>:
     * "DTEND;TZID=America/New_York:20070101T173511"<br>
     * <code>subFields[0]</code>: "DTEND"<br>
     * <code>subFields[1]</code>: "TZID=America/New_York"<br>
     * <code>subFields[2]</code>: "20070101T173511"
     */
    void processDTEND(String field, StringTokenizer subFields) {
        processDT("DTEND", field, subFields);
        
        if (this.startTime > 0 && this.endTime > 0 && this.duration == 0) {
            this.duration = this.endTime - this.startTime;
        }
    }
    
    /**
     * Processes time zone name from given iCal repeat rule string
     *
     * @param field string that contains time zone name in iCal
     *              format
     * @param subFields all tokens of <code>field</code>
     * @param standard if true, assigns the parsed time zone name to
     *                 the standard time zone code; else assigns the
     *                 name to the daylight saving time zone code
     * @example <code>field</code>:
     * "TZNAME:EST"<br> <code>subFields[0]</code>: "TZNAME"<br>
     * <code>subFields[1]</code>: "EST"<br>
     */
    void processTZNAME(String field, StringTokenizer subFields, boolean standard) {
        String tz;
        
        //time zone name is second subfield
        if (subFields.countTokens() >= 2) {
            subFields.nextToken();
            tz = subFields.nextToken();
            
            if (TimeZone.getTimeZone(tz) != null) {
                if (standard) this.tzCodeStd = tz;
                else this.tzCodeDay = tz;
            }
        }
    }
    
    /**
     * Processes time zone offset from given iCal repeat rule string
     *
     * @param field string that contains time zone offset in iCal
     *              format
     * @param subFields all tokens of <code>field</code>
     * @example <code>field</code>:
     * "TZOFFSETFROM:-0400"<br> <code>subFields[0]</code>:
     * "TZOFFSETFROM"<br> <code>subFields[1]</code>: "-0400"<br>
     * "TZOFFSETTO:-0500"<br> <code>subFields[0]</code>:
     * "TZOFFSETTO"<br> <code>subFields[1]</code>: "-0500"<br>
     */
    int processTZOFFSET(String field, StringTokenizer subFields) {
        String offset;
        
        //time zone offset is second subfield
        if (subFields.countTokens() >= 2) {
            subFields.nextToken();
            offset = subFields.nextToken().trim();
            
            int mult = 1;
            if(offset.startsWith("-")) {
                offset = offset.substring(1);
                mult = -1;
            }
            
            while(offset.startsWith("0")) {
                offset = offset.substring(1);
            }
            
            try {
                int val = Integer.parseInt(offset);
                int hours = val / 100;
                int minutes = val % 100;
                return  (hours * 60 + minutes) * 60 * 1000 * mult;
            } catch (Exception e) { }
        }
        
        return 0;
    }
    
    
    private Calendar processTZ_START(String field, StringTokenizer subFields) {
        try {
            if (subFields.countTokens() >= 2) {
                subFields.nextToken();
                String startString = subFields.nextToken().trim();
                long startTime = DateUtil.dateToLong(startString);
                
                Calendar result = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                result.setTime(new Date(startTime));
                return result;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Processes event duration from given iCal repeat rule string
     *
     * @param field string that contains event duration in iCal
     *              format
     * @param subFields all tokens of <code>field</code>
     * @example <code>field</code>:
     * "DURATION:PT3600S"<br> <code>subFields[0]</code>:
     * "DURATION"<br> <code>subFields[1]</code>: "PT3600S"<br>
     */
    void processDURATION(String field, StringTokenizer subFields) {
        String dur;
        int idx;
        long timeMs = 0;
        
        //second subfield is duration
        if (subFields.countTokens() >= 2) {
            try {
                subFields.nextToken();
                dur = subFields.nextToken();
                idx = dur.indexOf('P');
                
                //skip the optional neg sign and 'P'
                if (idx >= 0) dur = dur.substring(idx + 1);
                //else, unexpected format
                else return;
            } catch (Exception e) {
                return;
            }
            
            //get weeks
            timeMs += getUnitValue(dur, "W") * 1000 * 60 * 60 * 24 * 7;
            //get days
            timeMs += getUnitValue(dur, "D") * 1000 * 60 * 60 * 24;
            //get hours
            timeMs += getUnitValue(dur, "H") * 1000 * 60 * 60;
            //get minutes
            timeMs += getUnitValue(dur, "M") * 1000 * 60;
            //get seconds
            timeMs += getUnitValue(dur, "S") * 1000;
            
            this.duration = timeMs;
            if (this.endTime == 0) this.endTime = this.startTime + this.duration;
        }
    }
    
    /**
     * Processes repeat rule from given iCal string
     *
     * @param field string that contains repeat rule in iCal
     *              format
     * @param subFields all tokens of <code>field</code>
     * @example <code>field</code>:
     * "RRULE:FREQ=MONTHLY;INTERVAL=2"<br> <code>subFields[0]</code>:
     * "RRULE"<br> <code>subFields[1]</code>: "FREQ=MONTHLY"<br>
     * <code>subFields[1]</code>: "INTERVAL=2"<br>
     */
    void processRRULE(String field, StringTokenizer subFields) {
        String subField;
        String value;
        
        while (subFields.hasMoreTokens()) {
            //create new RepeatRule
            if (this.repeatRule == null) this.repeatRule = new RepeatRule();
            
            subField = subFields.nextToken();
            
            if (subField.startsWith("FREQ")) {
                try {
                    if (subField.indexOf("YEARLY") >= 0) {
                        this.frequency = RepeatRule.YEARLY;
                        this.repeatRule.setInt(RepeatRule.FREQUENCY, RepeatRule.YEARLY);
                    } else if (subField.indexOf("MONTHLY") >= 0) {
                        this.frequency = RepeatRule.MONTHLY;
                        this.repeatRule.setInt(RepeatRule.FREQUENCY, RepeatRule.MONTHLY);
                    } else if (subField.indexOf("WEEKLY") >= 0) {
                        this.frequency = RepeatRule.WEEKLY;
                        this.repeatRule.setInt(RepeatRule.FREQUENCY, RepeatRule.WEEKLY);
                    } else if (subField.indexOf("DAILY") >= 0) {
                        this.frequency =  RepeatRule.DAILY;
                        this.repeatRule.setInt(RepeatRule.FREQUENCY, RepeatRule.DAILY);
                    }
                } catch (Exception e){}
            } else if (subField.startsWith("INTERVAL")) {
                try {
                    this.interval = Integer.parseInt(subField.substring(subField.indexOf("=") + 1));
                    this.repeatRule.setInt(RepeatRule.INTERVAL, this.interval);
                } catch (Exception e){}
            }
            //parse expiration date of event recurrence
            else if (subField.startsWith("UNTIL")) {
                String date;
                date = getFieldValue(subField, "UNTIL");
                if (date != null) {
                    try {
                        this.expirationDate = DateUtil.dateToLong(date);
                    } catch (Exception e) {}
                }
            } else if (subField.startsWith("BYMONTH")) {
                String mo;
                mo = getFieldValue(subField, "BYMONTH");
                
                if (mo != null) {
                    try { this.repeatRule.setInt(RepeatRule.MONTH_IN_YEAR, Integer.parseInt(mo)); } catch (Exception e) {}
                }
            } else if (subField.startsWith("BYDAY")) {
                //get days of week that event occurs
                if (subField.indexOf("SU") >= 0) this.dayInWeek |= RepeatRule.SUNDAY;
                if (subField.indexOf("MO") >= 0) this.dayInWeek |= RepeatRule.MONDAY;
                if (subField.indexOf("TU") >= 0) this.dayInWeek |= RepeatRule.TUESDAY;
                if (subField.indexOf("WE") >= 0) this.dayInWeek |= RepeatRule.WEDNESDAY;
                if (subField.indexOf("TH") >= 0) this.dayInWeek |= RepeatRule.THURSDAY;
                if (subField.indexOf("FR") >= 0) this.dayInWeek |= RepeatRule.FRIDAY;
                if (subField.indexOf("SA") >= 0) this.dayInWeek |= RepeatRule.SATURDAY;
                
                //get week in month that event occurs
                //the number that precedes the day of the week indicates
                // the week in month that event occurs
                this.weekInMonth |= getWeekInMonth(subField, "SU");
                this.weekInMonth |= getWeekInMonth(subField, "MO");
                this.weekInMonth |= getWeekInMonth(subField, "TU");
                this.weekInMonth |= getWeekInMonth(subField, "WE");
                this.weekInMonth |= getWeekInMonth(subField, "TH");
                this.weekInMonth |= getWeekInMonth(subField, "FR");
                this.weekInMonth |= getWeekInMonth(subField, "SA");
                
                try {
                    //set day(s) in week
                    this.repeatRule.setInt(RepeatRule.DAY_IN_WEEK, this.dayInWeek);
                    
                    //set week in month
                    if (this.weekInMonth != 0) this.repeatRule.setInt(RepeatRule.WEEK_IN_MONTH, this.weekInMonth);
                } catch (Exception e) { }
            }
        }
    }
    
    /**
     * Gets the week in month from given iCal string
     *
     * @param field string that contains repeat rule in iCal
     *              format
     * @param subFields all tokens of <code>field</code>
     * @returns <code>RepeatRule.FIRST</code>,
     *        <code>RepeatRule.SECOND</code>,
     *        <code>RepeatRule.THIRD</code>,
     *        <code>RepeatRule.FOURTH</code>, or
     *        <code>RepeatRule.FIFTH</code>
     * @example <code>field</code>:
     * "BYDAY=2FR"<br> <code>day</code>: "FR"<br> returns
     * <code>RepeatRule.SECOND</code>
     */
    int getWeekInMonth(String field, String day) {
        int rval = 0;
        int wk = getUnitValue(field, day);
        
        //GCal uses -1 to indicate fifth week
        if (wk < 0) wk = 5;
        
        switch (wk) {
            case 1: rval = RepeatRule.FIRST; break;
            case 2: rval = RepeatRule.SECOND; break;
            case 3: rval = RepeatRule.THIRD; break;
            case 4: rval = RepeatRule.FOURTH; break;
            case 5: rval = RepeatRule.FIFTH; break;
            default: break;
        }
        
        return rval;
    }
    
    /**
     * Gets the integer that precedes a unit name in given
     * field
     *
     * @param unitField string that contains an integer and unit
     * @param unitName unit name whose integer is to be parsed
     * @returns integer value of <code>unitField</code>;
     *        <code>0</code> if <code>unitName</code> cannot be found
     * @example <code>unitField</code>:
     * "DURATION:123M"<br> <code>unitName</code>: "M"<br> returns
     * <code>123</code>
     */
    int getUnitValue(String unitField, String unitName) {
        int idx;
        int indexStart;
        int rval = 0;
        
        try {
            //find unit name
            idx = unitField.indexOf(unitName);
            if (idx >= 0) {
                //skip the unit name
                indexStart = idx - 1;
                
                //find any preceding digits
                if (java.lang.Character.isDigit(unitField.charAt(indexStart))) {
                    //look behind the unit name until non-digit found
                    for (int i = indexStart; i >= 0; i--) {
                        //quit when non-digit found
                        if (java.lang.Character.isDigit(unitField.charAt(i)) == false) {
                            //include negative sign
                            if (unitField.charAt(i) == '-') --indexStart;
                            break;
                        }
                        indexStart = i;
                    }
                    
                    rval = Integer.parseInt(unitField.substring(indexStart, idx));
                }
            }
        } catch (Exception e){}
        
        return rval;
    }
    
    /**
     * Gets the value of a specified field
     *
     * @param field string that contains a token name and value
     *              separated by the equal (=) or colon signs (:)
     * @param fieldName name of field whose value is to be parsed
     * @returns string value of <code>field</code>; <code>null</code>
     *        if <code>fieldName</code> cannot be found
     * @example <code>field</code>:
     * "COUNT=2"<br> <code>fieldName</code>: "COUNT"<br> returns
     * <code>2</code>
     */
    String getFieldValue(String field, String fieldName) {
        StringTokenizer tokens = new StringTokenizer(field, "=:");
        while (tokens.hasMoreTokens()) {
            if (tokens.nextToken().equals(fieldName)) {
                //found field, next token must be its value
                if (tokens.hasMoreTokens()) return tokens.nextToken();
                
                break;
            }
        }
        
        //token not found
        return null;
    }
    
    
    public boolean equals(Object obj) {
        if(!(obj instanceof Recurrence)) {
            return false;
        }
        
        Recurrence o = (Recurrence) obj;
        
        //verify that the recurrence attributes are all the same for both object
        boolean eq =
                (this.startTime % (24 * 60 * 60 * 1000) == o.startTime % (24 * 60 * 60 * 1000)) &&
                (this.endTime % (24 * 60 * 60 * 1000) == o.endTime % (24 * 60 * 60 * 1000)) &&
                (this.expirationDate == o.expirationDate) &&
                (this.frequency == o.frequency) &&
                (
                (this.interval == o.interval) ||
                ( (this.interval == 0 || this.interval == 1) && (o.interval == 0 || o.interval == 1) )
                ) /*&&
                (this.weekInMonth == o.weekInMonth) &&
                (this.dayInMonth == o.dayInMonth) &&
                (this.dayInWeek == o.dayInWeek) &&
                (this.dayInYear == o.dayInYear) &&
                (this.duration == o.duration)*/;
        
        if(!eq) {
            return false;
        }
        
        //check that the start time is the same for both
        if(this.startTime != o.startTime) {
            //Google will not return the actual start time of a recurrent event if it started in the past, but
            //it will return the current date, so I am going to ignore if the start time is different if the recurrent
            //event starts in the past of about today
            long nearFuture = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
            
            if(this.startTime > nearFuture && o.startTime > nearFuture) { //ignore the start only if it happens before the near future
                return false;
            }
        }
        
        //verify that the except dates are alll the same

        //if the except dates are null or empty then they equal
        boolean exceptDatesEquals =
                (this.exceptDates == null && o.exceptDates == null) ||
                (this.exceptDates == null && o.exceptDates != null && o.exceptDates.length == 0) ||
                (this.exceptDates != null && o.exceptDates == null && this.exceptDates.length == 0);
        
        if(exceptDatesEquals) {
            return true;
        }

        //if the except dates are not null or empty then verify that they are the same
        return compareExceptDatesArray(this.exceptDates, o.exceptDates) && compareExceptDatesArray(o.exceptDates, this.exceptDates);
    }
    
    private boolean compareExceptDatesArray(Date[] array1, Date[] array2) {
        long nearFuture = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
        for(int i = 0; i < array1.length; i++) {
            //ignore the except dates in the past
            if(array1[i].getTime() < nearFuture) {
                continue;
            }
            
            //look for the equal date in the other array
            boolean found = false;
            for(int j = 0; j < array2.length && !found; j++) {
                if(array1[i].equals(array2[j])) {
                    found = true;
                }
            }
            
            if(!found) {
                return false;
            }
        }
        
        return true;
    }
}
