package com.rudderstack.android.sample.kotlin

import android.app.Application
import com.rudderstack.android.integrations.moengage.MoengageIntegrationFactory
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

class MainApplication : Application() {
    companion object {
        private const val WRITE_KEY = "1ZIkdsNVvxrUC7bRGOorIxmJVEH"
        private const val DATA_PLANE_URL = "https://485fdffb.ngrok.io"
        private const val CONTROL_PLANE_URL = "https://485fdffb.ngrok.io"
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
               // .withFactory(MoengageIntegrationFactory.FACTORY)
                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
                .build()
        )
    }
}