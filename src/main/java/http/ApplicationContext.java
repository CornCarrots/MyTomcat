package http;

import catalina.Context;

import java.io.File;
import java.util.*;

/**
 * @Author: zerocoder
 * @Description: servlet应用上下文
 * @Date: 2021/2/21 16:53
 */

public class ApplicationContext extends BaseServletContext {

    private Context context;

    private Map<String, Object> attributesMap;

    public ApplicationContext(Context context) {
        this.context = context;
        this.attributesMap = new HashMap<>();
    }

    @Override
    public Object getAttribute(String s) {
        return attributesMap.get(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> set = attributesMap.keySet();
        return Collections.enumeration(set);
    }

    @Override
    public void setAttribute(String s, Object o) {
        attributesMap.put(s, o);
    }

    @Override
    public void removeAttribute(String s) {
        attributesMap.remove(s);
    }

    @Override
    public String getRealPath(String s) {
        return new File(context.getDocBase(), s).getAbsolutePath();
    }
}
