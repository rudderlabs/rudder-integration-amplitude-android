package com.rudderstack.android.integrations.amplitude;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amplitude.api.Amplitude;
import com.amplitude.api.AmplitudeClient;
import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;

import java.util.Locale;
import java.util.Map;

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

            // all good. initialize amplitude sdk
            amplitude = Amplitude.getInstance();
            Amplitude.initialize(RudderClient.getApplication(),apiKey);
            RudderLogger.logInfo("Configured Amplitude + Rudder integration and initialized Amplitude.");
        }
    }

    private void processRudderEvent(RudderMessage element) {
        if (element.getType() != null) {
            switch (element.getType()) {
                case MessageType.IDENTIFY:
                    break;
                case MessageType.TRACK:
                    break;
                case MessageType.SCREEN:
                    break;
                case "group":
                    break;
                case "alias":
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

            }
        } catch (Exception e) {
            RudderLogger.logError(e);
        }
    }

    @Override
    public AmplitudeClient getUnderlyingInstance() {
        return amplitude;
    }
}