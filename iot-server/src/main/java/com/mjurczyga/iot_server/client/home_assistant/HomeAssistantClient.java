package com.mjurczyga.iot_server.client.home_assistant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class HomeAssistantClient {

    private static final String ENTITY_ID = "switch.smart_plug_socket_1";

    private final WebClient webClient;


    public String turnOnSmartPlug() {
        return callSwitchService("turn_on");
    }

    public String turnOffSmartPlug() {
        return callSwitchService("turn_off");
    }

    private String callSwitchService(String action) {
        try {
            String response = webClient.post()
                    .uri("/api/services/switch/" + action)
                    .bodyValue("{\"entity_id\": \"" + ENTITY_ID + "\"}")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return "Smart plug " + action.replace("", " ") + ": " + response;

        } catch (WebClientResponseException e) {
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            return "Unexpected error: " + e.getMessage();
        }
    }
}