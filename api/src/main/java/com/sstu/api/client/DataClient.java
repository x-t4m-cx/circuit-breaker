package com.sstu.api.client;

import com.sstu.api.circuit.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DataClient {

    private final RestClient restClient;

    private final CircuitBreaker cb = new CircuitBreaker(5, 5000, 2, 2);

    public DataClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public String getHello() {
        return cb.execute(() -> restClient.get()
                .uri("/data/hello")
                .retrieve()
                .body(String.class));
    }
}
