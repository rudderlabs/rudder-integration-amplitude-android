package com.rudderstack.android.integrations.amplitude;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.amplitude.api.Amplitude;
import com.amplitude.api.AmplitudeClient;
import com.amplitude.api.Identify;
import com.amplitude.api.Revenue;
import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderContext;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderProperty;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class AmplitudeIntegrationFactory extends RudderIntegration<AmplitudeClient> {

    private static final String AMPLITUDE_KEY = "Amplitude";
    private AmplitudeClient amplitude;


    public static Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig rudderConfig) {
            return new AmplitudeIntegrationFactory(settings, client, rudderConfig);
        }

        @Override
        public String key() {
            return AMPLITUDE_KEY;
        }
    };

    private static final String API_KEY = "apiKey";
    private static final String GROUP_TYPE_TRAIT = "groupTypeTrait";
    private static final String GROUP_VALUE_TRAIT = "groupValueTrait";
    private static final String EVENT_UPLOAD_PERIOD_MILLIS = "eventUploadPeriodMillis";
    private static final String EVENT_UPLOAD_THRESHOLD = "eventUploadThreshold";
    private static final String VIEWED_EVENT_FORMAT = "Viewed %s Screen ";
    private static final String TRACK_ALL_PAGES = "trackAllPages";
    private static final String TRACK_NAMED_PAGES = "trackNamedPages";
    private static final String TRACK_CATEGORIZED_PAGES = "trackCategorizedPages";
    private static final String TRAITS_TO_INCREMENT = "traitsToIncrement";
    private static final String TRAITS_TO_SET_ONCE = "traitsToSetOnce";
    private static final String TRAITS_TO_APPEND = "traitsToAppend";
    private static final String TRAITS_TO_PREPEND = "traitsToPrepend";
    private static final String TRAITS_KEY = "traits";
    private static final String TRACK_PRODUCTS_ONCE = "trackProductsOnce";
    private static final String TRACK_REVENUE_PER_PRODUCT = "trackRevenuePerProduct";
    private static final String TRACK_SESSION_EVENTS = "trackSessionEvents";
    private static final String ENABLE_LOCATION_LISTENING = "enableLocationListening";
    private static final String USE_ADVERTISING_ID_FOR_DEVICE_ID = "useAdvertisingIdForDeviceId";


    String groupTypeTrait;
    String groupValueTrait;
    int eventUploadPeriodMillis;
    int eventUploadThreshold;
    boolean trackAllPages;
    boolean trackCategorizedPages;
    boolean trackNamedPages;
    boolean trackProductsOnce;
    boolean trackRevenuePerProduct;
    boolean trackSessionEvents;
    boolean enableLocationListening;
    boolean useAdvertisingIdForDeviceId;
    Set<String> traitsToIncrement;
    Set<String> traitsToSetOnce;
    Set<String> traitsToAppend;
    Set<String> traitsToPrepend;


    private AmplitudeIntegrationFactory(Object config, final RudderClient client, RudderConfig rudderConfig) {
        String apiKey = "";
        Map<String, Object> destinationConfig = (Map<String, Object>) config;
        if (destinationConfig == null) {
            RudderLogger.logError("Invalid api key. Aborting Amplitude initialization.");
        } else if (RudderClient.getApplication() == null) {
            RudderLogger.logError("RudderClient is not initialized correctly. Application is null. Aborting Amplitude initialization.");
        } else {
            // get apiKey and return if null or blank
            if (destinationConfig.containsKey(API_KEY)) {
                apiKey = (String) destinationConfig.get(API_KEY);
            }
            if (TextUtils.isEmpty(apiKey)) {
                RudderLogger.logError("Invalid api key. Aborting Amplitude initialization.");
                return;
            }
            // group settings
            if (destinationConfig.containsKey(GROUP_TYPE_TRAIT)) {
                groupTypeTrait = (String) destinationConfig.get(GROUP_TYPE_TRAIT);
            }
            if (destinationConfig.containsKey(GROUP_VALUE_TRAIT)) {
                groupValueTrait = (String) destinationConfig.get(GROUP_VALUE_TRAIT);
            }
            //batching settings
            if (destinationConfig.containsKey(EVENT_UPLOAD_PERIOD_MILLIS)) {
                eventUploadPeriodMillis = ((Double) destinationConfig.get(EVENT_UPLOAD_PERIOD_MILLIS)).intValue();
            }
            if (destinationConfig.containsKey(EVENT_UPLOAD_THRESHOLD)) {
                eventUploadThreshold = ((Double) destinationConfig.get(EVENT_UPLOAD_THRESHOLD)).intValue();
            }
            // screen settings
            if (destinationConfig.containsKey(TRACK_ALL_PAGES)) {
                trackAllPages = (boolean) destinationConfig.get(TRACK_ALL_PAGES);
            }
            if (destinationConfig.containsKey(TRACK_CATEGORIZED_PAGES)) {
                trackCategorizedPages = (boolean) destinationConfig.get(TRACK_CATEGORIZED_PAGES);
            }
            if (destinationConfig.containsKey(TRACK_NAMED_PAGES)) {
                trackNamedPages = (boolean) destinationConfig.get(TRACK_NAMED_PAGES);
            }
            // products & revenue settings
            if (destinationConfig.containsKey(TRACK_PRODUCTS_ONCE)) {
                trackProductsOnce = (boolean) destinationConfig.get(TRACK_PRODUCTS_ONCE);
            }
            if (destinationConfig.containsKey(TRACK_REVENUE_PER_PRODUCT)) {
                trackRevenuePerProduct = (boolean) destinationConfig.get(TRACK_REVENUE_PER_PRODUCT);
            }
            // traits settings
            if (destinationConfig.containsKey(TRAITS_TO_INCREMENT)) {
                traitsToIncrement = getStringSet(destinationConfig, TRAITS_TO_INCREMENT);
            }
            if (destinationConfig.containsKey(TRAITS_TO_SET_ONCE)) {
                traitsToSetOnce = getStringSet(destinationConfig, TRAITS_TO_SET_ONCE);
            }
            if (destinationConfig.containsKey(TRAITS_TO_APPEND)) {
                traitsToAppend = getStringSet(destinationConfig, TRAITS_TO_APPEND);
            }
            if (destinationConfig.containsKey(TRAITS_TO_PREPEND)) {
                traitsToPrepend = getStringSet(destinationConfig, TRAITS_TO_PREPEND);
            }
            // other settings
            if (destinationConfig.containsKey(TRACK_SESSION_EVENTS)) {
                trackSessionEvents = (boolean) destinationConfig.get(TRACK_SESSION_EVENTS);
            }
            if (destinationConfig.containsKey(ENABLE_LOCATION_LISTENING)) {
                enableLocationListening = (boolean) destinationConfig.get(ENABLE_LOCATION_LISTENING);
            }
            if (destinationConfig.containsKey(USE_ADVERTISING_ID_FOR_DEVICE_ID)) {
                useAdvertisingIdForDeviceId = (boolean) destinationConfig.get(USE_ADVERTISING_ID_FOR_DEVICE_ID);
            }
            // all good. initialize amplitude sdk
            amplitude = Amplitude.getInstance();
            amplitude.initialize(RudderClient.getApplication(), apiKey).setLogLevel(Log.VERBOSE);
            // enabling Foreground Tracking
            amplitude.enableForegroundTracking(RudderClient.getApplication());
            // configuring Track Session Events
            amplitude.trackSessionEvents(trackSessionEvents);
            // Configuring Location Listening
            if (!enableLocationListening) {
                amplitude.disableLocationListening();
            }
            // Configuring usage of Advertising Id as Device Id
            if (useAdvertisingIdForDeviceId) {
                amplitude.useAdvertisingIdForDeviceId();
            }
            // configuring batching settings
            amplitude.setEventUploadPeriodMillis(eventUploadPeriodMillis);
            amplitude.setEventUploadThreshold(eventUploadThreshold);
            RudderLogger.logInfo("Configured Amplitude + Rudder integration and initialized Amplitude.");
        }
    }

    private void processRudderEvent(RudderMessage element) {
        String type = element.getType();
        if (type != null) {
            switch (type) {
                case MessageType.IDENTIFY:
                    String userId = element.getUserId();
                    if (!TextUtils.isEmpty(userId)) {
                        amplitude.setUserId(userId);
                    }
                    Map<String, Object> traits = element.getTraits();
                    if (traits != null) {
                        if (!isNullOrEmpty(traitsToIncrement) || !isNullOrEmpty(traitsToSetOnce) || !isNullOrEmpty(traitsToAppend) || !isNullOrEmpty(traitsToPrepend)) {
                            handleTraits(traits);
                        } else {
                            JSONObject traitsJson = new JSONObject(traits);
                            amplitude.setUserProperties(traitsJson);
                        }
                    }
                    break;
                case MessageType.TRACK:
                    String eventName = element.getEventName();
                    if (eventName == null) {
                        return;
                    }
                    Map<String, Object> eventProperties = element.getProperties();
                    JSONArray products = null;
                    if (eventProperties != null) {
                        if (eventProperties.containsKey("products")) {
                            products = getJSONArray(eventProperties.get("products"));
                        }
                    }
                    JSONArray allProducts = new JSONArray();
                    try {
                        if (trackProductsOnce) {
                            if (products != null) {
                                for (int i = 0; i < products.length(); i++) {
                                    JSONObject newProduct = getProductAttributes((JSONObject) products.get(i));
                                    allProducts.put(newProduct);
                                }
                                eventProperties.put("products", allProducts);
                                logEventAndCorrespondingRevenue(eventProperties, eventName, trackRevenuePerProduct);
                                if (trackRevenuePerProduct) {
                                    trackingEventAndRevenuePerProduct(eventProperties, products, false);
                                }
                            } else {
                                logEventAndCorrespondingRevenue(eventProperties, eventName, false);
                            }
                            return;
                        }
                        if (products != null) {
                            eventProperties.remove("products");
                            logEventAndCorrespondingRevenue(eventProperties, eventName, trackRevenuePerProduct);
                            trackingEventAndRevenuePerProduct(eventProperties, products, true);
                        } else {
                            logEventAndCorrespondingRevenue(eventProperties, eventName, false);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MessageType.SCREEN:
                    Map<String, Object> properties = element.getProperties();
                    JSONObject propertiesJSON = new JSONObject(properties);
                    try {
                        if (trackAllPages) {
                            if (!TextUtils.isEmpty((String) propertiesJSON.get("name"))) {
                                amplitude.logEvent(String.format(VIEWED_EVENT_FORMAT, propertiesJSON.get("name")), propertiesJSON, null, false);

                            } else {
                                amplitude.logEvent("Loaded a Screen", propertiesJSON, null, false);
                            }
                        }
                        if (trackCategorizedPages && !TextUtils.isEmpty((String) propertiesJSON.get("category"))) {
                            amplitude.logEvent(String.format(VIEWED_EVENT_FORMAT, propertiesJSON.get("category")), propertiesJSON, null, false);
                        }
                        if (trackNamedPages && !TextUtils.isEmpty((String) propertiesJSON.get("name"))) {
                            amplitude.logEvent(String.format(VIEWED_EVENT_FORMAT, propertiesJSON.get("name")), propertiesJSON, null, false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MessageType.GROUP:
                    String groupName = null;
                    String groupValue = element.getUserId();
                    Map<String, Object> groupTraits = element.getTraits();
                    if (groupTraits != null && groupTraits.size() != 0) {
                        if (groupTraits.containsKey(groupTypeTrait) && groupTraits.containsKey(groupValueTrait)) {
                            groupName = (String) groupTraits.get(groupTypeTrait);
                            groupValue = (String) groupTraits.get(groupValueTrait);
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

    public void handleTraits(Map<String, Object> traits) {
        Identify identify = new Identify();
        for (Map.Entry<String, Object> entry : traits.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (traitsToIncrement.contains(key)) {
                incrementTrait(key, value, identify);
            } else if (traitsToSetOnce.contains(key)) {
                setOnce(key, value, identify);
            } else if (traitsToAppend.contains(key)) {
                appendTrait(key, value, identify);
            } else if (traitsToPrepend.contains(key)) {
                prependTrait(key, value, identify);
            } else {
                setTrait(key, value, identify);
            }
        }
        amplitude.identify(identify);
    }


    private void incrementTrait(String key, Object value, Identify identify) {
        if (value instanceof Double) {
            double doubleValue = (Double) value;
            identify.add(key, doubleValue);
        }
        if (value instanceof Float) {
            float floatValue = (Float) value;
            identify.add(key, floatValue);
        }
        if (value instanceof Integer) {
            int intValue = (Integer) value;
            identify.add(key, intValue);
        }
        if (value instanceof Long) {
            long longValue = (Long) value;
            identify.add(key, longValue);
        }
        if (value instanceof String) {
            String stringValue = String.valueOf(value);
            identify.add(key, stringValue);
        }
    }

    private void appendTrait(String key, Object value, Identify identify) {
        if (value instanceof Double) {
            double doubleValue = (Double) value;
            identify.append(key, doubleValue);
        }
        if (value instanceof Float) {
            float floatValue = (Float) value;
            identify.append(key, floatValue);
        }
        if (value instanceof Integer) {
            int intValue = (Integer) value;
            identify.append(key, intValue);
        }
        if (value instanceof Long) {
            long longValue = (Long) value;
            identify.append(key, longValue);
        }
        if (value instanceof String) {
            String stringValue = String.valueOf(value);
            identify.append(key, stringValue);
        }
    }

    private void prependTrait(String key, Object value, Identify identify) {
        if (value instanceof Double) {
            double doubleValue = (Double) value;
            identify.prepend(key, doubleValue);
        }
        if (value instanceof Float) {
            float floatValue = (Float) value;
            identify.prepend(key, floatValue);
        }
        if (value instanceof Integer) {
            int intValue = (Integer) value;
            identify.prepend(key, intValue);
        }
        if (value instanceof Long) {
            long longValue = (Long) value;
            identify.prepend(key, longValue);
        }
        if (value instanceof String) {
            String stringValue = String.valueOf(value);
            identify.prepend(key, stringValue);
        }
    }

    private void setOnce(String key, Object value, Identify identify) {
        if (value instanceof Double) {
            double doubleValue = (Double) value;
            identify.setOnce(key, doubleValue);
        }
        if (value instanceof Float) {
            float floatValue = (Float) value;
            identify.setOnce(key, floatValue);
        }
        if (value instanceof Integer) {
            int intValue = (Integer) value;
            identify.setOnce(key, intValue);
        }
        if (value instanceof Long) {
            long longValue = (Long) value;
            identify.setOnce(key, longValue);
        }
        if (value instanceof String) {
            String stringValue = String.valueOf(value);
            identify.setOnce(key, stringValue);
        }
    }

    private void setTrait(String key, Object value, Identify identify) {
        if (value instanceof Double) {
            double doubleValue = (Double) value;
            identify.set(key, doubleValue);
        }
        if (value instanceof Float) {
            float floatValue = (Float) value;
            identify.set(key, floatValue);
        }
        if (value instanceof Integer) {
            int intValue = (Integer) value;
            identify.set(key, intValue);
        }
        if (value instanceof Long) {
            long longValue = (Long) value;
            identify.set(key, longValue);
        }
        if (value instanceof String) {
            String stringValue = String.valueOf(value);
            identify.set(key, stringValue);
        }
        if (value instanceof String[]) {
            identify.set(key, (String[]) value);
        }
    }

    public boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    static Set<String> getStringSet(Map<String, Object> destinationConfig, String key) {
        Set<String> stringSet = new HashSet<>();
        JSONObject traitsJson = new JSONObject(destinationConfig);
        JSONArray traitsArray;
        try {
            traitsArray = (JSONArray) traitsJson.get(key);
            for (int i = 0; i < traitsArray.length(); i++) {
                JSONObject traitJSON = (JSONObject) traitsArray.get(i);
                stringSet.add((String) traitJSON.get(TRAITS_KEY));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringSet;
    }

    static JSONObject getProductAttributes(JSONObject product) {
        JSONObject newProduct = new JSONObject();
        try {
            if (product.has("productId")) {
                newProduct.put("productId", product.get("productId"));
            }
            if (!newProduct.has("productId")) {
                if (product.has("product_id")) {
                    newProduct.put("productId", product.get("product_id"));
                }
            }
            if (product.has("sku")) {
                newProduct.put("sku", product.get("sku"));
            }
            if (product.has("name")) {
                newProduct.put("name", product.get("name"));
            }
            if (product.has("price")) {
                newProduct.put("price", product.get("price"));
            }
            if (product.has("quantity")) {
                newProduct.put("quantity", product.get("quantity"));
            }
            if (product.has("category")) {
                newProduct.put("category", product.get("category"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newProduct;
    }

    public void logEventAndCorrespondingRevenue(Map<String, Object> eventProperties, String eventName, boolean doNotTrackRevenue) {
        if (eventProperties == null) {
            amplitude.logEvent(eventName);
        } else {
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
    }

    public void trackRevenue(Map<String, Object> eventProperties, String eventName) {

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
            if (revenueObject instanceof Integer) {
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
            productId = (String) eventProperties.get("productId");
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

    public void trackingEventAndRevenuePerProduct(Map<String, Object> eventProperties, JSONArray allProducts, boolean shouldTrackEventPerProduct) {
        String revenueType = null;
        if (eventProperties.containsKey("revenueType")) {
            revenueType = (String) eventProperties.get("revenueType");
        }
        if (revenueType == null) {
            revenueType = (String) eventProperties.get("revenue_type");
        }
        try {
            for (int i = 0; i < allProducts.length(); i++) {
                JSONObject product = (JSONObject) allProducts.get(i);
                if (trackRevenuePerProduct) {
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
            e.printStackTrace();
        }
    }

    public Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Iterator<?> keys = json.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object value = json.get(key);
                map.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public JSONArray getJSONArray(Object object) {
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        } else {
            // if the object receieved was ArrayList
            ArrayList arrayList = (ArrayList) object;
            JSONArray jsonArray = new JSONArray(arrayList);
            return jsonArray;
        }
    }

}