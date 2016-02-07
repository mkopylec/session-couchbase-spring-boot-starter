package com.github.mkopylec.sessioncouchbase

import org.couchbase.mock.CouchbaseMock
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

import static org.springframework.http.HttpHeaders.COOKIE
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.GET

@WebIntegrationTest(randomPort = true)
@ContextConfiguration(loader = SpringApplicationContextLoader, classes = TestApplication)
abstract class BasicSpec extends Specification {

    @Shared
    private RestTemplate restTemplate = new RestTemplate();
    @Shared
    private CouchbaseMock couchbase = new CouchbaseMock('localhost', 8091, 1, 1)
    @Autowired
    private EmbeddedWebApplicationContext context
    @Autowired
    private SessionCouchbaseProperties sessionCouchbase
    private ThreadLocal<String> cookies = new ThreadLocal<>()

    void setupSpec() {
        couchbase.start()
    }

    protected int getSessionTimeout() {
        return sessionCouchbase.timeoutInSeconds * 1000
    }

    protected void setSessionAttribute(Message attribute) {
        post('session/attribute', attribute)
    }

    protected void deleteSessionAttribute() {
        delete('session/attribute')
    }

    protected ResponseEntity<Message> getSessionAttribute() {
        return get('session/attribute', Message)
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

    private void post(String path, Object body) {
        def url = createUrl(path)
        HttpHeaders headers = addSessionCookie()
        def request = new HttpEntity<>(body, headers)
        def response = restTemplate.postForEntity(url, request, Object)
        saveSessionCookie(response)
    }

    private <T> ResponseEntity<T> get(String path, Class<T> responseType) {
        def url = createUrl(path)
        HttpHeaders headers = addSessionCookie()
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(url, GET, request, responseType) as ResponseEntity<T>
        saveSessionCookie(response)
        return response
    }

    private void delete(String path) {
        def url = createUrl(path)
        HttpHeaders headers = addSessionCookie()
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(url, DELETE, request, Object)
        saveSessionCookie(response)
    }

    private GString createUrl(String path) {
        "http://localhost:$context.embeddedServletContainer.port/$path"
    }

    private HttpHeaders addSessionCookie() {
        HttpHeaders headers = new HttpHeaders()
        headers.set(COOKIE, cookies.get())
        return headers
    }

    private saveSessionCookie(ResponseEntity response) {
        cookies.set(response.headers.get('Set-Cookie') as String)
    }

    void cleanup() {
        cookies.remove()
    }

    void cleanupSpec() {
        couchbase.stop()
    }
}
