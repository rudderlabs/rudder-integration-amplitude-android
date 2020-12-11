package com.rudderstack.android.integrations.amplitude;

import android.text.TextUtils;

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
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.rudderstack.android.integrations.amplitude.TraitsHandler.appendTrait;
import static com.rudderstack.android.integrations.amplitude.TraitsHandler.incrementTrait;
import static com.rudderstack.android.integrations.amplitude.TraitsHandler.prependTrait;
import static com.rudderstack.android.integrations.amplitude.TraitsHandler.setOnce;
import static com.rudderstack.android.integrations.amplitude.TraitsHandler.setTrait;
import static com.rudderstack.android.integrations.amplitude.Utils.getProducts;
import static com.rudderstack.android.integrations.amplitude.Utils.isNullOrEmpty;
import static com.rudderstack.android.integrations.amplitude.Utils.getStringSet;
import static com.rudderstack.android.integrations.amplitude.Utils.jsonToMap;
import static com.rudderstack.android.integrations.amplitude.Utils.rudderLogToAndroidLog;
import static com.rudderstack.android.integrations.amplitude.Utils.simplifyProducts;


public class AmplitudeIntegrationFactory extends RudderIntegration<AmplitudeClient> {

    private static final String AMPLITUDE_KEY = "Amplitude";
    private AmplitudeClient amplitude;
    private AmplitudeDestinationConfig amplitudeDestinationConfig;


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

    private static final String VIEWED_EVENT_FORMAT = "Viewed %s Screen ";

    private Set<String> traitsToIncrement;
    private Set<String> traitsToSetOnce;
    private Set<String> traitsToAppend;
    private Set<String> traitsToPrepend;


    private AmplitudeIntegrationFactory(Object config, RudderConfig rudderConfig) {
        Map<String, Object> destinationConfig = (Map<String, Object>) config;
        if (destinationConfig == null) {
            RudderLogger.logError("Invalid api key. Aborting Amplitude initialization.");
            return;
        }
        if (RudderClient.getApplication() == null) {
            RudderLogger.logError("RudderClient is not initialized correctly. Application is null. Aborting Amplitude initialization.");
            return;
        }

        JSONObject destObject = new JSONObject(destinationConfig);
        amplitudeDestinationConfig = new Gson().fromJson(destObject.toString(), AmplitudeDestinationConfig.class);

        if (TextUtils.isEmpty(amplitudeDestinationConfig.apiKey)) {
            RudderLogger.logError("Invalid api key. Aborting Amplitude initialization.");
            return;
        }

        // traits settings
        if (amplitudeDestinationConfig.traitsToIncrement != null) {
            traitsToIncrement = getStringSet(amplitudeDestinationConfig.traitsToIncrement);
        }
        if (amplitudeDestinationConfig.traitsToSetOnce != null) {
            traitsToSetOnce = getStringSet(amplitudeDestinationConfig.traitsToSetOnce);
        }
        if (amplitudeDestinationConfig.traitsToAppend != null) {
            traitsToAppend = getStringSet(amplitudeDestinationConfig.traitsToAppend);
        }
        if (amplitudeDestinationConfig.traitsToPrepend != null) {
            traitsToPrepend = getStringSet(amplitudeDestinationConfig.traitsToPrepend);
        }

        // all good. initialize amplitude sdk
        amplitude = Amplitude.getInstance();
        amplitude.initialize(RudderClient.getApplication(), amplitudeDestinationConfig.apiKey).setLogLevel(rudderLogToAndroidLog(rudderConfig.getLogLevel()));

        // enabling Foreground Tracking
        amplitude.enableForegroundTracking(RudderClient.getApplication());

        // configuring Track Session Events
        amplitude.trackSessionEvents(amplitudeDestinationConfig.trackSessionEvents);

        // Configuring Location Listening
        if (!amplitudeDestinationConfig.enableLocationListening) {
            amplitude.disableLocationListening();
        }

        // Configuring usage of Advertising Id as Device Id
        if (amplitudeDestinationConfig.useAdvertisingIdForDeviceId) {
            amplitude.useAdvertisingIdForDeviceId();
        }

        // configuring batching settings
        amplitude.setEventUploadPeriodMillis(amplitudeDestinationConfig.eventUploadPeriodMillis);
        amplitude.setEventUploadThreshold(amplitudeDestinationConfig.eventUploadThreshold);
        RudderLogger.logInfo("Configured Amplitude + Rudder integration and initialized Amplitude.");

    }

