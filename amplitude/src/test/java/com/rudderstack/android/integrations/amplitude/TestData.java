package com.rudderstack.android.integrations.amplitude;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.rudderstack.android.sdk.core.MessageType;
import com.rudderstack.android.sdk.core.RudderMessage;
import com.rudderstack.android.sdk.core.RudderMessageBuilder;
import com.rudderstack.android.sdk.core.TrackPropertyBuilder;

import org.hamcrest.Matchers;
import org.mockito.Mockito;

import java.util.Map;

public class TestData {
    private TestData() {
    }

    private static final RudderMessage RUDDER_INPUT_TRACK_1 = new RudderMessageBuilder().setEventName("track_1")
            .setProperty(new TrackPropertyBuilder()
                    .setCategory("category_1")
                    .setLabel("label_1")
                    .setValue("value_1")
                    .build()).build();
    //
    static final TestCondition TEST_TRACK_CONDITION_1 = new TestCondition(
    ) {
        private final RudderMessage inputMessage = Mockito.spy(RUDDER_INPUT_TRACK_1);

        @Override
        RudderMessage getInputMessage() {
            Mockito.doReturn(MessageType.TRACK).when(inputMessage).getType();
            return inputMessage;
        }

        @Override
        void assertCondition(String amplitudeEventName, Map<String, Object> outputProperties) {
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
}
