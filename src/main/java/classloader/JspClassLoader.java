package classloader;

import catalina.Context;
import cn.hutool.core.util.StrUtil;
import util.Constant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: zerocoder
 * @Description: jsp类加载器
 * @Date: 2021/2/24 22:49
 */

public class JspClassLoader extends URLClassLoader {

    private static Map<String, JspClassLoader> jspClassLoaderMap = new ConcurrentHashMap<>();


    /**
     * 基于webappsClassLoader构建
     * @param context
     * @throws MalformedURLException
     */
    private JspClassLoader(Context context) throws MalformedURLException {
        super(new URL[]{}, context.getClassLoader());
        String path = context.getPath();
        String subFolder;
        if (Constant.SEPARATOR.equals(path)) {
            subFolder = "_";
        }
        else {
            subFolder = StrUtil.subAfter(path, "/", false);
        }
        File classLoader = new File(Constant.WORK_FOLDER, subFolder);
        URL url = new URL("file:" + classLoader.getAbsolutePath() + "/");
        this.addURL(url);
    }

    /**
     * 取消关联
     * @param uri
     * @param context
     */
    public static void invalidJspClassLoader(String uri, Context context){
        String key = context.getPath() + "/" + uri;
        jspClassLoaderMap.remove(key);
    }

    /**
     * 获取jsp的类加载器
     * @param uri
     * @param context
     * @return
     * @throws MalformedURLException
     */
    public static JspClassLoader getJspClassLoader(String uri, Context context) throws MalformedURLException {
        String key = context.getPath() + "/" + uri;
        JspClassLoader jspClassLoader = jspClassLoaderMap.get(key);
        if (jspClassLoader == null){
            jspClassLoader = new JspClassLoader(context);
            jspClassLoaderMap.put(key, jspClassLoader);
        }
        return jspClassLoader;
    }

}
