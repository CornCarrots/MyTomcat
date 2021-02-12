package http;

import cn.hutool.core.util.StrUtil;
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

public class Request {
    private String requestStr;

    private String uri;

    private Socket socket;

    public Request(Socket socket) throws IOException {
        this.socket = socket;
        preHandler();
        if (StrUtil.isEmpty(requestStr)){
            return;
        }
        handler();
    }

    private void preHandler() throws IOException {
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = MyBrowserUtil.readBytes(inputStream);
        requestStr = new String(bytes, StandardCharsets.UTF_8);
    }

    private void handler(){
        uri = StrUtil.subBetween(requestStr, " ", " ");
        if (StrUtil.contains(uri, '?')){
            uri = StrUtil.subBefore(uri, "?", false);
        }
    }

    public String getRequestStr() {
        return requestStr;
    }

    public String getUri() {
        return uri;
    }
}
