package com.mjurczyga.iot_server.logic;

import com.mjurczyga.iot_server.config.DeviceConfig;
import com.mjurczyga.iot_server.model.DeviceDecision;
import com.mjurczyga.iot_server.model.DeviceDecision.ActionType;
import com.mjurczyga.iot_server.model.DeviceDecision.DeviceAction;
import com.mjurczyga.iot_server.model.DeviceDecision.DeviceType;
import com.mjurczyga.iot_server.model.SystemState;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Priority-based calculator for IoT device control
 * 
 * Implements three modes:
 * 1. MAX_USAGE (no one home) - maximize PV usage
 * 2. COMFORT (someone home) - prioritize comfort, use surplus for secondary devices
 * 3. CUSTOM - user-defined priority order
 */
@Service
@RequiredArgsConstructor
public class PriorityCalculator {
    
    private static final Logger log = LoggerFactory.getLogger(PriorityCalculator.class);
    
    private final DeviceConfig config;
    
    /**
     * Main calculation method - determines what actions to take based on current system state
     */
    public DeviceDecision calculatePriorities(SystemState state) {
        DeviceDecision decision = DeviceDecision.create();
        
        // Check if custom priority mode is enabled
        if (config.isCustomPriorityEnabled()) {
            log.info("Mode: CUSTOM (user-defined priorities)");
            decision.mode("CUSTOM");
            return calculateCustomMode(state, decision);
        }
        
        if (state.isAnyoneHome()) {
            log.info("Mode: COMFORT (someone is home)");
            decision.mode("COMFORT");
            return calculateComfortMode(state, decision);
        } else {
            log.info("Mode: MAX_USAGE (no one home)");
            decision.mode("MAX_USAGE");
            return calculateMaxUsageMode(state, decision);
        }
    }
    
    /**
     * NEW: Custom priority mode - user defines the order
     */
    private DeviceDecision calculateCustomMode(SystemState state, DeviceDecision decision) {
        double surplus = state.getAvailableSurplus();
        decision.availableSurplus(surplus);
        
        log.debug("Available surplus: {}W", surplus);
        log.debug("Custom priority order: {}", config.getCustomPriorityOrder());
        
        // Process devices in custom order
        for (String deviceName : config.getCustomPriorityOrder()) {
            DeviceType deviceType = parseDeviceType(deviceName);
            
            if (deviceType != null) {
                surplus = handleDeviceByType(deviceType, state, surplus, decision);
            }
        }
        
        decision.explanation(String.format(
            "Custom priority mode: Order %s. Remaining surplus: %.0fW", 
            config.getCustomPriorityOrder(),
            surplus
        ));
        
        return decision.build();
    }
    
    /**
     * Route to appropriate device handler based on type
     */
    private double handleDeviceByType(DeviceType type, SystemState state, double surplus, DeviceDecision decision) {
        switch (type) {
            case EV_CHARGER:
                return handleEvChargerMaxUsage(state, surplus, decision);
            case AC_CLIMATE:
                return handleClimateMaxUsage(state, surplus, decision);
            case DISHWASHER:
                return handleDishwasher(state, surplus, decision);
            case SMART_PLUG:
                return handleSmartPlug(state, surplus, decision);
            default:
                return surplus;
        }
    }
    
    /**
     * Parse device type from string
     */
    private DeviceType parseDeviceType(String deviceName) {
        try {
            return DeviceType.valueOf(deviceName);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown device type in custom priority: {}", deviceName);
            return null;
        }
    }
    
    /**
     * Scenario 1: No one home - maximize PV usage
     * Priority order:
     * 1. EV Charger (dynamic power regulation)
     * 2. AC/Heating (if temperature requires it)
     * 3. Dishwasher
     * 4. Smart Plug devices
     */
    private DeviceDecision calculateMaxUsageMode(SystemState state, DeviceDecision decision) {
        double surplus = state.getAvailableSurplus();
        decision.availableSurplus(surplus);
        
        log.debug("Available surplus: {}W", surplus);
        
        // Priority 1: EV Charger as dynamic regulator
        surplus = handleEvChargerMaxUsage(state, surplus, decision);
        
        // Priority 2: AC if temperature requires it
        surplus = handleClimateMaxUsage(state, surplus, decision);
        
        // Priority 3: Dishwasher
        surplus = handleDishwasher(state, surplus, decision);
        
        // Priority 4: Smart Plug
        surplus = handleSmartPlug(state, surplus, decision);
        
        decision.explanation(String.format(
            "Max usage mode: Utilizing %.0fW from PV. Remaining surplus: %.0fW", 
            state.getCurrentPvProduction(), 
            surplus
        ));
        
        return decision.build();
    }
    
