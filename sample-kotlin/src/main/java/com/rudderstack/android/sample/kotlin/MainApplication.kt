package com.rudderstack.android.sample.kotlin

import android.app.Application
import com.rudderstack.android.integrations.amplitude.AmplitudeIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        private const val WRITE_KEY = "1kYLXnUWTL1Q7WiUYr7uqX2hDnO"
        private const val DATA_PLANE_URL = "https://38845a2d0e72.ngrok.io"
        private const val CONTROL_PLANE_URL = "https://fb1a9fcc88c0.ngrok.io"
        lateinit var rudderClient: RudderClient
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderClient.getInstance(
            this,
            WRITE_KEY,
            RudderConfig.Builder()
                //.withDataPlaneUrl(DATA_PLANE_URL)
                .withControlPlaneUrl(CONTROL_PLANE_URL)
                .withFactory(AmplitudeIntegrationFactory.FACTORY)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .build()
        )
    }
}