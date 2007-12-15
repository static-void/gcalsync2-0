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
package com.gcalsync.option;

import com.gcalsync.store.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author $Author$
 * @version $Rev: 19 $
 * @date $Date$
 */
public class Options extends Storable
{

	public static final int LATEST_CHANGE_WINS_MERGE = 1;
	public static final int GCAL_WINS_MERGE = 2;
	public static final int PHONE_WINS_MERGE = 3;

	public String username = "";
	public String password = "";
	public String calendarAddress = "";
	public String[] calendarUrls = new String[5];
	public int pastDays = 1;
	public int futureDays = 30;
	public boolean upload = true;
	public boolean download = true;
	public boolean useGCalTimeZone = false;
	public boolean autoLogin = false;
	public int mergeStrategy = LATEST_CHANGE_WINS_MERGE;
	public long downloadTimeZoneOffset = 0;
	public long uploadTimeZoneOffset = 0;
	public boolean preview = true;
	public boolean downloadAllCalendars = false;
        public int autosyncTime = 30; //time between autosync in minutes

	static final int DEFAULT_RECORD_VERSION = 2;

	public Options()
	{
		super(RecordTypes.OPTIONS);

		for (int i=0; i<calendarUrls.length; i++)
			calendarUrls[i] = "";
	}

	public void readRecord(DataInputStream in) throws IOException 
	{
		int version;

		//read record version
		version = in.readInt();

		if (version >= 1)
		{
			username = in.readUTF();
			password = in.readUTF();
	
			for (int i=0; i<calendarUrls.length; i++)
				calendarUrls[i] = in.readUTF();
	
			pastDays = in.readInt();
			futureDays = in.readInt();
			useGCalTimeZone = in.readBoolean();
			downloadTimeZoneOffset = in.readLong();
			uploadTimeZoneOffset = in.readLong();
			upload = in.readBoolean();
			download = in.readBoolean();
			autoLogin = in.readBoolean();
		}
                
                if(version >= 2) {
                    autosyncTime = in.readInt();
                }
	}

	public void writeRecord(DataOutputStream out) throws IOException 
	{
		out.writeInt(DEFAULT_RECORD_VERSION);

		out.writeUTF(username);
		out.writeUTF(password);

		for (int i=0; i<calendarUrls.length; i++)
			out.writeUTF(calendarUrls[i]);

		out.writeInt(pastDays);
		out.writeInt(futureDays);
		out.writeBoolean(useGCalTimeZone);
		out.writeLong(downloadTimeZoneOffset);
		out.writeLong(uploadTimeZoneOffset);
		out.writeBoolean(upload);
		out.writeBoolean(download);
		out.writeBoolean(autoLogin);
                
                //version 2
                out.writeInt(autosyncTime);
	}
}
