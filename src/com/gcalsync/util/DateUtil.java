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
 * * * Changes:
 *  --/nov/2007 Agustin
 *      -isAllDay: now it correctly recognizes blackberry all day events
 *      -formatInterval: print all day events correctly
 *      -new functions to read GMT time
 */
package com.gcalsync.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.gcalsync.store.Store;
import com.gcalsync.option.Options;

/**
 *
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 40 $
 * @date $Date$
 */
public class DateUtil {
    
    public final static long DAY = 24 * 3600 * 1000;
    
    public final static int YEAR_MASK = (1 << Calendar.YEAR);
    public final static int MONTH_MASK = (1 << Calendar.MONTH);
    public final static int DAY_MASK = (1 << Calendar.DAY_OF_MONTH);
    public final static int HOUR_MASK = (1 << Calendar.HOUR);
    public final static int HOUR_OF_DAY_MASK = (1 << Calendar.HOUR_OF_DAY);
    public final static int MINUTE_MASK = (1 << Calendar.MINUTE);
    public final static int SECOND_MASK = (1 << Calendar.SECOND);
    public final static int MILLISECOND_MASK = (1 << Calendar.MILLISECOND);
    public final static int AM_PM_MASK = (1 << Calendar.AM_PM);
    
    /**
     * Determines if given times span at least an entire day
     *
     * @param startTime starting time in ms since 1970 Jan 1
     * @param endTime ending time in ms since 1970 Jan 1
     * @returns <code>true</code>: starts and ends daily at
     *        midnight<br><code>false</code> otherwise
     */
    public static boolean isAllDay(long startTime, long endTime) {
        //according to Java documentation all day events are events whose start and end time equals
        if(startTime == endTime) return true;
        
        //apparently in BlackBerry all day events on day X starts at midnight (in GMT time) of day (X-1)
        //and ends on day (X) at midnight. All in GMT time, no matter in what time zone we are
        boolean startsMidnight = (startTime % (24 * 60 * 60 * 1000)) == 0;
        boolean endsMidnight = (endTime % (24 * 60 * 60 * 1000)) == 0;
        //boolean startsMidnight = isMidnight(startTime);
        //boolean endsMidnight = isMidnight(endTime);
        
        long timeDiff = (endTime - startTime)/1000/60/60;
        
        //daily if difference in hours is a multiple of 24
        boolean daily = (timeDiff >= 24) && ((timeDiff % 24) == 0);
        
        //starts and ends daily at 12:00am
        return (daily &&  startsMidnight && endsMidnight);
    }
    
    public static boolean isMidnight(long time) {
        Calendar calendar = Calendar.getInstance();
        
        calendar.setTime(new Date(time));
        return isMidnight(calendar);
    }
    
    public static String dateToIsoDateTime(String date) {
        StringBuffer sb = new StringBuffer(date);
        
        if (date.length() >= 8) {
            //20070101 -> 2007-01-01
            //insert hyphens between year, month, and day
            sb.insert(4, '-');
            sb.insert(7, '-');
            
            //2007-01-01T173012 -> 2007-01-01T17:30:12
            if (date.length() >= 15) {
                //insert colons between hours, minutes, and seconds
                sb.insert(13, ':');
                sb.insert(16, ':');
                
                //2007-01-01T17:30:12 -> 2007-01-01T17:30:12.000
                //append milliseconds
                if (sb.toString().length() > 19)
                    sb.insert(20, ".000");
                else
                    sb.append(".000");
                
                //2007-01-01T17:30:12.000 -> 2007-01-01T17:30:12.000Z
                //append UTC indicator if exists
                if (date.length() > 15 && date.charAt(15) == 'Z')
                    sb.append('Z');
            }
            
            if (sb.length() > 19) sb.setLength(19);
        }
        
        return sb.toString();
    }
    
    public static String isoDateTimeToDate(String isoDate) {
        StringBuffer sb = new StringBuffer(isoDate);
        int maxlen;
        
        if (isoDate.length() >= 10) {
            //2007-01-01 -> 20070101
            //remove hyphens between year, month, and day
            sb.deleteCharAt(4);
            sb.deleteCharAt(6);
            
            maxlen = 8;
            
            //20070101T17:30:12 -> 20070101T173012
            if (isoDate.length() >= 19) {
                maxlen = 15;
                
                //remove colons between hours, minutes, and seconds
                sb.deleteCharAt(11);
                sb.deleteCharAt(13);
                
                if (isoDate.indexOf("Z") >= 0) {
                    sb.insert(15, 'Z');
                    ++maxlen;
                }
            }
            if (sb.length() > maxlen) sb.setLength(maxlen);
        }
        
        return sb.toString();
    }
    
