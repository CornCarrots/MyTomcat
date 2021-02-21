package catalina;

import classloader.WebappClassLoader;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.log.LogFactory;
import exception.WebConfigDuplicatedException;
import http.ApplicationContext;
import http.StandardServletConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import util.XmlUtil;
import watcher.ContextFileWatcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.util.*;

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
    private WebappClassLoader classLoader;

    /**
     * 主机
     */
    private Host host;

    /**
     * 是否热部署
     */
    private Boolean reloadable;

    /**
     * 文件监听器
     */
    private ContextFileWatcher contextFileWatcher;

    /**
     * 应用上下文
     */
    private ServletContext servletContext;

    /**
     * 保存应用各个servlet及其实例
     */
    private Map<Class<?>, HttpServlet> servletPool;

    /**
     * url映射servlet类路径
     */
    private Map<String, String> urlServletClass;

    /**
     * url映射servlet名字
     */
    private Map<String, String> urlServletName;

    /**
     * servlet名字映射类路径
     */
    private Map<String, String> servletNameClass;

    /**
     * servlet类路径映射名字
     */
    private Map<String, String> servletClassName;

    /**
     * servlet类包含的初始化参数键值对
     */
    private Map<String, Map<String, String>> servletClassParams;

    private List<String> loadOnStartupServletClassNames;

    public Context(String path, String docBase, Host host, Boolean reloadable) {
        try {
            TimeInterval timer = DateUtil.timer();
            this.path = path;
            this.docBase = docBase;
            this.host = host;
            this.reloadable = reloadable;
            urlServletClass = new HashMap<>();
            urlServletName = new HashMap<>();
            servletNameClass = new HashMap<>();
            servletClassName = new HashMap<>();
            servletClassParams = new HashMap<>();
            servletPool = new HashMap<>();
            loadOnStartupServletClassNames = new ArrayList<>();
            this.webXml = FileUtil.file(docBase, XmlUtil.getWatchedResource());
            ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();
            this.classLoader = new WebappClassLoader(docBase, commonClassLoader);
            this.servletContext = new ApplicationContext(this);
            deploy();
            if (reloadable){
                contextFileWatcher = new ContextFileWatcher(this);
                contextFileWatcher.start();
            }
            loadOnStartUpServlets();
            LogFactory.get().info("[load Context] Deployment of web application path:{}, directory:{} has finished in {} ms", this.path, this.docBase, timer.intervalMs());
        } catch (Exception e) {
            e.printStackTrace();
            LogFactory.get().error("[load Context] error! path:{}, docBase:{}", path, docBase, e);
        }
    }

    private void deploy(){
        if (!webXml.exists()){
            return;
        }
        try {
            String xml = FileUtil.readUtf8String(webXml);
            Document document = Jsoup.parse(xml);
            XmlUtil.checkDuplicated(document);
            XmlUtil.parseServlet(document, urlServletClass, urlServletName, servletNameClass, servletClassName, servletClassParams, loadOnStartupServletClassNames);
        } catch (WebConfigDuplicatedException e) {
            e.printStackTrace();
        }
    }

    public void reload(){
        host.reload(this);
    }

    public void stop(){
        classLoader.stop();
        contextFileWatcher.stop();
        destroyServlets();
    }

    public synchronized HttpServlet getServletByPool(Class<?> clazz) throws ServletException {
        HttpServlet servlet = servletPool.get(clazz);
        if (servlet == null){
//            servlet = (HttpServlet) clazz.newInstance();
            servlet = (HttpServlet) ReflectUtil.newInstance(clazz);
            // 初始化配置
            String clazzName = clazz.getName();
            String servletName = servletClassName.get(clazzName);
            Map<String, String> initParamsMap = servletClassParams.get(clazzName);
            ServletConfig servletConfig = new StandardServletConfig(getServletContext(), initParamsMap, servletName);
            servlet.init(servletConfig);
            servletPool.put(clazz, servlet);
        }
        return servlet;
    }

    private void destroyServlets(){
        Collection<HttpServlet> values = servletPool.values();
        for (HttpServlet servlet: values) {
            servlet.destroy();
        }
    }

    private void loadOnStartUpServlets() throws ClassNotFoundException, ServletException {
        if (CollectionUtil.isEmpty(loadOnStartupServletClassNames)){
            return;
        }
        for (String className: loadOnStartupServletClassNames) {
            Class<?> servletClazz = getClassLoader().loadClass(className);
            getServletByPool(servletClazz);
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

    public Boolean getReloadable() {
        return reloadable;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
}
