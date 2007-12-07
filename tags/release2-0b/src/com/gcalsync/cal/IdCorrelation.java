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
package com.gcalsync.cal;

import com.gcalsync.store.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 9 $
 * @date $Date$
 */
public class IdCorrelation extends Storable {

    public String phoneCalId;
    public String gCalId;
    public long endDate;

    public IdCorrelation() {
        super(RecordTypes.ID_CORRELATION);
    }

    public void readRecord(DataInputStream in) throws IOException {
        phoneCalId = in.readUTF();
        gCalId = in.readUTF();
    }

    public void writeRecord(DataOutputStream out) throws IOException {
        out.writeUTF(phoneCalId);
        out.writeUTF(gCalId);
    }
}
