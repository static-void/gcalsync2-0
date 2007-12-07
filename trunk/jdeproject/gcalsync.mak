#
# Warning! This file has been generated by the RIM Development environment.
# Do not modify by hand.
#

all: \
     GCalSync2_0_3.cod \


clean:
	@if exist GCalSync2_0_3.cod erase GCalSync2_0_3.cod > nul
	@if exist GCalSync2_0_3.lst erase GCalSync2_0_3.lst > nul
	@if exist GCalSync2_0_3.debug erase GCalSync2_0_3.debug > nul
	@if exist GCalSync2_0_3.csl erase GCalSync2_0_3.csl > nul
	@if exist GCalSync2_0_3.cso erase GCalSync2_0_3.cso > nul
	@if exist GCalSync2_0_3-*.lst erase GCalSync2_0_3-*.lst > nul
	@if exist GCalSync2_0_3-*.debug erase GCalSync2_0_3-*.debug > nul

rebuild: clean all

Private: \


Private_files:


Debug: \
    GCalSync2_0_3.cod \


Debug_files:
	@echo C:\data\GCalSync2.0\jdeproject\GCalSync2_0_3


Release: \
    GCalSync2_0_3.cod \


Release_files:
	@echo C:\data\GCalSync2.0\jdeproject\GCalSync2_0_3


gcalsync_sources = \
    ..\src\com\gcalsync.png \
    ..\src\com\gcalsync\cal\CommitEngine.java \
    ..\src\com\gcalsync\cal\gcal\GCalClient.java \
    ..\src\com\gcalsync\cal\gcal\GCalEvent.java \
    ..\src\com\gcalsync\cal\gcal\GCalFeed.java \
    ..\src\com\gcalsync\cal\gcal\GCalParser.java \
    ..\src\com\gcalsync\cal\gcal\NoSuchCalendarException.java \
    ..\src\com\gcalsync\cal\IdCorrelation.java \
    ..\src\com\gcalsync\cal\IdCorrelator.java \
    ..\src\com\gcalsync\cal\Merger.java \
    ..\src\com\gcalsync\cal\phonecal\PhoneCalClient.java \
    ..\src\com\gcalsync\cal\Recurrence.java \
    ..\src\com\gcalsync\cal\SyncEngine.java \
    ..\src\com\gcalsync\cal\Timestamps.java \
    ..\src\com\gcalsync\component\AboutComponent.java \
    ..\src\com\gcalsync\component\AutoSyncComponent.java \
    ..\src\com\gcalsync\component\AutosyncPeriodComponent.java \
    ..\src\com\gcalsync\component\CalendarFeedsComponent.java \
    ..\src\com\gcalsync\component\CommitComponent.java \
    ..\src\com\gcalsync\component\Components.java \
    ..\src\com\gcalsync\component\LoginComponent.java \
    ..\src\com\gcalsync\component\MVCComponent.java \
    ..\src\com\gcalsync\component\OptionsComponent.java \
    ..\src\com\gcalsync\component\PeriodComponent.java \
    ..\src\com\gcalsync\component\PreviewComponent.java \
    ..\src\com\gcalsync\component\PublicCalendarsComponent.java \
    ..\src\com\gcalsync\component\ResetOptionsComponent.java \
    ..\src\com\gcalsync\component\SyncComponent.java \
    ..\src\com\gcalsync\component\TimeZoneComponent.java \
    ..\src\com\gcalsync\component\UploadDownloadComponent.java \
    ..\src\com\gcalsync\GCalSync.java \
    ..\src\com\gcalsync\log\ErrorHandler.java \
    ..\src\com\gcalsync\log\StatusLogger.java \
    ..\src\com\gcalsync\option\Options.java \
    ..\src\com\gcalsync\store\factory\GCalFeedFactory.java \
    ..\src\com\gcalsync\store\factory\IdCorrelationFactory.java \
    ..\src\com\gcalsync\store\factory\OptionsFactory.java \
    ..\src\com\gcalsync\store\factory\TimestampsFactory.java \
    ..\src\com\gcalsync\store\RecordTypeFilter.java \
    ..\src\com\gcalsync\store\RecordTypeMapper.java \
    ..\src\com\gcalsync\store\RecordTypes.java \
    ..\src\com\gcalsync\store\Storable.java \
    ..\src\com\gcalsync\store\StorableFactory.java \
    ..\src\com\gcalsync\store\Store.java \
    ..\src\com\gcalsync\store\StoreController.java \
    ..\src\com\gcalsync\store\StoreException.java \
    ..\src\com\gcalsync\util\DateUtil.java \
    ..\src\com\gcalsync\util\HttpsUtil.java \
    ..\src\com\gcalsync\util\HttpUtil.java \
    ..\src\lib\harmony.jar \
    ..\src\lib\kxml-min.jar \

