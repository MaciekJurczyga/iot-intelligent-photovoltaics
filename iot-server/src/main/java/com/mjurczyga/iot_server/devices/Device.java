package com.iot.server.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Base class for all IoT devices in the energy management system
public abstract class Device {
    protected static final Logger logger = LoggerFactory.getLogger(Device.class);
    
    protected String name;
    protected boolean isOn;
    protected double currentPower; // Watts
    
    public Device(String name) {
        this.name = name;
        this.isOn = false;
        this.currentPower = 0.0;
    }
    

    // Returns current power consumption in watts
    public double getPowerConsumption() {
        return isOn ? currentPower : 0.0;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isOn() {
        return isOn;
    }
    
    public void setOn(boolean on) {
        isOn = on;
    }
    
    public double getCurrentPower() {
        return currentPower;
    }
    
    public void setCurrentPower(double currentPower) {
        this.currentPower = currentPower;
    }
}