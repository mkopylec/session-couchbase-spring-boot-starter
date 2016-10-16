package com.github.mkopylec.sessioncouchbase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import foodev.jsondiff.Jackson2Diff;
import foodev.jsondiff.JsonDiff;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class TestApplication {

    public static class Test {

        private int i;
        private String s;
        private Date d;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public Date getD() {
            return d;
        }

        public void setD(Date d) {
            this.d = d;
        }
    }

    public static void main(String[] args) throws JsonProcessingException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        Test t = new Test();
        t.setI(10);
        t.setS("10");
        t.setD(new Date());
        Test t2 = new Test();
        Thread.sleep(1000);
        t2.setI(10);
        t2.setS("20");
        t2.setD(new Date());
        JsonDiff jsonDiff = new Jackson2Diff();
        JsonNode node1 = objectMapper.valueToTree(t);
        JsonNode node2 = objectMapper.valueToTree(t2);
        String diff = jsonDiff.diff(objectMapper.writeValueAsString(t), objectMapper.writeValueAsString(t2));
        ObjectNode diff2 = (ObjectNode) jsonDiff.diff(node1, node2);

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