gcalsync_dependencies = \
    gcalsync.jdp \
    GCalSync2_0_3.rapc \
    "..\..\..\Program Files\Research In Motion\BlackBerry JDE 4.1.0\lib\net_rim_api.jar" \

GCalSync2_0_3.cod : $(gcalsync_sources) $(gcalsync_dependencies)
	@if exist GCalSync2_0_3.cod erase GCalSync2_0_3.cod > nul
	@if exist GCalSync2_0_3.lst erase GCalSync2_0_3.lst > nul
	@if exist GCalSync2_0_3.debug erase GCalSync2_0_3.debug > nul
	@if exist GCalSync2_0_3.csl erase GCalSync2_0_3.csl > nul
	@if exist GCalSync2_0_3.cso erase GCalSync2_0_3.cso > nul
	@if exist GCalSync2_0_3-*.lst erase GCalSync2_0_3-*.lst > nul
	@if exist GCalSync2_0_3-*.debug erase GCalSync2_0_3-*.debug > nul
	@echo Building gcalsync ...
	@echo ..\src\com\gcalsync.png> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\CommitEngine.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\gcal\GCalClient.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\gcal\GCalEvent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\gcal\GCalFeed.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\gcal\GCalParser.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\gcal\NoSuchCalendarException.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\IdCorrelation.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\IdCorrelator.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\Merger.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\phonecal\PhoneCalClient.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\Recurrence.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\SyncEngine.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\cal\Timestamps.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\AboutComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\AutoSyncComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\AutosyncPeriodComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\CalendarFeedsComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\CommitComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\Components.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\LoginComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\MVCComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\OptionsComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\PeriodComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\PreviewComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\PublicCalendarsComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\ResetOptionsComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\SyncComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\TimeZoneComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\component\UploadDownloadComponent.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\GCalSync.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\log\ErrorHandler.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\log\StatusLogger.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\option\Options.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\factory\GCalFeedFactory.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\factory\IdCorrelationFactory.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\factory\OptionsFactory.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\factory\TimestampsFactory.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\RecordTypeFilter.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\RecordTypeMapper.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\RecordTypes.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\Storable.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\StorableFactory.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\Store.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\StoreController.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\store\StoreException.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\util\DateUtil.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\util\HttpsUtil.java>> gcalsync_sources.files
	@echo ..\src\com\gcalsync\util\HttpUtil.java>> gcalsync_sources.files
	@echo ..\src\lib\harmony.jar>> gcalsync_sources.files
	@echo ..\src\lib\kxml-min.jar>> gcalsync_sources.files
	@"C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\rapc.exe"  -quiet import="..\..\..\Program Files\Research In Motion\BlackBerry JDE 4.1.0\lib\net_rim_api.jar" codename=GCalSync2_0_3 -midlet GCalSync2_0_3.rapc warnkey=0x52424200 @gcalsync_sources.files
	@if exist "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.cod" erase "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.cod" > nul
	@if exist GCalSync2_0_3.cod copy GCalSync2_0_3.cod "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.cod" > nul
	@if exist "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.jar" erase "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.jar" > nul
	@if exist GCalSync2_0_3.jar copy GCalSync2_0_3.jar "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.jar" > nul
	@if exist "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.lst" erase "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.lst" > nul
	@if exist GCalSync2_0_3.lst copy GCalSync2_0_3.lst "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.lst" > nul
	@if exist "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.debug" erase "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.debug" > nul
	@if exist GCalSync2_0_3.debug copy GCalSync2_0_3.debug "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.debug" > nul
	@if exist "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.csl" erase "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.csl" > nul
	@if exist GCalSync2_0_3.csl copy GCalSync2_0_3.csl "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.csl" > nul
	@if exist "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.cso" erase "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.cso" > nul
	@if exist GCalSync2_0_3.cso copy GCalSync2_0_3.cso "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3.cso" > nul
	@if exist "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3-*.lst" erase "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3-*.lst" > nul
	@if exist GCalSync2_0_3-*.lst copy GCalSync2_0_3-*.lst "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3-*.lst" > nul
	@if exist "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3-*.debug" erase "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3-*.debug" > nul
	@if exist GCalSync2_0_3-*.debug copy GCalSync2_0_3-*.debug "C:\Program Files\Research In Motion\BlackBerry JDE 4.1.0\bin\..\simulator\GCalSync2_0_3-*.debug" > nul


