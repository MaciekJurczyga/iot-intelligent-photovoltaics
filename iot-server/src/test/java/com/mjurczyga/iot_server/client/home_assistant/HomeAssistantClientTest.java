package com.mjurczyga.iot_server.client.home_assistant;

import com.mjurczyga.iot_server.client.response.HomeAssistantStateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeAssistantClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private HomeAssistantClient homeAssistantClient;

    @BeforeEach
    void setUp() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(anyString())).thenReturn(requestHeadersSpec);
    }

    private void mockGetEntityState(String state) {
        HomeAssistantStateResponse response = new HomeAssistantStateResponse();
        response.setState(state);
        when(responseSpec.bodyToMono(HomeAssistantStateResponse.class)).thenReturn(Mono.just(response));
    }

    private void mockCallService(String expectedResponse) {
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));
    }

    @Test
    void turnOnSmartPlug_shouldReturnSuccessMessage() {
        mockCallService("[{\"id\": 1, \"type\": \"result\", \"success\": true}]");
        String result = homeAssistantClient.turnOnSmartPlug();
        assertTrue(result.contains("Service switch.turn_on called successfully"));
    }

    @Test
    void turnOffSmartPlug_shouldReturnSuccessMessage() {
        mockCallService("[{\"id\": 2, \"type\": \"result\", \"success\": true}]");
        String result = homeAssistantClient.turnOffSmartPlug();
        assertTrue(result.contains("Service switch.turn_off called successfully"));
    }

    @Test
    void isAnyoneHomeState_shouldReturnState() {
        String expectedState = "home";
        mockGetEntityState(expectedState);
        String result = homeAssistantClient.isAnyoneHomeState();
        assertEquals(expectedState, result);
    }

    @Test
    void triggerNotificationScript_shouldReturnSuccessMessage() {
        mockCallService("[{\"id\": 3, \"type\": \"result\", \"success\": true}]");
        String result = homeAssistantClient.triggerNotificationScript();
        assertTrue(result.contains("Service script.turn_on called successfully"));
    }

    @Test
    void getIndoorTemperature_shouldReturnTemperature() {
        String expectedTemp = "21.5";
        mockGetEntityState(expectedTemp);
        String result = homeAssistantClient.getIndoorTemperature();
        assertEquals(expectedTemp, result);
    }

    @Test
    void getOutdoorTemperature_shouldReturnTemperature() {
        String expectedTemp = "15.0";
        mockGetEntityState(expectedTemp);
        String result = homeAssistantClient.getOutdoorTemperature();
        assertEquals(expectedTemp, result);
    }

    @Test
    void getTotalAcEnergyConsumption_shouldReturnConsumption() {
        String expectedConsumption = "1234.56";
        mockGetEntityState(expectedConsumption);
        String result = homeAssistantClient.getTotalAcEnergyConsumption();
        assertEquals(expectedConsumption, result);
    }

    @Test
    void getPhoneBatteryLevel_shouldReturnBatteryLevel() {
        String expectedLevel = "88";
        mockGetEntityState(expectedLevel);
        String result = homeAssistantClient.getPhoneBatteryLevel();
        assertEquals(expectedLevel, result);
    }

    @Test
    void getPhoneChargingState_shouldReturnChargingState() {
        String expectedState = "Charging";
        mockGetEntityState(expectedState);
        String result = homeAssistantClient.getPhoneChargingState();
        assertEquals(expectedState, result);
    }

    @Test
    void getSmartPlugCurrentPowerUsed_shouldReturnPower() {
        String expectedPower = "45.5";
        mockGetEntityState(expectedPower);
        String result = homeAssistantClient.getSmartPlugCurrentPowerUsed();
        assertEquals(expectedPower, result);
    }

    @Test
    void getPvProductionTotalDaily_shouldReturnProduction() {
        String expectedProduction = "10.2";
        mockGetEntityState(expectedProduction);
        String result = homeAssistantClient.getPvProductionTotalDaily();
        assertEquals(expectedProduction, result);
    }

    @Test
    void getTotalPvProduction_shouldReturnTotalProduction() {
        String expectedTotal = "5432.1";
        mockGetEntityState(expectedTotal);
        String result = homeAssistantClient.getTotalPvProduction();
        assertEquals(expectedTotal, result);
    }

    @Test
    void getTemporaryPvProduction_shouldReturnTemporaryProduction() {
        String expectedTempProduction = "2.1";
        mockGetEntityState(expectedTempProduction);
        String result = homeAssistantClient.getTemporaryPvProduction();
        assertEquals(expectedTempProduction, result);
    }

    @Test
    void getNextDawn_shouldReturnTime() {
        String expectedTime = "2025-11-05T06:30:00+00:00";
        mockGetEntityState(expectedTime);
        String result = homeAssistantClient.getNextDawn();
        assertEquals(expectedTime, result);
    }

    @Test
    void getNextDusk_shouldReturnTime() {
        String expectedTime = "2025-11-05T18:00:00+00:00";
        mockGetEntityState(expectedTime);
        String result = homeAssistantClient.getNextDusk();
        assertEquals(expectedTime, result);
    }

    @Test
    void getEntityState_shouldHandleWebClientResponseException() {
        when(responseSpec.bodyToMono(HomeAssistantStateResponse.class))
                .thenReturn(Mono.error(new WebClientResponseException(404, "Not Found", null, "Entity not found".getBytes(), null)));

        String result = homeAssistantClient.getIndoorTemperature();
        assertTrue(result.contains("Error fetching state for"));
        assertTrue(result.contains("Entity not found"));
    }

    @Test
    void getEntityState_shouldHandleGenericException() {
        when(responseSpec.bodyToMono(HomeAssistantStateResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        String result = homeAssistantClient.getIndoorTemperature();
        assertTrue(result.contains("Unexpected error fetching state for"));
        assertTrue(result.contains("Network error"));
    }

    @Test
    void getEntityState_shouldHandleEmptyResponse() {
        when(responseSpec.bodyToMono(HomeAssistantStateResponse.class)).thenReturn(Mono.empty());

        String result = homeAssistantClient.getIndoorTemperature();
        assertEquals("Error: Received an empty response from Home Assistant.", result);
    }

    @Test
    void callService_shouldHandleWebClientResponseException() {
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new WebClientResponseException(400, "Bad Request", null, "Invalid JSON".getBytes(), null)));

        String result = homeAssistantClient.turnOnSmartPlug();
        assertTrue(result.contains("Error:"));
        assertTrue(result.contains("Invalid JSON"));
    }

    @Test
    void callService_shouldHandleGenericException() {
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("Connection timed out")));

        String result = homeAssistantClient.turnOnSmartPlug();
        assertTrue(result.contains("Unexpected error:"));
        assertTrue(result.contains("Connection timed out"));
    }
}