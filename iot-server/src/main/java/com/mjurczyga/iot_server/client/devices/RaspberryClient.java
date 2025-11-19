package com.mjurczyga.iot_server.client.devices;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class RaspberryClient {

    private static final String BASE_URL = "http://100.80.187.125:5000/raspberry";

    private final HttpClient httpClient;

    public RaspberryClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public RaspberryClient(HttpClient client){
        this.httpClient = client;
    }

    public String test() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/test"))
                .GET()
                .build();

        return send(request);
    }

    public String getDistance() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/distance"))
                .GET()
                .build();

        return send(request);
    }

    public String getBme280() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/bme280"))
                .GET()
                .build();

        return send(request);
    }

    public String getBme280Continuous() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/bme280/continuous"))
                .GET()
                .build();

        return send(request);
    }

    private String send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
