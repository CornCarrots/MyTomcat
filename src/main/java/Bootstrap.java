import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import http.Request;
import http.Response;
import util.Constant;
import util.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: zerocoder
 * @Description: 启动类
 * @Date: 2021/2/10 13:46
 */

public class Bootstrap {
    private static void printJVM(){
        Map<String, String> infos = new LinkedHashMap<>();
        infos.put("Server version", "MyTomcat/1.0.0");
        infos.put("Server built", "2021-01-01 00:00:00");
        infos.put("Server number", "1.0.0");
        infos.put("OS Name\t", SystemUtil.get("os.name"));
        infos.put("OS Version", SystemUtil.get("os.version"));
        infos.put("Architecture", SystemUtil.get("os.arch"));
        infos.put("Java Home", SystemUtil.get("java.home"));
        infos.put("JVM Version", SystemUtil.get("java.runtime.version"));
        infos.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));

        for (String key: infos.keySet()) {
            LogFactory.get().info(key + "\t" + infos.get(key));
        }
    }
    public static void main(String[] args) {
        printJVM();
        try {
            System.out.println("start server...");
            int port = 10086;
//            if (!NetUtil.isUsableLocalPort(port)){
//                System.out.println("port is used!");
//                return;
//            }
            // 启动服务器socket
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("start server success! listen at 10086");
            while (true){
                // 接受客户端连接
                Socket socket = serverSocket.accept();
                Runnable runnable = () -> {
                    try {
                        Request request = new Request(socket);
                        System.out.println("accept requestStr: " + request.getRequestStr());
                        String uri = request.getUri();
                        System.out.println("accept uri: " + uri);
                        if (StrUtil.isEmpty(uri)){
                            return;
                        }
                        // 发送响应
                        System.out.println("send response...");
                        Response response = new Response();
                        if ("/".equals(uri)){
                            String html = "Hello MyTomcat";
                            response.getPrintWriter().println(html);
                        } else {
                            String fileName = StrUtil.removePrefix(uri, "/");
                            File file = FileUtil.file(Constant.ROOT_FOLDER, fileName);
                            if (file.exists()){
                                String text = FileUtil.readUtf8String(file);
                                response.getPrintWriter().println(text);
                                if (fileName.equals("time.html")){
                                    Thread.sleep(1000);
                                }
                            }else {
                                response.getPrintWriter().println("file not found:" + Constant.ROOT_FOLDER + fileName);
                            }
                        }
                        handler200(socket, response);
                    } catch (Exception e) {
                        LogFactory.get().error(e);
                        e.printStackTrace();
                    }
                };
                ThreadUtil.run(runnable);
            }
        } catch (Exception e) {
            LogFactory.get().error(e);
            e.printStackTrace();
        }
    }

    private static void handler200(Socket socket, Response response) throws IOException {
        String headerStr = StrUtil.format(Constant.RESPONSE_HEAD, response.getCode(), response.getDesc(), response.getContentType());
        byte[] header = headerStr.getBytes();
        byte[] body = response.getBody();
        byte[] responseBytes = new byte[header.length + body.length];
        ArrayUtil.copy(header, responseBytes, header.length);
        ArrayUtil.copy(body, 0, responseBytes, header.length, body.length);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(responseBytes);
        outputStream.flush();
        // 关闭连接
        socket.close();
    }
}
