package com.mjurczyga.iot_server.controllers;

import com.mjurczyga.iot_server.client.home_assistant.HomeAssistantClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/ha")
public class HomeAssistantController {

    private final HomeAssistantClient homeAssistantClient;

    // --- Actions ---

    @PostMapping("/smart-plug/on")
    public ResponseEntity<String> turnOnSmartPlug() {
        return ResponseEntity.ok(homeAssistantClient.turnOnSmartPlug());
    }

    @PostMapping("/smart-plug/off")
    public ResponseEntity<String> turnOffSmartPlug() {
        return ResponseEntity.ok(homeAssistantClient.turnOffSmartPlug());
    }

    @PostMapping("/notifications/trigger")
    public ResponseEntity<String> triggerNotification() {
        return ResponseEntity.ok(homeAssistantClient.triggerNotificationScript());
    }

    // --- Presence ---

    @GetMapping("/presence/home")
    public ResponseEntity<String> isAnyoneHome() {
        return ResponseEntity.ok("Is Jan home: " + homeAssistantClient.isAnyoneHomeState());
    }

    // --- Sensors ---

    @GetMapping("/sensors/temperature/indoor")
    public ResponseEntity<String> indoorTemperature() {
        return ResponseEntity.ok("Indoor temperature: " + homeAssistantClient.getIndoorTemperature());
    }

    @GetMapping("/sensors/temperature/outdoor")
    public ResponseEntity<String> outdoorTemperature() {
        return ResponseEntity.ok("Outdoor temperature: " + homeAssistantClient.getOutdoorTemperature());
    }

    // --- Energy ---

    @GetMapping("/energy/ac/total-consumption")
    public ResponseEntity<String> getTotalAcEnergy() {
        return ResponseEntity.ok("AC total energy consumed: " + homeAssistantClient.getTotalAcEnergyConsumption() + " [kWh]");
    }

    @GetMapping("/energy/smart-plug/current-power")
    public ResponseEntity<String> getCurrentSmartPlugPower() {
        return ResponseEntity.ok("Current power used by smart plug: " + homeAssistantClient.getSmartPlugCurrentPowerUsed() + " [W]");
    }

    @GetMapping("/energy/pv/daily-production")
    public ResponseEntity<String> getPvProductionTotalDaily() {
        return ResponseEntity.ok("PV production total daily: " + homeAssistantClient.getPvProductionTotalDaily() + " [kWh]");
    }

    @GetMapping("/energy/pv/total-production")
    public ResponseEntity<String> getTotalPvProduction() {
        return ResponseEntity.ok("Total PV production: " + homeAssistantClient.getTotalPvProduction() + " [kWh]");
    }

    @GetMapping("/energy/pv/current-production")
    public ResponseEntity<String> getCurrentPvProduction() {
        return ResponseEntity.ok("Current PV production: " + homeAssistantClient.getTemporaryPvProduction() + " [W]");
    }

    // --- Devices ---

    @GetMapping("/devices/phone/battery/level")
    public ResponseEntity<String> getPhoneBatteryLevel() {
        return ResponseEntity.ok("Phone battery level: " + homeAssistantClient.getPhoneBatteryLevel() + "%");
    }

    @GetMapping("/devices/phone/battery/state")
    public ResponseEntity<String> getPhoneChargingState() {
        return ResponseEntity.ok("Phone charging state: " + homeAssistantClient.getPhoneChargingState());
    }

    // --- Sun ---

    @GetMapping("/sun/dawn")
    public ResponseEntity<String> getNextDawn() {
        return ResponseEntity.ok("Next dawn: " + homeAssistantClient.getNextDawn());
    }

    @GetMapping("/sun/dusk")
    public ResponseEntity<String> getNextDusk() {
        return ResponseEntity.ok("Next dusk: " + homeAssistantClient.getNextDusk());
    }
}