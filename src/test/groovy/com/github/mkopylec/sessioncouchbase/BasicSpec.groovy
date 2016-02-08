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

import static org.springframework.boot.SpringApplication.run
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
    private EmbeddedWebApplicationContext extraInstanceContext
    @Autowired
    private SessionCouchbaseProperties sessionCouchbase
    private ThreadLocal<Map<Integer, String>> cookies = new ThreadLocal<>()

    void setupSpec() {
        couchbase.start()
    }

    protected void startExtraApplicationInstance(String namespace) {
        def extraInstancePort = getExtraInstancePort()
        extraInstanceContext = (EmbeddedWebApplicationContext) run(TestApplication, "--server.port=$extraInstancePort --session-couchbase.persistent.namespace=$namespace")
    }

    protected int getSessionTimeout() {
        return sessionCouchbase.timeoutInSeconds * 1000
    }

    protected void setSessionAttribute(Message attribute) {
        post('session/attribute', attribute, getPort())
    }

    protected void setSessionAttributeToExtraInstance(Message attribute) {
        post('session/attribute', attribute, getExtraInstancePort())
    }

    protected void deleteSessionAttribute() {
        delete('session/attribute', getPort())
    }

    protected void deleteSessionAttributeInExtraInstance() {
        delete('session/attribute', getExtraInstancePort())
    }

    protected ResponseEntity<Message> getSessionAttribute() {
        return get('session/attribute', Message, getPort())
    }

    protected ResponseEntity<Message> getSessionAttributeFromExtraInstance() {
        return get('session/attribute', Message, getExtraInstancePort())
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

    private void post(String path, Object body, int port = getPort()) {
        def url = createUrl(path, port)
        HttpHeaders headers = addSessionCookie(port)
        def request = new HttpEntity<>(body, headers)
        def response = restTemplate.postForEntity(url, request, Object)
        saveSessionCookie(response, port)
    }

    private <T> ResponseEntity<T> get(String path, Class<T> responseType, int port = getPort()) {
        def url = createUrl(path, port)
        HttpHeaders headers = addSessionCookie(port)
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(url, GET, request, responseType) as ResponseEntity<T>
        saveSessionCookie(response, port)
        return response
    }

    private void delete(String path, int port = getPort()) {
        def url = createUrl(path, port)
        HttpHeaders headers = addSessionCookie(port)
        def request = new HttpEntity<>(headers)
        def response = restTemplate.exchange(url, DELETE, request, Object)
        saveSessionCookie(response, port)
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

    private HttpHeaders addSessionCookie(int port) {
        def headers = new HttpHeaders()
        if (cookies.get() != null) {
            headers.set(COOKIE, cookies.get().get(port))
        }
        return headers
    }

    private saveSessionCookie(ResponseEntity response, int port) {
        if (cookies.get() == null) {
            cookies.set(new HashMap<Integer, String>())
        }
        cookies.get().put(port, response.headers.get('Set-Cookie') as String)
    }

    void cleanup() {
        cookies.remove()
    }

    void cleanupSpec() {
        couchbase.stop()
    }
}
