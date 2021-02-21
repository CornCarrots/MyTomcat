package catalina;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import http.Request;
import http.Response;
import servlet.DefaultServlet;
import servlet.InvokerServlet;
import util.Constant;
import util.XmlUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @Author: zerocoder
 * @Description: http请求处理器
 * @Date: 2021/2/14 18:54
 */

public class HttpProcessor {
    public static boolean execute(Socket socket, Request request, Response response) throws IOException {
        try {
            Context context = request.getContext();
            String uri = request.getUri();
            if (StrUtil.isEmpty(uri) || context == null) {
                return false;
            }
            // 欢迎页
            if (Constant.SEPARATOR.equals(uri)){
                uri = XmlUtil.getWelcomeFile(request.getContext());
            }
            String servletClass = context.getServletClassByUrl(uri);
            if (StrUtil.isNotEmpty(servletClass)){
                Class<?> servletClazz = context.getClassLoader().loadClass(servletClass);
//                Object servlet = ReflectUtil.newInstance(servletClazz);
                Object servlet = context.getServletByPool(servletClazz);
                ReflectUtil.invoke(servlet, "service", request, response);
//                InvokerServlet.newInstance().service(request, response);
//                Object servlet = ReflectUtil.newInstance(servletClass);
//                ReflectUtil.invoke(servlet, "doGet", request, response);
            }else {
                DefaultServlet.getInstance().service(request, response);
                if (response.getStatus() == HttpServletResponse.SC_NOT_FOUND){
                    response = Response.notFound(response.getDesc());
                }else if (response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR){
                    response = Response.error(response.getDesc());
                }
            }
            sendResponse(socket, response);
            return true;
        } catch (Exception e) {
            LogFactory.get().error(e);
            e.printStackTrace();
            sendResponse(socket, Response.error(e.getMessage()));
            return false;
        }
    }

    private static void sendResponse(Socket socket, Response response) throws IOException {
        String headerStr = StrUtil.format(Constant.RESPONSE_HEAD, response.getCode(), response.getDesc(), response.getMimeType());
        byte[] header = headerStr.getBytes();
        byte[] body = response.getBody();
        byte[] responseBytes = new byte[header.length + body.length];
        ArrayUtil.copy(header, responseBytes, header.length);
        ArrayUtil.copy(body, 0, responseBytes, header.length, body.length);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(responseBytes);
    }

}
