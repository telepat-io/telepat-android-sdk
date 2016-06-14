##Android SDK for Telepat

## About

The Telepat Android SDK provides the necessary bindings to interact with the Telepat Sync API, as well as a GCM transport implementation for receiving updates from a Telepat cloud instance.

* * *

## Setting up the plugin

#### Adding the library

The Telepat Android SDK is available as an open-source gradle module that can be cloned from Github [Github](#), or as a Maven artifact that can be downloaded from the Telepat Maven repo:

*   io.telepat:android-sdk:0.4.0.1

You can easily add the Cast plugin as a Maven dependency:

            repositories {
            maven {
            url "https://maven.telepat.io.s3.amazonaws.com/releases"
            }
            }
            dependencies {
            compile 'io.telepat:telepat-android-sdk:0.4.0.2'
            }

* * *

## Usage

#### AndroidManifest dependencies

The Telepat SDK exposes some receivers in order to keep the GCM token valid. We also require some basic permissions for accessing the network. The full changes required for the host application's AndroidManifest file are listed below

          <uses-permission android:name="android.permission.INTERNET"/>
          <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
          <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
          <service android:name="io.telepat.sdk.networking.transports.gcm.GcmIntentService">}</service>

Inside the application tag:

          <receiver
                  android:name="io.telepat.sdk.networking.transports.gcm.GcmBroadcastReceiver"
                  android:permission="com.google.android.c2dm.permission.SEND">
              <intent-filter>
                  <action android:name="com.google.android.c2dm.intent.RECEIVE"/>
                  <action android:name="com.google.android.c2dm.intent.REGISTRATION"/>

                  <category android:name="io.telepat.kraken"/>
              </intent-filter>
          </receiver>

          <receiver android:name="io.telepat.sdk.networking.transports.gcm.PackageReplacedReceiver">
              <intent-filter>
                  <action android:name="android.intent.action.PACKAGE_REPLACED"/>
                  <data
                          android:path="com.appscend.kraken"
                          android:scheme="package"/>
              </intent-filter>
          </receiver>

          <receiver android:name="io.telepat.sdk.networking.transports.gcm.BootReceiver">
              <intent-filter>
                  <action android:name="android.intent.action.BOOT_COMPLETED"/>
              </intent-filter>
          </receiver>

</application>

Additional usage documentation can be found at: http://docs.telepat.io/android-sdk.html
