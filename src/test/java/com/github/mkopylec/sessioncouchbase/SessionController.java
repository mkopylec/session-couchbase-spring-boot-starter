package com.github.mkopylec.sessioncouchbase;

import com.github.mkopylec.sessioncouchbase.persistent.CouchbaseSession;
import com.github.mkopylec.sessioncouchbase.persistent.CouchbaseSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.mkopylec.sessioncouchbase.persistent.CouchbaseSession.globalAttributeName;
import static java.util.Collections.list;
import static org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME;

@RestController
@RequestMapping("session")
public class SessionController {

    private static final String PRINCIPAL_NAME = "user";
    private static final String SESSION_ATTRIBUTE_NAME = "attribute";
    private static final String SECOND_SESSION_ATTRIBUTE_NAME = "attribute2";

    @Autowired(required = false)
    private SessionScopedBean sessionBean;
    @Autowired(required = false)
    private CouchbaseSessionRepository sessionRepository;

    @PostMapping("attribute")
    public void setAttribute(@RequestBody Message dto, HttpSession session) {
        session.setAttribute(SESSION_ATTRIBUTE_NAME, dto);
    }

    @PostMapping("attribute/global")
    public void setGlobalAttribute(@RequestBody Message dto, HttpSession session) {
        session.setAttribute(globalAttributeName(SESSION_ATTRIBUTE_NAME), dto);
    }

    @GetMapping("attribute")
    public Object getAttribute(HttpSession session) {
        return session.getAttribute(SESSION_ATTRIBUTE_NAME);
    }

    @GetMapping("attribute/global")
    public Object getGlobalAttribute(HttpSession session) {
        return session.getAttribute(globalAttributeName(SESSION_ATTRIBUTE_NAME));
    }

    @DeleteMapping("attribute")
    public void deleteAttribute(HttpSession session) {
        session.removeAttribute(SESSION_ATTRIBUTE_NAME);
    }

    @DeleteMapping("attribute/global")
    public void deleteGlobalAttribute(HttpSession session) {
        session.removeAttribute(globalAttributeName(SESSION_ATTRIBUTE_NAME));
    }

    @PutMapping("attribute")
    public void setAndRemoveAttributes(HttpSession session) {
        session.setAttribute(SECOND_SESSION_ATTRIBUTE_NAME, "second");
        session.removeAttribute(SESSION_ATTRIBUTE_NAME);
    }

    @PostMapping("bean")
    public void setBean(@RequestBody Message dto) {
        sessionBean.setText(dto.getText());
        sessionBean.setNumber(dto.getNumber());
    }

    @GetMapping("bean")
    public Message getBean() {
        Message message = new Message();
        message.setText(sessionBean.getText());
        message.setNumber(sessionBean.getNumber());
        return message;
    }

    @DeleteMapping
    public void invalidateSession(HttpSession session) {
        session.invalidate();
    }

    @PutMapping("id")
    public void changeSessionId(HttpServletRequest request) {
        request.changeSessionId();
    }

    @PostMapping("principal")
    public String setPrincipalAttribute(HttpSession session) {
        session.setAttribute(PRINCIPAL_NAME_INDEX_NAME, PRINCIPAL_NAME);
        return session.getId();
    }

    @GetMapping("principal")
    public Set<String> getPrincipalSessions() {
        Map<String, CouchbaseSession> sessionsById = sessionRepository.findByIndexNameAndIndexValue(PRINCIPAL_NAME_INDEX_NAME, PRINCIPAL_NAME);
        return sessionsById.keySet();
    }

    @GetMapping("attribute/names")
    public List<String> getAttributeNames(HttpSession session) {
        return list(session.getAttributeNames());
    }
}
