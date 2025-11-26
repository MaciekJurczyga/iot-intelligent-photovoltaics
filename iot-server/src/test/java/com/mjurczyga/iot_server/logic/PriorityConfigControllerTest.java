package com.mjurczyga.iot_server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjurczyga.iot_server.config.DeviceConfig;
import com.mjurczyga.iot_server.controllers.PriorityConfigController.CustomPriorityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PriorityConfigController.class)
class PriorityConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceConfig deviceConfig;

    @BeforeEach
    void setUp() {
        // Default mock behavior
        when(deviceConfig.isCustomPriorityEnabled()).thenReturn(false);
        when(deviceConfig.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "EV_CHARGER", "AC_CLIMATE", "DISHWASHER", "SMART_PLUG"
        ));
    }

    @Test
    void getPriorityConfig_shouldReturnCurrentConfiguration() throws Exception {
        when(deviceConfig.isCustomPriorityEnabled()).thenReturn(true);
        when(deviceConfig.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "SMART_PLUG", "AC_CLIMATE", "EV_CHARGER", "DISHWASHER"
        ));

        mockMvc.perform(get("/api/v1/priority/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customEnabled").value(true))
                .andExpect(jsonPath("$.priorityOrder[0]").value("SMART_PLUG"))
                .andExpect(jsonPath("$.priorityOrder[1]").value("AC_CLIMATE"))
                .andExpect(jsonPath("$.priorityOrder[2]").value("EV_CHARGER"))
                .andExpect(jsonPath("$.priorityOrder[3]").value("DISHWASHER"));
    }

    @Test
    void setCustomPriorities_shouldAcceptValidPriorities() throws Exception {
        Map<String, Integer> priorities = new HashMap<>();
        priorities.put("EV_CHARGER", 1);
        priorities.put("AC_CLIMATE", 2);
        priorities.put("DISHWASHER", 3);
        priorities.put("SMART_PLUG", 4);

        CustomPriorityRequest request = new CustomPriorityRequest();
        request.setEnabled(true);
        request.setPriorities(priorities);

        mockMvc.perform(post("/api/v1/priority/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(deviceConfig).setCustomPriorityEnabled(true);
        verify(deviceConfig).setCustomPriorityOrder(anyList());
    }

    @Test
    void setCustomPriorities_shouldRejectMissingDevice() throws Exception {
        Map<String, Integer> priorities = new HashMap<>();
        priorities.put("EV_CHARGER", 1);
        priorities.put("AC_CLIMATE", 2);
        priorities.put("DISHWASHER", 3);
        // Missing SMART_PLUG

        CustomPriorityRequest request = new CustomPriorityRequest();
        request.setEnabled(true);
        request.setPriorities(priorities);

        mockMvc.perform(post("/api/v1/priority/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(deviceConfig, never()).setCustomPriorityEnabled(anyBoolean());
        verify(deviceConfig, never()).setCustomPriorityOrder(anyList());
    }

    @Test
    void setCustomPriorities_shouldRejectDuplicatePriorities() throws Exception {
        Map<String, Integer> priorities = new HashMap<>();
        priorities.put("EV_CHARGER", 1);
        priorities.put("AC_CLIMATE", 1); // Duplicate!
        priorities.put("DISHWASHER", 3);
        priorities.put("SMART_PLUG", 4);

        CustomPriorityRequest request = new CustomPriorityRequest();
        request.setEnabled(true);
        request.setPriorities(priorities);

        mockMvc.perform(post("/api/v1/priority/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(deviceConfig, never()).setCustomPriorityEnabled(anyBoolean());
    }

    @Test
    void setCustomPriorities_shouldRejectInvalidPriorityValues() throws Exception {
        Map<String, Integer> priorities = new HashMap<>();
        priorities.put("EV_CHARGER", 1);
        priorities.put("AC_CLIMATE", 2);
        priorities.put("DISHWASHER", 5); // Invalid! Should be 1-4
        priorities.put("SMART_PLUG", 4);

        CustomPriorityRequest request = new CustomPriorityRequest();
        request.setEnabled(true);
        request.setPriorities(priorities);

        mockMvc.perform(post("/api/v1/priority/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(deviceConfig, never()).setCustomPriorityEnabled(anyBoolean());
    }

    @Test
    void setCustomPriorities_shouldConvertToCorrectOrder() throws Exception {
        Map<String, Integer> priorities = new HashMap<>();
        priorities.put("SMART_PLUG", 1);
        priorities.put("AC_CLIMATE", 2);
        priorities.put("EV_CHARGER", 3);
        priorities.put("DISHWASHER", 4);

        CustomPriorityRequest request = new CustomPriorityRequest();
        request.setEnabled(true);
        request.setPriorities(priorities);

        // Mock the order that will be returned after setting
        when(deviceConfig.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "SMART_PLUG", "AC_CLIMATE", "EV_CHARGER", "DISHWASHER"
        ));

        mockMvc.perform(post("/api/v1/priority/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priorityOrder[0]").value("SMART_PLUG"))
                .andExpect(jsonPath("$.priorityOrder[1]").value("AC_CLIMATE"))
                .andExpect(jsonPath("$.priorityOrder[2]").value("EV_CHARGER"))
                .andExpect(jsonPath("$.priorityOrder[3]").value("DISHWASHER"));
    }

    @Test
    void toggleCustomMode_shouldEnableCustomMode() throws Exception {
        mockMvc.perform(post("/api/v1/priority/custom/toggle")
                .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customEnabled").value(true))
                .andExpect(jsonPath("$.mode").value("CUSTOM"));

        verify(deviceConfig).setCustomPriorityEnabled(true);
    }

    @Test
    void toggleCustomMode_shouldDisableCustomMode() throws Exception {
        mockMvc.perform(post("/api/v1/priority/custom/toggle")
                .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customEnabled").value(false))
                .andExpect(jsonPath("$.mode").value("AUTO (MAX_USAGE/COMFORT)"));

        verify(deviceConfig).setCustomPriorityEnabled(false);
    }

    @Test
    void getAvailableDevices_shouldReturnAllDevices() throws Exception {
        mockMvc.perform(get("/api/v1/priority/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("EV_CHARGER"))
                .andExpect(jsonPath("$[0].displayName").value("Electric Vehicle Charger"))
                .andExpect(jsonPath("$[1].name").value("AC_CLIMATE"))
                .andExpect(jsonPath("$[2].name").value("DISHWASHER"))
                .andExpect(jsonPath("$[3].name").value("SMART_PLUG"))
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void setCustomPriorities_shouldAcceptEnabledFalse() throws Exception {
        Map<String, Integer> priorities = new HashMap<>();
        priorities.put("EV_CHARGER", 1);
        priorities.put("AC_CLIMATE", 2);
        priorities.put("DISHWASHER", 3);
        priorities.put("SMART_PLUG", 4);

        CustomPriorityRequest request = new CustomPriorityRequest();
        request.setEnabled(false); // Disabled
        request.setPriorities(priorities);

        mockMvc.perform(post("/api/v1/priority/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(deviceConfig).setCustomPriorityEnabled(false);
        verify(deviceConfig).setCustomPriorityOrder(anyList());
    }

    @Test
    void setCustomPriorities_shouldRejectExtraDevices() throws Exception {
        Map<String, Integer> priorities = new HashMap<>();
        priorities.put("EV_CHARGER", 1);
        priorities.put("AC_CLIMATE", 2);
        priorities.put("DISHWASHER", 3);
        priorities.put("SMART_PLUG", 4);
        priorities.put("UNKNOWN_DEVICE", 5); // Extra device

        CustomPriorityRequest request = new CustomPriorityRequest();
        request.setEnabled(true);
        request.setPriorities(priorities);

        mockMvc.perform(post("/api/v1/priority/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(deviceConfig, never()).setCustomPriorityEnabled(anyBoolean());
    }

    @Test
    void setCustomPriorities_reversedOrder_shouldWork() throws Exception {
        Map<String, Integer> priorities = new HashMap<>();
        priorities.put("DISHWASHER", 1);
        priorities.put("SMART_PLUG", 2);
        priorities.put("AC_CLIMATE", 3);
        priorities.put("EV_CHARGER", 4);

        CustomPriorityRequest request = new CustomPriorityRequest();
        request.setEnabled(true);
        request.setPriorities(priorities);

        when(deviceConfig.getCustomPriorityOrder()).thenReturn(Arrays.asList(
            "DISHWASHER", "SMART_PLUG", "AC_CLIMATE", "EV_CHARGER"
        ));

        mockMvc.perform(post("/api/v1/priority/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priorityOrder[0]").value("DISHWASHER"))
                .andExpect(jsonPath("$.priorityOrder[3]").value("EV_CHARGER"));
    }

    @Test
    void setCustomPriorities_shouldHandleJsonParsingError() throws Exception {
        String invalidJson = "{invalid json}";

        mockMvc.perform(post("/api/v1/priority/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(deviceConfig, never()).setCustomPriorityEnabled(anyBoolean());
    }
}