package com.github.mkopylec.sessioncouchbase;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import static org.springframework.boot.SpringApplication.run;

@EnableCouchbaseHttpSession
@SpringBootApplication
@Import(TestConfiguration.class)
public class TestApplication {

    public static void main(String[] args) {
        run(TestApplication.class, args);
    }
}
