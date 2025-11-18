package com.mjurczyga.iot_server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for IoT devices in the home
 */
@Data
@Component
@ConfigurationProperties(prefix = "iot.devices")
public class DeviceConfig {
    
    // PV System
    private double pvMaxProduction = 6000.0; // Watts
    
    // Climate Control (AC)
    private double acCoolingPower = 1000.0; // Watts
    private double acHeatingPower = 1000.0; // Watts
    private double targetTemperature = 22.0; // Celsius
    private double temperatureHysteresis = 0.5; // Celsius
    
    // EV Charger
    private double evMinPower = 1400.0; // Watts
    private double evMaxPower = 7400.0; // Watts
    private double evBatteryCapacity = 60000.0; // Wh (60 kWh)
    
    // Dishwasher
    private double dishwasherPower = 1800.0; // Watts
    
    // Smart Plug (general devices)
    private double smartPlugPower = 500.0; // Watts
    
    // Stability threshold for turning on devices
    private int stabilityDurationSeconds = 300; // 5 minutes
    
    // Minimum surplus required before activating device (buffer)
    private double surplusBuffer = 200.0; // Watts
}