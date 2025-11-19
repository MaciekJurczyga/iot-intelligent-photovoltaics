package com.mjurczyga.iot_server.devices;

import java.time.Instant;

public class OnOffDevice extends Device {
    protected double ratedPower;
    protected boolean requiresSurplus; // whether it requires energy surplus to turn on
    protected Instant stableSince; // timestamp when conditions became stable
    
    public OnOffDevice(String name, double power, boolean requiresSurplus) {
        super(name);
        this.ratedPower = power;
        this.requiresSurplus = requiresSurplus;
        this.stableSince = null;
    }
    
    public OnOffDevice(String name, double power) {
        this(name, power, true);
    }
    
    /**
     * Turns the device on
     * @return true if device was turned on, false if already on
     */
    public boolean turnOn() {
        if (!isOn) {
            isOn = true;
            currentPower = ratedPower;
            logger.debug("{}: Turned ON (power: {}W)", name, ratedPower);
            return true;
        }
        return false;
    }
    
    /**
     * Turns the device off
     * @return true if device was turned off, false if already off
     */
    public boolean turnOff() {
        if (isOn) {
            isOn = false;
            currentPower = 0.0;
            logger.debug("{}: Turned OFF", name);
            return true;
        }
        return false;
    }
    

    public double getRatedPower() {
        return ratedPower;
    }
    
    public void setRatedPower(double ratedPower) {
        this.ratedPower = ratedPower;
    }
    
    public boolean isRequiresSurplus() {
        return requiresSurplus;
    }
    
    public void setRequiresSurplus(boolean requiresSurplus) {
        this.requiresSurplus = requiresSurplus;
    }
    
    public Instant getStableSince() {
        return stableSince;
    }
    
    public void setStableSince(Instant stableSince) {
        this.stableSince = stableSince;
    }
}