    public static String longToDate(long longDate) {
        String str = longToIsoDate(longDate);
        return isoDateTimeToDate(str);
    }
    
    public static String longToDateGMT(long longDate) {
        String str = longToIsoDateGMT(longDate);
        return isoDateTimeToDate(str);
    }
    
    /**
     * Gets formatted time
     *
     * @param time time in ms since 01/01/70
     * @param symbols if true, inserts symbols
     * @param fields <code>Calendar.MONTH</code>,
     *               <code>Calendar.DAY</code>,
     *               <code>Calendar.YEAR</code>,
     *               <code>Calendar.HOUR</code>,
     *               <code>Calendar.HOUR_OF_DAY</code>,
     *               <code>Calendar.MINUTE</code>,
     *               <code>Calendar.SECOND</code>,
     *               <code>Calendar.MILLISECOND</code>,
     *               <code>Calendar.AM_PM</code>
     *
     * @returns formatted time
     */
    public static String formatTime(long time, boolean symbols, int fields) {
        return formatTime(time, symbols, fields, TimeZone.getDefault());
    }
    public static String formatTimeGMT(long time, boolean symbols, int fields) {
        return formatTime(time, symbols, fields, TimeZone.getTimeZone("GMT"));
    }
    public static String formatTime(long time, boolean symbols, int fields, TimeZone timeZone) {
        StringBuffer sb = new StringBuffer();
        Calendar calendar = Calendar.getInstance(timeZone);
        String year;
        int hour;
        
        calendar.setTime(new Date(time));
        
        if ((fields & MONTH_MASK) != 0) {
            sb.append(twoDigit(calendar.get(Calendar.MONTH)+1));
        }
        
        if ((fields & DAY_MASK) != 0) {
            if (symbols) sb.append("/");
            sb.append(twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));
        }
        
        if ((fields & YEAR_MASK) != 0) {
            if (symbols) sb.append("/");
            
            //get last two digits of year
            year = Integer.toString(calendar.get(Calendar.YEAR));
            sb.append(year.substring(year.length()-2));
        }
        
        if ((fields & HOUR_MASK) != 0 || (fields & HOUR_OF_DAY_MASK) != 0) {
            if ((fields & HOUR_MASK) != 0)
                hour = calendar.get(Calendar.HOUR);
            else if ((fields & HOUR_OF_DAY_MASK) != 0)
                hour = calendar.get(Calendar.HOUR_OF_DAY);
            else
                hour = 0;
            
            if (hour == 0) hour = 12;
            sb.append(twoDigit(hour));
        }
        
        if ((fields & MINUTE_MASK) != 0) {
            if (symbols) sb.append(":");
            sb.append(twoDigit(calendar.get(Calendar.MINUTE)));
        }
        
        if ((fields & SECOND_MASK) != 0) {
            if (symbols) sb.append(":");
            sb.append(twoDigit(calendar.get(Calendar.SECOND)));
        }
        
        if ((fields & MILLISECOND_MASK) != 0) {
            if (symbols) sb.append(".");
            sb.append(threeDigit(calendar.get(Calendar.MILLISECOND)));
        }
        
        if ((fields & AM_PM_MASK) != 0) {
            sb.append(calendar.get(Calendar.AM_PM) == Calendar.AM ? "A":"P");
        }
        
