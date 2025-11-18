package com.mjurczyga.iot_server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the decision result from priority calculator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDecision {
    
    private List<DeviceAction> actions = new ArrayList<>();
    private double availableSurplus;
    private String mode; // "MAX_USAGE" or "COMFORT"
    private String explanation;
    
    /**
     * Add an action to the decision
     */
    public void addAction(DeviceAction action) {
        if (this.actions == null) {
            this.actions = new ArrayList<>();
        }
        this.actions.add(action);
    }
    
    /**
     * Static factory method to start building a decision
     */
    public static DeviceDecision create() {
        return new DeviceDecision();
    }
    
    /**
     * Builder-style method to set mode
     */
    public DeviceDecision mode(String mode) {
        this.mode = mode;
        return this;
    }
    
    /**
     * Builder-style method to set available surplus
     */
    public DeviceDecision availableSurplus(double surplus) {
        this.availableSurplus = surplus;
        return this;
    }
    
    /**
     * Builder-style method to set explanation
     */
    public DeviceDecision explanation(String explanation) {
        this.explanation = explanation;
        return this;
    }
    
    /**
     * Terminal method for builder pattern
     */
    public DeviceDecision build() {
        return this;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceAction {
        private DeviceType device;
        private ActionType action;
        private Double targetPower; // For EV charger, null for ON/OFF devices
        private String reason;
        
        /**
         * Static factory method to create a builder
         */
        public static DeviceActionBuilder builder() {
            return new DeviceActionBuilder();
        }
        
        /**
         * Builder for DeviceAction
         */
        public static class DeviceActionBuilder {
            private DeviceType device;
            private ActionType action;
            private Double targetPower;
            private String reason;
            
            public DeviceActionBuilder device(DeviceType device) {
                this.device = device;
                return this;
            }
            
            public DeviceActionBuilder action(ActionType action) {
                this.action = action;
                return this;
            }
            
            public DeviceActionBuilder targetPower(Double targetPower) {
                this.targetPower = targetPower;
                return this;
            }
            
            public DeviceActionBuilder reason(String reason) {
                this.reason = reason;
                return this;
            }
            
            public DeviceAction build() {
                return new DeviceAction(device, action, targetPower, reason);
            }
        }
    }
    
    public enum DeviceType {
        EV_CHARGER,
        AC_CLIMATE,
        DISHWASHER,
        SMART_PLUG
    }
    
    public enum ActionType {
        TURN_ON,
        TURN_OFF,
        SET_POWER, // For EV charger
        NO_CHANGE
    }
}