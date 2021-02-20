package servlet;

import catalina.Context;
import cn.hutool.core.util.ReflectUtil;
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

public class InvokerServlet extends HttpServlet {

    private static InvokerServlet invokerServlet = new InvokerServlet();

    private InvokerServlet(){}

    public static InvokerServlet newInstance(){
        return invokerServlet;
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Request request = (Request) req;
        Response response = (Response) resp;
        String uri = request.getUri();
        Context context = request.getContext();
        String servletClass = context.getServletClassByUrl(uri);
        Object instance = ReflectUtil.newInstance(servletClass);
        ReflectUtil.invoke(instance, "service", request, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
