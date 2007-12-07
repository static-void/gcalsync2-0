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
import com.gcalsync.cal.*;
import com.gcalsync.option.Options;
import com.gcalsync.cal.gcal.GCalFeed;
import com.gcalsync.store.Storable;
import com.gcalsync.store.StoreController;
import javax.microedition.rms.RecordStore;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 23 $
 * @date $Date$
 * <p/>
 */
public class Store {

    private static final String RECORD_STORE_NAME = "GcalSyncOptions";

    private static StoreController storeController;

    private static Options options;
    private static Timestamps timestamps;
    private static IdCorrelator idCorrelator;
    private static GCalFeed[] feeds;

    static {
        try {
            storeController = new StoreController(RECORD_STORE_NAME, new RecordTypes());
        } catch (Exception e) {
            ErrorHandler.showError("Failed to initailize store", e);
        }
    }

    public static Options getOptions() {
        if (options == null) {
			try { options = (Options) storeController.read(RecordTypes.OPTIONS); }
			catch (Exception e) 
			{ 
//#ifdef DEBUG_ERR
//# 				System.out.println("getOptions() failed...using default values, err=" + e);
//#endif
				options = null;
			}

			if (options == null)
			{
				options = new Options();
			}
        }
        return options;
    }

	public static void deleteRecordStore()
	{
		try {
			RecordStore.deleteRecordStore(RECORD_STORE_NAME);
			options = new Options();
		}
		catch (Exception e) {
//#ifdef DEBUG_ERR
//# 			System.out.println("Failed to delete record store: " + e.toString());
//#endif
		}
	}

    public static void setOptions(Options options) {
        Store.options = options;
    }

    public static Timestamps getTimestamps() {
        if (timestamps == null) {
            try { timestamps = (Timestamps) storeController.read(RecordTypes.TIMESTAMPS); }
			catch (Exception e) 
			{ 
//#ifdef DEBUG_ERR
//# 				System.out.println("getTimestamps() failed...using default values");
//#endif
				timestamps = null; 
			}

			if (timestamps == null)
			{
				timestamps = new Timestamps(); 
			}
        }
        return timestamps;
    }


    public static void setTimestamps(Timestamps timestamps) {
        Store.timestamps = timestamps;
    }

    public static IdCorrelator getIdCorrelator() {
        if (idCorrelator == null) {
            idCorrelator = new IdCorrelator(storeController.readAll(RecordTypes.ID_CORRELATION));
        }
        //idCorrelator.printContents();
        return idCorrelator;
    }

    public static void setIdCorrelator(IdCorrelator idCorrelator) {
        Store.idCorrelator = idCorrelator;
    }

    public static void addCorrelation(IdCorrelation idCorrelation) {
        getIdCorrelator().addCorrelation(idCorrelation);
        storeController.write(idCorrelation);
    }

    public static GCalFeed[] getFeeds() {
        if (feeds == null) {
			Storable[] storedFeeds;
            try { storedFeeds = storeController.readAll(RecordTypes.FEED); }
			catch (Exception e) 
			{ 
//#ifdef DEBUG_ERR
//# 				System.out.println("getFeeds() failed...using default values");
//#endif
				storedFeeds = null;
			}
            if (storedFeeds == null) {
                feeds = new GCalFeed[0];
            } else {
                feeds = new GCalFeed[storedFeeds.length];
                for (int i = 0; i < storedFeeds.length; i++) {
                    feeds[i] = (GCalFeed) storedFeeds[i];
                }                
            }
        }
        return feeds;
    }


    public static void setFeeds(GCalFeed[] feeds) {
        Store.feeds = feeds;
    }

    public static void saveOptions() {
        storeController.write(options);
    }

    public static void saveTimestamps() {
        storeController.write(timestamps);
    }

    public static void saveFeeds() {
        storeController.writeAll(feeds);
    }

	public static void deleteFeeds() {
		storeController.deleteRecords(RecordTypes.FEED);
	}
}
