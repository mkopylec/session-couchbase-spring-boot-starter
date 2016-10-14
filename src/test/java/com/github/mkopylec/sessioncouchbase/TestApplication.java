package com.github.mkopylec.sessioncouchbase;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        run(TestApplication.class, args);
    }

//    @Bean
//    public EmbeddedServletContainerCustomizer customizer() {
//        return new EmbeddedServletContainerCustomizer() {
//
//            @Override
//            public void customize(ConfigurableEmbeddedServletContainer container) {
//                TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
//                TomcatContextCustomizer contextCustomizer = new TomcatContextCustomizer() {
//
//                    @Override
//                    public void customize(Context context) {
//                        context.setCookieProcessor(new LegacyCookieProcessor());
//                    }
//                };
//                tomcat.addContextCustomizers(contextCustomizer);
//            }
//        };
//    }
}
