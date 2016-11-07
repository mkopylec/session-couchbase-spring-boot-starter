package com.github.mkopylec.sessioncouchbase;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) throws JsonProcessingException, InterruptedException {
        run(TestApplication.class, args);
    }
}
