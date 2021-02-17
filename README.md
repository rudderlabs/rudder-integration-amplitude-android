# What is RudderStack?

[RudderStack](https://rudderstack.com/) is a **customer data pipeline** tool for collecting, routing and processing data from your websites, apps, cloud tools, and data warehouse.

More information on RudderStack can be found [here](https://github.com/rudderlabs/rudder-server).

## Integrating Amplitude with RudderStack's Android SDK

1. Add [Amplitude](https://amplitude.com) as a destination in the [RudderStack dashboard](https://app.rudderstack.com/) and define `apiKey` and all other applicable settings .

2. Add the dependency under ```dependencies```

```
implementation 'com.rudderstack.android.sdk:core:1.+'
implementation 'com.rudderstack.android.integration:amplitude:1.0.1'
implementation 'com.google.code.gson:gson:2.8.6'

// Amplitude
implementation 'com.amplitude:android-sdk:2.25.2'
implementation 'com.squareup.okhttp3:okhttp:4.2.2'

// For using Google Advertising Id as device id
implementation 'com.google.android.gms:play-services-ads:18.3.0'

```

3. Add these lines to your ```app/build.gradle``` under ```compileOptions``` in the ```android``` tag:

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

Follow the steps from [RudderStack Android SDK](https://github.com/rudderlabs/rudder-sdk-android).

## Contact Us

If you come across any issues while configuring or using this integration, feel free to start a conversation on our [Slack](https://resources.rudderstack.com/join-rudderstack-slack) channel. We will be happy to help you.
