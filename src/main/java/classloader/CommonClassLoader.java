package classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @Author: zerocoder
 * @Description: 公共类加载器 扫描服务器lib下面的jar包，加入到当前库中
 * @Date: 2021/2/15 14:34
 */

public class CommonClassLoader extends URLClassLoader {
    public CommonClassLoader(){
        super(new URL[]{});
        try {
            // 获取tomcat路径
            File workFolder = new File(System.getProperty("user.dir"));
            // tomcat的lib
            File libFolder = new File(workFolder, "lib");
            File[] jarFiles = libFolder.listFiles();
            if (jarFiles != null) {
                for (File file: jarFiles) {
                    if (file.getName().endsWith("jar")){
                        URL url = new URL("file:" + file.getAbsolutePath());
                        this.addURL(url);
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
