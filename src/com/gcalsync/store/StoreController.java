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
package com.gcalsync.store;

import com.gcalsync.log.*;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 22 $
 * @date $Date$
 */
public class StoreController {

    private String name;
    private RecordStore recordStore;
    private RecordTypeMapper recordTypeMapper;
    private RecordTypeFilter recordFilter = new RecordTypeFilter();

    public StoreController(String name, RecordTypeMapper recordTypeMapper) {
        this.name = name;
        this.recordTypeMapper = recordTypeMapper;
    }

    public Storable read(int recordId) {
        openRecordStore();
        Storable object = null;
        object = readRecord(recordId);
        closeRecordStore();
        return object;
    }

    private Storable readRecord(int recordId) {
        ByteArrayInputStream byteIn = null;
        DataInputStream dataIn = null;
        byte recordType = -1;
        Storable object = null;
        try {
            byte[] record = recordStore.getRecord(recordId);
            recordType = record[0];
            byteIn = new ByteArrayInputStream(record, 1, record.length - 1);
            dataIn = new DataInputStream(byteIn);
            StorableFactory factory = recordTypeMapper.getFactory(recordType);
            object = factory.create();
            object.recordId = recordId;
            object.readRecord(dataIn);
//#ifdef DEBUG_INFO
            System.out.println("Read record " + recordId + " of type " + recordType);
//#endif
        } catch (InvalidRecordIDException e) {
            throw new StoreException("Failed to read record " + recordId + " ( " + recordType + " from " + name, e);
        } catch (IOException e) {
            throw new StoreException("Failed to read record " + recordId + " ( " + recordType + " from " + name, e);
        } catch (RecordStoreException e) {
            throw new StoreException("Failed to read record " + recordId + " ( " + recordType + ") from " + name, e);
        } finally {
            if (dataIn != null) {
                try {
                    dataIn.close();
                } catch (IOException e) {
//#ifdef DEBUG_ERR
                    ErrorHandler.log.append("Failed to close dataIn due to " + e);
                    e.printStackTrace();
//#endif
                }
            }
            if (byteIn != null) {
                try {
                    byteIn.close();
                } catch (IOException e) {
//#ifdef DEBUG_ERR
                    ErrorHandler.log.append("Failed to close byteIn due to " + e);
                    e.printStackTrace();
//#endif
                }
            }
        }
        return object;
    }

    public Storable read(byte recordType) {
        Storable[] objects = readAll(recordType);
        if (objects == null || (objects != null && objects.length == 0)) {
            return null;
        }
		
        if (objects.length > 1) {
            throw new StoreException("name is not unique, found " + objects.length + " records");
        }
        return objects[0];
    }

    public Storable[] readAll(byte recordType) {
        openRecordStore();
        RecordEnumeration recordEnumeration = getRecordEnumeration(recordType);
        int recordCount = recordEnumeration.numRecords();
        Storable[] result = new Storable[recordCount];

        try {
            for (int i = 0; i < recordCount; i++) {
                try {
                    result[i] = readRecord(recordEnumeration.nextRecordId());
                } catch (InvalidRecordIDException e) {
                    throw new StoreException("Failed to read record from " + name, e);
                }
            }
        } finally {
            recordEnumeration.destroy();
            closeRecordStore();
        }

        return result;
    }

	public void deleteRecords(byte recordType)
	{
		try
		{
			//get all records of the specified type
			Storable[] st = readAll(recordType);

			//open the record store to begin deleting records,
			//delete the records, and then close the store
			openRecordStore();

			for (int i=0; i<st.length; i++)
				recordStore.deleteRecord(st[i].recordId);

			closeRecordStore();
		}
		catch (Exception e)
		{
//#ifdef DEBUG_ERR
			System.out.println("deleteRec(): " + e);
//#endif
		}
	}

    public void write(Storable object) {
        openRecordStore();
        writeRecord(object);
        closeRecordStore();
    }

    private void writeRecord(Storable object) {
        if (object == null) {
            throw new IllegalArgumentException("Attempt to store null object");
        }
        ByteArrayOutputStream byteOut = null;
        DataOutputStream dataOut = null;
        try {
            byteOut = new ByteArrayOutputStream();
            byteOut.write(object.recordType);
            dataOut = new DataOutputStream(byteOut);
            object.writeRecord(dataOut);
            dataOut.flush();
            byte [] newRecord = byteOut.toByteArray();
            openRecordStore();
            if (object.recordId > 0) {
                recordStore.setRecord(object.recordId, newRecord, 0, newRecord.length);
            } else {
                object.recordId = recordStore.addRecord(newRecord, 0, newRecord.length);
            }
//#ifdef DEBUG_INFO
            System.out.println("Wrote record " + object.recordId + " of type " + object.recordType);
//#endif
        } catch (RecordStoreException e) {
            throw new StoreException("Failed to write record " + object.recordId + " ( " + object + ") to " + name, e);
        } catch (IOException e) {
            throw new StoreException("Failed to write record " + object.recordId + " ( " + object + ") to " + name, e);
        } finally {
            closeRecordStore();
            if (dataOut != null) {
                try {
                    dataOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (byteOut != null) {
                try {
                    byteOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void writeAll(Storable[] objects) {
        if ((objects == null) || (objects.length == 0)) {
            // nothing to write
            return;
        }
        openRecordStore();
        try {
            for (int i = 0; i < objects.length; i++) {
                writeRecord(objects[i]);
            }
        } finally {
            closeRecordStore();
        }
    }

    private void openRecordStore() {
        try {
            recordStore = RecordStore.openRecordStore(name, true, RecordStore.AUTHMODE_PRIVATE, false);
        } catch (RecordStoreException e) {
            throw new StoreException("Unable to open record store " + name, e);
        }
    }

    private void closeRecordStore() {
        try {
            recordStore.closeRecordStore();
        } catch (RecordStoreException e) {
//#ifdef DEBUG_ERR
            ErrorHandler.log.append("Failed to close record store " + name + " due to " + e + "\n");
//#endif
        }
    }

    private RecordEnumeration getRecordEnumeration(byte recordType) {
        recordFilter.recordType = recordType;
        try {
            return recordStore.enumerateRecords(recordFilter, null, false);
        } catch (RecordStoreNotOpenException e) {
            throw new StoreException("Record store " + name + " not open", e);
        }
    }

}


