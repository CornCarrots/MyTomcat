import catalina.*;

/**
 * @Author: zerocoder
 * @Description: 启动类
 * @Date: 2021/2/10 13:46
 */

public class Bootstrap {

    public static void main(String[] args) {
//        printJVM();
//        scanContextOnWebappsFolder();
//        loadConfig();
        Server server = new Server();
        server.start();
    }
}
