package com.mjurczyga.iot_server.controllers;

import com.mjurczyga.iot_server.model.DeviceDecision;
import com.mjurczyga.iot_server.model.SystemState;
import com.mjurczyga.iot_server.service.EnergyManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for energy management system
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/energy")
public class EnergyManagementController {
    
    private final EnergyManagementService energyManagementService;
    
    /**
     * Get current system state
     */
    @GetMapping("/state")
    public ResponseEntity<SystemState> getSystemState() {
        SystemState state = energyManagementService.getSystemState();
        return ResponseEntity.ok(state);
    }
    
    /**
     * Calculate decision without executing (dry run)
     */
    @GetMapping("/decision")
    public ResponseEntity<DeviceDecision> getDecision() {
        DeviceDecision decision = energyManagementService.calculateDecision();
        return ResponseEntity.ok(decision);
    }
    
    /**
     * Manually trigger control loop
     */
    @PostMapping("/control")
    public ResponseEntity<DeviceDecision> manualControl() {
        DeviceDecision decision = energyManagementService.manualControl();
        return ResponseEntity.ok(decision);
    }
}