package catalina;

import classloader.WebappClassLoader;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.log.LogFactory;
import exception.WebConfigDuplicatedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import util.XmlUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zerocoder
 * @Description: 上下文
 * @Date: 2021/2/12 23:35
 */

public class Context {
    /**
     * 相对路径
     */
    private String path;

    /**
     * 绝对路径
     */
    private String docBase;

    /**
     * 应用的web.xml文件
     */
    private File webXml;

    /**
     * 类加载器
     */
    private ClassLoader classLoader;

    private Map<String, String> urlServletClass;

    private Map<String, String> urlServletName;

    private Map<String, String> servletNameClass;

    private Map<String, String> servletClassServlet;

    public Context(String path, String docBase) {
        TimeInterval timer = DateUtil.timer();
        this.path = path;
        this.docBase = docBase;
        urlServletClass = new HashMap<>();
        urlServletName = new HashMap<>();
        servletNameClass = new HashMap<>();
        servletClassServlet = new HashMap<>();
        this.webXml = FileUtil.file(docBase, XmlUtil.getWatchedResource());
        ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();
        this.classLoader = new WebappClassLoader(docBase, commonClassLoader);
        deploy();
        LogFactory.get().info("[load Context] Deployment of web application path:{}, directory:{} has finished in {} ms", this.path, this.docBase, timer.intervalMs());
    }

    private void deploy(){
        if (!webXml.exists()){
            return;
        }
        try {
            String xml = FileUtil.readUtf8String(webXml);
            Document document = Jsoup.parse(xml);
            XmlUtil.checkDuplicated(document);
            XmlUtil.parseServletMapping(document, urlServletClass, urlServletName, servletNameClass);
        } catch (WebConfigDuplicatedException e) {
            e.printStackTrace();
        }
    }

    public String getPath() {
        return path;
    }

    public String getDocBase() {
        return docBase;
    }

    public String getServletClassByUrl(String url){
        return urlServletClass.get(url);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
