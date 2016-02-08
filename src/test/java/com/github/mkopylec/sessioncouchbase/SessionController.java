package com.github.mkopylec.sessioncouchbase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("session")
public class SessionController {

    private static final String SESSION_ATTRIBUTE_NAME = "attribute";

    @Autowired
    private SessionScopedBean sessionBean;

    @RequestMapping(value = "attribute", method = POST)
    public void setAttribute(@RequestBody Message dto, HttpSession session) {
        session.setAttribute(SESSION_ATTRIBUTE_NAME, dto);
    }

    @RequestMapping(value = "attribute", method = GET)
    public Object getAttribute(HttpSession session) {
        return session.getAttribute(SESSION_ATTRIBUTE_NAME);
    }

    @RequestMapping(value = "attribute", method = DELETE)
    public void deleteAttribute(HttpSession session) {
        session.removeAttribute(SESSION_ATTRIBUTE_NAME);
    }

    @RequestMapping(value = "bean", method = POST)
    public void setBean(@RequestBody Message dto) {
        sessionBean.setText(dto.getText());
        sessionBean.setNumber(dto.getNumber());
    }

    @RequestMapping(value = "bean", method = GET)
    public Message getBean() {
        Message message = new Message();
        message.setText(sessionBean.getText());
        message.setNumber(sessionBean.getNumber());
        return message;
    }

    @RequestMapping(method = DELETE)
    public void invalidateSession(HttpSession session) {
        session.invalidate();
    }
}
