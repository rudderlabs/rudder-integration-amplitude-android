package com.rudderstack.android.integrations.amplitude;

import java.util.List;
import java.util.Map;

public class AmplitudeDestinationConfig {
    String apiKey;
    int eventUploadPeriodMillis;
    int eventUploadThreshold;
    boolean trackAllPages;
    boolean trackCategorizedPages;
    boolean trackNamedPages;
    boolean trackProductsOnce;
    boolean trackRevenuePerProduct;
    boolean enableLocationListening;
    boolean useAdvertisingIdForDeviceId;
    boolean trackSessionEvents;
    String residencyServer;
    List<Map<String, Object>> traitsToIncrement;
    List<Map<String, Object>> traitsToSetOnce;
    List<Map<String, Object>> traitsToAppend;
    List<Map<String, Object>> traitsToPrepend;
}
