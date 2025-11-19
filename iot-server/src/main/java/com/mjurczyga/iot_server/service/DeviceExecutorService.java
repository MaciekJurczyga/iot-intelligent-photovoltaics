package com.mjurczyga.iot_server.service;

import com.mjurczyga.iot_server.client.home_assistant.HomeAssistantClient;
import com.mjurczyga.iot_server.model.DeviceDecision;
import com.mjurczyga.iot_server.model.DeviceDecision.DeviceAction;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service to execute device control decisions
 */
@Service
@RequiredArgsConstructor
public class DeviceExecutorService {
    
    private static final Logger log = LoggerFactory.getLogger(DeviceExecutorService.class);
    
    private final HomeAssistantClient homeAssistantClient;
    
    /**
     * Execute all device actions from a decision
     */
    public void executeDecision(DeviceDecision decision) {
        log.info("Executing decision in {} mode", decision.getMode());
        log.info("Explanation: {}", decision.getExplanation());
        
        for (DeviceAction action : decision.getActions()) {
            executeAction(action);
        }
    }
    
    /**
     * Execute a single device action
     */
    private void executeAction(DeviceAction action) {
        log.info("Action: {} {} - {}", action.getAction(), action.getDevice(), action.getReason());
        
        try {
            switch (action.getDevice()) {
                case EV_CHARGER:
                    executeEvChargerAction(action);
                    break;
                    
                case AC_CLIMATE:
                    executeClimateAction(action);
                    break;
                    
                case DISHWASHER:
                    executeDishwasherAction(action);
                    break;
                    
                case SMART_PLUG:
                    executeSmartPlugAction(action);
                    break;
                    
                default:
                    log.warn("Unknown device type: {}", action.getDevice());
            }
        } catch (Exception e) {
            log.error("Error executing action for {}: {}", action.getDevice(), e.getMessage(), e);
        }
    }
    
    /**
     * Execute EV charger action
     */
    private void executeEvChargerAction(DeviceAction action) {
        switch (action.getAction()) {
            case SET_POWER:
                if (action.getTargetPower() != null) {
                    log.info("Setting EV charger power to {}W", action.getTargetPower());
                    // TODO: Implement actual EV charger control via Home Assistant
                    // homeAssistantClient.setEvChargingPower(action.getTargetPower());
                }
                break;
                
            case TURN_OFF:
                log.info("Turning off EV charger");
                // TODO: Implement actual EV charger control
                // homeAssistantClient.setEvChargingPower(0.0);
                break;
                
            default:
                log.warn("Unsupported action {} for EV charger", action.getAction());
        }
    }
    
    /**
     * Execute climate control action
     */
    private void executeClimateAction(DeviceAction action) {
        switch (action.getAction()) {
            case TURN_ON:
                log.info("Turning on climate control");
                // TODO: Implement actual climate control via Home Assistant
                // This would typically be done via climate entity in HA
                // homeAssistantClient.turnOnClimate();
                break;
                
            case TURN_OFF:
                log.info("Turning off climate control");
                // TODO: Implement actual climate control
                // homeAssistantClient.turnOffClimate();
                break;
                
            default:
                log.warn("Unsupported action {} for climate control", action.getAction());
        }
    }
    
    /**
     * Execute dishwasher action
     */
    private void executeDishwasherAction(DeviceAction action) {
        switch (action.getAction()) {
            case TURN_ON:
                log.info("Turning on dishwasher");
                // TODO: Implement dishwasher control
                // This might be via a smart plug or direct integration
                // homeAssistantClient.turnOnDishwasher();
                break;
                
            case TURN_OFF:
                log.info("Turning off dishwasher");
                // TODO: Implement dishwasher control
                // homeAssistantClient.turnOffDishwasher();
                break;
                
            default:
                log.warn("Unsupported action {} for dishwasher", action.getAction());
        }
    }
    
    /**
     * Execute smart plug action
     */
    private void executeSmartPlugAction(DeviceAction action) {
        switch (action.getAction()) {
            case TURN_ON:
                log.info("Turning on smart plug");
                String onResult = homeAssistantClient.turnOnSmartPlug();
                log.debug("Smart plug ON result: {}", onResult);
                break;
                
            case TURN_OFF:
                log.info("Turning off smart plug");
                String offResult = homeAssistantClient.turnOffSmartPlug();
                log.debug("Smart plug OFF result: {}", offResult);
                break;
                
            default:
                log.warn("Unsupported action {} for smart plug", action.getAction());
        }
    }
}