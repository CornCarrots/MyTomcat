package servlet;

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
        try {
            response.getWriter().println("Hello Servlet");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
