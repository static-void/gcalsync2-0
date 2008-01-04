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

import com.gcalsync.store.Storable;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 9 $
 * @date $Date$
 * TODO:
 * - write test, observed strange behiour with phoneToGcalId.size() = 8 and gcalIdToPhone.size() = 6
 * - delete records expired before sync period start
 */
public class IdCorrelator {

    public Hashtable phoneIdToGcalId;
    public Hashtable gcalIdToPhoneId;
    //private int[] obsoleteRecords;
    public Vector mainCalendarPhoneIds; //save which events are from the main calendar

    public IdCorrelator(Storable[] idCorrelations) {
        phoneIdToGcalId = new Hashtable(idCorrelations.length);
        gcalIdToPhoneId = new Hashtable(idCorrelations.length);
        mainCalendarPhoneIds = new Vector(idCorrelations.length);
        for (int i = 0; i < idCorrelations.length; i++) {
            addCorrelation((IdCorrelation) idCorrelations[i]);
        }
    }
//#ifdef DEBUG
//#     public void printContents() {
//#         // TODO: remove
//#         System.out.println("p->g: " + phoneIdToGcalId.size() + ", g->p: " + gcalIdToPhoneId.size());
//#         for (Enumeration i = phoneIdToGcalId.keys(); i.hasMoreElements();) {
//#             String phoneId = (String) i.nextElement();
//#             String gCalId = null;
//#             String reversePimId = null;
//#             if (phoneId != null) {
//#                 gCalId = (String) phoneIdToGcalId.get(phoneId);
//#             }
//#             if (gCalId != null) {
//#                 reversePimId = (String) gcalIdToPhoneId.get(gCalId);
//#             }
//#             System.out.println(phoneId + "->" + gCalId + "<-" + reversePimId);
//#         }
//#     }
//#endif

    public void addCorrelation(IdCorrelation idCorrelation) {
        phoneIdToGcalId.put(idCorrelation.phoneCalId, idCorrelation.gCalId);
        gcalIdToPhoneId.put(idCorrelation.gCalId, idCorrelation.phoneCalId);
        if(idCorrelation.isMainCalendarEvent)
            mainCalendarPhoneIds.addElement(idCorrelation.phoneCalId);
    }
}
