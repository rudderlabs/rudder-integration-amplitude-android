package com.rudderstack.android.integrations.amplitude;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderClient;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderIntegration;
import com.rudderstack.android.sdk.core.RudderLogger;
import com.rudderstack.android.sdk.core.RudderMessage;

import java.util.Locale;
import java.util.Map;

public class AmplitudeIntegrationFactory extends RudderIntegration<RudderClient> {

    @Override
    public void reset() {

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
}