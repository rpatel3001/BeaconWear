THIS IS OBSOLETE, THE HOME ASSISTANT WEAR OS APP CAN NOW PRODUCE IBEACONS.

This app is intended to run on Wear OS 3 devices and transmit BLE iBeacon advertisements meant for presence detection and rough distance estimation. 

It has only been tested on my Galaxy Watch 4.

The UUID, major and minor numbers, and reference transmitter power are hard-coded in the source code and the UI is nonfunctional. As this meets my needs and I anticipate that the Home Assistant app will add this capability eventually, I won't be developing this any further. 

Credit to the good folks at @AltBeacon, this was forked from https://github.com/davidgyoung/android-beacon-library-reference-kotlin and modified with code from https://altbeacon.github.io/android-beacon-library/beacon-transmitter.html.
