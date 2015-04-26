#FAQ.

# Introduction #

This is a FAQ for GCalSync2.

# Installation Issues #
**How do you install it?**

Using your phone, go to http://gcalsync.mobi and your phone will be prompted to
download GCalSync2.

For NOKIA Phones (you might need to allow downloading unsigned JAR)

1) Go to Tools

2) Click on App.Mgr

3) Select Options->Settings

4) Change Software Installation from Signed Only to "All"

5) Change Online Certif. Check to "Off"

And then try to download application.

o SAMSUNG A1200 PHONES

1. Install gcalsync to the memory card of the phone (not the phone itself)

2. Put the phone in mass storage mode and plug into your computer

3. Set your folder view to be able to see invisible files & folders

4. go to .system/java/DownloadApps/MIDletxxx/registry.txt and change the following values:

Domain: untrusted -> Domain: Manufacturer
Is-Trusted: 0 -> Is-Trusted: 1
DRM-Mode: Forbidden -> DRM-Mode: Allowed

5. Save the changes and and all should be well.

Originally posted here: http://www.motorolafans.com/forums/showthread.php?t=7466

**GCalSync2 installs and runs fine but it won't login. Why?**

o BlackBerry APN Settings


Set the following option
Options>Advanced Options>TCP>APN set to the following:

  * US T-Mobile: wap.voicestream.com
  * US Cellular One: cellular1wap
  * Italy Vodafone: web.vodafone.it

_Note: For other provider, try to determine their APN settings if one exists_

You can find your phone's APN from this site: http://www.pinstack.com/carrier_settings_apn_gateway.html

o For all other BlackBerrys

- Go to options
- Go to security options
- Go to TLS
- Change TLS Default to Handled instead of Proxy

After making the changes, you may need to reboot your BlackBerry