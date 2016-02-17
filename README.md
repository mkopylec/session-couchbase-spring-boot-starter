# Session Couchbase Spring Boot Starter
[![Circle CI](https://circleci.com/gh/mkopylec/session-couchbase-spring-boot-starter.svg?style=shield)](https://circleci.com/gh/mkopylec/session-couchbase-spring-boot-starter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mkopylec/session-couchbase-spring-boot-starter/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.mkopylec/session-couchbase-spring-boot-starter)

For more info about couchbase click [here](http://www.couchbase.com/)

## Installing

```gradle
repositories {
    mavenCentral()
}
dependencies {
    compile 'com.github.mkopylec:session-couchbase-spring-boot-starter:1.0.0'
}
```

## How to use
Simply use `HttpSession` interface to control HTTP session. For example:

```java
@Controller
public class SessionController {

    @RequestMapping("uri")
    public void doSomething(HttpSession session) {
        ...
    }
}
```

The starter can be used in 2 different modes:

### Couchbase backed persistence usage
Configure couchbase connection in _application.yml_ file:

```yaml
session-couchbase.persistent:
    namespace: <application_namespace>
    hosts: <list_of_couchbase_cluster_hosts>
    bucket-name: <couchbase_bucket_name>
    password: <couchbase_bucket_password>
```

##### Additional info
Using couchbase backed HTTP session you can share session among multiple web applications.
The session will not be destroyed when the web applications will be shut down.

You can access session attributes in 2 ways:
 - _application namespace_ - attributes are visible only to instances of the same web application
 - _global namespace_ - attributes are visible to all instances of all web applications
 
To access application namespace just pass an attribute name:

```java
...
@RequestMapping("uri")
public void doSomething(HttpSession session) {
    session.setAttribute("name");
    session.getAttribute("name");
    ...
}
```

To access global attributes create an attribute name using `CouchbaseSession.globalAttributeName(...)` method:

```java
...
@RequestMapping("uri")
public void doSomething(HttpSession session) {
    String attributeName = CouchbaseSession.globalAttributeName("name");
    session.setAttribute(attributeName);
    session.getAttribute(attributeName);
    ...
}
```

### In-memory usage
Enable in-memory mode in _application.yml_ file:

```yaml
session-couchbase.in-memory.enabled: true
```

##### Additional info
Using in-memory HTTP session you can not share session among multiple web applications.
The session is visible only within a single web application instance and will be destroyed when the web application will be shut down.

There are no namespaces in in-memory mode.

## Configuration properties list

```yaml
session-couchbase:
    timeout-in-seconds: 1800 # HTTP session timeout.
    persistent:
        namespace: default # HTTP session application namespace under which session data must be stored.
        hosts: localhost # Couchbase cluster hosts.
        bucket-name: default # Couchbase bucket name where session data must be stored.
        password: # Couchbase bucket password.
    in-memory:
        enabled: false # Flag for enabling and disabling in-memory mode.
```

## Examples
Go to [sample controller](https://github.com/mkopylec/session-couchbase-spring-boot-starter/blob/master/src/test/java/com/github/mkopylec/sessioncouchbase/SessionController.java) to see more examples.

## License
Session Couchbase Spring Boot Starter is published under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
