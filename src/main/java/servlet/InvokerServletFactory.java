package servlet;

import catalina.Context;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import http.Request;
import http.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: zerocoder
 * @Description: 动态加载Servlet
 * @Date: 2021/2/15 12:33
 */

public class InvokerServletFactory extends HttpServlet {

    private static InvokerServletFactory invokerServlet = new InvokerServletFactory();

    private InvokerServletFactory() {
    }

    public static InvokerServletFactory getInstance() {
        return invokerServlet;
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) {
        try {
            Request request = (Request) req;
            Response response = (Response) resp;
            // 获取类
            String uri = request.getUri();
            Context context = request.getContext();
            String servletClass = context.getServletClassByUrl(uri);
            // 加载类
            Class<?> servletClazz = context.getClassLoader().loadClass(servletClass);
            Object servlet = context.getServletByPool(servletClazz);
            // 使用类
            ReflectUtil.invoke(servlet, "service", request, response);
            if (StrUtil.isNotEmpty(response.getRedirectPath())){
                response.setStatus(HttpServletResponse.SC_FOUND);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
//        Object instance = ReflectUtil.newInstance(servletClass);
//        ReflectUtil.invoke(instance, "service", request, response);
    }
}
