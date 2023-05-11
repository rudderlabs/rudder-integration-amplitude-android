package com.rudderstack.android.integrations.amplitude;

import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.RudderMessage;

import java.util.Map;

abstract class TestCondition {

    abstract RudderMessage getInputMessage();

    abstract void assertCondition(@Nullable String event, Map<String, Object> outputProperties,
                                  @Nullable String userId);
}
