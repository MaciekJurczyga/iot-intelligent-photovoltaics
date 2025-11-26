package com.mjurczyga.iot_server.controllers;

import com.mjurczyga.iot_server.config.DeviceConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for managing custom priority configuration
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/priority")
public class PriorityConfigController {
    
    private final DeviceConfig deviceConfig;
    
    /**
     * Get current priority configuration
     */
    @GetMapping("/config")
    public ResponseEntity<PriorityConfig> getPriorityConfig() {
        PriorityConfig config = new PriorityConfig(
            deviceConfig.isCustomPriorityEnabled(),
            deviceConfig.getCustomPriorityOrder()
        );
        return ResponseEntity.ok(config);
    }
    
    /**
     * Set custom priorities - send device priorities as JSON
     * 
     * Example request body:
     * {
     *   "enabled": true,
     *   "priorities": {
     *     "EV_CHARGER": 3,
     *     "AC_CLIMATE": 2,
     *     "DISHWASHER": 4,
     *     "SMART_PLUG": 1
     *   }
     * }
     * 
     * Priority 1 = highest, 4 = lowest
     */
    @PostMapping("/custom")
    public ResponseEntity<?> setCustomPriorities(@RequestBody CustomPriorityRequest request) {
        try {
            // Validate priorities
            if (!validatePriorities(request.getPriorities())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid priorities. Must include all 4 devices with priorities 1-4"));
            }
            
            // Convert priority map to ordered list
            List<String> orderedDevices = convertPrioritiesToList(request.getPriorities());
            
            // Update configuration
            deviceConfig.setCustomPriorityEnabled(request.isEnabled());
            deviceConfig.setCustomPriorityOrder(orderedDevices);
            
            PriorityConfig config = new PriorityConfig(
                deviceConfig.isCustomPriorityEnabled(),
                deviceConfig.getCustomPriorityOrder()
            );
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Quick enable/disable custom mode without changing priorities
     */
    @PostMapping("/custom/toggle")
    public ResponseEntity<Map<String, Object>> toggleCustomMode(@RequestParam boolean enabled) {
        deviceConfig.setCustomPriorityEnabled(enabled);
        
        return ResponseEntity.ok(Map.of(
            "customEnabled", enabled,
            "mode", enabled ? "CUSTOM" : "AUTO (MAX_USAGE/COMFORT)",
            "currentPriorities", deviceConfig.getCustomPriorityOrder()
        ));
    }
    
    /**
     * Get available devices that can be prioritized
     */
    @GetMapping("/devices")
    public ResponseEntity<List<DeviceInfo>> getAvailableDevices() {
        List<DeviceInfo> devices = List.of(
            new DeviceInfo("EV_CHARGER", "Electric Vehicle Charger", "1.4-7.4 kW dynamic charging"),
            new DeviceInfo("AC_CLIMATE", "Air Conditioning / Heating", "Temperature-based climate control"),
            new DeviceInfo("DISHWASHER", "Dishwasher", "1.8 kW appliance"),
            new DeviceInfo("SMART_PLUG", "Smart Plug Devices", "0.5 kW plugged devices")
        );
        return ResponseEntity.ok(devices);
    }
    
    /**
     * Validate that priorities include all devices with values 1-4
     */
    private boolean validatePriorities(Map<String, Integer> priorities) {
        // Must have exactly 4 devices
        if (priorities.size() != 4) {
            return false;
        }
        
        // Must include all required devices
        List<String> requiredDevices = List.of("EV_CHARGER", "AC_CLIMATE", "DISHWASHER", "SMART_PLUG");
        for (String device : requiredDevices) {
            if (!priorities.containsKey(device)) {
                return false;
            }
        }
        
        // Must have priorities 1, 2, 3, 4 (no duplicates)
        List<Integer> values = priorities.values().stream().sorted().toList();
        return values.equals(List.of(1, 2, 3, 4));
    }
    
    /**
     * Convert priority map to ordered list
     * Example: {SMART_PLUG: 1, AC: 2, EV: 3, DISH: 4} -> [SMART_PLUG, AC, EV, DISH]
     */
    private List<String> convertPrioritiesToList(Map<String, Integer> priorities) {
        return priorities.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())  // Sort by priority value
            .map(Map.Entry::getKey)                // Get device names
            .toList();
    }
    
    // ===== DTOs =====
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomPriorityRequest {
        private boolean enabled;
        private Map<String, Integer> priorities;
    }
    
    @Data
    @AllArgsConstructor
    public static class PriorityConfig {
        private boolean customEnabled;
        private List<String> priorityOrder;
    }
    
    @Data
    @AllArgsConstructor
    public static class DeviceInfo {
        private String name;
        private String displayName;
        private String description;
    }
}