package com.rudderstack.android.integrations.amplitude;

import static java.util.Objects.requireNonNull;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.amplitude.android.Amplitude;
import com.amplitude.android.Configuration;
import com.amplitude.android.TrackingOptions;
import com.amplitude.android.events.Identify;
import com.amplitude.android.events.Revenue;
import com.amplitude.android.utilities.AndroidLoggerProvider;
import com.amplitude.android.utilities.AndroidStorageProvider;
import com.amplitude.core.LoggerProvider;
import com.amplitude.core.ServerZone;
import com.amplitude.core.StorageProvider;
import com.amplitude.core.events.IngestionMetadata;
import com.amplitude.core.events.Plan;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class AmplitudeIntegrationFactory extends RudderIntegration<Amplitude> {
    private static final String AMPLITUDE_KEY = "Amplitude";
    private static final String VIEWED_EVENT_FORMAT = "Viewed %s Screen ";
    private static final String DEFAULT_INSTANCE_NAME = "DEFAULT_INSTANCE";
    private static final Set<String> REVENUE_TYPE_SET = Collections.unmodifiableSet(new
            HashSet<>(Arrays.asList("order completed",
            "completed order",
            "product purchased")));
    private static final boolean DEFAULT_OPT_OUT = false;
    private static final String PARTNER_ID = "Rudderstack";
    private static final String REVENUE_TYPE_LABEL = "revenueType";
    private static final String REVENUE_LABEL = "revenue";
//    private static final int MA

    private Amplitude amplitude;
    private AmplitudeDestinationConfig destinationConfig;
    private Set<String> traitsToIncrement;
    private Set<String> traitsToSetOnce;
    private Set<String> traitsToAppend;
    private Set<String> traitsToPrepend;


    public static final Factory FACTORY = new Factory() {
        @Override
        public RudderIntegration<?> create(Object settings, RudderClient client, RudderConfig rudderConfig) {
            return new AmplitudeIntegrationFactory(settings);
        }

        @Override
        public String key() {
            return AMPLITUDE_KEY;
        }
    };

    @VisibleForTesting
    AmplitudeIntegrationFactory() {
    }

    private AmplitudeIntegrationFactory(Object config) {
        super();
        if (!assertValidConfigs(config))
            return;
        AmplitudeDestinationConfig parsedDestinationConfig = parseDestinationConfig(config);
        if (!assertValidDestinationConfig(parsedDestinationConfig))
            return;
        setup(parsedDestinationConfig,
                createAmplitudeInstance(requireNonNull(RudderClient.getApplication()),
                        parsedDestinationConfig, new AndroidStorageProvider(),
                        new AndroidLoggerProvider()));
    }

    @VisibleForTesting
    void setup(@NonNull AmplitudeDestinationConfig config, @NonNull Amplitude amplitude) {
        this.destinationConfig = config;
        configureTraitsSettings();
        this.amplitude = amplitude;
    }

    @VisibleForTesting
    Amplitude createAmplitudeInstance(
            @NonNull Application application, AmplitudeDestinationConfig destinationConfig,
            @NonNull StorageProvider storageProvider, @NonNull LoggerProvider loggerProvider) {
        Configuration configuration = new Configuration(
                destinationConfig.apiKey, application,
                getFlushQueueSizeFromConfig(destinationConfig), getFlushIntervalFromConfig(destinationConfig),
                DEFAULT_INSTANCE_NAME, getOptOutFromConfig(destinationConfig),
                storageProvider, loggerProvider, null, PARTNER_ID,
                null, getMaxRetriesFromConfig(destinationConfig),
                getUseBatchFromConfig(destinationConfig),
                getServerZone(destinationConfig), destinationConfig.serverUrl,
                getPlanFromConfig(destinationConfig),
                getIngestionMetaDataFromConfig(destinationConfig),
                destinationConfig.useAdvertisingIdForDeviceId,
                getUseAppSetIdForDeviceId(destinationConfig),
                getNewDeviceIdPerInstall(destinationConfig), getTrackingOptions(destinationConfig.trackingOptions),
                getEnableCoppaControlFroConfig(destinationConfig), destinationConfig.enableLocationListening,
                getFlushEventsOnCloseFromConfig(destinationConfig), getMinTimeBetweenSessionMillisFromConfig(destinationConfig),
                destinationConfig.trackSessionEvents,
                getIdentifyBatchIntervalMillisFromConfig(destinationConfig),
                storageProvider

        );
        return new Amplitude(configuration);
    }

    private TrackingOptions getTrackingOptions(@Nullable AmplitudeTrackingOptions amplitudeTrackingOptions) {
        TrackingOptions trackingOptions = new TrackingOptions();
        if (amplitudeTrackingOptions == null)
            return trackingOptions;
        if (amplitudeTrackingOptions.adId != null && !amplitudeTrackingOptions.adId) {
            trackingOptions.disableAdid();
        }
        if (amplitudeTrackingOptions.appSetId != null && !amplitudeTrackingOptions.appSetId) {
            trackingOptions.disableAppSetId();
        }
        if (amplitudeTrackingOptions.carrier != null && !amplitudeTrackingOptions.carrier) {
            trackingOptions.disableCarrier();
        }
        if (amplitudeTrackingOptions.city != null && !amplitudeTrackingOptions.city) {
            trackingOptions.disableCity();
        }
        if (amplitudeTrackingOptions.country != null && !amplitudeTrackingOptions.country) {
            trackingOptions.disableCountry();
        }
        if (amplitudeTrackingOptions.deviceBrand != null && !amplitudeTrackingOptions.deviceBrand) {
            trackingOptions.disableDeviceBrand();
        }
        if (amplitudeTrackingOptions.deviceManufacturer != null && !amplitudeTrackingOptions.deviceManufacturer) {
            trackingOptions.disableDeviceManufacturer();
        }
        if (amplitudeTrackingOptions.deviceModel != null && !amplitudeTrackingOptions.deviceModel) {
            trackingOptions.disableDeviceModel();
        }
        if (amplitudeTrackingOptions.dma != null && !amplitudeTrackingOptions.dma){
            trackingOptions.disableDma();
        }
        if (amplitudeTrackingOptions.ipAddress != null && !amplitudeTrackingOptions.ipAddress) {
            trackingOptions.disableIpAddress();
        }
        if (amplitudeTrackingOptions.language != null && !amplitudeTrackingOptions.language) {
            trackingOptions.disableLanguage();
        }
        if (amplitudeTrackingOptions.latlng != null && !amplitudeTrackingOptions.latlng) {
            trackingOptions.disableLatLng();
        }
        if (amplitudeTrackingOptions.apiLevel != null && !amplitudeTrackingOptions.apiLevel) {
            trackingOptions.disableApiLevel();
        }
        if (amplitudeTrackingOptions.platform != null && !amplitudeTrackingOptions.platform) {
            trackingOptions.disablePlatform();
        }
        if (amplitudeTrackingOptions.region != null && !amplitudeTrackingOptions.region) {
            trackingOptions.disableRegion();
        }
        if (amplitudeTrackingOptions.versionName != null && !amplitudeTrackingOptions.versionName) {
            trackingOptions.disableVersionName();
        }
        if (amplitudeTrackingOptions.osName != null && !amplitudeTrackingOptions.osName) {
            trackingOptions.disableOsName();
        }
        if (amplitudeTrackingOptions.osVersion != null && !amplitudeTrackingOptions.osVersion) {
            trackingOptions.disableOsVersion();
        }
        return trackingOptions;
    }

    private int getFlushIntervalFromConfig(AmplitudeDestinationConfig destinationConfig) {
        if (destinationConfig.eventUploadPeriodMillis > 0)
            return destinationConfig.eventUploadPeriodMillis;
        return com.amplitude.core.Configuration.FLUSH_INTERVAL_MILLIS;
    }

    private int getFlushQueueSizeFromConfig(AmplitudeDestinationConfig destinationConfig) {
        if (destinationConfig.eventUploadThreshold > 0)
            return destinationConfig.eventUploadThreshold;
        return com.amplitude.core.Configuration.FLUSH_QUEUE_SIZE;
    }

    private boolean getEnableCoppaControlFroConfig(AmplitudeDestinationConfig destinationConfig) {
        if (destinationConfig.enableCoppaControl == null)
            return false;
        return destinationConfig.enableCoppaControl;
    }

    private boolean getFlushEventsOnCloseFromConfig(AmplitudeDestinationConfig destinationConfig) {
        if (destinationConfig.flushEventsOnClose == null)
            return true;
        return destinationConfig.flushEventsOnClose;
    }

    private long getMinTimeBetweenSessionMillisFromConfig(AmplitudeDestinationConfig destinationConfig) {
        if (destinationConfig.minTimeBetweenSessionMillis == null ||
                destinationConfig.minTimeBetweenSessionMillis == 0)
            return Configuration.MIN_TIME_BETWEEN_SESSIONS_MILLIS;
        return destinationConfig.minTimeBetweenSessionMillis;
    }

    private long getIdentifyBatchIntervalMillisFromConfig(AmplitudeDestinationConfig destinationConfig) {
        if (destinationConfig.identifyBatchIntervalMillis == null ||
                destinationConfig.identifyBatchIntervalMillis == 0)
            return com.amplitude.core.Configuration.IDENTIFY_BATCH_INVERVAL_MILLIS;
        return destinationConfig.identifyBatchIntervalMillis;
    }


    private IngestionMetadata getIngestionMetaDataFromConfig(AmplitudeDestinationConfig destinationConfig) {
        AmplitudeIngestionMetadata amplitudeIngestionMetadata = destinationConfig.ingestionMetadata;
        if (amplitudeIngestionMetadata == null)
            return null;
        return new com.amplitude.android.events.IngestionMetadata(amplitudeIngestionMetadata.sourceName,
                amplitudeIngestionMetadata.sourceVersion);
    }

    private boolean getUseAppSetIdForDeviceId(AmplitudeDestinationConfig destinationConfig) {
        if (destinationConfig.useAppSetIdForDeviceId == null)
            return false;
        return destinationConfig.useAppSetIdForDeviceId;
    }

    private boolean getNewDeviceIdPerInstall(AmplitudeDestinationConfig destinationConfig) {
        if (destinationConfig.newDeviceIdPerInstall == null)
            return false;
        return destinationConfig.newDeviceIdPerInstall;
    }

    private @Nullable
    Plan getPlanFromConfig(AmplitudeDestinationConfig destinationConfig) {
        AmplitudePlan amplitudePlan = destinationConfig.plan;
        if (amplitudePlan == null)
            return null;
        return new Plan(amplitudePlan.branch, amplitudePlan.source, amplitudePlan.version,
                amplitudePlan.versionId);
    }

    private boolean getUseBatchFromConfig(AmplitudeDestinationConfig destinationConfig) {
        if (destinationConfig.useBatch == null)
            return true;
        return destinationConfig.useBatch;
    }

    private int getMaxRetriesFromConfig(AmplitudeDestinationConfig destinationConfig) {
        if (destinationConfig.flushMaxRetries == null || destinationConfig.flushMaxRetries == 0)
            return com.amplitude.core.Configuration.FLUSH_MAX_RETRIES;
        return destinationConfig.flushMaxRetries;
    }

    private boolean getOptOutFromConfig(AmplitudeDestinationConfig destinationConfig) {
        if (destinationConfig.optOut == null)
            return DEFAULT_OPT_OUT;
        return destinationConfig.optOut;
    }


    private ServerZone getServerZone(AmplitudeDestinationConfig config) {
        if ("EU".equals(config.residencyServer)) {
            return ServerZone.US;
        }
        return ServerZone.EU;
    }

    private void configureTraitsSettings() {
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
    }

    private boolean assertValidDestinationConfig(AmplitudeDestinationConfig destinationConfig) {
        if (TextUtils.isEmpty(destinationConfig.apiKey)) {
            RudderLogger.logError("Invalid api key. Aborting Amplitude initialization.");
            return false;
        }
        return true;
    }

    private AmplitudeDestinationConfig parseDestinationConfig(Object config) {
        // parse server config
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(config), AmplitudeDestinationConfig.class);

    }

    private boolean assertValidConfigs(Object config) {
        if (RudderClient.getApplication() == null) {
            RudderLogger.logError("Application is null. Aborting Amplitude initialization.");
            return false;
        }

        if (config == null) {
            RudderLogger.logError("Invalid api key. Aborting Amplitude initialization.");
            return false;
        }
        return true;
    }

    @Override
    public void flush() {
        super.flush();
        this.amplitude.flush();
        RudderLogger.logDebug("Amplitude uploadEvents().");
    }

    @Override
    public void reset() {
        this.amplitude.reset();
        RudderLogger.logVerbose("Amplitude reset().");
    }

    @Override
    public void dump(RudderMessage rudderMessage) {
        try {
            if (rudderMessage != null) {
                processRudderEvent(rudderMessage);
            }
        } catch (Exception e) {
            RudderLogger.logError(e);
        }
    }

    @Override
    public Amplitude getUnderlyingInstance() {
        return this.amplitude;
    }

    private void processRudderEvent(RudderMessage message) throws Exception {
        String type = message.getType();
        if (type == null)
            return;
        switch (type) {
            case MessageType.IDENTIFY:
                identify(message);
                break;
            case MessageType.TRACK:
                track(message);
                break;
            case MessageType.SCREEN:
                screen(message);
                break;
            default:
                RudderLogger.logError(type + " is not supported for Amplitude. Dropping event");
        }
    }

    private void screen(RudderMessage message) {
        Map<String, Object> properties = message.getProperties();
        if (this.destinationConfig.trackAllPages) {
            trackAllPages(properties);
        }
        if (this.destinationConfig.trackCategorizedPages) {
            trackCategorizedPages(properties);
        }
        if (this.destinationConfig.trackNamedPages) {
            trackNamedPages(properties);
        }
    }

    private void trackNamedPages(Map<String, Object> properties) {
        if (properties == null)
            return;
        Object categoryObject = properties.get("name");
        if (categoryObject instanceof String && !TextUtils.isEmpty((String) categoryObject)) {
            amplitude.track(String.format(VIEWED_EVENT_FORMAT, categoryObject),
                    properties);
        }
    }

    private void trackCategorizedPages(Map<String, Object> properties) {
        if (properties == null)
            return;
        Object categoryObject = properties.get("category");
        if (categoryObject instanceof String && !TextUtils.isEmpty((String) categoryObject)) {
            amplitude.track(String.format(VIEWED_EVENT_FORMAT, categoryObject),
                    properties);
        }
    }

    private void trackAllPages(Map<String, Object> properties) {
        if (properties == null)
            return;
        Object nameObject = properties.get("name");

        if (nameObject instanceof String && !TextUtils.isEmpty((String) nameObject)) {
            this.amplitude.track(String.format(VIEWED_EVENT_FORMAT, nameObject),
                    properties);
        } else {
            this.amplitude.track("Loaded a Screen",
                    properties);
        }
    }

    @VisibleForTesting
    void track(RudderMessage message) throws Exception {
        String eventName = message.getEventName();
        if (eventName == null) {
            return;
        }
        Map<String, Object> eventProperties = message.getProperties();
        JSONArray products = Utils.getProducts(eventProperties);
        if (this.destinationConfig.trackProductsOnce) {
            trackProductOnce(products, eventName, eventProperties);
            return;
        }
        // if track products once is disabled and we are having a products array
        if (products != null && eventProperties != null) {
            trackProductArray(products, eventName, eventProperties);
            return;
        }
        // if track products once is disabled and we are not having a products array
        trackEventAndCorrespondingRevenue(
                eventProperties,
                eventName,
                false
        );
    }

    private void trackProductArray(JSONArray products, String eventName,
                                   Map<String, Object> eventProperties) throws JSONException {
        // removing products property from event properties to make
        // a call with no products first and then we will make a call for
        // each product separately as trackProductsOnce is disabled
        eventProperties.remove("products");
        trackEventAndCorrespondingRevenue(
                eventProperties,
                eventName,
                this.destinationConfig.trackRevenuePerProduct
        );
        trackEventAndRevenuePerProduct(
                eventProperties,
                products,
                true
        );
    }

    private void trackProductOnce(JSONArray products, String eventName,
                                  Map<String, Object> eventProperties) throws Exception {
        // if track products once is enabled and  we are having products array
        if (products != null && eventProperties != null) {
            JSONArray simplifiedProducts = Utils.simplifyProducts(products);
            eventProperties.put("products", simplifiedProducts);
            trackEventAndCorrespondingRevenue(
                    eventProperties,
                    eventName,
                    this.destinationConfig.trackRevenuePerProduct
            );
            // if track revenue per product is enabled
            if (this.destinationConfig.trackRevenuePerProduct) {
                trackEventAndRevenuePerProduct(
                        eventProperties,
                        products,
                        false
                );
            }
            return;
        }
        // if track products once is enabled and
        // we are not having a products array
        trackEventAndCorrespondingRevenue(
                eventProperties,
                eventName,
                false
        );
    }

    private void trackEventAndRevenuePerProduct(Map<String, Object> eventProperties, JSONArray allProducts,
                                                boolean shouldTrackEventPerProduct) throws JSONException {
        String revenueType = getRevenueTypeFromProperties(eventProperties);

        for (int i = 0; i < allProducts.length(); i++) {
            JSONObject product = (JSONObject) allProducts.get(i);
            trackProductRevenue(revenueType, product);
            if (shouldTrackEventPerProduct) {
                trackEventAndCorrespondingRevenue(
                        Utils.jsonToMap(product),
                        "Product Purchased",
                        true
                );
            }
        }
    }

    private void trackProductRevenue(String revenueType, JSONObject product) throws JSONException {
        if (this.destinationConfig.trackRevenuePerProduct) {
            if (revenueType != null) {
                product.put(REVENUE_TYPE_LABEL, revenueType);
            }
            trackRevenue(
                    Utils.jsonToMap(product),
                    "Product Purchased"
            );
        }

    }

    private @Nullable
    String getRevenueTypeFromProperties(Map<String, Object> eventProperties) {
        if (eventProperties.containsKey(REVENUE_TYPE_LABEL)) {
            return (String) eventProperties.get(REVENUE_TYPE_LABEL);
        }
        return null;
    }

    private void trackEventAndCorrespondingRevenue(Map<String, Object> eventProperties,
                                                   String eventName, boolean doNotTrackRevenue) {
        if (eventProperties == null) {
            this.amplitude.track(eventName);
            return;
        }
        amplitude.track(eventName, eventProperties);
        if (eventProperties.containsKey(REVENUE_LABEL) && !doNotTrackRevenue) {
            trackRevenue(eventProperties, eventName);
        }
    }

    private void trackRevenue(Map<String, Object> eventProperties, String eventName) {
        if (eventProperties == null) {
            RudderLogger.logDebug("AmplitudeIntegration: eventProperties is null");
            return;
        }
        Revenue revenue = new Revenue();
        revenue.setProperties(eventProperties);
        updateRevenueWithValues(revenue, eventName, eventProperties);
        amplitude.revenue(revenue);
    }

    private void updateRevenueWithValues(Revenue revenue, String eventName,
                                         Map<String, Object> eventProperties) {
        updateRevenueWithPricingDetails(revenue, eventProperties);
        updateRevenueWithroductDetails(revenue, eventProperties);
        updateRevenueWithRevenueDetails(revenue, eventName, eventProperties);
        updateRevenueWithReceiptDetails(revenue, eventProperties);

    }

    private void updateRevenueWithReceiptDetails(Revenue revenue, Map<String, Object> eventProperties) {
        Object receipt = eventProperties.get("receipt");
        Object receiptSignature = eventProperties.get("receiptSignature");
        if (receipt instanceof String && receiptSignature instanceof String
        ) {
            revenue.setReceipt(
                    (String) receipt,
                    (String) receiptSignature
            );
        }
    }

    private void updateRevenueWithRevenueDetails(Revenue revenue, String eventName,
                                                 Map<String, Object> eventProperties) {
        if (eventProperties.containsKey(REVENUE_TYPE_LABEL)) {
            revenue.setRevenueType((String) eventProperties.get(REVENUE_TYPE_LABEL));
        } else if (REVENUE_TYPE_SET.contains(eventName.toLowerCase())) {
            revenue.setRevenueType("Purchase");
        }

    }

    private void updateRevenueWithroductDetails(Revenue revenue, Map<String, Object> eventProperties) {
        if (eventProperties.containsKey("productId")) {
            revenue.setProductId(String.valueOf(eventProperties.get("productId")));
        } else if (eventProperties.containsKey("product_id")) {
            revenue.setProductId(String.valueOf(eventProperties.get("product_id")));
        }

    }

    private void updateRevenueWithPricingDetails(Revenue revenue, Map<String, Object> eventProperties) {
        double quantity = 0;
        double revenueValue = 0;
        double price = 0;
        if (eventProperties.containsKey("quantity")) {
            quantity = new NumberObject(eventProperties.get("quantity"))
                    .getNumber();
        }

        if (eventProperties.containsKey(REVENUE_LABEL)) {
            revenueValue = new NumberObject(eventProperties.get(REVENUE_LABEL)).getNumber();
        }

        if (eventProperties.containsKey("price")) {
            price = new NumberObject(eventProperties.get("price")).getNumber();
        }

        if (revenueValue == 0 && price == 0) {
            RudderLogger.logDebug("revenue or price is not present.");
            return;
        }

        if (price == 0) {
            price = revenueValue;
            quantity = 1;
        }

        if (quantity == 0) {
            quantity = 1;
        }
        revenue.setPrice(price);
        revenue.setQuantity((int) quantity);
    }


    private void identify(RudderMessage message) {
        String userId = message.getUserId();
        if (!TextUtils.isEmpty(userId)) {
            this.amplitude.setUserId(userId);
        }
        Map<String, Object> traits = message.getTraits();
        com.amplitude.core.events.Identify identify = configureTraits(traits);
        amplitude.identify(identify);
    }

    private Identify configureTraits(Map<String, Object> traits) {
        Identify identify = new Identify();
        for (Map.Entry<String, Object> entry : traits.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            addTraitToIdentify(identify, key, value);
        }
        return identify;
    }

    private void addTraitToIdentify(Identify identify, String key, Object property) {
        if (traitsToIncrement != null && this.traitsToIncrement.contains(key)) {
            TraitsHandler.incrementTrait(identify, key, property);
            return;
        }
        if (traitsToSetOnce != null && this.traitsToSetOnce.contains(key)) {
            TraitsHandler.setOnce(identify, key, property);
            return;
        }
        if (traitsToAppend != null && this.traitsToAppend.contains(key)) {
            TraitsHandler.appendTrait(identify, key, property);
            return;
        }
        if (traitsToPrepend != null && this.traitsToPrepend.contains(key)) {
            TraitsHandler.prependTrait(identify, key, property);
            return;
        }
        TraitsHandler.setTrait(identify, key, property);
    }
}