    /**
     * Scenario 2: Someone home - prioritize comfort
     * Priority order:
     * 1. AC/Heating (comfort always first)
     * 2. EV Charger (use remaining surplus)
     * 3. Dishwasher (if enough surplus)
     * 4. Smart Plug devices (if enough surplus)
     */
    private DeviceDecision calculateComfortMode(SystemState state, DeviceDecision decision) {
        double surplus = state.getAvailableSurplus();
        decision.availableSurplus(surplus);
        
        log.debug("Available surplus: {}W", surplus);
        
        // Priority 1: AC for comfort (independent of surplus)
        handleClimateComfort(state, decision);
        
        // Recalculate surplus after AC decision
        surplus = state.getAvailableSurplus();
        
        // Priority 2: EV Charger with remaining surplus
        surplus = handleEvChargerComfort(state, surplus, decision);
        
        // Priority 3: Dishwasher
        surplus = handleDishwasher(state, surplus, decision);
        
        // Priority 4: Smart Plug
        surplus = handleSmartPlug(state, surplus, decision);
        
        decision.explanation(String.format(
            "Comfort mode: Priority on comfort. Available surplus: %.0fW", 
            surplus
        ));
        
        return decision.build();
    }
    
    /**
     * Handle EV charger in max usage mode - charge with any available surplus
     */
    private double handleEvChargerMaxUsage(SystemState state, double surplus, DeviceDecision decision) {
        if (!state.isEvConnected()) {
            if (state.getEvChargingPower() > 0) {
                decision.addAction(DeviceAction.builder()
                    .device(DeviceType.EV_CHARGER)
                    .action(ActionType.TURN_OFF)
                    .reason("EV not connected")
                    .build());
            }
            return surplus;
        }
        
        if (state.getEvChargePercentage() >= 95.0) {
            if (state.getEvChargingPower() > 0) {
                decision.addAction(DeviceAction.builder()
                    .device(DeviceType.EV_CHARGER)
                    .action(ActionType.TURN_OFF)
                    .reason("EV fully charged (95%+)")
                    .build());
            }
            return surplus;
        }
        
        // Calculate optimal charging power
        double targetPower = calculateEvChargingPower(surplus, state);
        
        if (targetPower >= config.getEvMinPower()) {
            decision.addAction(DeviceAction.builder()
                .device(DeviceType.EV_CHARGER)
                .action(ActionType.SET_POWER)
                .targetPower(targetPower)
                .reason(String.format("Charging with %.0fW (%.0f%% charged)", targetPower, state.getEvChargePercentage()))
                .build());
            return surplus - targetPower;
        } else if (state.getEvChargingPower() > 0) {
            decision.addAction(DeviceAction.builder()
                .device(DeviceType.EV_CHARGER)
                .action(ActionType.TURN_OFF)
                .reason("Insufficient surplus for minimum charging power")
                .build());
        }
        
        return surplus;
    }
    
    /**
     * Handle EV charger in comfort mode - use surplus after AC
     */
    private double handleEvChargerComfort(SystemState state, double surplus, DeviceDecision decision) {
        return handleEvChargerMaxUsage(state, surplus, decision); // Same logic but different context
    }
    
    /**
     * Calculate optimal EV charging power based on available surplus
     */
    private double calculateEvChargingPower(double surplus, SystemState state) {
        // Add current EV consumption back to surplus to get total available
        double totalAvailable = surplus + state.getEvChargingPower();
        
        // Apply buffer
        totalAvailable -= config.getSurplusBuffer();
        
        // Clamp to EV charger limits
        if (totalAvailable < config.getEvMinPower()) {
            return 0.0;
        }
        
        return Math.min(totalAvailable, config.getEvMaxPower());
    }
    
