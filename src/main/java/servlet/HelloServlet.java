package servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: zerocoder
 * @Description:
 * @Date: 2021/2/14 19:02
 */

public class HelloServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        System.out.println(this + " get方法");
        try {
            response.getWriter().println("Hello Servlet");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HelloServlet() {
        System.out.println(this + " 构造方法");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        String author = config.getInitParameter("author");
        System.out.println(this + " 初始化");
        System.out.println("获取参数：" + author);
    }

    @Override
    public void destroy() {
        System.out.println(this + " 销毁了");
        super.destroy();
    }
}
