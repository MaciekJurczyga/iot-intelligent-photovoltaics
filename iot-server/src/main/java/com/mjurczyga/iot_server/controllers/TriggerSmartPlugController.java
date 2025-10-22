package com.mjurczyga.iot_server.controllers;


import com.mjurczyga.iot_server.client.home_assistant.HomeAssistantClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class TriggerSmartPlugController {

    private final HomeAssistantClient homeAssistantClient;

    @PostMapping("/on")
    public ResponseEntity<String> turnOnSmartPlug(){
        return ResponseEntity.ok(homeAssistantClient.turnOnSmartPlug());
    }

    @PostMapping("/off")
    public ResponseEntity<String> turnOffSmartPlug(){
        return ResponseEntity.ok(homeAssistantClient.turnOffSmartPlug());
    }


}