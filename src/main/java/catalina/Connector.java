package catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import http.Request;
import http.Response;
import util.Constant;
import util.ThreadUtil;
import util.XmlUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * @Author: zerocoder
 * @Description: 连接器
 * @Date: 2021/2/14 15:03
 */

public class Connector implements Runnable{

    private Integer port;

    private Service service;

    public Connector(Service service) {
        this.service = service;
    }

    public void init(){
        LogFactory.get().info("Initializing ProtocolHandler {http-bio-{}}", port);
    }

    public void start(){
        LogFactory.get().info("Starting ProtocolHandler {http-bio-{}}", port);
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            LogFactory.get().info("[start server] success! listen port:{}", port);
            while (true) {
                // 接受客户端连接
                Socket socket = serverSocket.accept();
                try {
                    Callable callable = () -> {
                        try {
                            Request request = new Request(socket, this);
//                        System.out.println("accept requestStr: " + request.getRequestStr());
                            Context context = request.getContext();
                            String uri = request.getUri();
                            if (StrUtil.isEmpty(uri)) {
                                return false;
                            }
                            // 发送响应
                            Response response = Response.success();
                            // 欢迎页
                            if (Constant.SEPARATOR.equals(uri)){
                                uri = XmlUtil.getWelcomeFile(request.getContext());
                            }
                            String fileName = StrUtil.removePrefix(uri, Constant.SEPARATOR);
                            // 多应用
                            File file;
                            if (context != null && (file = FileUtil.file(context.getDocBase(), fileName)).exists()) {
                                // 网页内容
//                                String html = FileUtil.readUtf8String(file);
//                                response.getPrintWriter().println(html);
                                byte[] bytes = FileUtil.readBytes(file);
                                response.setBody(bytes);
                                // 多媒体类型
                                String extName = FileUtil.extName(file);
                                String mimeType = XmlUtil.getMimeType(extName);
                                response.setMimeType(mimeType);
                                if (fileName.equals("time.html")) {
                                    Thread.sleep(1000);
                                }
                            } else {
                                response = Response.notFound(Constant.ROOT_FOLDER + fileName);
                            }
//                            System.out.println(response);
                            sendResponse(socket, response);
                            return true;
                        } catch (Exception e) {
                            LogFactory.get().error(e);
                            e.printStackTrace();
                            sendResponse(socket, Response.error(e));
                            return false;
                        }
                    };
                    Boolean result = (Boolean) ThreadUtil.runSync(callable);
                    LogFactory.get().info("send response:{}", result);
                }
                finally {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                }
            }
        } catch (Exception e) {
            LogFactory.get().error(e);
            e.printStackTrace();
        }
    }

    private void sendResponse(Socket socket, Response response) throws IOException {
        String headerStr = StrUtil.format(Constant.RESPONSE_HEAD, response.getCode(), response.getDesc(), response.getMimeType());
        byte[] header = headerStr.getBytes();
        byte[] body = response.getBody();
        byte[] responseBytes = new byte[header.length + body.length];
        ArrayUtil.copy(header, responseBytes, header.length);
        ArrayUtil.copy(body, 0, responseBytes, header.length, body.length);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(responseBytes);
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Service getService() {
        return service;
    }
}
