package catalina;

import cn.hutool.log.LogFactory;
import http.Request;
import http.Response;
import lombok.Data;
import util.ThreadUtil;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * @Author: zerocoder
 * @Description: 连接器
 * @Date: 2021/2/14 15:03
 */
@Data
public class Connector implements Runnable{

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 包含的服务
     */
    private Service service;

    /**
     * 是否压缩
     */
    private String compression;

    /**
     * 压缩限制
     */
    private Integer compressionMinSize;

    /**
     * 避免压缩的浏览器
     */
    private String noCompressionUserAgents;

    /**
     * 压缩媒体类型
     *
     */
    private String compressableMimeType;

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
}
