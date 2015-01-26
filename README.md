DISCLAIMER: This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 
# Contents 

There are two Android Studio modules in this project. One is the android library project "cms50fwlib". The other is a test app named "cms50fwlibtestapp". 

The lib tries to simplify the Android code required to detect the CMS50FW Pulse Oximeter, obtain a bluetooth connection to it, read the data stream, and turn it into a convenient object.

The test app reads the data stream in real-time over bluetooth, and tries to alert the end user if it thinks it has detected a problem. 

## CMS50FWLib 

The basic steps to using this library are: 

Implement a custom CMS50FWConnectionListener instance for your app Activity or Fragment.

Get an instance of the CMS50FWBluetoothConnectionManager: 

    cms50FWBluetoothConnectionManager = new CMS50FWBluetoothConnectionManager("SpO202"); // BT name of your CMS50FW 

Call setCMS50FWConnectionListener on the CMS50FWBluetoothConnectionManager instance, feeding in your custom implementation of CMS50FWConnectionListener: 

    cms50FWBluetoothConnectionManager.setCMS50FWConnectionListener(cms50fwCallbacks);

Call connect on the cms50FWBluetoothConnectionManager, feeding it an instance of Context: 

    cms50FWBluetoothConnectionManager.connect(aContextObject) 


## CMS50FWLibTestApp 

The test app can be compiled as part of this Android Studio project or <a href="https://play.google.com/store/apps/details?id=com.albertcbraun.cms50fw.alert">installed directly from the Google Play Store.</a>

# MORE DISCLAIMERS

This source code is for software testing, entertainment and educational purposes only. This source code is NOT to be used for medical, health care or fitness related purposes. This source code is neither certified nor approved by any government or regulatory agency. 

Manufacturer(s) and retailer(s) of the CMS50FW pulse oximeter do not support, endorse, or take responsibility for this source code. Neither the author of this source code nor the publisher accept any responsibility whatsoever for any issues, problems or complaints experienced by anyone who uses or relies upon this source code. 

The app author and app publisher accept no responsibility whatsoever for source code bugs, failures, crashes, expected or unexpected behavior. The author of this source code disclaims all liability for its use or abuse.

All responsibility and liability for using this source code belong solely to the user. If you use or in any way rely upon this source code, you agree that you are using it solely at your own risk. 
