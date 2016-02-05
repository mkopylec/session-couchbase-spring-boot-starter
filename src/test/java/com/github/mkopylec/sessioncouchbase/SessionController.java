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
    public void setAttribute(@RequestBody Object attribute, HttpSession session) {
        session.setAttribute(SESSION_ATTRIBUTE_NAME, attribute);
    }

    @RequestMapping(value = "attribute", method = GET)
    public Object setAttribute(HttpSession session) {
        return session.getAttribute(SESSION_ATTRIBUTE_NAME);
    }

    @RequestMapping(value = "bean", method = POST)
    public void setBean(@RequestBody SessionScopedBean bean) {
        sessionBean.setText(bean.getText());
        sessionBean.setNumber(bean.getNumber());
    }

    @RequestMapping(value = "bean", method = GET)
    public SessionScopedBean getBean() {
        return sessionBean;
    }

    @RequestMapping(method = DELETE)
    public void invalidateSession(HttpSession session) {
        session.invalidate();
    }
}
