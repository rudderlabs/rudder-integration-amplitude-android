package com.rudderstack.android.integrations.amplitude;

import java.util.ArrayList;
import java.util.Map;

public class AmplitudeDestinationConfig {
    String apiKey;
    String groupTypeTrait;
    String groupValueTrait;
    int eventUploadPeriodMillis;
    int eventUploadThreshold;
    boolean trackAllPages;
    boolean trackCategorizedPages;
    boolean trackNamedPages;
    static boolean trackProductsOnce;
    boolean trackRevenuePerProduct;
    boolean enableLocationListening;
    boolean useAdvertisingIdForDeviceId;
    boolean trackSessionEvents;
    ArrayList<Map<String,Object>> traitsToIncrement;
    ArrayList<Map<String,Object>> traitsToSetOnce;
    public ArrayList<Map<String,Object>> traitsToAppend;
    ArrayList<Map<String,Object>> traitsToPrepend;
}
