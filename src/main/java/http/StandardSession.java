package http;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zerocoder
 * @Description: session
 * @Date: 2021/2/23 13:46
 */

public class StandardSession implements HttpSession {
    /**
     * 属性
     */
    private Map<String, Object> attributeMap;
    /**
     * 唯一ID
     */
    private String id;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 最后访问时间
     */
    private Long lastAccessTime;

    private ServletContext servletContext;

    private int maxActiveInterval;

    public StandardSession(String id, ServletContext servletContext) {
        this.id = id;
        this.servletContext = servletContext;
    }

    @Override
    public long getCreationTime() {
        return createTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int i) {
        this.maxActiveInterval = i;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxActiveInterval;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return (HttpSessionContext) servletContext;
    }

    @Override
    public Object getAttribute(String s) {
        return attributeMap.get(s);
    }

    @Override
    public Object getValue(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> set = attributeMap.keySet();
        return Collections.enumeration(set);
    }

    @Override
    public String[] getValueNames() {
        return new String[0];
    }

    @Override
    public void setAttribute(String s, Object o) {
        attributeMap.put(s, o);
    }

    @Override
    public void putValue(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {
        attributeMap.remove(s);
    }

    @Override
    public void removeValue(String s) {

    }

    @Override
    public void invalidate() {
        attributeMap.clear();
    }

    @Override
    public boolean isNew() {
        return createTime.equals(lastAccessTime);
    }
}
