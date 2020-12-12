# What is Rudder?

**Short answer:** 
Rudder is an open-source Segment alternative written in Go, built for the enterprise.

**Long answer:** 
Rudder is a platform for collecting, storing and routing customer event data to dozens of tools. Rudder is open-source, can run in your cloud environment (AWS, GCP, Azure or even your data-centre) and provides a powerful transformation framework to process your event data on the fly.

Released under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

## Getting Started with Amplitude Integration of Android SDK
1. Add [Amplitude](https://amplitude.com) as a destination in the [Dashboard](https://app.rudderstack.com/) and define `apiKey` and all other applicable settings .

2. Add the dependency under ```dependencies```
```
implementation 'com.rudderstack.android.sdk:core:1.+'
implementation 'com.rudderstack.android.integration:amplitude:1.0.0'
implementation 'com.google.code.gson:gson:2.8.6'

// For using Google Advertising Id as device id
implementation 'com.google.android.gms:play-services-ads:18.3.0'

```

3. Add these lines to your ```app/build.gradle``` under ```compileOptions``` in the ```android``` tag
```
compileOptions {
  sourceCompatibility JavaVersion.VERSION_1_8
  targetCompatibility JavaVersion.VERSION_1_8
}
```

## Initialize ```RudderClient```
```
val rudderClient = RudderClient.getInstance(
    this,
    WRITE_KEY,
    RudderConfig.Builder()
        .withDataPlaneUrl(DATA_PLANE_URL)
        .withFactory(AmplitudeIntegrationFactory.FACTORY)
        .build()
)
```
and if you would like to send Google Advertising Id of the device as device id to the Amplitude then add the below code in the `AndroidManifest.xml` of your app under `<application>` tag:
```
<meta-data
    android:name="com.google.android.gms.ads.AD_MANAGER_APP"
    android:value="true" />
```

## Send Events
Follow the steps from [Rudder Android SDK](https://github.com/rudderlabs/rudder-sdk-android)

## Contact Us
If you come across any issues while configuring or using RudderStack, please feel free to [contact us](https://rudderstack.com/contact/) or start a conversation on our [Slack](https://resources.rudderstack.com/join-rudderstack-slack) channel. We will be happy to help you.
