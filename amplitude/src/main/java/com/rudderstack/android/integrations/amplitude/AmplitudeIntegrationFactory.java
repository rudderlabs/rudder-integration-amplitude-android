package com.rudderstack.android.integrations.amplitude;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amplitude.api.Amplitude;
import com.amplitude.api.AmplitudeClient;
import com.amplitude.api.Identify;
import com.amplitude.api.Revenue;
import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AmplitudeIntegrationFactory extends RudderIntegration<AmplitudeClient> {
    private static final String AMPLITUDE_KEY = "Amplitude";
    private static final String VIEWED_EVENT_FORMAT = "Viewed %s Screen ";

    private AmplitudeClient amplitude;
    private AmplitudeDestinationConfig destinationConfig;

    private Set<String> traitsToIncrement;
    private Set<String> traitsToSetOnce;
    private Set<String> traitsToAppend;
    private Set<String> traitsToPrepend;

    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig rudderConfig) {
            return new AmplitudeIntegrationFactory(settings, rudderConfig);
        }

        @Override
        public String key() {
            return AMPLITUDE_KEY;
        }
    };

    private AmplitudeIntegrationFactory(Object config, RudderConfig rudderConfig) {
        if (RudderClient.getApplication() == null) {
            RudderLogger.logError("Application is null. Aborting Amplitude initialization.");
            return;
        }

        if (config == null) {
            RudderLogger.logError("Invalid api key. Aborting Amplitude initialization.");
            return;
        }

        // parse server config
        Gson gson = new Gson();
        this.destinationConfig = gson.fromJson(gson.toJson(config), AmplitudeDestinationConfig.class);

        if (TextUtils.isEmpty(this.destinationConfig.apiKey)) {
            RudderLogger.logError("Invalid api key. Aborting Amplitude initialization.");
            return;
        }

        // traits settings
        if (this.destinationConfig.traitsToIncrement != null) {
            this.traitsToIncrement = Utils.getStringSet(this.destinationConfig.traitsToIncrement);
        }
        if (this.destinationConfig.traitsToSetOnce != null) {
            this.traitsToSetOnce = Utils.getStringSet(this.destinationConfig.traitsToSetOnce);
        }
        if (this.destinationConfig.traitsToAppend != null) {
            this.traitsToAppend = Utils.getStringSet(this.destinationConfig.traitsToAppend);
        }
        if (this.destinationConfig.traitsToPrepend != null) {
            this.traitsToPrepend = Utils.getStringSet(this.destinationConfig.traitsToPrepend);
        }

        // all good. initialize amplitude sdk
        this.amplitude = Amplitude.getInstance();
        this.amplitude.initialize(RudderClient.getApplication(), this.destinationConfig.apiKey)
                .setLogLevel(
                        rudderConfig.getLogLevel() >= RudderLogger.RudderLogLevel.DEBUG
                                ? Log.VERBOSE
                                : Log.ERROR
                );

        // enabling Foreground Tracking
        this.amplitude.enableForegroundTracking(RudderClient.getApplication());

        // configuring Track Session Events
        this.amplitude.trackSessionEvents(this.destinationConfig.trackSessionEvents);

        // Configuring Location Listening
        if (!this.destinationConfig.enableLocationListening) {
            this.amplitude.disableLocationListening();
        }

        // Configuring usage of Advertising Id as Device Id
        if (this.destinationConfig.useAdvertisingIdForDeviceId) {
            this.amplitude.useAdvertisingIdForDeviceId();
        }

        // configuring batching settings
        this.amplitude.setEventUploadPeriodMillis(this.destinationConfig.eventUploadPeriodMillis);
        this.amplitude.setEventUploadThreshold(this.destinationConfig.eventUploadThreshold);
        RudderLogger.logInfo("Configured Amplitude + Rudder integration and initialized Amplitude.");
    }

    private void processRudderEvent(RudderMessage element) throws Exception {
        String type = element.getType();
        if (type != null) {
            switch (type) {
                case MessageType.IDENTIFY:
                    String userId = element.getUserId();
                    if (!TextUtils.isEmpty(userId)) {
                        this.amplitude.setUserId(userId);
                    }
                    Map<String, Object> traits = element.getTraits();
                    boolean optOutOfSession = false;
                    if (traits.containsKey("optOutOfSession")) {
                        optOutOfSession = (boolean) traits.get("optOutOfSession");
                    }
                    handleTraits(traits, optOutOfSession);
                    break;
                case MessageType.TRACK:
                    String eventName = element.getEventName();
                    if (eventName != null) {
                        Map<String, Object> eventProperties = element.getProperties();
                        JSONArray products = Utils.getProducts(eventProperties);
                        // if track products once is enabled
                        if (this.destinationConfig.trackProductsOnce) {
                            // if track products once is enabled and  we are having products array
                            if (products != null && eventProperties != null) {
                                JSONArray simplifiedProducts = Utils.simplifyProducts(products);
                                eventProperties.put("products", simplifiedProducts);
                                logEventAndCorrespondingRevenue(
                                        eventProperties,
                                        eventName,
                                        this.destinationConfig.trackRevenuePerProduct
                                );
                                // if track revenue per product is enabled
                                if (this.destinationConfig.trackRevenuePerProduct) {
                                    trackingEventAndRevenuePerProduct(
                                            eventProperties,
                                            products,
                                            false
                                    );
                                }
                                return;
                            }
                            // if track products once is enabled and
                            // we are not having a products array
                            logEventAndCorrespondingRevenue(
                                    eventProperties,
                                    eventName,
                                    false
                            );
                            return;
                        }
                        // if track products once is disabled and we are having a products array
                        if (products != null && eventProperties != null) {
                            // removing products property from event properties to make
                            // a call with no products first and then we will make a call for
                            // each product separately as trackProductsOnce is disabled
                            eventProperties.remove("products");
                            logEventAndCorrespondingRevenue(
                                    eventProperties,
                                    eventName,
                                    this.destinationConfig.trackRevenuePerProduct
                            );
                            trackingEventAndRevenuePerProduct(
                                    eventProperties,
                                    products,
                                    true
                            );
                            return;
                        }
                        // if track products once is disabled and we are not having a products array
                        logEventAndCorrespondingRevenue(
                                eventProperties,
                                eventName,
                                false
                        );
                    }
                    break;
                case MessageType.SCREEN:
                    Map<String, Object> properties = element.getProperties();
                    JSONObject propertiesJSON = null;
                    if (properties != null) {
                        propertiesJSON = new JSONObject(properties);
                    }
                    if (this.destinationConfig.trackAllPages) {
                        if (propertiesJSON != null &&
                                propertiesJSON.has("name") &&
                                !TextUtils.isEmpty((String) propertiesJSON.get("name"))
                        ) {
                            this.amplitude.logEvent(
                                    String.format(VIEWED_EVENT_FORMAT, propertiesJSON.get("name")),
                                    propertiesJSON,
                                    null,
                                    false
                            );
                        } else {
                            this.amplitude.logEvent(
                                    "Loaded a Screen",
                                    propertiesJSON,
                                    null,
                                    false
                            );
                        }
                    }
                    if (this.destinationConfig.trackCategorizedPages &&
                            propertiesJSON != null &&
                            propertiesJSON.has("category") &&
                            !TextUtils.isEmpty((String) propertiesJSON.get("category"))
                    ) {
                        this.amplitude.logEvent(
                                String.format(VIEWED_EVENT_FORMAT, propertiesJSON.get("category")),
                                propertiesJSON,
                                null,
                                false
                        );
                    }
                    if (this.destinationConfig.trackNamedPages &&
                            propertiesJSON != null &&
                            propertiesJSON.has("name") &&
                            !TextUtils.isEmpty((String) propertiesJSON.get("name"))
                    ) {
                        this.amplitude.logEvent(
                                String.format(VIEWED_EVENT_FORMAT, propertiesJSON.get("name")),
                                propertiesJSON,
                                null,
                                false
                        );
                    }
                    break;
//                case MessageType.GROUP:
//                    String groupName = null;
//                    String groupValue = element.getUserId();
//                    Map<String, Object> groupTraits = element.getTraits();
//                    if (groupTraits != null && groupTraits.size() != 0) {
//                        if (groupTraits.containsKey(this.destinationConfig.groupTypeTrait) && groupTraits.containsKey(this.destinationConfig.groupValueTrait)) {
//                            groupName = (String) groupTraits.get(this.destinationConfig.groupTypeTrait);
//                            groupValue = (String) groupTraits.get(this.destinationConfig.groupValueTrait);
//                        }
//                    }
//                    if (groupName == null) {
//                        groupName = "[RudderStack] Group";
//                    }
//                    // Set group
//                    this.amplitude.setGroup(groupName, groupValue);
//                    // Set group properties
//                    Identify groupIdentify = new Identify();
//                    groupIdentify.set("library", "RudderStack");
//                    if (groupTraits != null && groupTraits.size() != 0) {
//                        groupIdentify.set("group_properties", new JSONObject(groupTraits));
//                    }
//                    this.amplitude.groupIdentify(groupName, groupValue, groupIdentify);
//                    break;
                default:
                    RudderLogger.logWarn("AmplitudeIntegrationFactory: MessageType is not specified");
                    break;
            }
        }
    }

    @Override
    public void flush() {
        super.flush();
        this.amplitude.uploadEvents();
        RudderLogger.logDebug("Amplitude uploadEvents().");
    }

    @Override
    public void reset() {
        this.amplitude.setUserId(null);
        this.amplitude.regenerateDeviceId();
        RudderLogger.logVerbose("Amplitude setUserId(null).");
        RudderLogger.logVerbose("Amplitude regenerateDeviceId().");
    }

    @Override
    public void dump(@Nullable RudderMessage element) {
        try {
            if (element != null) {
                processRudderEvent(element);
            }
        } catch (Exception e) {
            RudderLogger.logError(e);
        }
    }

    @Override
    public AmplitudeClient getUnderlyingInstance() {
        return this.amplitude;
    }

    private void handleTraits(Map<String, Object> traits, Boolean optOutOfSession) {
        Identify identify = new Identify();
        for (Map.Entry<String, Object> entry : traits.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (this.traitsToIncrement.contains(key)) {
                TraitsHandler.incrementTrait(key, value, identify);
                continue;
            }
            if (this.traitsToSetOnce.contains(key)) {
                TraitsHandler.setOnce(key, value, identify);
                continue;
            }
            if (this.traitsToAppend.contains(key)) {
                TraitsHandler.appendTrait(key, value, identify);
                continue;
            }
            if (this.traitsToPrepend.contains(key)) {
                TraitsHandler.prependTrait(key, value, identify);
                continue;
            }
            TraitsHandler.setTrait(key, value, identify);
        }
        this.amplitude.identify(identify, optOutOfSession);
    }

    private void logEventAndCorrespondingRevenue(
            Map<String, Object> eventProperties,
            String eventName,
            boolean doNotTrackRevenue
    ) {
        if (eventProperties == null) {
            this.amplitude.logEvent(eventName);
            return;
        }
        boolean optOutOfSession = false;
        JSONObject eventPropsObject = new JSONObject(eventProperties);
        // should move optOutOfSession to RudderOption
        // in feature Instead of sending it in Event Properties
        if (eventProperties.containsKey("optOutOfSession")) {
            optOutOfSession = (boolean) eventProperties.get("optOutOfSession");
        }
        this.amplitude.logEvent(
                eventName,
                eventPropsObject,
                null,
                optOutOfSession
        );
        if (eventProperties.containsKey("revenue") && !doNotTrackRevenue) {
            this.trackRevenue(eventProperties, eventName);
        }
    }

    private void trackRevenue(
            @Nullable Map<String, Object> eventProperties,
            @NonNull String eventName
    ) {
        HashSet<String> revenueEventTypeSet = new HashSet<>();
        revenueEventTypeSet.add("order completed");
        revenueEventTypeSet.add("completed order");
        revenueEventTypeSet.add("product purchased");

        double quantity = 0;
        double revenue = 0;
        double price = 0;

        if (eventProperties == null) {
            RudderLogger.logDebug("AmplitudeIntegration: eventProperties is null");
            return;
        }

        Revenue amplitudeRevenue = new Revenue();
        amplitudeRevenue.setEventProperties(new JSONObject(eventProperties));

        if (eventProperties.containsKey("quantity")) {
            quantity = new NumberObject(eventProperties.get("quantity"))
                    .getNumber();
        }

        if (eventProperties.containsKey("revenue")) {
            revenue = new NumberObject(eventProperties.get("revenue")).getNumber();
        }

        if (eventProperties.containsKey("price")) {
            price = new NumberObject(eventProperties.get("price")).getNumber();
        }

        if (revenue == 0 && price == 0) {
            RudderLogger.logDebug("revenue or price is not present.");
            return;
        }

        if (price == 0) {
            price = revenue;
            quantity = 1;
        }

        if (quantity == 0) {
            quantity = 1;
        }
        amplitudeRevenue.setPrice(price);
        amplitudeRevenue.setQuantity((int) quantity);

        if (eventProperties.containsKey("productId")) {
            amplitudeRevenue.setProductId(String.valueOf(eventProperties.get("productId")));
        } else if (eventProperties.containsKey("product_id")) {
            amplitudeRevenue.setProductId(String.valueOf(eventProperties.get("product_id")));
        }

        if (eventProperties.containsKey("revenueType")) {
            amplitudeRevenue.setRevenueType((String) eventProperties.get("revenueType"));
        } else if (eventProperties.containsKey("revenue_type")) {
            amplitudeRevenue.setRevenueType((String) eventProperties.get("revenue_type"));
        } else if (revenueEventTypeSet.contains(eventName.toLowerCase())) {
            amplitudeRevenue.setRevenueType("Purchase");
        }

        if (eventProperties.containsKey("receipt") &&
                eventProperties.containsKey("receiptSignature")
        ) {
            amplitudeRevenue.setReceipt(
                    (String) eventProperties.get("receipt"),
                    (String) eventProperties.get("receiptSignature")
            );
        }

        this.amplitude.logRevenueV2(amplitudeRevenue);
    }

    private void trackingEventAndRevenuePerProduct(
            Map<String, Object> eventProperties,
            JSONArray allProducts,
            boolean shouldTrackEventPerProduct
    ) throws JSONException {
        String revenueType = null;
        if (eventProperties.containsKey("revenueType")) {
            revenueType = (String) eventProperties.get("revenueType");
        } else if (eventProperties.containsKey("revenue_type")) {
            revenueType = (String) eventProperties.get("revenue_type");
        }

        for (int i = 0; i < allProducts.length(); i++) {
            JSONObject product = (JSONObject) allProducts.get(i);
            if (this.destinationConfig.trackRevenuePerProduct) {
                if (revenueType != null) {
                    product.put("revenueType", revenueType);
                }
                trackRevenue(
                        Utils.jsonToMap(product),
                        "Product Purchased"
                );
            }
            if (shouldTrackEventPerProduct) {
                logEventAndCorrespondingRevenue(
                        Utils.jsonToMap(product),
                        "Product Purchased",
                        true
                );
            }
        }
    }
}