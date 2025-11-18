package com.mjurczyga.iot_server.service;

import com.mjurczyga.iot_server.client.home_assistant.HomeAssistantClient;
import com.mjurczyga.iot_server.model.SystemState;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service to gather current system state from Home Assistant
 */
@Service
@RequiredArgsConstructor
public class SystemStateService {
    
    private static final Logger log = LoggerFactory.getLogger(SystemStateService.class);
    
    private final HomeAssistantClient homeAssistantClient;
    
    /**
     * Gather current system state from Home Assistant
     */
    public SystemState getCurrentState() {
        try {
            return SystemState.builder()
                // Presence
                .anyoneHome(isAnyoneHome())
                
                // Temperatures
                .indoorTemperature(parseDouble(homeAssistantClient.getIndoorTemperature(), 22.0))
                .outdoorTemperature(parseDouble(homeAssistantClient.getOutdoorTemperature(), 20.0))
                
                // Energy
                .currentPvProduction(parseDouble(homeAssistantClient.getTemporaryPvProduction(), 0.0))
                .currentHouseConsumption(estimateBaseConsumption())
                
                // Climate
                .acOn(false) // TODO: Add method to check AC state
                .acPowerUsage(0.0) // TODO: Get actual AC power usage
                
                // EV Charger
                .evConnected(true) // TODO: Add method to check EV connection
                .evChargePercentage(50.0) // TODO: Get actual charge level
                .evChargingPower(0.0) // TODO: Get current charging power
                
                // Dishwasher
                .dishwasherReady(true) // Simplified for MVP
                .dishwasherOn(false) // TODO: Add method to check dishwasher state
                
                // Smart Plug
                .smartPlugOn(false) // TODO: Check actual state
                .smartPlugPower(parseDouble(homeAssistantClient.getSmartPlugCurrentPowerUsed(), 0.0))
                
                .build();
                
        } catch (Exception e) {
            log.error("Error gathering system state", e);
            return getDefaultState();
        }
    }
    
    /**
     * Check if anyone is home based on device tracker
     */
    private boolean isAnyoneHome() {
        try {
            String state = homeAssistantClient.isAnyoneHomeState();
            return "home".equalsIgnoreCase(state);
        } catch (Exception e) {
            log.warn("Error checking presence, assuming no one home", e);
            return false;
        }
    }
    
    /**
     * Estimate base house consumption (lighting, appliances, etc.)
     * This is a simplified approach - in production you'd track this more precisely
     */
    private double estimateBaseConsumption() {
        // TODO: Implement actual measurement
        // For now, return a typical base load
        return 300.0; // Watts
    }
    
    /**
     * Parse double from string, return default on error
     */
    private double parseDouble(String value, double defaultValue) {
        try {
            // Remove any non-numeric characters except . and -
            String cleaned = value.replaceAll("[^0-9.-]", "");
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            log.debug("Could not parse double from '{}', using default {}", value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get a default safe state when sensors are unavailable
     */
    private SystemState getDefaultState() {
        return SystemState.builder()
            .anyoneHome(false)
            .indoorTemperature(22.0)
            .outdoorTemperature(20.0)
            .currentPvProduction(0.0)
            .currentHouseConsumption(300.0)
            .acOn(false)
            .acPowerUsage(0.0)
            .evConnected(false)
            .evChargePercentage(0.0)
            .evChargingPower(0.0)
            .dishwasherReady(false)
            .dishwasherOn(false)
            .smartPlugOn(false)
            .smartPlugPower(0.0)
            .build();
    }
}