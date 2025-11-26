package com.mjurczyga.iot_server.logic;

import com.mjurczyga.iot_server.config.DeviceConfig;
import com.mjurczyga.iot_server.model.DeviceDecision;
import com.mjurczyga.iot_server.model.DeviceDecision.ActionType;
import com.mjurczyga.iot_server.model.DeviceDecision.DeviceType;
import com.mjurczyga.iot_server.model.SystemState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PriorityCalculatorCustomModeTest {

    @Mock
    private DeviceConfig config;

    @InjectMocks
    private PriorityCalculator priorityCalculator;

    @BeforeEach
    void setUp() {
        // Set up default configuration values
        when(config.getEvMinPower()).thenReturn(1400.0);
        when(config.getEvMaxPower()).thenReturn(7400.0);
        when(config.getAcCoolingPower()).thenReturn(2000.0);
        when(config.getAcHeatingPower()).thenReturn(2000.0);
        when(config.getTargetTemperature()).thenReturn(22.0);
        when(config.getTemperatureHysteresis()).thenReturn(0.5);
        when(config.getDishwasherPower()).thenReturn(1800.0);
        when(config.getSmartPlugPower()).thenReturn(500.0);
        when(config.getSurplusBuffer()).thenReturn(200.0);
        
        // Default: custom mode disabled
        when(config.isCustomPriorityEnabled()).thenReturn(false);
    }

    @Test
    void customMode_shouldBeActivated_whenEnabled() {
        // Enable custom mode
        when(config.isCustomPriorityEnabled()).thenReturn(true);
        when(config.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "EV_CHARGER", "AC_CLIMATE", "DISHWASHER", "SMART_PLUG"
        ));

        SystemState state = SystemState.builder()
                .anyoneHome(true) // Even though someone is home, custom mode should override
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        assertEquals("CUSTOM", decision.getMode());
    }

    @Test
    void customMode_shouldProcessDevicesInOrder_smartPlugFirst() {
        when(config.isCustomPriorityEnabled()).thenReturn(true);
        when(config.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "SMART_PLUG", "AC_CLIMATE", "EV_CHARGER", "DISHWASHER"
        ));

        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(3000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should turn on smart plug first (priority 1)
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.SMART_PLUG 
                        && action.getAction() == ActionType.TURN_ON));
    }

    @Test
    void customMode_shouldProcessDevicesInOrder_evFirst() {
        when(config.isCustomPriorityEnabled()).thenReturn(true);
        when(config.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "EV_CHARGER", "AC_CLIMATE", "DISHWASHER", "SMART_PLUG"
        ));

        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // EV should be first priority
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.EV_CHARGER 
                        && action.getAction() == ActionType.SET_POWER));
    }

    @Test
    void customMode_shouldRespectSurplusLimits() {
        when(config.isCustomPriorityEnabled()).thenReturn(true);
        when(config.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "DISHWASHER", "SMART_PLUG", "EV_CHARGER", "AC_CLIMATE"
        ));

        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(1000.0) // Low production
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // With only 700W surplus, dishwasher (1800W) should not turn on
        assertFalse(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.DISHWASHER 
                        && action.getAction() == ActionType.TURN_ON));
        
        // But smart plug (500W) should turn on
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.SMART_PLUG 
                        && action.getAction() == ActionType.TURN_ON));
    }

    @Test
    void customMode_shouldHandleMultipleDevices() {
        when(config.isCustomPriorityEnabled()).thenReturn(true);
        when(config.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "SMART_PLUG", "EV_CHARGER", "DISHWASHER", "AC_CLIMATE"
        ));

        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(6000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should have multiple actions
        assertTrue(decision.getActions().size() >= 2);
        
        // Smart plug should be on
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.SMART_PLUG));
        
        // EV should be charging
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.EV_CHARGER));
    }

    @Test
    void customMode_shouldOverridePresenceDetection() {
        when(config.isCustomPriorityEnabled()).thenReturn(true);
        when(config.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "EV_CHARGER", "SMART_PLUG", "DISHWASHER", "AC_CLIMATE"
        ));

        SystemState state = SystemState.builder()
                .anyoneHome(true) // Someone is home
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should use CUSTOM mode, not COMFORT
        assertEquals("CUSTOM", decision.getMode());
    }

    @Test
    void customMode_acFirst_shouldStillRespectTemperature() {
        when(config.isCustomPriorityEnabled()).thenReturn(true);
        when(config.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "AC_CLIMATE", "SMART_PLUG", "EV_CHARGER", "DISHWASHER"
        ));

        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0) // Temperature is fine
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // AC should not turn on even though it's priority 1, because temp is OK
        assertFalse(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.AC_CLIMATE 
                        && action.getAction() == ActionType.TURN_ON));
    }

    @Test
    void customMode_acFirst_shouldTurnOnWhenHot() {
        when(config.isCustomPriorityEnabled()).thenReturn(true);
        when(config.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "AC_CLIMATE", "SMART_PLUG", "EV_CHARGER", "DISHWASHER"
        ));

        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(26.0) // Too hot!
                .outdoorTemperature(30.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // AC should turn on because it's hot
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.AC_CLIMATE 
                        && action.getAction() == ActionType.TURN_ON
                        && action.getReason().contains("cooling")));
    }

    @Test
    void customMode_shouldHandleInvalidDeviceNames() {
        when(config.isCustomPriorityEnabled()).thenReturn(true);
        when(config.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "EV_CHARGER", "INVALID_DEVICE", "AC_CLIMATE", "SMART_PLUG"
        ));

        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        // Should not throw exception
        assertDoesNotThrow(() -> priorityCalculator.calculatePriorities(state));
    }

    @Test
    void customMode_dishwasherFirst_shouldTurnOnWithSurplus() {
        when(config.isCustomPriorityEnabled()).thenReturn(true);
        when(config.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "DISHWASHER", "SMART_PLUG", "AC_CLIMATE", "EV_CHARGER"
        ));

        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Dishwasher should turn on as priority 1
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.DISHWASHER 
                        && action.getAction() == ActionType.TURN_ON));
    }

    @Test
    void customMode_shouldWorkWithEmptyPriorityList() {
        when(config.isCustomPriorityEnabled()).thenReturn(true);
        when(config.getCustomPriorityOrder()).thenReturn(Arrays.asList());

        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        // Should not throw exception with empty list
        assertDoesNotThrow(() -> priorityCalculator.calculatePriorities(state));
        
        DeviceDecision decision = priorityCalculator.calculatePriorities(state);
        assertEquals("CUSTOM", decision.getMode());
    }

    @Test
    void disabledCustomMode_shouldUseMaxUsageMode() {
        when(config.isCustomPriorityEnabled()).thenReturn(false);

        SystemState state = SystemState.builder()
                .anyoneHome(false) // No one home
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        assertEquals("MAX_USAGE", decision.getMode());
    }

    @Test
    void disabledCustomMode_shouldUseComfortMode() {
        when(config.isCustomPriorityEnabled()).thenReturn(false);

        SystemState state = SystemState.builder()
                .anyoneHome(true) // Someone is home
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        assertEquals("COMFORT", decision.getMode());
    }
}