    /**
     * Handle climate control in max usage mode - only if temperature requires
     */
    private double handleClimateMaxUsage(SystemState state, double surplus, DeviceDecision decision) {
        boolean needsCooling = state.getIndoorTemperature() > config.getTargetTemperature() + config.getTemperatureHysteresis();
        boolean needsHeating = state.getIndoorTemperature() < config.getTargetTemperature() - config.getTemperatureHysteresis();
        
        if (!needsCooling && !needsHeating) {
            if (state.isAcOn()) {
                decision.addAction(DeviceAction.builder()
                    .device(DeviceType.AC_CLIMATE)
                    .action(ActionType.TURN_OFF)
                    .reason("Temperature in acceptable range")
                    .build());
                return surplus + state.getAcPowerUsage();
            }
            return surplus;
        }
        
        double requiredPower = needsCooling ? config.getAcCoolingPower() : config.getAcHeatingPower();
        
        // In max usage mode, only turn on if we have surplus
        if (surplus + state.getAcPowerUsage() >= requiredPower + config.getSurplusBuffer()) {
            if (!state.isAcOn()) {
                decision.addAction(DeviceAction.builder()
                    .device(DeviceType.AC_CLIMATE)
                    .action(ActionType.TURN_ON)
                    .reason(String.format("Temperature %.1f°C, target %.1f°C (%s)", 
                        state.getIndoorTemperature(), 
                        config.getTargetTemperature(),
                        needsCooling ? "cooling" : "heating"))
                    .build());
                return surplus - requiredPower;
            }
            return surplus;
        } else if (state.isAcOn()) {
            decision.addAction(DeviceAction.builder()
                .device(DeviceType.AC_CLIMATE)
                .action(ActionType.TURN_OFF)
                .reason("Insufficient surplus for climate control")
                .build());
            return surplus + state.getAcPowerUsage();
        }
        
        return surplus;
    }
    
    /**
     * Handle climate control in comfort mode - always operate based on temperature needs
     */
    private void handleClimateComfort(SystemState state, DeviceDecision decision) {
        boolean needsCooling = state.getIndoorTemperature() > config.getTargetTemperature() + config.getTemperatureHysteresis();
        boolean needsHeating = state.getIndoorTemperature() < config.getTargetTemperature() - config.getTemperatureHysteresis();
        
        if (needsCooling || needsHeating) {
            if (!state.isAcOn()) {
                decision.addAction(DeviceAction.builder()
                    .device(DeviceType.AC_CLIMATE)
                    .action(ActionType.TURN_ON)
                    .reason(String.format("Comfort priority: Temperature %.1f°C, target %.1f°C (%s)", 
                        state.getIndoorTemperature(), 
                        config.getTargetTemperature(),
                        needsCooling ? "cooling" : "heating"))
                    .build());
            }
        } else {
            if (state.isAcOn()) {
                decision.addAction(DeviceAction.builder()
                    .device(DeviceType.AC_CLIMATE)
                    .action(ActionType.TURN_OFF)
                    .reason("Temperature in acceptable range")
                    .build());
            }
        }
    }
    
    /**
     * Handle dishwasher - only turn on with stable surplus
     */
    private double handleDishwasher(SystemState state, double surplus, DeviceDecision decision) {
        if (!state.isDishwasherReady()) {
            return surplus;
        }
        
        if (state.isDishwasherOn()) {
            // Already running
            return surplus;
        }
        
        // Turn on only if we have enough surplus
        if (surplus >= config.getDishwasherPower() + config.getSurplusBuffer()) {
            decision.addAction(DeviceAction.builder()
                .device(DeviceType.DISHWASHER)
                .action(ActionType.TURN_ON)
                .reason(String.format("Sufficient surplus (%.0fW) for dishwasher", surplus))
                .build());
            return surplus - config.getDishwasherPower();
        }
        
        return surplus;
    }
    
    /**
     * Handle smart plug devices - turn on with stable surplus
     */
    private double handleSmartPlug(SystemState state, double surplus, DeviceDecision decision) {
        if (state.isSmartPlugOn()) {
            // Check if we should turn it off
            if (surplus + state.getSmartPlugPower() < config.getSurplusBuffer()) {
                decision.addAction(DeviceAction.builder()
                    .device(DeviceType.SMART_PLUG)
                    .action(ActionType.TURN_OFF)
                    .reason("Insufficient surplus to maintain smart plug")
                    .build());
                return surplus + state.getSmartPlugPower();
            }
            return surplus;
        } else {
            // Turn on if we have enough surplus
            if (surplus >= config.getSmartPlugPower() + config.getSurplusBuffer()) {
                decision.addAction(DeviceAction.builder()
                    .device(DeviceType.SMART_PLUG)
                    .action(ActionType.TURN_ON)
                    .reason(String.format("Sufficient surplus (%.0fW) for smart plug", surplus))
                    .build());
                return surplus - config.getSmartPlugPower();
            }
        }
        
        return surplus;
    }
}