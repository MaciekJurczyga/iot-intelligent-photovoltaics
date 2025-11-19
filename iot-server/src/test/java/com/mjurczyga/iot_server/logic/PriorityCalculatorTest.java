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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PriorityCalculatorTest {

    @Mock
    private DeviceConfig config;

    @InjectMocks
    private PriorityCalculator priorityCalculator;

    @BeforeEach
    void setUp() {
        // Set up default configuration values
        when(config.getEvMinPower()).thenReturn(1400.0);
        when(config.getEvMaxPower()).thenReturn(5000.0);
        when(config.getAcCoolingPower()).thenReturn(1000.0);
        when(config.getAcHeatingPower()).thenReturn(1000.0);
        when(config.getTargetTemperature()).thenReturn(22.0);
        when(config.getTemperatureHysteresis()).thenReturn(0.5);
        when(config.getDishwasherPower()).thenReturn(1800.0);
        when(config.getSmartPlugPower()).thenReturn(500.0);
        when(config.getSurplusBuffer()).thenReturn(200.0);
    }

    @Test
    void calculatePriorities_maxUsageMode_noOneHome() {
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

        assertEquals("MAX_USAGE", decision.getMode());
        assertNotNull(decision.getActions());
    }

    @Test
    void calculatePriorities_comfortMode_someoneHome() {
        SystemState state = SystemState.builder()
                .anyoneHome(true)
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
        assertNotNull(decision.getActions());
    }

    @Test
    void maxUsageMode_shouldChargeEV_whenSufficientSurplus() {
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
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should have action to charge EV
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.EV_CHARGER 
                        && action.getAction() == ActionType.SET_POWER));
    }

    @Test
    void maxUsageMode_shouldNotChargeEV_whenNotConnected() {
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(false)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should not have SET_POWER action for EV
        assertFalse(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.EV_CHARGER 
                        && action.getAction() == ActionType.SET_POWER));
    }

    @Test
    void maxUsageMode_shouldStopCharging_whenEVFullyCharged() {
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(96.0)
                .evChargingPower(3000.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should turn off EV charger when fully charged
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.EV_CHARGER 
                        && action.getAction() == ActionType.TURN_OFF
                        && action.getReason().contains("fully charged")));
    }

    @Test
    void maxUsageMode_shouldTurnOnAC_whenTemperatureTooHigh() {
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(25.0) // Above target + hysteresis
                .outdoorTemperature(28.0)
                .evConnected(false)
                .evChargePercentage(0.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should turn on AC when too hot and sufficient surplus
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.AC_CLIMATE 
                        && action.getAction() == ActionType.TURN_ON
                        && action.getReason().contains("cooling")));
    }

    @Test
    void maxUsageMode_shouldTurnOnAC_whenTemperatureTooLow() {
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(20.0) // Below target - hysteresis
                .outdoorTemperature(15.0)
                .evConnected(false)
                .evChargePercentage(0.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should turn on heating when too cold and sufficient surplus
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.AC_CLIMATE 
                        && action.getAction() == ActionType.TURN_ON
                        && action.getReason().contains("heating")));
    }

    @Test
    void maxUsageMode_shouldNotTurnOnAC_whenInsufficientSurplus() {
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(1000.0) // Not enough for AC
                .currentHouseConsumption(300.0)
                .indoorTemperature(25.0)
                .outdoorTemperature(28.0)
                .evConnected(false)
                .evChargePercentage(0.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should not turn on AC with insufficient surplus
        assertFalse(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.AC_CLIMATE 
                        && action.getAction() == ActionType.TURN_ON));
    }

    @Test
    void comfortMode_shouldTurnOnAC_regardlessOfSurplus() {
        SystemState state = SystemState.builder()
                .anyoneHome(true)
                .currentPvProduction(500.0) // Very low production
                .currentHouseConsumption(300.0)
                .indoorTemperature(25.0)
                .outdoorTemperature(28.0)
                .evConnected(false)
                .evChargePercentage(0.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // In comfort mode, AC should turn on even with low surplus
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.AC_CLIMATE 
                        && action.getAction() == ActionType.TURN_ON
                        && action.getReason().contains("Comfort priority")));
    }

    @Test
    void maxUsageMode_shouldTurnOnDishwasher_whenSufficientSurplus() {
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(false)
                .evChargePercentage(0.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(true)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should turn on dishwasher with sufficient surplus
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.DISHWASHER 
                        && action.getAction() == ActionType.TURN_ON));
    }

    @Test
    void maxUsageMode_shouldNotTurnOnDishwasher_whenNotReady() {
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(false)
                .evChargePercentage(0.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should not turn on dishwasher when not ready
        assertFalse(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.DISHWASHER 
                        && action.getAction() == ActionType.TURN_ON));
    }

    @Test
    void maxUsageMode_shouldTurnOnSmartPlug_whenSufficientSurplus() {
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(2000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(false)
                .evChargePercentage(0.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should turn on smart plug with sufficient surplus
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.SMART_PLUG 
                        && action.getAction() == ActionType.TURN_ON));
    }

    @Test
    void maxUsageMode_shouldTurnOffSmartPlug_whenInsufficientSurplus() {
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(500.0)
                .currentHouseConsumption(500.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(false)
                .evChargePercentage(0.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(true)
                .smartPlugPower(500.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should turn off smart plug when insufficient surplus
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.SMART_PLUG 
                        && action.getAction() == ActionType.TURN_OFF));
    }

    @Test
    void evCharger_shouldRespectMinimumPower() {
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(1500.0) // Just below min + buffer
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should not charge if below minimum power
        assertFalse(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.EV_CHARGER 
                        && action.getAction() == ActionType.SET_POWER));
    }

    @Test
    void evCharger_shouldRespectMaximumPower() {
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(10000.0) // Way more than max
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.0)
                .outdoorTemperature(20.0)
                .evConnected(true)
                .evChargePercentage(50.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should cap at maximum power
        decision.getActions().stream()
                .filter(action -> action.getDevice() == DeviceType.EV_CHARGER 
                        && action.getAction() == ActionType.SET_POWER)
                .forEach(action -> {
                    assertNotNull(action.getTargetPower());
                    assertTrue(action.getTargetPower() <= config.getEvMaxPower());
                });
    }

    @Test
    void complexScenario_multipleDevices_shouldPrioritizeCorrectly() {
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
        assertTrue(decision.getActions().size() > 1);
        
        // EV should be first priority (should charge)
        assertTrue(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.EV_CHARGER));
    }

    @Test
    void temperatureHysteresis_shouldPreventRapidCycling() {
        // Temperature slightly above target but within hysteresis
        SystemState state = SystemState.builder()
                .anyoneHome(false)
                .currentPvProduction(5000.0)
                .currentHouseConsumption(300.0)
                .indoorTemperature(22.3) // Within hysteresis range
                .outdoorTemperature(25.0)
                .evConnected(false)
                .evChargePercentage(0.0)
                .evChargingPower(0.0)
                .acOn(false)
                .acPowerUsage(0.0)
                .dishwasherReady(false)
                .dishwasherOn(false)
                .smartPlugOn(false)
                .smartPlugPower(0.0)
                .build();

        DeviceDecision decision = priorityCalculator.calculatePriorities(state);

        // Should not turn on AC when temperature is within hysteresis
        assertFalse(decision.getActions().stream()
                .anyMatch(action -> action.getDevice() == DeviceType.AC_CLIMATE 
                        && action.getAction() == ActionType.TURN_ON));
    }
}