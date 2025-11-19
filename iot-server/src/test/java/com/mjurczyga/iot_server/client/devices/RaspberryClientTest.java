package com.mjurczyga.iot_server.client.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RaspberryClientTest {

    private RaspberryClient client;
    private HttpClient httpClientMock;
    private HttpResponse<String> httpResponseMock;

    @BeforeEach
    void setUp() {
        httpClientMock = mock(HttpClient.class);
        httpResponseMock = mock(HttpResponse.class);

        client = new RaspberryClient(httpClientMock);
    }

    @Test
    void test_testEndpoint() throws Exception {
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponseMock);
        when(httpResponseMock.body()).thenReturn("{\"message\":\"Hello\"}");

        String result = client.test();

        assertEquals("{\"message\":\"Hello\"}", result);
    }

    @Test
    void test_getDistance() throws Exception {
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponseMock);
        when(httpResponseMock.body()).thenReturn("{\"distance\": 123}");

        String result = client.getDistance();

        assertEquals("{\"distance\": 123}", result);
    }

    @Test
    void test_getBme280() throws Exception {
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponseMock);
        when(httpResponseMock.body()).thenReturn("{\"temp\": 22.5}");

        String result = client.getBme280();

        assertEquals("{\"temp\": 22.5}", result);
    }

    @Test
    void test_getBme280Continuous() throws Exception {
        when(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponseMock);
        when(httpResponseMock.body()).thenReturn("{\"temp\": [22.5, 22.6]}");

        String result = client.getBme280Continuous();

        assertEquals("{\"temp\": [22.5, 22.6]}", result);
    }
}
