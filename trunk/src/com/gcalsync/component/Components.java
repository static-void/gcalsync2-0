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
package com.gcalsync.component;

import com.gcalsync.log.ErrorHandler;

/**
 * @author Thomas Oldervoll, thomas@zenior.no
 * @author $Author$
 * @version $Rev: 25 $
 * @date $Date$
 */
public class Components {

    public static OptionsComponent options = new OptionsComponent();
    public static LoginComponent login = null;
    public static PeriodComponent period = new PeriodComponent();
    public static TimeZoneComponent timeZone = new TimeZoneComponent();
    public static UploadDownloadComponent uploadDownload = new UploadDownloadComponent();
    public static CalendarFeedsComponent feeds = new CalendarFeedsComponent();
	public static ResetOptionsComponent resetOptions = new ResetOptionsComponent();
	public static PublicCalendarsComponent pubCal = new PublicCalendarsComponent();
    public static AutosyncPeriodComponent autosyncPeriodComponent = new AutosyncPeriodComponent();
    
    static {
        try {
            login = new LoginComponent();
        }catch(Exception e) {
            ErrorHandler.showError(e);
        }
    }
}
