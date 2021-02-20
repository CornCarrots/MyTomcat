package catalina;

import cn.hutool.log.LogFactory;
import http.Request;
import http.Response;
import util.ThreadUtil;

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
                        Request request = new Request(socket, this);
                        Response response = Response.success();
                        return HttpProcessor.execute(socket, request, response);
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
