package com.rudderstack.android.integrations.amplitude

import android.app.Application
import android.text.TextUtils
import android.util.Log
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.amplitude.api.Identify
import com.amplitude.api.Revenue

import com.rudderstack.core.Analytics
import com.rudderstack.core.BaseDestinationPlugin
import com.rudderstack.core.Logger
import com.rudderstack.core.Plugin
import com.rudderstack.core.internal.optAdd
import com.rudderstack.models.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class AmplitudeDestinationPlugin(
    private val application: Application
) : BaseDestinationPlugin<AmplitudeClient>(AMPLITUDE_KEY) {

    companion object {
        private const val AMPLITUDE_KEY = "Amplitude"
        private const val VIEWED_EVENT_FORMAT = "Viewed %s Screen "
    }

    private var destinationConfig: AmplitudeDestinationConfig? = null
    private var analytics: Analytics? = null

    private var amplitude: AmplitudeClient? = null

    private var traitsToIncrement: Set<String>? = null
    private var traitsToSetOnce: Set<String>? = null
    private var traitsToAppend: Set<String>? = null
    private var traitsToPrepend: Set<String>? = null

    override fun intercept(chain: Plugin.Chain): Message {
//        if(config == null)
        return chain.proceed(chain.originalMessage)
    }

    override fun updateRudderServerConfig(config: RudderServerConfig) {
        super.updateRudderServerConfig(config)

        this.destinationConfig = config.source?.destinations?.first {
            ((it.destinationDefinition?.displayName) ?: (it.destinationDefinition?.definitionName)
            ?: it.destinationName) == AMPLITUDE_KEY
        }?.destinationConfig?.let {

            analytics?.jsonAdapter?.readMap(
                it,
                AmplitudeDestinationConfig::class.java
            )
        }

        traitsToIncrement = destinationConfig?.traitsToIncrement?.let { Utils.getStringSet(it) }
        traitsToSetOnce = destinationConfig?.traitsToSetOnce?.let { Utils.getStringSet(it) }
        traitsToAppend = destinationConfig?.traitsToAppend?.let { Utils.getStringSet(it) }
        traitsToPrepend = destinationConfig?.traitsToPrepend?.let { Utils.getStringSet(it) }

        destinationConfig?.let {
            configureAmplitude(it)
        } ?: analytics?.logger?.error(log = "Amplitude config not found")
    }

    override fun setup(analytics: Analytics) {
        super.setup(analytics)
        this.analytics = analytics

        // all good. initialize amplitude sdk

        // all good. initialize amplitude sdk
        this.amplitude = Amplitude.getInstance()
        this.amplitude?.initialize(application, destinationConfig!!.apiKey)
            ?.setLogLevel(
                when (analytics.logger.level) {
                    Logger.LogLevel.Info -> Log.INFO
                    Logger.LogLevel.Debug -> Log.DEBUG
                    Logger.LogLevel.Warn -> Log.WARN
                    Logger.LogLevel.None, Logger.LogLevel.Error -> Log.ERROR
                }
            )
    }

    private fun configureAmplitude(destinationConfig: AmplitudeDestinationConfig) {
        // enabling Foreground Tracking

        // enabling Foreground Tracking
        amplitude?.enableForegroundTracking(application)

        // configuring Track Session Events

        // configuring Track Session Events
        amplitude?.trackSessionEvents(this.destinationConfig!!.trackSessionEvents)

        // Configuring Location Listening

        // Configuring Location Listening
        if (!destinationConfig.enableLocationListening) {
            amplitude?.disableLocationListening()
        }

        // Configuring usage of Advertising Id as Device Id

        // Configuring usage of Advertising Id as Device Id
        if (destinationConfig.useAdvertisingIdForDeviceId) {
            amplitude?.useAdvertisingIdForDeviceId()
        }

        // configuring batching settings

        // configuring batching settings
        amplitude?.setEventUploadPeriodMillis(destinationConfig.eventUploadPeriodMillis)
        amplitude?.setEventUploadThreshold(destinationConfig.eventUploadThreshold)
        analytics?.logger?.info(log = "Configured Amplitude + Rudder integration and initialized Amplitude.")
    }

    @Throws(Exception::class)
    private fun processRudderEvent(element: Message) {
        destinationConfig?.also { destinationConfig ->
            when (element) {
                is IdentifyMessage -> {
                    val userId = element.userId
                    if (!TextUtils.isEmpty(userId)) {
                        amplitude?.userId = userId
                    }
                    val traits = element.context?.traits
                    var optOutOfSession = false
                    if (traits?.containsKey("optOutOfSession") == true) {
                        optOutOfSession = traits["optOutOfSession"] as Boolean
                    }
                    traits?.let {
                        handleTraits(it, optOutOfSession)
                    }
                }
                is TrackMessage -> {
                    val eventName = element.eventName
                    if (eventName != null) {
                        val eventProperties = element.properties
                        val products = Utils.getProducts(eventProperties)
                        // if track products once is enabled
                        if (destinationConfig.trackProductsOnce) {
                            // if track products once is enabled and  we are having products array
                            if (products != null && eventProperties != null) {
                                val simplifiedProducts = Utils.simplifyProducts(products)
                                val newEventProperties = eventProperties optAdd mapOf("products" to simplifiedProducts)
                                logEventAndCorrespondingRevenue(
                                    newEventProperties,
                                    eventName,
                                    destinationConfig.trackRevenuePerProduct
                                )
                                // if track revenue per product is enabled
                                if (destinationConfig.trackRevenuePerProduct) {
                                    trackingEventAndRevenuePerProduct(
                                        newEventProperties,
                                        products,
                                        false
                                    )
                                }
                                return
                            }
                            // if track products once is enabled and
                            // we are not having a products array
                            logEventAndCorrespondingRevenue(
                                eventProperties,
                                eventName,
                                false
                            )
                            return
                        }
                        // if track products once is disabled and we are having a products array
                        if (products != null && eventProperties != null) {
                            // removing products property from event properties to make
                            // a call with no products first and then we will make a call for
                            // each product separately as trackProductsOnce is disabled
                            val newEventProperties = eventProperties - "products"
                            logEventAndCorrespondingRevenue(
                               newEventProperties ,
                                eventName,
                                destinationConfig.trackRevenuePerProduct
                            )
                            trackingEventAndRevenuePerProduct(
                                newEventProperties,
                                products,
                                true
                            )
                            return
                        }
                        // if track products once is disabled and we are not having a products array
                        logEventAndCorrespondingRevenue(
                            eventProperties,
                            eventName,
                            false
                        )
                    }
                }
                is ScreenMessage -> {
                    val properties = element.properties
                    var propertiesJSON: JSONObject? = null
                    if (properties != null) {
                        propertiesJSON = JSONObject(properties)
                    }
                    if (destinationConfig.trackAllPages) {
                        if (propertiesJSON != null &&
                            propertiesJSON.has("name") &&
                            !TextUtils.isEmpty(propertiesJSON["name"] as String)
                        ) {
                            amplitude?.logEvent(
                                String.format(
                                    VIEWED_EVENT_FORMAT,
                                    propertiesJSON["name"]
                                ),
                                propertiesJSON,
                                null,
                                false
                            )
                        } else {
                            amplitude?.logEvent(
                                "Loaded a Screen",
                                propertiesJSON,
                                null,
                                false
                            )
                        }
                    }
                    if (destinationConfig.trackCategorizedPages && propertiesJSON != null &&
                        propertiesJSON.has("category") &&
                        !TextUtils.isEmpty(propertiesJSON["category"] as String)
                    ) {
                        amplitude?.logEvent(
                            String.format(
                                VIEWED_EVENT_FORMAT,
                                propertiesJSON["category"]
                            ),
                            propertiesJSON,
                            null,
                            false
                        )
                    }
                    if (destinationConfig.trackNamedPages && propertiesJSON != null &&
                        propertiesJSON.has("name") &&
                        !TextUtils.isEmpty(propertiesJSON["name"] as String)) {
                        amplitude?.logEvent(
                            String.format(
                                VIEWED_EVENT_FORMAT,
                                propertiesJSON["name"]
                            ),
                            propertiesJSON,
                            null,
                            false
                        )
                    }
                }

            /*RudderLogger.logWarn("AmplitudeIntegrationFactory: MessageType is not specified")*/
//                is AliasMessage -> TODO()
//                is GroupMessage -> TODO()
//                is PageMessage -> analytics?.logger?.warn( log = "AmplitudeIntegrationFactory: Page type unsupported for mobile")
                else -> analytics?.logger?.warn( log = "AmplitudeIntegrationFactory: Type unsupported for mobile")

            }
        }
    }


    private fun handleTraits(traits: Map<String, Any?>, optOutOfSession: Boolean) {
        val identify = Identify()
        traits.forEach {
            val key = it.key
            val value = it.value
            if (traitsToIncrement!!.contains(key)) {
                TraitsHandler.incrementTrait(key, value, identify)

            } else if (traitsToSetOnce!!.contains(key)) {
                TraitsHandler.setOnce(key, value, identify)
            } else if (traitsToAppend!!.contains(key)) {
                TraitsHandler.appendTrait(key, value, identify)
            } else if (traitsToPrepend!!.contains(key)) {
                TraitsHandler.prependTrait(key, value, identify)

            } else
                TraitsHandler.setTrait(key, value, identify)
        }
        amplitude?.identify(identify, optOutOfSession)
    }

    private fun logEventAndCorrespondingRevenue(
        eventProperties: Map<String, Any>?,
        eventName: String,
        doNotTrackRevenue: Boolean
    ) {
        if (eventProperties == null) {
            amplitude?.logEvent(eventName)
            return
        }
        var optOutOfSession = false
        val eventPropsObject = JSONObject(eventProperties)
        // should move optOutOfSession to RudderOption
        // in feature Instead of sending it in Event Properties
        if (eventProperties.containsKey("optOutOfSession")) {
            optOutOfSession = eventProperties["optOutOfSession"] as Boolean
        }
        amplitude!!.logEvent(
            eventName,
            eventPropsObject,
            null,
            optOutOfSession
        )
        if (eventProperties.containsKey("revenue") && !doNotTrackRevenue) {
            this.trackRevenue(eventProperties, eventName)
        }
    }

    private fun trackRevenue(
        eventProperties: Map<String, Any>?,
        eventName: String
    ) {
        val revenueEventTypeSet = HashSet<String>()
        revenueEventTypeSet.add("order completed")
        revenueEventTypeSet.add("completed order")
        revenueEventTypeSet.add("product purchased")
        var quantity = 0.0
        var revenue = 0.0
        var price = 0.0
        if (eventProperties == null) {
            analytics?.logger?.debug(log = "AmplitudeIntegration: eventProperties is null")
            return
        }
        val amplitudeRevenue = Revenue()
        amplitudeRevenue.setEventProperties(JSONObject(eventProperties))
        if (eventProperties.containsKey("quantity")) {
            quantity = NumberObject(eventProperties["quantity"])
                .number
        }
        if (eventProperties.containsKey("revenue")) {
            revenue = NumberObject(eventProperties["revenue"]).number
        }
        if (eventProperties.containsKey("price")) {
            price = NumberObject(eventProperties["price"]).number
        }
        if (revenue == 0.0 && price == 0.0) {
            analytics?.logger?.debug(log = "revenue or price is not present.")
            return
        }
        if (price == 0.0) {
            price = revenue
            quantity = 1.0
        }
        if (quantity == 0.0) {
            quantity = 1.0
        }
        amplitudeRevenue.setPrice(price)
        amplitudeRevenue.setQuantity(quantity.toInt())
        if (eventProperties.containsKey("productId")) {
            amplitudeRevenue.setProductId(eventProperties["productId"].toString())
        } else if (eventProperties.containsKey("product_id")) {
            amplitudeRevenue.setProductId(eventProperties["product_id"].toString())
        }
        if (eventProperties.containsKey("revenueType")) {
            amplitudeRevenue.setRevenueType(eventProperties["revenueType"] as String?)
        } else if (eventProperties.containsKey("revenue_type")) {
            amplitudeRevenue.setRevenueType(eventProperties["revenue_type"] as String?)
        } else if (revenueEventTypeSet.contains(eventName.lowercase(Locale.getDefault()))) {
            amplitudeRevenue.setRevenueType("Purchase")
        }
        if (eventProperties.containsKey("receipt") &&
            eventProperties.containsKey("receiptSignature")
        ) {
            amplitudeRevenue.setReceipt(
                eventProperties["receipt"] as String?,
                eventProperties["receiptSignature"] as String?
            )
        }
        amplitude!!.logRevenueV2(amplitudeRevenue)
    }

    @Throws(JSONException::class)
    private fun trackingEventAndRevenuePerProduct(
        eventProperties: Map<String, Any>,
        allProducts: JSONArray,
        shouldTrackEventPerProduct: Boolean
    ) {
        var revenueType: String? = null
        if (eventProperties.containsKey("revenueType")) {
            revenueType = eventProperties["revenueType"] as String?
        } else if (eventProperties.containsKey("revenue_type")) {
            revenueType = eventProperties["revenue_type"] as String?
        }
        for (i in 0 until allProducts.length()) {
            val product = allProducts[i] as JSONObject
            if (destinationConfig?.trackRevenuePerProduct == true) {
                if (revenueType != null) {
                    product.put("revenueType", revenueType)
                }
                trackRevenue(
                    Utils.jsonToMap(product),
                    "Product Purchased"
                )
            }
            if (shouldTrackEventPerProduct) {
                logEventAndCorrespondingRevenue(
                    Utils.jsonToMap(product),
                    "Product Purchased",
                    true
                )
            }
        }
    }
}