package catalina;

import cn.hutool.core.util.ArrayUtil;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;

/**
 * @Author: zerocoder
 * @Description: 过滤器链条
 * @Date: 2021/2/25 14:37
 */

public class ApplicationFilterChain implements FilterChain {

    private Filter[] filters;

    private Servlet servlet;

    private int index;

    public ApplicationFilterChain(List<Filter> filters, Servlet servlet) {
        this.filters = ArrayUtil.toArray(filters, Filter.class);
        this.servlet = servlet;
        this.index = 0;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
        if (index < filters.length){
            Filter filter = filters[index++];
            filter.doFilter(servletRequest, servletResponse, this);
        }else {
            servlet.service(servletRequest, servletResponse);
        }
    }
}