    private void processRudderEvent(RudderMessage element) throws Exception {
        String type = element.getType();
        if (type != null) {
            switch (type) {
                case MessageType.IDENTIFY:
                    String userId = element.getUserId();
                    if (!TextUtils.isEmpty(userId)) {
                        amplitude.setUserId(userId);
                    }
                    Map<String, Object> traits = element.getTraits();
                    boolean optOutOfSession = false;
                    if (traits.containsKey("optOutOfSession")) {
                        optOutOfSession = (boolean) traits.get("optOutOfSession");
                    }
                    if (traits != null) {
                        handleTraits(traits, optOutOfSession);
                    }
                    break;
                case MessageType.TRACK:
                    String eventName = element.getEventName();
                    if (eventName != null) {
                        Map<String, Object> eventProperties = element.getProperties();
                        JSONArray products = getProducts(eventProperties);
                        // if track products once is enabled
                        if (amplitudeDestinationConfig.trackProductsOnce) {
                            // if track products once is enabled and  we are having products array
                            if (products != null) {
                                JSONArray simplifiedProducts = simplifyProducts(products);
                                eventProperties.put("products", simplifiedProducts);
                                logEventAndCorrespondingRevenue(eventProperties, eventName, amplitudeDestinationConfig.trackRevenuePerProduct);
                                // if track revenue per product is enabled
                                if (amplitudeDestinationConfig.trackRevenuePerProduct) {
                                    trackingEventAndRevenuePerProduct(eventProperties, products, false);
                                }
                                return;
                            }
                            //if track products once is enabled and  we are not having a products array
                            logEventAndCorrespondingRevenue(eventProperties, eventName, false);
                            return;
                        }
                        // if track products once is disabled and we are having a products array
                        if (products != null) {
                            // removing products property from event properties to make a call with no products first and then we will make a call for
                            // each product separately as trackProductsOnce is disabled
                            eventProperties.remove("products");
                            logEventAndCorrespondingRevenue(eventProperties, eventName, amplitudeDestinationConfig.trackRevenuePerProduct);
                            trackingEventAndRevenuePerProduct(eventProperties, products, true);
                            return;
                        }
                        // if track products once is disabled and we are not having a products array
                        logEventAndCorrespondingRevenue(eventProperties, eventName, false);
                    }
                    break;
                case MessageType.SCREEN:
                    Map<String, Object> properties = element.getProperties();
                    JSONObject propertiesJSON = null;
                    if (properties != null) {
                        propertiesJSON = new JSONObject(properties);
                    }
                    if (amplitudeDestinationConfig.trackAllPages) {
                        if (propertiesJSON.has("name") && !TextUtils.isEmpty((String) propertiesJSON.get("name"))) {
                            amplitude.logEvent(String.format(VIEWED_EVENT_FORMAT, propertiesJSON.get("name")), propertiesJSON, null, false);
                        } else {
                            amplitude.logEvent("Loaded a Screen", propertiesJSON, null, false);
                        }
                    }
                    if (amplitudeDestinationConfig.trackCategorizedPages && propertiesJSON.has("category") && !TextUtils.isEmpty((String) propertiesJSON.get("category"))) {
                        amplitude.logEvent(String.format(VIEWED_EVENT_FORMAT, propertiesJSON.get("category")), propertiesJSON, null, false);
                    }
                    if (amplitudeDestinationConfig.trackNamedPages && propertiesJSON.has("name") && !TextUtils.isEmpty((String) propertiesJSON.get("name"))) {
                        amplitude.logEvent(String.format(VIEWED_EVENT_FORMAT, propertiesJSON.get("name")), propertiesJSON, null, false);
                    }

                    break;
                case MessageType.GROUP:
                    String groupName = null;
                    String groupValue = element.getUserId();
                    Map<String, Object> groupTraits = element.getTraits();
                    if (groupTraits != null && groupTraits.size() != 0) {
                        if (groupTraits.containsKey(amplitudeDestinationConfig.groupTypeTrait) && groupTraits.containsKey(amplitudeDestinationConfig.groupValueTrait)) {
                            groupName = (String) groupTraits.get(amplitudeDestinationConfig.groupTypeTrait);
                            groupValue = (String) groupTraits.get(amplitudeDestinationConfig.groupValueTrait);
                        }
                    }
                    if (groupName == null) {
                        groupName = "[RudderStack] Group";
                    }
                    // Set group
                    amplitude.setGroup(groupName, groupValue);
                    // Set group properties
                    Identify groupIdentify = new Identify();
                    groupIdentify.set("library", "RudderStack");
                    if (groupTraits != null && groupTraits.size() != 0) {
                        groupIdentify.set("group_properties", new JSONObject(groupTraits));
                    }
                    amplitude.groupIdentify(groupName, groupValue, groupIdentify);
                    break;
                default:
                    RudderLogger.logWarn("AmplitudeIntegrationFactory: MessageType is not specified");
                    break;
            }
        }

    }

    @Override
    public void flush() {
        super.flush();
        amplitude.uploadEvents();
        RudderLogger.logDebug("Amplitude uploadEvents().");
    }

    @Override
    public void reset() {
        amplitude.setUserId(null);
        amplitude.regenerateDeviceId();
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
        return amplitude;
    }

    private void handleTraits(Map<String, Object> traits, Boolean optOutOfSession) {
        Identify identify = new Identify();

        for (Map.Entry<String, Object> entry : traits.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (traitsToIncrement.contains(key)) {
                incrementTrait(key, value, identify);
                continue;
            }
            if (traitsToSetOnce.contains(key)) {
                setOnce(key, value, identify);
                continue;
            }
            if (traitsToAppend.contains(key)) {
                appendTrait(key, value, identify);
                continue;
            }
            if (traitsToPrepend.contains(key)) {
                prependTrait(key, value, identify);
                continue;
            }
            setTrait(key, value, identify);
        }
        amplitude.identify(identify, optOutOfSession);
    }


