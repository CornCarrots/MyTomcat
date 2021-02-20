import catalina.*;
import classloader.CommonClassLoader;

import java.lang.reflect.Method;

/**
 * @Author: zerocoder
 * @Description: 启动类
 * @Date: 2021/2/10 13:46
 */

public class Bootstrap {

    public static void main(String[] args) throws Exception{
//        printJVM();
//        scanContextOnWebappsFolder();
//        loadConfig();
        CommonClassLoader commonClassLoader = new CommonClassLoader();
        Thread.currentThread().setContextClassLoader(commonClassLoader);
        String serverClassName = "catalina.Server";
        Class<?> serverClass = commonClassLoader.loadClass(serverClassName);
        Object server = serverClass.newInstance();
        Method method = serverClass.getMethod("start");
        method.invoke(server);
//        Server server = new Server();
//        server.start();
    }
}
