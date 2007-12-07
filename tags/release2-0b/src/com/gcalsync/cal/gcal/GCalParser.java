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
package com.gcalsync.cal.gcal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.microedition.pim.RepeatRule;

import harmony.java.util.StringTokenizer;

import org.kxml.Attribute;
import org.kxml.Xml;
import org.kxml.parser.ParseEvent;
import org.kxml.parser.XmlParser;

import com.gcalsync.util.DateUtil;
import com.gcalsync.log.*;
import com.gcalsync.cal.Recurrence;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author: batcage $
 * @version $Rev: 42 $
 * @date $Date: 2006-12-30 03:52:37 -0500 (Sat, 30 Dec 2006) $
 */
public class GCalParser {
  
    public void parseCalendar(byte[] calendarBytes, String title, boolean useRemindersForThisCalendar, Vector gcalEvents) {
		GCalEvent gCalEvent;
		if (calendarBytes != null && calendarBytes.length > 0) {
            try {
                InputStreamReader reader = null;
                try {
                    reader = new InputStreamReader(new ByteArrayInputStream(calendarBytes), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    reader = new InputStreamReader(new ByteArrayInputStream(calendarBytes));
                }
                XmlParser xmlParser = new XmlParser(reader, 300);
                ParseEvent event = xmlParser.read();
                while (!isEnd(event)) {
                    if (event.getType() == Xml.START_TAG) {
                        String name = event.getName();
                        if ("entry".equals(name)) {
                            gCalEvent = parseEvent(xmlParser);
							gCalEvent.parentCalendarTitle = title;
                            if (!useRemindersForThisCalendar) {
                                gCalEvent.reminder = -1;
                            }
                            gcalEvents.addElement(gCalEvent);
                        }
                    }
                    event = xmlParser.read();
                }

				//find all recurrence exceptions, search the <gcalEvents> Vector for 
				// the original events by ID, and add an exception to their repeat rule
				GCalEvent origEvent;
				for (int i=0; i<gcalEvents.size(); i++)
				{
					gCalEvent = (GCalEvent)gcalEvents.elementAt(i);
					if (gCalEvent.origEventId.length() > 0)
					{
						origEvent = findEvent(gcalEvents, gCalEvent.origEventId);
						if (origEvent != null && origEvent.recur != null)
							origEvent.recur.addExceptDate(gCalEvent.startTime);
					}
				}
                                
            } catch (IOException e) {
                ErrorHandler.showError("Failed to download calendar", e);
            }
        }
    }

	private GCalEvent findEvent(Vector eventVector, String id)
	{
		GCalEvent ev;
		GCalEvent rval = null;

		for (int i=0; i<eventVector.size(); i++) {
				ev = (GCalEvent)eventVector.elementAt(i);

				if (ev.id.equals(id)) {
						rval = ev;
						break;
				}
		}

		return rval;
	}

    public GCalFeed[] parseFeeds(byte[] feedsBytes) {
        // extract feed URLs into vector
        Vector feeds = new Vector();
        
        if (feedsBytes != null && feedsBytes.length > 0) {
          try {
              InputStreamReader reader = null;
              try {
                  reader = new InputStreamReader(new ByteArrayInputStream(feedsBytes), "UTF-8");
              } catch (UnsupportedEncodingException e) {
                  reader = new InputStreamReader(new ByteArrayInputStream(feedsBytes));
              }
              XmlParser xmlParser = new XmlParser(reader, 300);
              ParseEvent event = xmlParser.read();
              while (!isEnd(event)) {
                  if (event.getType() == Xml.START_TAG) {
                      String name = event.getName();
                      if ("entry".equals(name)) {
                          GCalFeed feed = parseFeedEntry(xmlParser);
                          if (feed != null) {
                              feeds.addElement(feed);
                          }
                      }
                  }
                  event = xmlParser.read();
              }
          } catch (Exception e) {
              ErrorHandler.showError("Failed to download feeds", e);
          }
        }
        
        // copy feed into an array
        GCalFeed[] feedsArray = new GCalFeed[feeds.size()];
        feeds.copyInto(feedsArray);
        return feedsArray;
    }

    private GCalEvent parseEvent(XmlParser xmlParser) throws IOException {
        GCalEvent gCalEvent = new GCalEvent();
        ParseEvent nextEvent = xmlParser.peek();

        while (!isEnd(nextEvent, "entry")) {
            String name = nextEvent.getName();
            int type = nextEvent.getType();
            xmlParser.read();
            if ((type == Xml.START_TAG)) {
                if ("id".equals(name)) {
                    gCalEvent.id = parseTextNode(xmlParser, "id");
                } else if ("title".equals(name)) {
                    gCalEvent.title = parseTextNode(xmlParser, "title");
                } else if ("content".equals(name)) {
                    gCalEvent.note = parseTextNode(xmlParser, "content");
                } else if ("where".equals(name)) {
                    gCalEvent.location = parseValue(nextEvent);
                } else if ("updated".equals(name)) {
                    String updatedIsoDate = parseTextNode(xmlParser, "updated");
                    if ((updatedIsoDate != null) && (updatedIsoDate.length() > 0)) {
                        gCalEvent.updated = DateUtil.isoDateToLong(updatedIsoDate);
                    }
				} else if ("published".equals(name)) {
					String date = parseTextNode(xmlParser, "published");
					if ((date != null) && (date.length() > 0)) {
						gCalEvent.published = DateUtil.isoDateToLong(date);
					}
                } else if ("when".equals(name)) {
                    long[] when = parseWhen(nextEvent);
                    gCalEvent.startTime = when[0];
                    gCalEvent.endTime = when[1];
                } else if ("eventStatus".equals(name)) {
                    gCalEvent.cancelled = isCancelled(parseValue(nextEvent));
				 } else if ("reminder".equals(name)) { 
					 gCalEvent.reminder = parseReminder(nextEvent); 
				} else if ("recurrence".equals(name)) {
					parseRecurrence(xmlParser, gCalEvent);
				} else if (name.toLowerCase().equals("originalevent")) {
					String id = nextEvent.getValue("href");
					if (id != null) gCalEvent.origEventId = id;
				} 
            } 
            nextEvent = xmlParser.peek();
        }
        return gCalEvent;
    }

	private int parseReminder(ParseEvent event) { 
		Attribute minutes = event.getAttribute("minutes"); 
		return Integer.parseInt(minutes.getValue()); 
	} 

	private void parseRecurrence(XmlParser xmlParser, GCalEvent event)
	{
		try 
		{ 
			event.recur = new Recurrence(parseTextNode(xmlParser, "recurrence")); 
			event.endTime = event.recur.getEndDateTime();
			event.startTime = event.recur.getStartDateTime();
		}
		catch (Exception e) {
//#ifdef DEBUG_ERR
			System.out.println("parseRecurrence() failed: " + e); 
//#endif
		}
	}

    private GCalFeed parseFeedEntry(XmlParser xmlParser) throws IOException {
      String id = null;
      String title = null;
      String url = null;
      ParseEvent nextEvent = xmlParser.peek();
      while (!isEnd(nextEvent, "entry")) {
          String name = nextEvent.getName();
          int type = nextEvent.getType();
          xmlParser.read();
          if ((type == Xml.START_TAG)) {
              if ("id".equals(name)) {
                  id = parseTextNode(xmlParser, "id");
              } else if ("title".equals(name)) {
                  title = parseTextNode(xmlParser, "title");
              } else if ("link".equals(name)) {
                  if (url == null) {
                      Attribute rel = nextEvent.getAttribute("rel");
                      if (rel != null && "alternate".equals(rel.getValue())) {
                          Attribute href = nextEvent.getAttribute("href");
                          if (href != null) {
                              url = href.getValue();
                          }
                      }
                  }
              }
          }
          nextEvent = xmlParser.peek();
      }
      if (id != null && title != null && url != null) {
          return new GCalFeed(id, title, url);
      } else {
//#ifdef DEBUG_ERR
          System.out.println("Failed to parse feed.");
//#endif
          return null;
      }
  }


    private boolean isEnd(ParseEvent event) {
        return event.getType() == Xml.END_DOCUMENT;
    }

    private boolean isEnd(ParseEvent event, String tagName) {
        return isEnd(event) || ((event.getType() == Xml.END_TAG) && (event.getName().equals(tagName)));
    }

    private boolean isCancelled(String eventStatus) {
        return (eventStatus != null) && eventStatus.endsWith("canceled");
    }

    private long[] parseWhen(ParseEvent event) {
        long[] when = new long[2];
        Attribute startTime = event.getAttribute("startTime");
        if ((startTime != null) && (startTime.getValue() != null)) {
            //System.out.println("Start time: " + startTime.getValue());
            when[0] = DateUtil.isoDateToLong(startTime.getValue());
        }
        Attribute endTime = event.getAttribute("endTime");
        if ((endTime != null) && (endTime.getValue() != null)) {
            //System.out.println("End time: " + endTime.getValue());
            when[1] = DateUtil.isoDateToLong(endTime.getValue());
        }
        return when;
    }

	/**
     * Parses the "value" or "valueString" attribute from the given
     * <code>ParseEvent</code>
	 * 
     * @param event <code>ParseEvent</code> to parse
     * @returns value of "value"/"valueString" attribute
	 */
    private String parseValue(ParseEvent event) {

		String str;

		str = event.getValueDefault("value", null);
		if (str == null) {
			str = event.getValueDefault("valueString", null);
		}

		return str;
    }

    private String parseTextNode(XmlParser xmlParser, String name) throws IOException {
        ParseEvent nextEvent = xmlParser.peek();
        while (!isEnd(nextEvent)) {
            String nextName = nextEvent.getName();
            int type = nextEvent.getType();

            if ((type == Xml.END_TAG) && name.equals(nextName)) {
                // end tag found
                return null;
            }

            xmlParser.read();
            if (type == Xml.TEXT) {
                String text = nextEvent.getText();
                //System.out.println("Found event: " + name + "=" + text);
                return text;
            }
            nextEvent = xmlParser.peek();
        }
        // no more events found
        return null;
    }

    public String parseUploadResponse(byte[] uploadResponse) throws IOException {
        XmlParser xmlParser = new XmlParser(new InputStreamReader(new ByteArrayInputStream(uploadResponse)), 300);
        return parseTextNode(xmlParser, "id");
    }

}
