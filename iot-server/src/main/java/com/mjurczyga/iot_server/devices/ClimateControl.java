package com.iot.server.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Climate control (air conditioning/heating) with temperature control
public class ClimateControl extends OnOffDevice {
    private static final Logger logger = LoggerFactory.getLogger(ClimateControl.class);
    
    public enum Mode {
        COOLING,
        HEATING,
        OFF
    }
    
    private double coolingPower;
    private double heatingPower;
    private double targetTemp;
    private double hysteresis;
    private Mode mode;
    
    public ClimateControl(String name, double coolingPower, double heatingPower, 
                         double targetTemp, double hysteresis) {
        super(name, coolingPower, false); // Climate doesn't require surplus
        this.coolingPower = coolingPower;
        this.heatingPower = heatingPower;
        this.targetTemp = targetTemp;
        this.hysteresis = hysteresis;
        this.mode = Mode.OFF;
    }
    
    public ClimateControl(String name, double coolingPower, double heatingPower, 
                         double targetTemp) {
        this(name, coolingPower, heatingPower, targetTemp, 0.5);
    }
    
    /**
     * Checks if cooling is needed
     * @param currentTemp Current temperature
     * @return true if cooling is needed
     */
    public boolean needsCooling(double currentTemp) {
        if (mode == Mode.COOLING && isOn) {
            return currentTemp > (targetTemp - hysteresis);
        } else {
            return currentTemp > (targetTemp + hysteresis);
        }
    }
    
    /**
     * Checks if heating is needed
     * @param currentTemp Current temperature
     * @return true if heating is needed
     */
    public boolean needsHeating(double currentTemp) {
        if (mode == Mode.HEATING && isOn) {
            return currentTemp < (targetTemp + hysteresis);
        } else {
            return currentTemp < (targetTemp - hysteresis);
        }
    }
    
    public void setModeCooling() {
        this.mode = Mode.COOLING;
        this.ratedPower = coolingPower;
        this.currentPower = isOn ? coolingPower : 0.0;
    }
    
    public void setModeHeating() {
        this.mode = Mode.HEATING;
        this.ratedPower = heatingPower;
        this.currentPower = isOn ? heatingPower : 0.0;
    }
    
    /**
     * Updates climate control state based on temperature
     * In comfort mode, operates independently of energy surplus
     * @param currentTemp Current indoor temperature
     * @param outdoorTemp Current outdoor temperature
     */
    public void update(double currentTemp, double outdoorTemp) {
        if (currentTemp > targetTemp + hysteresis) {
            // Need cooling
            if (mode != Mode.COOLING) {
                setModeCooling();
                logger.debug("{}: Switched to COOLING mode", name);
            }
            
            if (needsCooling(currentTemp)) {
                if (!isOn) {
                    turnOn();
                }
            } else {
                if (isOn) {
                    turnOff();
                }
            }
            
        } else if (currentTemp < targetTemp - hysteresis) {
            // Need heating
            if (mode != Mode.HEATING) {
                setModeHeating();
                logger.info("{}: Switched to HEATING mode", name);
            }
            
            if (needsHeating(currentTemp)) {
                if (!isOn) {
                    turnOn();
                }
            } else {
                if (isOn) {
                    turnOff();
                }
            }
        } else {
            // Temperature in acceptable range
            if (isOn) {
                turnOff();
            }
        }
    }
    
    public double getCoolingPower() {
        return coolingPower;
    }
    
    public void setCoolingPower(double coolingPower) {
        this.coolingPower = coolingPower;
    }
    
    public double getHeatingPower() {
        return heatingPower;
    }
    
    public void setHeatingPower(double heatingPower) {
        this.heatingPower = heatingPower;
    }
    
    public double getTargetTemp() {
        return targetTemp;
    }
    
    public void setTargetTemp(double targetTemp) {
        this.targetTemp = targetTemp;
    }
    
    public double getHysteresis() {
        return hysteresis;
    }
    
    public void setHysteresis(double hysteresis) {
        this.hysteresis = hysteresis;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode mode) {
        this.mode = mode;
    }
}