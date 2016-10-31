# Session Couchbase Spring Boot Starter
[![Build Status](https://travis-ci.org/mkopylec/session-couchbase-spring-boot-starter.svg?branch=master)](https://travis-ci.org/mkopylec/session-couchbase-spring-boot-starter)
[![Coverage Status](https://coveralls.io/repos/github/mkopylec/session-couchbase-spring-boot-starter/badge.svg?branch=master)](https://coveralls.io/github/mkopylec/session-couchbase-spring-boot-starter?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mkopylec/session-couchbase-spring-boot-starter/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.mkopylec/session-couchbase-spring-boot-starter)

The project is based on:  
[Spring Session](http://projects.spring.io/spring-session/)  
[Spring Data Couchbase](http://projects.spring.io/spring-data-couchbase/)  

The project supports only Couchbase 4 and higher versions. For more information about Couchbase click [here](http://www.couchbase.com/).

## Installing

```gradle
repositories {
    mavenCentral()
}
dependencies {
    compile 'com.github.mkopylec:session-couchbase-spring-boot-starter:1.2.2'
}
```

## How to use
Create a Spring Boot web application:

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Simply use `HttpSession` interface to control HTTP session. For example:

```java
@Controller
public class SessionController {

    @GetMapping("uri")
    public void doSomething(HttpSession session) {
        ...
    }
}
```

The starter can be used in 2 different modes:

### Couchbase backed persistence usage
Configure Couchbase connection in _application.yml_ file using Session Couchbase and Spring Data Couchbase properties:

```yaml
session-couchbase.persistent:
    namespace: <application_namespace>

spring.couchbase:
  bootstrap-hosts: <list_of_couchbase_cluster_hosts>
  bucket:
    name: <couchbase_bucket_name>
    password: <couchbase_bucket_password>
```

For full list of supported Spring Data Couchbase properties see [here](http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html).

##### Additional info
Using Couchbase backed HTTP session you can share session among multiple web applications.
The session will not be destroyed when the web applications will be shut down.

You can access session attributes in 2 ways:
 - _application namespace_ - attributes are visible only to instances of the same web application within a distributed system
 - _global namespace_ - attributes are visible to all instances of all web applications within a distributed system
 
To access application namespace just pass an attribute name:

```java
...
@RequestMapping("uri")
public void doSomething(HttpSession session) {
    String attributeName = "name";
    session.setAttribute(attributeName, "value");
    session.getAttribute(attributeName);
    ...
}
```

To access global attributes create an attribute name using `CouchbaseSession.globalAttributeName(...)` method:

```java
...
@RequestMapping("uri")
public void doSomething(HttpSession session) {
    String attributeName = CouchbaseSession.globalAttributeName("name");
    session.setAttribute(attributeName, "value");
    session.getAttribute(attributeName);
    ...
}
```

When changing HTTP session ID every attribute is copied to the new session, no matter what namespace it belongs.

### In-memory usage
Enable in-memory mode in _application.yml_ file:

```yaml
session-couchbase.in-memory.enabled: true
```

##### Additional info
Using in-memory HTTP session you can not share session among multiple web applications.
The session is visible only within a single web application instance and will be destroyed when the web application will be shut down.

There are no namespaces in the in-memory mode.

The mode is useful for integration tests when you don't want to communicate with the real Couchbase server instance.

## Configuration properties list

```yaml
session-couchbase:
    timeout-in-seconds: 1800 # HTTP session timeout.
    persistent:
        namespace: # HTTP session application namespace under which session data must be stored.
        principal-sessions:
            enabled: false # Flag for enabling and disabling finding HTTP sessions by principal. Can significantly decrease application performance when enabled.
    in-memory:
        enabled: false # Flag for enabling and disabling in-memory mode.
```

## Examples
Go to [sample controller](https://github.com/mkopylec/session-couchbase-spring-boot-starter/blob/master/src/test/java/com/github/mkopylec/sessioncouchbase/SessionController.java) to see more examples.

## License
Session Couchbase Spring Boot Starter is published under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
