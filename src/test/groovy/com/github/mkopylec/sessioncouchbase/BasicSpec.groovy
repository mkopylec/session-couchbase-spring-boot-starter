package com.github.mkopylec.sessioncouchbase

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import spock.lang.Shared
import spock.lang.Specification

import static java.net.HttpCookie.parse
import static org.springframework.boot.SpringApplication.run
import static org.springframework.http.HttpHeaders.COOKIE
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpMethod.PUT

@WebIntegrationTest(randomPort = true)
@ContextConfiguration(loader = SpringApplicationContextLoader, classes = TestApplication)
abstract class BasicSpec extends Specification {

    @Shared
    private RestTemplate restTemplate = new RestTemplate()
    @Autowired
    private EmbeddedWebApplicationContext context
    @Shared
    private EmbeddedWebApplicationContext extraInstanceContext
    @Autowired
    private SessionCouchbaseProperties sessionCouchbase
    // Cannot store cookie in thread local because some tests starts more than one app instance. CANNOT run tests in parallel.
    private String currentSessionCookie

    protected void startExtraApplicationInstance(String namespace = sessionCouchbase.persistent.namespace) {
        extraInstanceContext = (EmbeddedWebApplicationContext) run(TestApplication, "--server.port=0", "--session-couchbase.persistent.namespace=$namespace")
    }

    protected int getSessionTimeout() {
        return sessionCouchbase.timeoutInSeconds * 1000
    }

    protected String getCurrentSessionId() {
        return parse(currentSessionCookie)[0].value
    }

    protected void setSessionAttribute(Message attribute) {
        post('session/attribute', attribute, getPort())
    }

    protected void setGlobalSessionAttribute(Message attribute) {
        post('session/attribute/global', attribute, getPort())
    }

    protected void setSessionAttributeToExtraInstance(Message attribute) {
        post('session/attribute', attribute, getExtraInstancePort())
    }

    protected void deleteSessionAttribute() {
        delete('session/attribute', getPort())
    }

    protected void deleteGlobalSessionAttribute() {
        delete('session/attribute/global', getPort())
    }

    protected ResponseEntity<Message> getSessionAttribute() {
        return get('session/attribute', Message, getPort())
    }

    protected ResponseEntity<Message> getSessionAttributeFromExtraInstance() {
        return get('session/attribute', Message, getExtraInstancePort())
    }

    protected ResponseEntity<Message> getGlobalSessionAttributeFromExtraInstance() {
        return get('session/attribute/global', Message, getExtraInstancePort())
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
        return post('session/principal', null, getExtraInstancePort(), String).body
    }

    protected ResponseEntity<Set<String>> getPrincipalSessions() {
        return get('session/principal', Set, getPort())
    }

    protected void clearSessionCookie() {
        currentSessionCookie = null
    }

    private <T> ResponseEntity<T> post(String path, Object body, int port = getPort(), Class<T> responseType = Object) {
        def url = createUrl(path, port)
        HttpHeaders headers = addSessionCookie()
        def request = new HttpEntity<>(body, headers)
        def response = restTemplate.postForEntity(url, request, responseType)
        saveSessionCookie(response)
        return response
    }

    private <T> ResponseEntity<T> get(String path, Class<T> responseType, int port = getPort()) {
        def url = createUrl(path, port)
        HttpHeaders headers = addSessionCookie()
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(url, GET, request, responseType) as ResponseEntity<T>
        saveSessionCookie(response)
        return response
    }

    private void delete(String path, int port = getPort()) {
        def url = createUrl(path, port)
        HttpHeaders headers = addSessionCookie()
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(url, DELETE, request, Object)
        saveSessionCookie(response)
    }

    private ResponseEntity<Object> put(String path, int port = getPort()) {
        def url = createUrl(path, port)
        HttpHeaders headers = addSessionCookie()
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(url, PUT, request, Object)
        saveSessionCookie(response)
        return response
    }

    private static GString createUrl(String path, int port) {
        return "http://localhost:$port/$path"
    }

    private int getPort() {
        return context.embeddedServletContainer.port
    }

    private int getExtraInstancePort() {
        return extraInstanceContext.embeddedServletContainer.port
    }

    private HttpHeaders addSessionCookie() {
        def headers = new HttpHeaders()
        headers.set(COOKIE, currentSessionCookie)
        return headers
    }

    private void saveSessionCookie(ResponseEntity response) {
        def cookie = response.headers.get('Set-Cookie')
        if (cookie != null) {
            currentSessionCookie = cookie;
        }
    }

    void cleanup() {
        clearSessionCookie()
    }
}
