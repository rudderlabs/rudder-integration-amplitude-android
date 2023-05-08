package com.rudderstack.android.integrations.amplitude;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderMessageBuilder;
import com.rudderstack.android.sdk.core.RudderUserProperty;
import com.rudderstack.android.sdk.core.TrackPropertyBuilder;

import org.hamcrest.Matchers;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestData {
    private TestData() {
    }

    private static final RudderMessage RUDDER_INPUT_TRACK_1 = new RudderMessageBuilder().setEventName("track_1")
            .setUserId("u-id")
            .setUserProperty(Map.of("name", "Debanjan", "gender", "male"))
            .setProperty(new TrackPropertyBuilder()
                    .setCategory("category_1")
                    .setLabel("label_1")
                    .setValue("value_1")
                    .build()).build();
    static final TestCondition TEST_TRACK_CONDITION_1 = new TestCondition(
    ) {
        private final RudderMessage inputMessage = Mockito.spy(RUDDER_INPUT_TRACK_1);

        @Override
        RudderMessage getInputMessage() {
            Mockito.doReturn(MessageType.TRACK).when(inputMessage).getType();
            return inputMessage;
        }

        @Override
        void assertCondition(String amplitudeEventName, Map<String, Object> outputProperties, String userId) {
            assertThat(amplitudeEventName, is(getInputMessage().getEventName()));
            System.out.println("Captured: \n" + outputProperties);
            assertThat(outputProperties.keySet(), hasSize(3));
            assertThat(outputProperties, allOf(
                    hasEntry("label", "label_1"),
                    hasEntry("category", "category_1"),
                    hasEntry("value", "value_1")
            ));
        }
    };
    private static final RudderMessage RUDDER_INPUT_IDENTIFY_1 = new RudderMessageBuilder().setEventName("identify_1")
            .setUserId("u-id")
            .build();
    static final TestCondition TEST_IDENTIFY_CONDITION_1 = new TestCondition(
    ) {
        private final RudderMessage inputMessage = Mockito.spy(RUDDER_INPUT_IDENTIFY_1);

        @Override
        RudderMessage getInputMessage() {
            Mockito.doReturn(MessageType.IDENTIFY).when(inputMessage).getType();
            Mockito.doReturn(Map.of("name", "Debanjan", "gender", "male")).when(inputMessage).getTraits();
            return inputMessage;
        }

        @Override
        void assertCondition(String amplitudeEventName, Map<String, Object> outputProperties, String userId) {
            System.out.println("Captured: \n" + outputProperties);
            assertThat(userId, is(inputMessage.getUserId()));
            assertThat(outputProperties.keySet(), hasSize(2));
            assertThat(outputProperties, allOf(
                    hasEntry("name", "Debanjan"),
                    hasEntry("gender", "male")
            ));
        }
    };


}
