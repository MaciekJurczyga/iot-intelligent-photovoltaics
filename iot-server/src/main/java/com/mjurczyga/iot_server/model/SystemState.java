package com.mjurczyga.iot_server.model;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the current state of the entire IoT system
 */
@Data
@Builder
public class SystemState {
    // Presence
    private boolean anyoneHome;
    
    // Temperatures
    private double indoorTemperature;
    private double outdoorTemperature;
    
    // Energy
    private double currentPvProduction; // Watts
    private double currentHouseConsumption; // Watts
    
    // Climate
    private boolean acOn;
    private double acPowerUsage; // Watts
    
    // EV Charger
    private boolean evConnected;
    private double evChargePercentage;
    private double evChargingPower; // Watts
    
    // Dishwasher
    private boolean dishwasherReady; // Placeholder - assume always ready for MVP
    private boolean dishwasherOn;
    
    // Smart Plug
    private boolean smartPlugOn;
    private double smartPlugPower; // Watts
    
    /**
     * Calculate total current consumption
     */
    public double getTotalConsumption() {
        return currentHouseConsumption + acPowerUsage + evChargingPower + smartPlugPower;
    }
    
    /**
     * Calculate available surplus power
     */
    public double getAvailableSurplus() {
        return currentPvProduction - getTotalConsumption();
    }
}