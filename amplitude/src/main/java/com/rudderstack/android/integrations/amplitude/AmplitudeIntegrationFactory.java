package com.rudderstack.android.integrations.amplitude;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.amplitude.api.Amplitude;
import com.amplitude.api.AmplitudeClient;
import com.amplitude.api.Identify;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private static final String VIEWED_EVENT_FORMAT = "Viewed Screen %s";
    private static final String TRACK_ALL_PAGES = "trackAllPages";
    private static final String TRACK_NAMED_PAGES = "trackNamedPages";
    private static final String TRACK_CATEGORIZED_PAGES = "trackCategorizedPages";
    private static final String TRAITS_TO_INCREMENT = "traitsToIncrement";
    private static final String TRAITS_TO_SET_ONCE = "traitsToSetOnce";
    private static final String TRAITS_KEY = "traits";

    String groupTypeTrait;
    String groupValueTrait;
    boolean trackAllPages;
    boolean trackCategorizedPages;
    boolean trackNamedPages;
    Set<String> traitsToIncrement;
    Set<String> traitsToSetOnce;


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
            if (destinationConfig.containsKey(GROUP_TYPE_TRAIT)) {
                groupTypeTrait = (String) destinationConfig.get(GROUP_TYPE_TRAIT);
            }
            if (destinationConfig.containsKey(GROUP_VALUE_TRAIT)) {
                groupValueTrait = (String) destinationConfig.get(GROUP_VALUE_TRAIT);
            }
            if (destinationConfig.containsKey(TRACK_ALL_PAGES)) {
                trackAllPages = (boolean) destinationConfig.get(TRACK_ALL_PAGES);
            }
            if (destinationConfig.containsKey(TRACK_CATEGORIZED_PAGES)) {
                trackCategorizedPages = (boolean) destinationConfig.get(TRACK_CATEGORIZED_PAGES);
            }
            if (destinationConfig.containsKey(TRACK_NAMED_PAGES)) {
                trackNamedPages = (boolean) destinationConfig.get(TRACK_NAMED_PAGES);
            }
            if (destinationConfig.containsKey(TRAITS_TO_INCREMENT)) {
                traitsToIncrement = getStringSet(destinationConfig, TRAITS_TO_INCREMENT);
            }
            if (destinationConfig.containsKey(TRAITS_TO_SET_ONCE)) {
                traitsToSetOnce = getStringSet(destinationConfig, TRAITS_TO_SET_ONCE);
            }

            // all good. initialize amplitude sdk
            amplitude = Amplitude.getInstance();
            amplitude.initialize(RudderClient.getApplication(), apiKey);
            RudderLogger.logInfo("Configured Amplitude + Rudder integration and initialized Amplitude.");
        }
    }

    private void processRudderEvent(RudderMessage element) {
        if (element.getType() != null) {
            switch (element.getType()) {
                case MessageType.IDENTIFY:
                    String userId = element.getUserId();
                    amplitude.setUserId(userId);
                    Map<String, Object> traits = element.getTraits();
                    if (!isNullOrEmpty(traitsToIncrement) || !isNullOrEmpty(traitsToSetOnce)) {
                        handleTraits(traits);
                    } else {
                        JSONObject traitsJson = new JSONObject(traits);
                        amplitude.setUserProperties(traitsJson);
                    }
                    break;
                case MessageType.TRACK:
                    String eventName = element.getEventName();
                    if (eventName == null) {
                        return;
                    }
                    Map<String, Object> eventProperties = element.getProperties();
                    try {
                        if (element.getEventName().equals("Order Completed") && eventProperties != null && eventProperties.containsKey("revenue")) {
                            //logic to handle the revenue based events
                        }
                    } catch (Exception exception) {
                        RudderLogger.logError(exception);
                    }
                    if (eventProperties == null || eventProperties.size() == 0) {
                        RudderLogger.logDebug("Amplitude event has no properties");
                        amplitude.logEvent(element.getEventName());
                        return;
                    }
                    // Do we need to handle nested JSON Objects, once check with Amplitude Native SDK
                    JSONObject propertiesJson = mapToJSON(eventProperties);
                    amplitude.logEvent(eventName, propertiesJson);
                    break;
                case MessageType.SCREEN:
                    Map<String, Object> properties = element.getProperties();
                    JSONObject propertiesJSON = new JSONObject(properties);
                    try {
                        if (trackAllPages) {
                            amplitude.logEvent("Loaded a Screen", propertiesJSON, null, false);
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

    static JSONObject mapToJSON(Map<String, Object> eventProperties) {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, Object> entry : eventProperties.entrySet()) {
                if (entry.getValue() instanceof RudderProperty) {
                    RudderProperty rudderProperty = (RudderProperty) entry.getValue();
                    // need to convert RudderProperty to Map or JSON because if we do new JSONObject(eventProperties)
                    // we are having a null in the place of nested object
                    // We had a method called getMap() in RudderProperty but its not Public
                    // It could be better if we could have a method called getJSON() and it would return the JSON Structure of the entire object
                    //Map<String,Object> nestedMap = (Map<String,Object>)entry.getValue();
                    //JSONObject nestedObject = new JSONObject(nestedMap);
                    //System.out.println("Nested Object : "+nestedObject.toString());
                    //json.put(entry.getKey(), new JSONObject(nestedMap));
                } else {
                    json.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

}