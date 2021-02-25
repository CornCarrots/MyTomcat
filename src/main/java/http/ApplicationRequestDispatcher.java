package http;

import catalina.HttpProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @Author: zerocoder
 * @Description: 服务端跳转
 * @Date: 2021/2/24 23:58
 */

public class ApplicationRequestDispatcher implements RequestDispatcher {
    private String uri;

    public ApplicationRequestDispatcher(String uri) {
        if (!uri.startsWith("/")){
            uri = "/" + uri;
        }
        this.uri = uri;
    }

    @Override
    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        Request request = (Request) servletRequest;
        Response response = (Response) servletResponse;
        request.setUri(uri);
        response.resetBuffer();
        HttpProcessor.execute(request.getSocket(), request, response);
        request.setForwarded(true);
    }

    @Override
    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {

    }
}
