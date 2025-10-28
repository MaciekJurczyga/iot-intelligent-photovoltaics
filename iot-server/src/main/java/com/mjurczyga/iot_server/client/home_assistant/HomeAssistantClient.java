package com.mjurczyga.iot_server.client.home_assistant;

import com.mjurczyga.iot_server.client.response.HomeAssistantStateResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class HomeAssistantClient {

    private static final Logger log = LoggerFactory.getLogger(HomeAssistantClient.class);

    private static final String SWITCH_ENTITY_ID = "switch.smart_plug_socket_1";
    private static final String IPHONE_JAN_TRACKER_ID = "device_tracker.iphone_jan";
    private static final String NOTIFICATION_SCRIPT_ID = "script.notification_script";
    private static final String OUTDOOR_TEMPERATURE_SENSOR_ID = "sensor.153931628518786_outdoor_temperature";
    private static final String INDOOR_TEMPERATURE_SENSOR_ID = "sensor.153931628518786_indoor_temperature";
    private static final String AC_TOTAL_ENERGY_CONSUMPTION_ID = "sensor.153931628518786_total_energy_consumption";
    private static final String PHONE_BATTER_LEVEL = "sensor.iphone_jan_battery_level";
    private static final String PHONE_CHARGING_STATE = "sensor.iphone_jan_battery_state";
    private static final String SMART_PLUG_CURRENT_POWER_USED = "sensor.smart_plug_moc";
    private static final String PV_PRODUCTION_TOTAL_DAILY = "sensor.solarman_daily_production";
    private static final String TOTAL_PV_PRODUCTION = "sensor.solarman_total_production";
    private static final String TEMPORARY_PV_PRODUCTION = "sensor.solarman_total_ac_output_power_active";
    private static final String NEXT_DAWN = "sensor.sun_next_dawn";
    private static final String NEXT_DUSK = "sensor.sun_next_dusk";

    private final WebClient webClient;

    /**
     * Turns on the smart plug.
     * @return A confirmation message.
     */
    public String turnOnSmartPlug() {
        return callService("switch", "turn_on", SWITCH_ENTITY_ID);
    }

    /**
     * Turns off the smart plug.
     * @return A confirmation message.
     */
    public String turnOffSmartPlug() {
        return callService("switch", "turn_off", SWITCH_ENTITY_ID);
    }

    /**
     * Checks if anyone is home.
     * @return The state of the device tracker.
     */
    public String isAnyoneHomeState() {
        return getEntityState(IPHONE_JAN_TRACKER_ID);
    }

    /**
     * Triggers a notification script.
     * @return A confirmation message.
     */
    public String triggerNotificationScript() {
        return callService("script", "turn_on", NOTIFICATION_SCRIPT_ID);
    }

    /**
     * Gets the indoor temperature.
     * @return The indoor temperature.
     */
    public String getIndoorTemperature() {
        return getEntityState(INDOOR_TEMPERATURE_SENSOR_ID);
    }

    /**
     * Gets the outdoor temperature.
     * @return The outdoor temperature.
     */
    public String getOutdoorTemperature() {
        return getEntityState(OUTDOOR_TEMPERATURE_SENSOR_ID);
    }

    /**
     * Gets the total AC energy consumption.
     * @return The total AC energy consumption.
     */
    public String getTotalAcEnergyConsumption() {
        return getEntityState(AC_TOTAL_ENERGY_CONSUMPTION_ID);
    }

    /**
     * Gets the phone battery level.
     * @return The phone battery level.
     */
    public String getPhoneBatteryLevel() {
        return getEntityState(PHONE_BATTER_LEVEL);
    }

    /**
     * Gets the phone's charging state.
     * @return The phone's charging state.
     */
    public String getPhoneChargingState() {
        return getEntityState(PHONE_CHARGING_STATE);
    }

    /**
     * Gets the current power used by the smart plug.
     * @return The current power usage.
     */
    public String getSmartPlugCurrentPowerUsed() {
        return getEntityState(SMART_PLUG_CURRENT_POWER_USED);
    }

    /**
     * Gets the total daily PV production.
     * @return The total daily PV production.
     */
    public String getPvProductionTotalDaily() {
        return getEntityState(PV_PRODUCTION_TOTAL_DAILY);
    }

    /**
     * Gets the total PV production.
     * @return The total PV production.
     */
    public String getTotalPvProduction() {
        return getEntityState(TOTAL_PV_PRODUCTION);
    }

    /**
     * Gets the temporary PV production.
     * @return The temporary PV production.
     */
    public String getTemporaryPvProduction() {
        return getEntityState(TEMPORARY_PV_PRODUCTION);
    }

    /**
     * Gets the time of the next dawn.
     * @return The next dawn time.
     */
    public String getNextDawn() {
        return getEntityState(NEXT_DAWN);
    }

    /**
     * Gets the time of the next dusk.
     * @return The next dusk time.
     */
    public String getNextDusk() {
        return getEntityState(NEXT_DUSK);
    }

    private String getEntityState(String entityId) {
        try {
            log.info("Fetching state for entity: {}", entityId);
            HomeAssistantStateResponse response = webClient.get()
                    .uri("/api/states/" + entityId)
                    .retrieve()
                    .bodyToMono(HomeAssistantStateResponse.class)
                    .block();

            if (response != null) {
                log.info("Successfully fetched state for {}: {}", entityId, response.getState());
                return response.getState();
            } else {
                log.warn("Received an empty response from Home Assistant for entity: {}", entityId);
                return "Error: Received an empty response from Home Assistant.";
            }

        } catch (WebClientResponseException e) {
            log.error("Error fetching state for {}: {} - {}", entityId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "Error fetching state for " + entityId + ": " + e.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Unexpected error fetching state for {}: {}", entityId, e.getMessage(), e);
            return "Unexpected error fetching state for " + entityId + ": " + e.getMessage();
        }
    }

    private String callService(String domain, String service, String entityId) {
        try {
            log.info("Calling service {}.{} for entity: {}", domain, service, entityId);
            String uri = String.format("/api/services/%s/%s", domain, service);
            String requestBody = String.format("{\"entity_id\": \"%s\"}", entityId);

            String response = webClient.post()
                    .uri(uri)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Service {}.{} called successfully for {}. Response: {}", domain, service, entityId, response);
            return String.format("Service %s.%s called successfully for %s. Response: %s",
                    domain, service, entityId, response);

        } catch (WebClientResponseException e) {
            log.error("Error calling service {}.{} for {}: {} - {}", domain, service, entityId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "Error: " + e.getResponseBodyAsString();
        } catch (Exception e) {
            log.error("Unexpected error calling service {}.{} for {}: {}", domain, service, entityId, e.getMessage(), e);
            return "Unexpected error: " + e.getMessage();
        }
    }
}