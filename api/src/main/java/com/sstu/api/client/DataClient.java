package com.sstu.api.client;

import com.sstu.api.circuit.CircuitBreaker;
import com.sstu.api.exception.ServerErrorException;
import org.springframework.http.HttpStatusCode;
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
                .onStatus(HttpStatusCode::is5xxServerError,
                        (request, response) -> {
                            throw new ServerErrorException(
                                    "Server error " + response.getStatusCode()
                            );
                        }
                )
                .body(String.class));
    }
}
