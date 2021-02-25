package http;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.*;

/**
 * @Author: zerocoder
 * @Description: 拦截器配置
 * @Date: 2021/2/25 13:08
 */

public class StandardFilterConfig implements FilterConfig {

    private ServletContext servletContext;

    private Map<String, String> initParameters;

    private String name;

    public StandardFilterConfig(ServletContext servletContext, Map<String, String> initParameters, String name) {
        this.servletContext = servletContext;
        this.initParameters = initParameters;
        this.name = name;
        if (null == initParameters){
            this.initParameters = new HashMap<>();
        }
    }

    @Override
    public String getFilterName() {
        return name;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return initParameters.get(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        Set<String> set = initParameters.keySet();
        return Collections.enumeration(set);
    }
}
