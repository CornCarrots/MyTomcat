package classloader;


import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.ClassLoader;
import java.util.List;

/**
 * @Author: zerocoder
 * @Description:
 * @Date: 2021/2/19 23:04
 */

public class WebappClassLoader extends URLClassLoader {
    /**
     * @param docBase           WEB应用目录
     * @param commonClassLoader 父亲 类加载器
     */
    public WebappClassLoader(String docBase, ClassLoader commonClassLoader) {
        super(new URL[]{}, commonClassLoader);
        try {
            // 获取应用的web-inf目录
            File webinfoFolder = new File(docBase, "WEB-INF");
            // 获取servlet路径
            File classesFolder = new File(webinfoFolder, "classes");
            // 获取lib路径
            File libFolder = new File(webinfoFolder, "lib");
            // 加载classes
            URL url = new URL("file:" + classesFolder.getAbsolutePath() + "/");
            this.addURL(url);

            // 加载jar包
            List<File> libFiles = FileUtil.loopFiles(libFolder);
            for (File file : libFiles) {
                url = new URL("file:" + file.getAbsolutePath());
                this.addURL(url);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        try {
            close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
