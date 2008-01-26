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

import com.gcalsync.store.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author: thomasold $
 * @version $Rev: 19 $
 * @date $Date: 2006-12-21 16:42:52 -0500 (Thu, 21 Dec 2006) $
 */
public class GCalFeed extends Storable {

    public String id;
    public String title;
    public String url;
    public String prefix;
    public boolean reminders;
    public boolean sync;
	
    public GCalFeed() {
        super(RecordTypes.FEED);
    }

    public GCalFeed(String id, String title, String url) {
        this();
        this.id = id;
        this.title = title;
        this.url = url;
        this.prefix = "";
        this.sync = false;
        this.reminders = false;

    }

    public void readRecord(DataInputStream in) throws IOException {
        id = in.readUTF();
        title = in.readUTF();
        url = in.readUTF();
        prefix = in.readUTF();
        sync = in.readBoolean();
        reminders = in.readBoolean();
    }

    public void writeRecord(DataOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(title);
        out.writeUTF(url);
        out.writeUTF(prefix);
        out.writeBoolean(sync);
        out.writeBoolean(reminders);
    }
}
