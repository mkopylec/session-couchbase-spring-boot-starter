package com.github.mkopylec.sessioncouchbase;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) throws JsonProcessingException, InterruptedException {
        run(TestApplication.class, args);
    }

    @Bean
    public EmbeddedServletContainerCustomizer customizer() {
        return container -> {
            TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
            TomcatContextCustomizer contextCustomizer = context -> context.setCookieProcessor(new LegacyCookieProcessor());
            tomcat.addContextCustomizers(contextCustomizer);
        };
    }
}
