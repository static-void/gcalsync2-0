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

import com.gcalsync.store.factory.*;
import com.gcalsync.store.RecordTypeMapper;
import com.gcalsync.store.StorableFactory;
import com.gcalsync.store.StoreException;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 19 $
 * @date $Date$
 */
public class RecordTypes implements RecordTypeMapper {

    public static final byte OPTIONS = 1;
    public static final byte TIMESTAMPS = 2;
    public static final byte ID_CORRELATION = 3;
    public static final byte FEED = 4;

    private OptionsFactory optionsFactory = new OptionsFactory();
    private TimestampsFactory timestampsFactory = new TimestampsFactory();
    private IdCorrelationFactory idCorrelationFactory = new IdCorrelationFactory();
    private GCalFeedFactory feedFactory = new GCalFeedFactory();

    public StorableFactory getFactory(byte recordType) {
        switch (recordType) {
            case OPTIONS:
                return optionsFactory;
            case TIMESTAMPS:
                return timestampsFactory;
            case ID_CORRELATION:
                return idCorrelationFactory;
            case FEED:
                return feedFactory;
            default:
                throw new StoreException("Unknown record type " + recordType);
        }
    }
}
