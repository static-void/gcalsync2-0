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
package com.gcalsync.store.factory;

import com.gcalsync.store.Storable;
import com.gcalsync.store.StorableFactory;
import com.gcalsync.cal.Timestamps;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 1 $
 * @date $Date$
 */
public class TimestampsFactory implements StorableFactory {

    public Storable create() {
        return new Timestamps();
    }
}
