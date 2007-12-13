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
package com.gcalsync.util;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.gcalsync.cal.gcal.NoSuchCalendarException;
import java.io.UnsupportedEncodingException;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 7 $
 * @date $Date$
 */
public class HttpUtil {

	static int lastResponseCode = 0;
	static String lastResponseMsg = null;

	public static int getLastResponseCode()
	{
		return lastResponseCode;
	}
	public static String getLastResponseMsg()
	{
		return lastResponseMsg;
	}

    public static byte[] sendRequest(String url, String method, String postData, String authorization) {
        return sendRequest(url, method, postData, authorization, "application/x-www-form-urlencoded");
    }

    public static byte[] sendAtomRequest(String url, String method, String postData, String authorization) {
        return sendRequest(url, method, postData, authorization, "application/atom+xml");
    }

    public static byte[] sendRequest(String url, String method, String postData, String authorization, String contentType) {
//#ifdef DEBUG_INFO
//#         log("Sending request: url='" + url + "', method='" + method + "', postData='" + postData + "', authorization='" + authorization + "', contentType='" + contentType + "'");
//#endif
        HttpConnection connection = null;
        OutputStream out = null;
        DataInputStream in = null;
        byte[] responseData = null;
        int status = -1;

        try {
            // Open the connection and check for re-directs
            while (true) {
                connection = (HttpConnection) Connector.open(url);
                if (connection == null) {
                    throw new IllegalStateException("null connection when opening " + url);
                }

                if (HttpConnection.GET.equals(method) || HttpConnection.POST.equals(method)) {
                    connection.setRequestMethod(method);
                } else {
                    connection.setRequestMethod(HttpConnection.POST);
                    connection.setRequestProperty("X-HTTP-Method-Override", method);
                }
                if (authorization != null) {
                    connection.setRequestProperty("Authorization", authorization);
                }
                if (postData != null) {
                    byte[] data = null;
                    try {
                        data = postData.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        data = postData.getBytes();
                    }
                    connection.setRequestProperty("Content-Type", contentType);
                    connection.setRequestProperty("Content-Length", Integer.toString(data.length));
                    out = connection.openOutputStream();
                    out.write(data);

                    try {
                        out.close();
                    } catch (IOException e) {
//#ifdef DEBUG_ERR
//#                         log("failed to close out for " + url + " due to " + e);
//#endif
                        e.printStackTrace();
                    }
                    out = null;
                }

                // Get the status code, causing the connection to be made
                status = connection.getResponseCode();
				lastResponseCode = status;
				lastResponseMsg = connection.getResponseMessage();
//#ifdef DEBUG_INFO
//#                 System.out.println("HTTP status code: " + status);
//#endif
                if (status == HttpConnection.HTTP_TEMP_REDIRECT ||
                        status == HttpConnection.HTTP_MOVED_TEMP ||
                        status == HttpConnection.HTTP_MOVED_PERM) {
                    // Get the new location and close the connection
                    url = connection.getHeaderField("location");
                    connection.close();
//#ifdef DEBUG_INFO
//#                     log("Redirecting to " + url);
//#endif
                } else {
                    // no redirect
                    break;
                }
            }

            int length = (int) connection.getLength();
            if (length > 0) {
                responseData = new byte[length];
                in = new DataInputStream(connection.openInputStream());
                in.readFully(responseData);
            } else {
                // If content length is not given, read in chunks.
                int chunkSize = 1024;
                int chunkNumber = 0;
                int index = 0;
                int readLength = 0;
                in = new DataInputStream(connection.openInputStream());
                responseData = new byte[chunkSize];
                do {
                    if (responseData.length < index + chunkSize) {
                        byte[] newData = new byte[index + chunkSize];
                        System.arraycopy(responseData, 0, newData, 0, responseData.length);
                        responseData = newData;
                    }
                    readLength = in.read(responseData, index, chunkSize);
                    index += readLength;
                    chunkNumber++;
                    //progressGauge.updateLabel("Downloading (" + chunkNumber + " kB)...");
                } while (readLength > 0);
                length = index;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("HTTP connection to " + url + " failed due to " + e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (status != 200) {
            String responseString = new String(responseData);
            if (responseString.indexOf("Cannot access the calendar you requested") >= 0) {
                throw new NoSuchCalendarException();
            }
			else {
				lastResponseMsg += ": " + responseString;
			}
        }

        return responseData;
    }
//#if DEBUG || DEBUG_INFO || DEBUG_WARN || DEBUG_ERR
//#     private static void log(String message) {
//#         System.out.println(message);
//#     }
//#endif
}
