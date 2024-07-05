package util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpSession;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class CustomSession {
//    Map<String, Object> values;
    private HttpSession session;

    public Object get(String key) {
        return session.getAttribute(key);
    }

    public void add(String key, Object value) {
        session.setAttribute(key, value);
    }

    public void update(String key, Object value) {
        session.setAttribute(key, value);
    }

    public void delete(String key) {
        session.removeAttribute(key);
    }

//    public Object get(String key) {
//        return values.get(key);
//    }
//
//    public void add(String key, Object value) {
//        values.put(key, value);
//    }
//
//    public void update(String key, Object value) {
//        values.put(key, value);
//    }
//
//    public void delete(String key) {
//        values.remove(key);
//    }

    public CustomSession(HttpSession session) {
        this.session = session;
    }

//    public CustomSession(Map<String, Object> values) {
//        this.values = values;
//    }
//
//    public void fromHttpSession(HttpSession session) throws ServletException {
//
//        Enumeration<String> keys = session.getAttributeNames();
//        while (keys.hasMoreElements()) {
//            String key = keys.nextElement();
//            this.add(key ,session.getAttribute(key));
//        }
//
//    }
//
//    public void toHttpSession(HttpSession session) throws ServletException {
//        Enumeration<String> keys = session.getAttributeNames();
//        while(keys.hasMoreElements()) {
//            String key = keys.nextElement();
//            session.removeAttribute(key);
//        }
//
//        values.forEach((k,v) -> {
//            session.setAttribute(k,v);
//        });
//
//    }
}
