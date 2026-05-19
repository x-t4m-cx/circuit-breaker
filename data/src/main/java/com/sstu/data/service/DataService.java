package com.sstu.data.service;


import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DataService {

    Random random = new Random();
    public String getHello() {

        if (random.nextInt(100) < 30) {
            throw new RuntimeException("Server Error");
        }

        return "Hello";
    }
}
