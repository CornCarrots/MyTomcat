package http;

import catalina.*;
import cn.hutool.core.util.StrUtil;
import util.Constant;
import util.MyBrowserUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @Author: zerocoder
 * @Description: 请求体
 * @Date: 2021/2/12 12:57
 */

public class Request extends BaseRequest{
    private String requestStr;

    private String uri;

    private Socket socket;

    private Context context;

    private String method;

    private Connector connector;

    public Request(Socket socket, Connector connector) throws IOException {
        this.socket = socket;
        this.connector = connector;
        preHandler();
        if (StrUtil.isEmpty(requestStr)){
            return;
        }
        parseUri();
        parseContext();
        parseMethod();
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

    public String getRequestStr() {
        return requestStr;
    }

    public String getUri() {
        return uri;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public String getMethod() {
        return method;
    }
}
