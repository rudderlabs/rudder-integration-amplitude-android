package com.rudderstack.android.integrations.amplitude;

import com.rudderstack.android.sdk.core.RudderMessage;

import java.util.Map;

abstract class TestCondition {

    abstract RudderMessage getInputMessage();

    abstract void assertCondition(String amplitudeEventName, Map<String, Object> outputProperties);
}