        return sb.toString();
    }
    
    int getTimeVal(long time, int field) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        return calendar.get(field);
    }
    
    public static String longToDateTime(long longDate) {
        String str = longToIsoDateTime(longDate);
        return isoDateTimeToDate(str);
    }
    
    public static String longToDateTimeGMT(long longDate) {
        String str = longToIsoDateTimeGMT(longDate);
        return isoDateTimeToDate(str);
    }
    
    public static String formatInterval(long startTime, long endTime) {
        StringBuffer sb = new StringBuffer();
        boolean startsMidnight;
        boolean endsMidnight;
        
        if(isAllDay(startTime, endTime)) {
            sb.append(formatTime(startTime, true, YEAR_MASK | MONTH_MASK | DAY_MASK, TimeZone.getTimeZone("GMT")));
            long days = (endTime - startTime) / (24 * 60 * 60 * 1000);
            if(days > 1) {
                sb.append("-");
                sb.append(formatTime(endTime - (24 * 60 * 60 * 1000), true, YEAR_MASK | MONTH_MASK | DAY_MASK, TimeZone.getTimeZone("GMT")));
            }
            sb.append("-All Day");
            return sb.toString();
        }
        
        sb.append(formatTime(startTime, true, YEAR_MASK | MONTH_MASK | DAY_MASK));
        
        //exclude times if starts and ends at midnight
        startsMidnight = isMidnight(startTime);
        endsMidnight = isMidnight(endTime);
        if (!(startsMidnight && endsMidnight)) {
            sb.append("(");
            sb.append(formatTime(startTime, true, HOUR_MASK | MINUTE_MASK | AM_PM_MASK));
            sb.append(")");
        }
        
        //if event ends at midnight, exclude end-date and include all of the previous day
        if (endsMidnight) endTime -= DAY;
        
        if (startTime < endTime) {
            sb.append("-");
            
            //omit end-date if the start and end dates match
            if (!longToIsoDate(startTime).equals(longToIsoDate(endTime)))
                sb.append(formatTime(endTime, true, YEAR_MASK | MONTH_MASK | DAY_MASK));
            
            if (!(startsMidnight && endsMidnight)) {
                sb.append("(");
                sb.append(formatTime(endTime, true, HOUR_MASK | MINUTE_MASK | AM_PM_MASK));
                sb.append(")");
            }
        }
        
        return sb.toString();
    }
    
    public static long dateToLong(String date) {
        String str = dateToIsoDateTime(date);
        return isoDateToLong(str);
    }
    public static long isoDateToLong(String isoDate) {
        return isoDateToLongExtraInfo(isoDate)[0];
    }
    
    public static long[] isoDateToLongExtraInfo(String isoDate) {
        String year = isoDate.substring(0, 4);
        String month = isoDate.substring(5, 7);
        String day = isoDate.substring(8, 10);
        String hour = "0";
        String min = "0";
        String sec = "0";
        String millis = "0";
        String timeZoneHour = "+00";
        String timeZoneMin = "00";
        boolean timeIncluded = false;
        long timeZoneAdjustment = 0;
        boolean timeZoneIncluded = false;
        
        if (isoDate.length() >= 11) {
            if (isoDate.charAt(10) == 'T' && isoDate.length() >= 18) {
                // Time of day given. Example: 2006-04-24T16:29:59 or 2006-04-24T16:29:59.001
                hour = isoDate.substring(11, 13);
                min = isoDate.substring(14, 16);
                sec = isoDate.substring(17, 19);
                
                if(isoDate.length() >= 23) {
                    //Time has miloseconds too: 2006-04-24T16:29:59.001
                    millis = isoDate.substring(20, 23);
                    if (isoDate.length() >= 29) {
                        // Time of day and timezone given. Example: 2006-04-24T16:29:59.001-07:00
                        timeZoneHour = isoDate.substring(23, 26);
                        timeZoneMin = isoDate.substring(27, 29);
                        timeZoneIncluded = true;
                    }
                }
                
                timeIncluded = true;
                
            } else {
                // No time of day but timezone given. Example: 2006-04-24-07:00
                if (isoDate.length() >= 16) {
                    timeZoneHour = isoDate.substring(10, 13);
                    timeZoneMin = isoDate.substring(14, 16);
                    timeZoneIncluded = true;
                }
            }
        }
        // else: no time of day, no timezone. Example: 2006-04-24
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.YEAR, Integer.parseInt(year));
        calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1); // January is 0
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(min));
        calendar.set(Calendar.SECOND, Integer.parseInt(sec));
        calendar.set(Calendar.MILLISECOND, Integer.parseInt(millis));
        
        //System.out.println("Time without timezone: " + calendar.getTime());
        
        //adjust time if it was included
        if (timeIncluded) {
            Options options = Store.getOptions();
            timeZoneAdjustment = options.downloadTimeZoneOffset;
            
            if(timeZoneIncluded) {
                timeZoneAdjustment -= stringTimeZoneToLong(timeZoneHour, timeZoneMin);
            }
        }
        
        long timeMillis = calendar.getTime().getTime() + timeZoneAdjustment;
        
        //System.out.println("Time is: " + new Date(timeMillis));
        return new long[] { timeMillis, timeIncluded ? 1 : 0};
    }
    
    private static long stringTimeZoneToLong(String timeZoneHour, String timeZoneMin) {
        int timeZoneHourInt = parseSignedInt(timeZoneHour);
        int timeZoneMinInt = Integer.parseInt(timeZoneMin);
        
        if(isNegativeInt(timeZoneHour)) {
            timeZoneMinInt = -timeZoneMinInt;
        }
        
        return  timeZoneHourInt * 3600 * 1000 + timeZoneMinInt * 60 * 1000;
    }
    
    public static int parseSignedInt(String value) {
        int sign = 1;
        if(value.startsWith("-")) {
            sign = -1;
            value = value.substring(1);
        } else if(value.startsWith("+")) {
            value = value.substring(1);
        }
        int absoluteValue = Integer.parseInt(value);
        return sign * absoluteValue;
    }
    
    private static boolean isNegativeInt(String value) {
        return value.startsWith("-");
    }
    
    public static String[] longIntervalToIsoDateInterval(long[] longInterval) {
        String startDate = null;
        String endDate = null;
        boolean includeTime = true;
        if (longInterval[0] == longInterval[1]) {
            // all-day event (according to javax.microedition.pim.Event JavaDoc, this is how a all-day event is specified)
            startDate = longToIsoDate(longInterval[0]);
            endDate = startDate;
        } else {
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(new Date(longInterval[0]));
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(new Date(longInterval[1]));
            if (isMidnight(startCalendar) && isMidnight(endCalendar)) {
                // all-day event, multiple days
                includeTime = false;
            }
            startDate = longToIsoDateTime(longInterval[0], includeTime);
            endDate = longToIsoDateTime(longInterval[1], includeTime);
        }
        return new String[]{startDate, endDate};
    }
    
    private static boolean isMidnight(Calendar calendar) {
        return (calendar.get(Calendar.HOUR_OF_DAY) == 0) &&
                (calendar.get(Calendar.MINUTE) == 0);
    }
    
    public static String longToIsoDate(long longDate, int offsetDays) {
        return longToIsoDate(longDate + offsetDays * DAY);
    }
    
    public static String longToIsoDateGMT(long longDate, int offsetDays) {
        return longToIsoDateGMT(longDate + offsetDays * DAY);
    }
    
    public static String longToIsoDateGMT(long longDate) {
        return longToIsoDateTime(longDate, false, TimeZone.getTimeZone("GMT")) + "Z";
    }
    
    public static String longToIsoDate(long longDate) {
        return longToIsoDateTime(longDate, false);
    }
    
    public static String longToIsoDateTimeGMT(long longDate) {
        return longToIsoDateTime(longDate, true, TimeZone.getTimeZone("GMT")) + "Z";
    }
    
    public static String longToIsoDateTime(long longDate) {
        return longToIsoDateTime(longDate, true);
    }
    
    private static String longToIsoDateTime(long longDate, boolean includeTime) {
        return longToIsoDateTime(longDate, includeTime, TimeZone.getDefault());
    }
    
    private static String longToIsoDateTime(long longDate, boolean includeTime, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(new Date(longDate));
        return calendarToIsoDateTime(calendar, includeTime);
    }
    
    private static String calendarToIsoDateTime(Calendar calendar, boolean includeTime) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (!includeTime) {
            return year + "-" + twoDigit(month) + "-" + twoDigit(day);
        } else {
            
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            int sec = calendar.get(Calendar.SECOND);
            int millis = calendar.get(Calendar.MILLISECOND);
            
            return year + "-" + twoDigit(month) + "-" + twoDigit(day)
            + "T" + twoDigit(hour) + ":" + twoDigit(min) + ":" + twoDigit(sec) + "." + threeDigit(millis);
        }
    }
    
    public static String twoDigit(int value) {
        if (value < 10) {
            return "0" + value;
        } else {
            //get last two digits
            String str = Integer.toString(value);
            return str.substring(str.length() - 2);
        }
    }
    
    private static String threeDigit(int value) {
        if (value < 10) {
            return "00" + value;
        } else if (value < 100) {
            return "0" + value;
        } else {
            return "" + value;
        }
    }
    
    /*
    private void computeFields(Calendar calendar) {
        if (calendar instanceof CalendarImpl) {
     
        }
    }
     */
    
}



