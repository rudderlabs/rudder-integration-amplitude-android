package com.rudderstack.android.sample.kotlin

import androidx.multidex.MultiDexApplication
import com.rudderstack.android.RudderAnalytics
import com.rudderstack.android.integrations.amplitude.AmplitudeDestinationPlugin
import com.rudderstack.core.Analytics
import com.rudderstack.core.Logger
import com.rudderstack.core.Settings
import com.rudderstack.gsonrudderadapter.GsonAdapter

class MainApplication : MultiDexApplication() {
    companion object {
        private const val WRITE_KEY = "2CZ4Yh3XHKfn6LCWDFk3leBvFdM"
        private const val DATA_PLANE_URL = "https://rudderstaczbta.dataplane.rudderstack.com"
        private const val CONTROL_PLANE_URL = "https://api.dev.rudderlabs.com"
        lateinit var rudderClient: Analytics
    }

    override fun onCreate() {
        super.onCreate()
        rudderClient = RudderAnalytics(
            this,
            WRITE_KEY,
            Settings(),
            GsonAdapter(),
            dataPlaneUrl = DATA_PLANE_URL,
            controlPlaneUrl = CONTROL_PLANE_URL,
            trackLifecycleEvents = true,
            recordScreenViews = true
//            RudderConfig.Builder()
//                .withDataPlaneUrl(DATA_PLANE_URL)
//                .withControlPlaneUrl(CONTROL_PLANE_URL)
//                .withFactory(AmplitudeIntegrationFactory.FACTORY)
//                .withLogLevel(RudderLogger.RudderLogLevel.VERBOSE)
//                .build()
        )
        rudderClient.addPlugin(AmplitudeDestinationPlugin(this))
        rudderClient.logger.activate(Logger.LogLevel.Info)
    }
}