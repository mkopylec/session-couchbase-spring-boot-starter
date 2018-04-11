package com.github.mkopylec.sessioncouchbase;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        run(TestApplication.class, args);
    }

    @Configuration
    public static class TestConfiguration {

        public static final String COOKIE_NAME = "sessionCookie";

        @Bean
        public CookieSerializer cookieSerializer() {
            DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
            cookieSerializer.setCookieName(COOKIE_NAME);
            return cookieSerializer;
        }
    }
}
