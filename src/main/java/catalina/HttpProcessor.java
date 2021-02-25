package catalina;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.LogFactory;
import http.Request;
import http.Response;
import servlet.DefaultServlet;
import servlet.InvokerServletFactory;
import servlet.JspServletFactory;
import util.Constant;
import util.SessionManager;
import util.XmlUtil;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * @Author: zerocoder
 * @Description: http请求处理器
 * @Date: 2021/2/14 18:54
 */

public class HttpProcessor {
    public static boolean execute(Socket socket, Request request, Response response) throws IOException {
        boolean isGizp = false;
        try {
            Context context = request.getContext();
            String uri = request.getUri();
            if (StrUtil.isEmpty(uri) || context == null) {
                return false;
            }
            // 获取session
            getSession(request, response);
            // 获取servlet
            String servletClass = context.getServletClassByUrl(uri);
            HttpServlet workerServlet;
            if (StrUtil.isNotEmpty(servletClass)){
                workerServlet = InvokerServletFactory.getInstance();
            }else if (uri.endsWith(".jsp")){
                workerServlet = JspServletFactory.getInstance();
            }
            else {
                workerServlet = DefaultServlet.getInstance();
            }
            // 过滤
            List<Filter> filters = request.getContext().listMatchFilters(uri);
            ApplicationFilterChain chain = new ApplicationFilterChain(filters, workerServlet);
            chain.doFilter(request, response);
            // 是否重定向
            if (request.isForwarded()){
                return true;
            }
            // 是否压缩
            isGizp = isGzip(request, response.getBody(), response.getMimeType());
            // 修复响应
            if (response.getStatus() == HttpServletResponse.SC_FOUND){
                response = Response.found();
            }
            else if (response.getStatus() == HttpServletResponse.SC_NOT_FOUND){
                response = Response.notFound(response.getDesc());
            }else if (response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR){
                response = Response.error(response.getDesc());
            }
            // 发送响应
            sendResponse(socket, response, isGizp);
            return true;
        } catch (Exception e) {
            LogFactory.get().error(e);
            e.printStackTrace();
            sendResponse(socket, Response.error(e.getMessage()), isGizp);
            return false;
        }
    }

    private static void sendResponse(Socket socket, Response response, Boolean isGizp) throws IOException {
        String headerStr = StrUtil.format(Constant.RESPONSE_HEAD, response.getCode(), response.getDesc(), response.getMimeType(), response.getCookieHeader());
        if (isGizp){
            headerStr += "Content-Type: gzip";
        }
        if (response.getStatus() == HttpServletResponse.SC_FOUND){
            headerStr += "Location:" + response.getRedirectPath();
        }
        headerStr += "\r\n";
        byte[] header = headerStr.getBytes();
        byte[] body = response.getBody();
        if (isGizp){
            body = ZipUtil.gzip(body);
        }
        byte[] responseBytes = new byte[header.length + body.length];
        ArrayUtil.copy(header, responseBytes, header.length);
        ArrayUtil.copy(body, 0, responseBytes, header.length, body.length);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(responseBytes);
    }

    private static void getSession(Request request, Response response){
        String jSessionId = request.getJSessionIdFromCookie();
        HttpSession session = SessionManager.getSession(jSessionId, request, response);
        request.setSession(session);
    }

    private static boolean isGzip(Request request, byte[] body, String mimeType){
        String acceptEncoding = request.getHeader("Accept-Encoding");
        if (!StrUtil.containsAny(acceptEncoding, "gzip")){
            return false;
        }
        Connector connector = request.getConnector();
        String compression = connector.getCompression();
        if (!compression.equals("on")){
            return false;
        }
        Integer minSize = connector.getCompressionMinSize();
        if (body.length < minSize){
            return false;
        }
        String noCompressionUserAgents = connector.getNoCompressionUserAgents();
        String[] userAgents = noCompressionUserAgents.split(",");
        String userAgentHeader = request.getHeader("User-Agent");
        for (String userAgent: userAgents) {
            userAgent = userAgent.trim();
            if (StrUtil.containsAny(userAgentHeader, userAgent)){
                return false;
            }
        }
        if (mimeType.contains(",")){
            mimeType = StrUtil.subBefore(mimeType, ",", false);
        }
        String compressableMimeType = connector.getCompressableMimeType();
        String[] mimeTypes = compressableMimeType.split(",");
        for (String mimeTypeStr: mimeTypes) {
            if (mimeType.equals(mimeTypeStr)){
                return true;
            }
        }
        return false;
    }

}
