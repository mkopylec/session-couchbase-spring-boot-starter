package com.github.mkopylec.sessioncouchbase

import com.couchbase.client.java.query.N1qlQueryResult
import com.github.mkopylec.sessioncouchbase.configuration.SessionCouchbaseProperties
import com.github.mkopylec.sessioncouchbase.data.SessionDao
import com.github.mkopylec.sessioncouchbase.utils.ApplicationInstance
import com.github.mkopylec.sessioncouchbase.utils.ApplicationInstanceRunner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.data.couchbase.core.CouchbaseTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.client.RestTemplate
import spock.lang.Shared
import spock.lang.Specification

import static com.couchbase.client.java.query.N1qlQuery.simple
import static com.github.mkopylec.sessioncouchbase.SessionController.PRINCIPAL_NAME
import static java.net.HttpCookie.parse
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpHeaders.COOKIE
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpMethod.PUT

@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class BasicSpec extends Specification {

    private static boolean bucketIndexCreated = false

    @Shared
    private RestTemplate rest = new RestTemplate()
    @Shared
    private ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor()
    @Autowired
    private Environment environment
    @Autowired(required = false)
    private CouchbaseTemplate template
    @Autowired
    private SessionDao sessionDao
    @Autowired
    private SessionEventConsumer eventConsumer
    @Autowired
    private EmbeddedWebApplicationContext context
    private int extraInstancePort
    private ApplicationInstance instance
    @Autowired
    private CouchbaseProperties couchbase
    @Autowired
    private SessionCouchbaseProperties sessionCouchbase
    // Cannot store cookie in thread local because some tests starts more than one app instance. CANNOT run tests in parallel.
    private String currentSessionCookie

    void setup() {
        clearEventsAssertions()
        createBucketIndex()
        clearSessions()
    }

    void cleanup() {
        clearSessionCookie()
        stopExtraApplicationInstance()
    }

    protected void startExtraApplicationInstance(String... activeProfiles) {
        URL[] urls = [new File('/build/classes/test').toURI().toURL()]
        def classLoader = new URLClassLoader(urls, getClass().classLoader)
        def runnerClass = classLoader.loadClass(ApplicationInstanceRunner.class.name)
        def runnerInstance = runnerClass.newInstance()
        instance = new ApplicationInstance(runnerClass, runnerInstance)
        def profiles = activeProfiles ? activeProfiles + environment.activeProfiles : environment.activeProfiles
        runnerClass.getMethod('setActiveProfiles', String).invoke(runnerInstance, profiles.join(','))
        runnerClass.getMethod('run').invoke(runnerInstance)
        extraInstancePort = runnerClass.getMethod('getPort').invoke(runnerInstance) as int
    }

    protected void stopExtraApplicationInstance() {
        if (instance) {
            instance.runnerClass.getMethod('stop').invoke(instance.runnerInstance)
            instance = null
        }
    }

    protected boolean currentSessionExists() {
        return sessionDao.exists(getCurrentSessionId())
    }

    protected boolean currentPrincipalSessionsExists() {
        return sessionDao.exists(PRINCIPAL_NAME)
    }

    protected int getSessionTimeout() {
        return sessionCouchbase.timeoutInSeconds * 1000
    }

    protected void executeConcurrently(Closure operation) {
        initExecutor()
        def futures = []
        for (int i = 0; i < 100; i++) {
            def future = executor.submit(new Runnable() {

                @Override
                void run() {
                    operation()
                }
            })
            futures.add(future)
        }
        futures.each { it -> it.get() }
    }

    protected void setSessionAttribute(Message attribute) {
        post('session/attribute', attribute, getPort())
    }

    protected void setGlobalSessionAttribute(Message attribute) {
        post('session/attribute/global', attribute, getPort())
    }

    protected void setSessionAttributeToExtraInstance(Message attribute) {
        post('session/attribute', attribute, extraInstancePort)
    }

    protected void deleteSessionAttribute() {
        delete('session/attribute', getPort())
    }

    protected void deleteGlobalSessionAttribute() {
        delete('session/attribute/global', getPort())
    }

    protected void setAndRemoveSessionAttribute(Message attribute) {
        put('session/attribute', getPort(), attribute)
    }

    protected ResponseEntity<Message> getSessionAttribute() {
        return get('session/attribute', Message, getPort())
    }

    protected ResponseEntity<Message> getSecondSessionAttribute() {
        return get('session/attribute/second', Message, getPort())
    }

    protected ResponseEntity<Message> getSessionAttributeFromExtraInstance() {
        return get('session/attribute', Message, extraInstancePort)
    }

    protected ResponseEntity<Message> getGlobalSessionAttribute() {
        return get('session/attribute/global', Message, getPort())
    }

    protected ResponseEntity<Message> getGlobalSessionAttributeFromExtraInstance() {
        return get('session/attribute/global', Message, extraInstancePort)
    }

    protected void setSessionBean(Message attribute) {
        post('session/bean', attribute)
    }

    protected ResponseEntity<Message> getSessionBean() {
        return get('session/bean', Message)
    }

    protected void invalidateSession() {
        delete('session')
    }

    protected void changeSessionId() {
        put('session/id')
    }

    protected String setPrincipalSessionAttribute() {
        return post('session/principal', null, getPort(), String).body
    }

    protected String setPrincipalSessionAttributeToExtraInstance() {
        return post('session/principal', null, extraInstancePort, String).body
    }

    protected ResponseEntity<List<String>> getPrincipalSessions() {
        return get('session/principal', List, getPort())
    }

    protected ResponseEntity<List<String>> getSessionAttributeNames() {
        return get('session/attribute/names', List, getPort())
    }

    protected void clearSessionCookie() {
        currentSessionCookie = null
    }

    protected void clearSessions() {
        sessionDao.deleteAll()
    }

    protected boolean sessionCreatedEventSent() {
        return eventConsumer.sessionCreated
    }

    protected boolean sessionExpiredEventSent() {
        return eventConsumer.sessionExpired
    }

    protected boolean sessionDeletedEventSent() {
        return eventConsumer.sessionDeleted
    }

    private void createBucketIndex() {
        if (!bucketIndexCreated && template) {
            def result = template.queryN1QL(simple('SELECT * FROM system:indexes'))
            failOnErrors(result)
            if (result.allRows().empty) {
                result = template.queryN1QL(simple("CREATE PRIMARY INDEX ON $couchbase.bucket.name USING GSI"))
                failOnErrors(result)
            }
            bucketIndexCreated = true
        }
    }

    private void clearEventsAssertions() {
        eventConsumer.resetAssertions()
    }

    private void initExecutor() {
        executor.queueCapacity = 1
        executor.corePoolSize = 200
        executor.initialize()
    }

    private static void failOnErrors(N1qlQueryResult result) {
        if (!result.finalSuccess() || isNotEmpty(result.errors())) {
            throw new RuntimeException(result.errors().toString())
        }
    }

    private String getCurrentSessionId() {
        return parse(currentSessionCookie)[0].value
    }

    private <T> ResponseEntity<T> post(String path, Object body, int port = getPort(), Class<T> responseType = Object) {
        def url = createUrl(path, port)
        HttpHeaders headers = addSessionCookie()
        def request = new HttpEntity<>(body, headers)
        def response = rest.postForEntity(url, request, responseType)
        saveSessionCookie(response)
        return response
    }

    private <T> ResponseEntity<T> get(String path, Class<T> responseType, int port = getPort()) {
        def url = createUrl(path, port)
        HttpHeaders headers = addSessionCookie()
        def request = new HttpEntity<>(headers)
        def response = rest.exchange(url, GET, request, responseType) as ResponseEntity<T>
        saveSessionCookie(response)
        return response
    }

    private void delete(String path, int port = getPort()) {
        def url = createUrl(path, port)
        HttpHeaders headers = addSessionCookie()
        def request = new HttpEntity<>(headers)
        def response = rest.exchange(url, DELETE, request, Object)
        saveSessionCookie(response)
    }

    private void put(String path, int port = getPort(), Object body = null) {
        def url = createUrl(path, port)
        HttpHeaders headers = addSessionCookie()
        def request = new HttpEntity<>(body, headers)
        def response = rest.exchange(url, PUT, request, Object)
        saveSessionCookie(response)
    }

    private static GString createUrl(String path, int port) {
        return "http://localhost:$port/$path"
    }

    private int getPort() {
        return context.embeddedServletContainer.port
    }

    private HttpHeaders addSessionCookie() {
        def headers = new HttpHeaders()
        headers.set(COOKIE, currentSessionCookie)
        return headers
    }

    private void saveSessionCookie(ResponseEntity response) {
        def cookiesHeader = response.headers.get('Set-Cookie')
        if (cookiesHeader == null) {
            return
        }
        def cookieHeader = cookiesHeader.find { it -> it.contains('SESSION') }
        if (cookieHeader == null) {
            return
        }
        def cookie = parse(cookieHeader)[0]
        if (cookie != null) {
            currentSessionCookie = cookie.toString()
        }
    }
}
