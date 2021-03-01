package catalina;

import classloader.WebappClassLoader;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import exception.WebConfigDuplicatedException;
import http.ApplicationContext;
import http.StandardFilterConfig;
import http.StandardServletConfig;
import lombok.Data;
import org.apache.jasper.JspC;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import util.XmlUtil;
import watcher.ContextFileWatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.util.*;

/**
 * @Author: zerocoder
 * @Description: 上下文
 * @Date: 2021/2/12 23:35
 */
@Data
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

    // -----------------------------servlet------------------------

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

    /**
     * 保存应用各个servlet及其实例
     */
    private Map<Class<?>, HttpServlet> servletPool;

    // -----------------------------servlet------------------------

    // ----------------------------- filter ------------------------


    /**
     * url映射Filter类路径
     */
    private Map<String, List<String>> urlFilterClass;

    /**
     * url映射Filter名字
     */
    private Map<String, List<String>> urlFilterName;

    /**
     * Filter名字映射类路径
     */
    private Map<String, String> filterNameClass;

    /**
     * Filter类路径映射名字
     */
    private Map<String, String> filterClassName;

    /**
     * Filter类包含的初始化参数键值对
     */
    private Map<String, Map<String, String>> filterClassParams;

    /**
     * 保存应用各个Filter及其实例
     */
    private Map<String, Filter> filterPool;

    // ----------------------------- filter ------------------------

    private List<String> listenerClasses;

    private List<ServletContextListener> listeners;

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
            loadOnStartupServletClassNames = new ArrayList<>();

            urlFilterClass = new HashMap<>();
            urlFilterName = new HashMap<>();
            filterNameClass = new HashMap<>();
            filterClassName = new HashMap<>();
            filterClassParams = new HashMap<>();

            servletPool = new HashMap<>();
            filterPool = new HashMap<>();

            listeners = new ArrayList<>();
            listenerClasses = new ArrayList<>();
            this.webXml = FileUtil.file(docBase, XmlUtil.getWatchedResource());
            ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();
            this.classLoader = new WebappClassLoader(docBase, commonClassLoader);
            this.servletContext = new ApplicationContext(this);
            deploy();
            // 初始化servlet
            loadOnStartUpServlets();
            // 让jsp转换的java文件里的工厂有返回值
            new JspRuntimeContext(servletContext, new JspC());
            // 初始化filter
            initFilterPool();
            // 应用context监听器
            fireEvent("init");
            // 热部署
            if (reloadable){
                contextFileWatcher = new ContextFileWatcher(this);
                contextFileWatcher.start();
            }
            LogFactory.get().info("[load Context] Deployment of web application path:{}, directory:{} has finished in {} ms", this.path, this.docBase, timer.intervalMs());
        } catch (Exception e) {
            e.printStackTrace();
            LogFactory.get().error("[load Context] error! path:{}, docBase:{}", path, docBase, e);
        }
    }

    /**
     * 解析路径
     */
    private void deploy(){
        if (!webXml.exists()){
            return;
        }
        try {
            String xml = FileUtil.readUtf8String(webXml);
            Document document = Jsoup.parse(xml);
            XmlUtil.checkDuplicated(document);
            XmlUtil.parseServletAndFilter("servlet", document, urlServletClass, urlServletName, null, null, servletNameClass, servletClassName, servletClassParams, loadOnStartupServletClassNames);
            XmlUtil.parseServletAndFilter("filter", document, null, null, urlFilterClass, urlFilterName, filterNameClass, filterClassName, filterClassParams, null);
            XmlUtil.parseListener(document, listenerClasses);
        } catch (WebConfigDuplicatedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重新加载应用上下文
     */
    public void reload(){
        host.reload(this);
    }

    /**
     * 停止加载应用上下文
     * 1：停止类加载器
     * 2：停止监听
     * 3：销毁servlet类实例
     */
    public void stop(){
        classLoader.stop();
        contextFileWatcher.stop();
        destroyServlets();
        fireEvent("destroy");
    }

    /**
     * 初始化过滤器
     * @throws ClassNotFoundException
     * @throws ServletException
     */
    private void initFilterPool() throws ClassNotFoundException, ServletException {
        Set<String> filterClassNames = this.filterClassName.keySet();
        for (String filterClassName: filterClassNames) {
            Map<String, String> initParamsMap = servletClassParams.get(filterClassName);
            String filterName = filterNameClass.get(filterClassName);
            // 初始化配置
            FilterConfig filterConfig = new StandardFilterConfig(getServletContext(), initParamsMap, filterName);
            Class<?> filterClass = this.getClassLoader().loadClass(filterClassName);
            Filter filter = (Filter) ReflectUtil.newInstance(filterClass);
            filter.init(filterConfig);
            filterPool.put(filterClassName, filter);
        }
    }

    /**
     * 从池加载servlet
     * @param clazz
     * @return
     * @throws ServletException
     */
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

    /**
     * 销毁servlet
     */
    private void destroyServlets(){
        Collection<HttpServlet> values = servletPool.values();
        for (HttpServlet servlet: values) {
            servlet.destroy();
        }
    }

    /**
     * 自动加载servlet
     * @throws ClassNotFoundException
     * @throws ServletException
     */
    private void loadOnStartUpServlets() throws ClassNotFoundException, ServletException {
        if (CollectionUtil.isEmpty(loadOnStartupServletClassNames)){
            return;
        }
        for (String className: loadOnStartupServletClassNames) {
            Class<?> servletClazz = getClassLoader().loadClass(className);
            getServletByPool(servletClazz);
        }
    }

    /**
     * 获取servlet类
     * @param url
     * @return
     */
    public String getServletClassByUrl(String url){
        return urlServletClass.get(url);
    }

    /**
     * 根据正则表达式匹配过滤器
     * @param pattern
     * @param uri
     * @return
     */
    private boolean match(String pattern, String uri){
        // 完全匹配
        if (StrUtil.equals(pattern, uri)){
            return true;
        }
        // /*模式
        if (StrUtil.equals(pattern, "/")){
            return true;
        }
        // 后缀名 /*.jsp
        if (StrUtil.startWith(pattern, "/")){
            String patternExt = StrUtil.subAfter(pattern, ".", false);
            String uriExt = StrUtil.subAfter(uri, ".", false);
            if (StrUtil.equals(patternExt, uriExt)){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取url匹配的所有过滤器
     * @param uri
     * @return
     */
    public List<Filter> listMatchFilters(String uri){
        List<Filter> res = new ArrayList<>();
        Set<String> matchPatterns = new HashSet<>();
        Set<String> patterns = urlFilterClass.keySet();
        Set<String> matchClass = new HashSet<>();
        for (String pattern: patterns) {
            if (match(pattern, uri)){
                matchPatterns.add(pattern);
            }
        }
        for (String pattern: matchPatterns) {
            List<String> list = urlFilterClass.get(pattern);
            matchClass.addAll(list);
        }
        for (String className: matchClass) {
            Filter filter = filterPool.get(className);
            res.add(filter);
        }
        return res;
    }

    /**
     * 初始化监听器
     */
    private void initListener(){
        if (CollUtil.isEmpty(listenerClasses)){
            return;
        }
        listenerClasses.forEach(listenerClassName -> {
            try {
                Class<?> listenerClass = this.getClassLoader().loadClass(listenerClassName);
                ServletContextListener listener = (ServletContextListener) listenerClass.newInstance();
                listeners.add(listener);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 创建方法
     * @param type
     */
    private void fireEvent(String type){
        ServletContextEvent event = new ServletContextEvent(servletContext);
        listeners.forEach(servletContextListener -> {
            if (type.equals("init")){
                servletContextListener.contextInitialized(event);
            }
            if (type.equals("destroy")){
                servletContextListener.contextDestroyed(event);
            }
        });
    }
}
