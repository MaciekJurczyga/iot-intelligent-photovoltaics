package com.mjurczyga.iot_server.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class HomeAssistantStateResponse {

    @JsonProperty("state")
    private String state;

    public void setState(String state) {
        this.state = state;
    }
}