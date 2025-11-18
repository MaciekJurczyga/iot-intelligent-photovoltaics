package com.iot.server.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Electric Vehicle Charger with power regulation

public class EVCharger extends Device {
    private static final Logger logger = LoggerFactory.getLogger(EVCharger.class);
    
    private double minPower;
    private double maxPower;
    private double batteryCapacity;
    private double currentCharge; // Wh - current battery charge level
    private double chargingPower; // W - current charging power
    
    public EVCharger(String name, double minPower, double maxPower, double batteryCapacity) {
        super(name);
        this.minPower = minPower;
        this.maxPower = maxPower;
        this.batteryCapacity = batteryCapacity;
        this.currentCharge = 0.0;
        this.chargingPower = 0.0;
    }
    
    /**
     * Checks if the vehicle is connected
     * @return true if vehicle is connected
     */
    public boolean isConnected() {
        return true; // Placeholder - would integrate with HA sensor
    }
    
    /**
     * Checks if battery is fully charged
     * @return true if battery is at least 95% charged
     */
    public boolean isFullyCharged() {
        return currentCharge >= batteryCapacity * 0.95;
    }
    
    /**
     * Sets the charging power
     * @param targetPower Desired charging power in watts
     * @return Actual charging power set
     */
    public double setChargingPower(double targetPower) {
        if (!isConnected() || isFullyCharged()) {
            this.chargingPower = 0.0;
            this.isOn = false;
            this.currentPower = 0.0;
            return 0.0;
        }
        
        // Limit to min/max range
        if (targetPower < minPower) {
            this.chargingPower = 0.0;
            this.isOn = false;
            this.currentPower = 0.0;
        } else {
            targetPower = Math.min(targetPower, maxPower);
            this.chargingPower = targetPower;
            this.currentPower = targetPower;
            this.isOn = true;
        }
        
        if (this.isOn) {
            logger.debug("{}: Set charging power to {}W", name, chargingPower);
        }
        
        return this.chargingPower;
    }
    
    /**
     * Simulates charging for a specified duration
     * @param durationSeconds Duration in seconds
     */
    public void charge(double durationSeconds) {
        if (isOn && !isFullyCharged()) {
            double energyAdded = (chargingPower * durationSeconds) / 3600.0; // Wh
            currentCharge = Math.min(currentCharge + energyAdded, batteryCapacity);
            
            double chargePercent = (currentCharge / batteryCapacity) * 100.0;
            logger.debug("{}: Charged {:.1f}Wh, battery state: {:.1f}%", 
                        name, energyAdded, chargePercent);
        }
    }
    
    public double getMinPower() {
        return minPower;
    }
    
    public void setMinPower(double minPower) {
        this.minPower = minPower;
    }
    
    public double getMaxPower() {
        return maxPower;
    }
    
    public void setMaxPower(double maxPower) {
        this.maxPower = maxPower;
    }
    
    public double getBatteryCapacity() {
        return batteryCapacity;
    }
    
    public void setBatteryCapacity(double batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }
    
    public double getCurrentCharge() {
        return currentCharge;
    }
    
    public void setCurrentCharge(double currentCharge) {
        this.currentCharge = currentCharge;
    }
    
    public double getChargingPower() {
        return chargingPower;
    }
    
    public double getChargePercentage() {
        return (currentCharge / batteryCapacity) * 100.0;
    }
}