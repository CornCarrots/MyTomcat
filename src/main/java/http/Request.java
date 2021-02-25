package http;

import catalina.*;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import enums.HttpMethodEnum;
import lombok.Data;
import servlet.HelloServlet;
import util.Constant;
import util.MyBrowserUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author: zerocoder
 * @Description: 请求体
 * @Date: 2021/2/12 12:57
 */
@Data
public class Request extends BaseRequest{
    /**
     * 完整请求体
     */
    private String requestStr;

    /**
     * 请求资源
     */
    private String uri;

    /**
     * 请求的客户端socket
     */
    private Socket socket;

    /**
     * 当前请求的上下文
     */
    private Context context;

    /**
     * 请求HTTP方式
     */
    private String method;

    /**
     * 当前请求的服务连接器
     */
    private Connector connector;

    /**
     * 请求字符串
     */
    private String queryStr;

    /**
     * 请求参数
     */
    private Map<String, String[]> parameterMap;

    /**
     * 请求头
     */
    private Map<String, String> headMap;

    /**
     * cookie
     */
    private Cookie[] cookies;

    /**
     * session
     */
    private HttpSession session;

    /**
     * 跳转
     */
    private boolean isForwarded;

    private Map<String, Object> attributeMap;

    public Request(Socket socket, Connector connector) throws IOException {
        this.socket = socket;
        this.connector = connector;
        this.parameterMap = new HashMap<>();
        this.headMap = new HashMap<>();
        this.attributeMap = new HashMap<>();
        preHandler();
        if (StrUtil.isEmpty(requestStr)){
            return;
        }
        parseUri();
        parseContext();
        parseMethod();
        parseParameter();
        parseHeader();
        parseCookies();
        if (context != null && !context.getPath().equals(Constant.SEPARATOR)){
            uri = StrUtil.removePrefix(uri, context.getPath());
            if (StrUtil.isEmpty(uri)){
                uri = "/";
            }
        }
    }

    private void preHandler() throws IOException {
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = MyBrowserUtil.readBytes(inputStream, false);
        requestStr = new String(bytes, StandardCharsets.UTF_8);
    }

    private void parseUri(){
        uri = StrUtil.subBetween(requestStr, " ", " ");
        if (StrUtil.contains(uri, '?')){
            uri = StrUtil.subBefore(uri, "?", false);
        }
        System.out.println("accept uri: " + uri);
    }

    private void parseContext(){
        Service service = connector.getService();
        Engine engine = service.getEngine();
        Host host = engine.getDefaultHost();
        context = host.getContext(uri);
        if (context != null){
            return;
        }
        String path = StrUtil.subBetween(uri, "/", "/");
        if (StrUtil.isEmpty(path)){
            path = Constant.SEPARATOR;
        }else {
            path = Constant.SEPARATOR + path;
        }
        context = engine.getDefaultHost().getContext(path);
        if (context == null){
            context = engine.getDefaultHost().getContext(Constant.SEPARATOR);
        }
    }

    private void parseMethod(){
        method = StrUtil.subBefore(requestStr, " ", false);
    }

    private void parseParameter(){
        if (method.equals(HttpMethodEnum.GET.getName())){
            String url = StrUtil.subBetween(requestStr, " ", " ");
            if (StrUtil.contains(url, '?')){
                queryStr = StrUtil.subAfter(url, '?', false);
            }
        }
        if (method.equals(HttpMethodEnum.POST.getName())){
            queryStr = StrUtil.subAfter(requestStr, "\r\n\r\n", false);
        }
        if (StrUtil.isEmpty(queryStr)){
            return;
        }
        queryStr = URLUtil.decode(queryStr);
        String[] params = queryStr.split("&");
        if (ArrayUtil.isNotEmpty(params)){
            for (int i = 0; i < params.length; i++) {
                String[] split = params[i].split("=");
                String key = split[0];
                String value = split[1];
                String[] values = parameterMap.get(key);
                if (values == null){
                    values = new String[]{value};
                }else {
                    values = ArrayUtil.append(values, value);
                }
                parameterMap.put(key, values);
            }
        }
    }

