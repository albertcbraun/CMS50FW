## Disclaimer 
This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 
 ## Credits 
This library was informed, inspired and guided significantly by Yujia Yan's GitHub project: 

https://github.com/Yujia-Yan/CMS50SerialProtocolKit 

which works on a slightly earlier version of the Contec CMS50. Many kind thanks to Yujia Yan for that code. Any misunderstandings or misinterpretations belong to me. 

## Contents of this Android Studio project

There are two Android Studio modules in this project. One is the android library project "cms50fwlib". The other is a test app named "cms50fwlibtestapp". 

The cms50fwlib project outputs an aar file. This library tries to simplify the Android code required to detect the <a href="http://www.amazon.com/Acc-bluetooth-enabled-Oximeter-SnugFit/dp/B00LKUHD9K/">CMS50FW Pulse Oximeter</a>, obtain a bluetooth connection to it, read the data stream, and turn it into a convenient object.

The cms50fw test app tries to read the Bluetooth data stream in real-time, writes pulse and oxygen level data to the screen, and also tries to alert the end user if it thinks it has detected a problem (e.g. oxygen level too low). 

### CMS50FWLib 

The basic steps for using the cms50fwlib are:

Implement a custom [CMS50FWConnectionListener](https://github.com/albertcbraun/CMS50FW/blob/master/cms50fwlib/src/main/java/com/albertcbraun/cms50fwlib/CMS50FWConnectionListener.java) instance for your app Activity or Fragment. (An example of this is found in the test app project.) 

Now, obtain an instance of the [CMS50FWBluetoothConnectionManager](https://github.com/albertcbraun/CMS50FW/blob/master/cms50fwlib/src/main/java/com/albertcbraun/cms50fwlib/CMS50FWBluetoothConnectionManager.java): 
````
cms50FWBluetoothConnectionManager = new CMS50FWBluetoothConnectionManager("SpO202"); 
````

Call setCMS50FWConnectionListener on the CMS50FWBluetoothConnectionManager instance, feeding in your custom implementation of CMS50FWConnectionListener: 
````
cms50FWBluetoothConnectionManager.setCMS50FWConnectionListener(cms50fwCallbacks);
````

At this point, you should have your Contec CMS50FW turned on and the 'wireless' setting should also be set to 'on'. 

Once that is done, call connect on the cms50FWBluetoothConnectionManager, feeding it an instance of [an Android Context object](http://developer.android.com/reference/android/content/Context.html): 
````
cms50FWBluetoothConnectionManager.connect(aContextObject) 
````

At this point, there will be a delay while the Bluetooth "machinery" on your Android device carries out discovery and tries to find the CMS50FW. It should take about 10 seconds (or less). 

If the connection succeeds, you will be called back on your implementation of the method CMS50FWConnectionListener.onConnectionEstablished. 

Once that has happened, you can ask the CMS50FW to start streaming data: 
````
cms50FWBluetoothConnectionManager.startData()
````
You should start to see callbacks about 60 times per second on your implementation of the method CMS50FWConnectionListener.onDataFrameArrived. The cms50fwlib library feeds onDataFrameArrived a convenient  [DataFrame](https://github.com/albertcbraun/CMS50FW/blob/master/cms50fwlib/src/main/java/com/albertcbraun/cms50fwlib/DataFrame.java) object whose properties can be read in your client. 


### CMS50FWLibTestApp 

The test app can be compiled as part of this Android Studio project or <a href="https://play.google.com/store/apps/details?id=com.albertcbraun.cms50fw.alert">installed directly from the Google Play Store.</a>

## More Disclaimers

This source code is for software testing, entertainment and educational purposes only. This source code is NOT to be used for medical, health care or fitness related purposes. This source code is neither certified nor approved by any government or regulatory agency. 

Manufacturer(s) and retailer(s) of the CMS50FW pulse oximeter do not support, endorse, or take responsibility for this source code. Neither the author of this source code nor the publisher accept any responsibility whatsoever for any issues, problems or complaints experienced by anyone who uses or relies upon this source code. 

The app author and app publisher accept no responsibility whatsoever for source code bugs, failures, crashes, expected or unexpected behavior. The author of this source code disclaims all liability for its use or abuse.

All responsibility and liability for using this source code belong solely to the user. If you use or in any way rely upon this source code, you agree that you are using it solely at your own risk. 





