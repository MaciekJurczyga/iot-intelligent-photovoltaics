package com.mjurczyga.iot_server.controllers;

import com.mjurczyga.iot_server.client.devices.RaspberryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/raspberry")
@RequiredArgsConstructor
public class RaspberryController {

    private final RaspberryClient raspberryClient;


    @GetMapping("/test")
    public String test() {
        try {
            return raspberryClient.test();
        } catch (Exception e) {
            return error(e);
        }
    }

    @GetMapping("/distance")
    public String distance() {
        try {
            return raspberryClient.getDistance();
        } catch (Exception e) {
            return error(e);
        }
    }

    @GetMapping("/bme280")
    public String bme280() {
        try {
            return raspberryClient.getBme280();
        } catch (Exception e) {
            return error(e);
        }
    }

    @GetMapping("/bme280/continuous")
    public String bme280Continuous() {
        try {
            return raspberryClient.getBme280Continuous();
        } catch (Exception e) {
            return error(e);
        }
    }

    private String error(Exception e) {
        return "{\"error\": \"" + e.getMessage() + "\"}";
    }
}