    public void parseHeader(){
        StringReader reader = new StringReader(requestStr);
        List<String> lines = new ArrayList<>();
        IoUtil.readLines(reader, lines);
        if (CollUtil.isEmpty(lines)){
            return;
        }
        // 去掉开始的方法请求
        lines.remove(0);
        for (String line: lines) {
            if (StrUtil.isEmpty(line)){
                break;
            }
            String[] segs = line.split(":");
            String name = segs[0].toLowerCase();
            String value = segs[1];
            headMap.put(name, value);
        }
    }

    private void parseCookies(){
        List<Cookie> cookieList = new ArrayList<>();
        String cookies = headMap.get("cookie");
        if (StrUtil.isNotEmpty(cookies)){
            String[] splits = StrUtil.split(cookies, ";");
            for (String split: splits) {
                if (StrUtil.isNotBlank(split)){
                    String[] temp = StrUtil.split(split, "=");
                    String name = temp[0].trim();
                    String value = temp[1].trim();
                    Cookie cookie = new Cookie(name, value);
                    cookieList.add(cookie);
                }
            }
        }
        this.cookies = ArrayUtil.toArray(cookieList, Cookie.class);
    }

    public String getJSessionIdFromCookie(){
        String res = null;
        if (ArrayUtil.isEmpty(cookies)){
            return res;
        }
        Optional<String> jsessionid = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("JSESSIONID")).map(Cookie::getValue).findFirst();
        if (jsessionid.isPresent()){
            res = jsessionid.get();
        }
        return res;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public ServletContext getServletContext() {
        return context.getServletContext();
    }

    @Override
    public String getRealPath(String s) {
        return getServletContext().getRealPath(s);
    }

    @Override
    public String getParameter(String s) {
        String[] values = parameterMap.get(s);
        if (ArrayUtil.isNotEmpty(values)){
            return values[0];
        }
        return null;
    }

    @Override
    public String getHeader(String s) {
        return headMap.get(s);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> set = headMap.keySet();
        return Collections.enumeration(set);
    }

    @Override
    public int getIntHeader(String s) {
        String value = headMap.get(s);
        return Convert.toInt(value, 0);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Set<String> set = parameterMap.keySet();
        return Collections.enumeration(set);
    }

    @Override
    public String[] getParameterValues(String s) {
        return parameterMap.get(s);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    @Override
    public String getLocalName() {
        return socket.getLocalAddress().getHostName();
    }

    @Override
    public String getLocalAddr() {
        return socket.getLocalAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    @Override
    public String getProtocol() {
        return "HTTP/1.1";
    }

    @Override
    public String getRemoteAddr() {
        InetSocketAddress remoteSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        String address = remoteSocketAddress.getAddress().toString();
        return StrUtil.subAfter(address, "/", false);
    }

    @Override
    public String getRemoteHost() {
        InetSocketAddress remoteSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        return remoteSocketAddress.getHostName();
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return getHeader("host").trim();
    }

    @Override
    public int getServerPort() {
        return getLocalPort();
    }

    @Override
    public String getContextPath() {
        String path = context.getPath();
        if (path.equals("/")){
            return "";
        }
        return path;
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer builder = new StringBuffer();
        String scheme = getScheme();
        int serverPort = getServerPort();
        String requestURI = getRequestURI();
        String serverName = getServerName();
        if (serverPort <= 0){
            serverPort = 80;
        }
        builder.append(scheme).append("://").append(serverName);
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)){
            builder.append(":").append(serverPort);
        }
        builder.append(requestURI);
        return builder;
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return new ApplicationRequestDispatcher(uri);
    }

    @Override
    public Object getAttribute(String s) {
        return attributeMap.get(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> set = attributeMap.keySet();
        return Collections.enumeration(set);
    }

    @Override
    public void setAttribute(String s, Object o) {
        attributeMap.put(s, o);
    }

    @Override
    public void removeAttribute(String s) {
        attributeMap.remove(s);
    }
}