    private void logEventAndCorrespondingRevenue(Map<String, Object> eventProperties, String eventName, boolean doNotTrackRevenue) {
        if (eventProperties == null) {
            amplitude.logEvent(eventName);
            return;
        }
        boolean optOutOfSession = false;
        JSONObject eventPropsObject = new JSONObject(eventProperties);
        // should move optOutOfSession to RudderOption in feature Instead of sending it in Event Properties
        if (eventProperties.containsKey("optOutOfSession")) {
            optOutOfSession = (boolean) eventProperties.get("optOutOfSession");
        }
        amplitude.logEvent(eventName, eventPropsObject, null, optOutOfSession);
        if (eventProperties.containsKey("revenue") && !doNotTrackRevenue) {
            trackRevenue(eventProperties, eventName);
        }

    }

    private void trackRevenue(Map<String, Object> eventProperties, String eventName) {

        Map<String, String> mapRevenueType = new HashMap<String, String>() {{
            put("order completed", "Purchase");
            put("completed order", "Purchase");
            put("product purchased", "Purchase");
        }};
        int quantity = 0;
        double revenue = 0;
        double price = 0;
        String productId = null;
        String revenueType = null;
        String receipt = null;
        String receiptSignature = null;
        if (eventProperties.containsKey("quantity")) {
            quantity = (int) eventProperties.get("quantity");
        }
        if (eventProperties.containsKey("revenue")) {
            Object revenueObject = eventProperties.get("revenue");
            if (revenueObject instanceof String) {
                try {
                    Number number = NumberFormat.getInstance().parse((String) revenueObject);
                    revenue = number.doubleValue();
                } catch (Exception e) {
                    RudderLogger.logDebug("String cannot be converted to Number");
                }
            } else if (revenueObject instanceof Integer) {
                revenue = (double) ((Integer) revenueObject);
            } else if (revenueObject instanceof Double) {
                revenue = (double) revenueObject;
            }
        }
        if (eventProperties.containsKey("price")) {
            Object priceObject = eventProperties.get("price");
            if (priceObject instanceof Integer) {
                price = (double) ((Integer) priceObject);
            } else if (priceObject instanceof Double) {
                price = (double) priceObject;
            }
        }
        if (eventProperties.containsKey("productId")) {
            productId = String.valueOf(eventProperties.get("productId"));
        }
        if (productId == null && eventProperties.containsKey("product_id")) {
            productId = String.valueOf(eventProperties.get("product_id"));
        }
        if (eventProperties.containsKey("revenueType")) {
            revenueType = (String) eventProperties.get("revenueType");
        }
        if (revenueType == null && eventProperties.containsKey("revenue_type")) {
            revenueType = (String) eventProperties.get("revenue_type");
        }
        if (revenueType == null) {
            revenueType = mapRevenueType.get(eventName.toLowerCase());
        }

        if (eventProperties.containsKey("receipt")) {
            receipt = (String) eventProperties.get("receipt");
        }
        if (eventProperties.containsKey("receiptSignature")) {
            receiptSignature = (String) eventProperties.get("receiptSignature");
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
        JSONObject eventPropsObject = new JSONObject(eventProperties);
        Revenue amplitudeRevenue = new Revenue().setPrice(price).setQuantity(quantity).setEventProperties(eventPropsObject);
        if (revenueType != null) {
            amplitudeRevenue.setRevenueType(revenueType);
        }
        if (productId != null) {
            amplitudeRevenue.setProductId(productId);
        }
        if (receipt != null && receiptSignature != null) {
            amplitudeRevenue.setReceipt(receipt, receiptSignature);
        }
        amplitude.logRevenueV2(amplitudeRevenue);
    }

    private void trackingEventAndRevenuePerProduct(Map<String, Object> eventProperties, JSONArray allProducts, boolean shouldTrackEventPerProduct) {
        String revenueType = null;
        if (eventProperties.containsKey("revenueType")) {
            revenueType = (String) eventProperties.get("revenueType");
        }
        if (revenueType == null) {
            if (eventProperties.containsKey("revenue_type")) {
                revenueType = (String) eventProperties.get("revenue_type");
            }
        }
        try {
            for (int i = 0; i < allProducts.length(); i++) {
                JSONObject product = (JSONObject) allProducts.get(i);
                if (amplitudeDestinationConfig.trackRevenuePerProduct) {
                    if (revenueType != null) {
                        product.put("revenueType", revenueType);
                    }
                    trackRevenue(jsonToMap(product), "Product Purchased");
                }
                if (shouldTrackEventPerProduct) {
                    logEventAndCorrespondingRevenue(jsonToMap(product), "Product Purchased", true);
                }
            }
        } catch (Exception e) {
            RudderLogger.logError(e);
        }
    }


}