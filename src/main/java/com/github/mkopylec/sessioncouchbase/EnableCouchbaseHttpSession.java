package com.github.mkopylec.sessioncouchbase;

import com.github.mkopylec.sessioncouchbase.inmemory.InMemoryConfiguration;
import com.github.mkopylec.sessioncouchbase.persistent.PersistentConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
@EnableSpringHttpSession
@Import({PersistentConfiguration.class, InMemoryConfiguration.class})
public @interface EnableCouchbaseHttpSession {

}
