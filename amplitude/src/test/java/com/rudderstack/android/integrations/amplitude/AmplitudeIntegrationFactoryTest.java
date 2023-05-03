package com.rudderstack.android.integrations.amplitude;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import android.app.Application;

import com.amplitude.android.Amplitude;
import com.amplitude.core.LoggerProvider;
import com.amplitude.core.StorageProvider;
import com.amplitude.core.events.Identify;
import com.amplitude.core.utilities.ConsoleLoggerProvider;
import com.amplitude.core.utilities.InMemoryStorage;
import com.rudderstack.android.sdk.core.RudderConfig;
import com.rudderstack.android.sdk.core.RudderMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;
//@RunWith(PowerMockRunner.class)
public class AmplitudeIntegrationFactoryTest {
    private static final String TEST_API_KEY = "test_key";
    Amplitude amplitude;
    private AmplitudeIntegrationFactory amplitudeIntegrationFactory;
    Application application;
    private AutoCloseable closeable;
    private final StorageProvider dummyStorageProvider = (amplitude, s) -> new InMemoryStorage(amplitude);

    private final LoggerProvider consoleLoggerProvider = new ConsoleLoggerProvider();

    @Captor
    ArgumentCaptor<Map<String, Object>> propertiesCaptor;
    @Captor
    ArgumentCaptor<String> eventNameCaptor;
    @Captor
    ArgumentCaptor<String> userIdCaptor;

    @Before
    public void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        application = Mockito.mock(Application.class);
        amplitudeIntegrationFactory = new AmplitudeIntegrationFactory();
//        amplitude = Mockito.mock(Amplitude.class);

    }
    @After
    public void release() throws Exception {
        closeable.close();
    }

    private AmplitudeDestinationConfig createTestConfigForTrackProductOnce() {
        AmplitudeDestinationConfig destinationConfig = new AmplitudeDestinationConfig();
        destinationConfig.apiKey = TEST_API_KEY;
        destinationConfig.trackProductsOnce = true;
        return destinationConfig;
    }

    @Test
    public void trackProductOnce() {
        AmplitudeDestinationConfig amplitudeDestinationConfig = createTestConfigForTrackProductOnce();
        setupAmplitude(amplitudeDestinationConfig);

        RudderMessage input = TestData.TEST_TRACK_CONDITION_1.getInputMessage();
        amplitudeIntegrationFactory.dump(input);

        Mockito.verify(amplitude).track(eventNameCaptor.capture(), propertiesCaptor.capture());
        TestData.TEST_TRACK_CONDITION_1.assertCondition(eventNameCaptor.getValue(), propertiesCaptor.getValue(), null);
    }
    @Test
    public void identify() {
        AmplitudeDestinationConfig amplitudeDestinationConfig = createTestConfigForTrackProductOnce();
        setupAmplitude(amplitudeDestinationConfig);

        RudderMessage input = TestData.TEST_IDENTIFY_CONDITION_1.getInputMessage();
        amplitudeIntegrationFactory.dump(input);

        ArgumentCaptor<Identify> identifyArgumentCaptor = ArgumentCaptor.forClass(Identify.class);
        Mockito.verify(amplitude).identify(identifyArgumentCaptor.capture());

        Mockito.verify(amplitude).setUserId(userIdCaptor.capture());
        Map<String, Object> identifyProperties = identifyArgumentCaptor.getValue().getProperties();
        //since the traits do not fall under traitsToIncrement, etc, they will be put inside a
        // map by amplitude
        Map<String, Object> setProperties = (Map<String, Object>) identifyProperties.get(identifyProperties.keySet().toArray()[0]);

        TestData.TEST_IDENTIFY_CONDITION_1.assertCondition(null, setProperties , userIdCaptor.getValue());
    }

    private void setupAmplitude(AmplitudeDestinationConfig amplitudeDestinationConfig) {
        amplitude = Mockito.mock(Amplitude.class);
        amplitudeIntegrationFactory.setup(amplitudeDestinationConfig, new RudderConfig.Builder().build(), amplitude);

    }


}
