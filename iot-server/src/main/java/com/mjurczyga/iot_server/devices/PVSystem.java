package com.iot.server.devices;


public class PVSystem {
    private String name;
    private double currentProduction; // Watts
    
    public PVSystem(String name) {
        this.name = name;
        this.currentProduction = 0.0;
    }
    
    public PVSystem() {
        this("PV System");
    }
    
    /**
     * Sets current production level
     * @param power Production power in watts 
     */
    public void setProduction(double power) {
        this.currentProduction = Math.max(0.0, power);
    }
    
    /**
     * Gets current production in watts
     * @return Production in watts
     */
    public double getProduction() {
        return currentProduction;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getCurrentProduction() {
        return currentProduction;
    }
}