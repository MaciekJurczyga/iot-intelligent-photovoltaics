package com.mjurczyga.iot_server.service;

import com.mjurczyga.iot_server.logic.PriorityCalculator;
import com.mjurczyga.iot_server.model.DeviceDecision;
import com.mjurczyga.iot_server.model.SystemState;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Main orchestration service for IoT energy management
 * Periodically checks system state and makes control decisions
 */
@Service
@RequiredArgsConstructor
public class EnergyManagementService {
    
    private static final Logger log = LoggerFactory.getLogger(EnergyManagementService.class);
    
    private final SystemStateService systemStateService;
    private final PriorityCalculator priorityCalculator;
    private final DeviceExecutorService executorService;
    
    /**
     * Main control loop - runs every 30 seconds
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 5000)
    public void controlLoop() {
        try {
            log.debug("Starting energy management control loop");
            
            // 1. Gather current system state
            SystemState state = systemStateService.getCurrentState();
            logSystemState(state);
            
            // 2. Calculate priorities and decisions
            DeviceDecision decision = priorityCalculator.calculatePriorities(state);
            
            // 3. Execute decisions
            executorService.executeDecision(decision);
            
            log.debug("Control loop completed successfully");
            
        } catch (Exception e) {
            log.error("Error in energy management control loop", e);
        }
    }
    
    /**
     * Manual trigger for control loop (useful for testing)
     */
    public DeviceDecision manualControl() {
        log.info("Manual control triggered");
        
        SystemState state = systemStateService.getCurrentState();
        DeviceDecision decision = priorityCalculator.calculatePriorities(state);
        executorService.executeDecision(decision);
        
        return decision;
    }
    
    /**
     * Get current system state without executing actions
     */
    public SystemState getSystemState() {
        return systemStateService.getCurrentState();
    }
    
    /**
     * Calculate priorities without executing actions (dry run)
     */
    public DeviceDecision calculateDecision() {
        SystemState state = systemStateService.getCurrentState();
        return priorityCalculator.calculatePriorities(state);
    }
    
    /**
     * Log system state for debugging
     */
    private void logSystemState(SystemState state) {
        log.info("=== System State ===");
        log.info("Mode: {}", state.isAnyoneHome() ? "COMFORT (someone home)" : "MAX_USAGE (empty)");
        log.info("PV Production: {}W", state.getCurrentPvProduction());
        log.info("House Consumption: {}W", state.getCurrentHouseConsumption());
        log.info("Available Surplus: {}W", state.getAvailableSurplus());
        log.info("Indoor Temp: {}°C, Outdoor: {}°C", state.getIndoorTemperature(), state.getOutdoorTemperature());
        log.info("AC: {}, EV: {}W, SmartPlug: {}W", 
            state.isAcOn() ? "ON" : "OFF",
            state.getEvChargingPower(),
            state.getSmartPlugPower());
        log.info("===================");
    }
}