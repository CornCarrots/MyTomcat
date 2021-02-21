package http;

import cn.hutool.core.collection.CollectionUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.*;

/**
 * @Author: zerocoder
 * @Description: servlet配置对象
 * @Date: 2021/2/21 17:11
 */

public class StandardServletConfig implements ServletConfig {

    private ServletContext servletContext;

    private Map<String, String> initParameters;

    private String servletName;

    public StandardServletConfig(ServletContext servletContext, Map<String, String> initParameters, String servletName) {
        this.servletContext = servletContext;
        this.servletName = servletName;
        if (CollectionUtil.isEmpty(initParameters)){
            this.initParameters = new HashMap<>();
        }
        else{
            this.initParameters = initParameters;
        }
    }

    @Override
    public String getServletName() {
        return servletName;
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
