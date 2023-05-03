package com.rudderstack.android.sample.kotlin

import androidx.multidex.MultiDexApplication
import com.rudderstack.android.integrations.amplitude.AmplitudeIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : MultiDexApplication() {
    companion object {
        private const val WRITE_KEY = "2BqDIDKDAnwqv18h0yZwG8GifNh"
        private const val DATA_PLANE_URL = "https://rudderstacyta.dataplane.dev.rudderlabs.com"
        private const val CONTROL_PLANE_URL = "https://api.dev.rudderlabs.com"
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                .withDataPlaneUrl(DATA_PLANE_URL)
                .withControlPlaneUrl(CONTROL_PLANE_URL)
                .withFactory(AmplitudeIntegrationFactory.FACTORY)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .build()
        )
    }
}