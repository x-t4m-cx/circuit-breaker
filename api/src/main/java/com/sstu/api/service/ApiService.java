package com.sstu.api.service;

import com.sstu.api.client.DataClient;
import org.springframework.stereotype.Service;

@Service
public class ApiService {

    private final DataClient dataClient;

    public ApiService(DataClient dataClient) {
        this.dataClient = dataClient;
    }

    public String getHello() {
        return dataClient.getHello();
    }
}
