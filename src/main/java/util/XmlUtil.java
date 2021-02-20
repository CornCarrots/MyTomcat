package util;

import catalina.*;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import exception.WebConfigDuplicatedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.*;

/**
 * @Author: zerocoder
 * @Description: XML工具类
 * @Date: 2021/2/13 11:31
 */

public class XmlUtil {
    private static final String serverXmlPath = FileUtil.readUtf8String(Constant.SERVER_XML);

    private static final String webXmlPath = FileUtil.readUtf8String(Constant.WEB_XML);

    private static final String contextXmlPath = FileUtil.readUtf8String(Constant.CONTEXT_XML);

    private static Document serverDocument = Jsoup.parse(serverXmlPath);

    private static Document webDocument = Jsoup.parse(webXmlPath);

    private static Document contextDocument = Jsoup.parse(contextXmlPath);

    public static Map<String, String> mimeTypeMapping = new HashMap<>();

    /**
     * 获取所有上下文
     * @return
     */
    public static List<Context> listContexts(Host host){
        List<Context> result = new ArrayList<>();
        Elements hostElements = serverDocument.select("Host");
        for (Element hostElement: hostElements) {
            if (!hostElement.attr("name").equals(host.getName())){
                continue;
            }
            Elements contextElements = hostElement.children();
            for (Element element: contextElements) {
                String path = element.attr("path");
                String docBase = element.attr("docBase");
                result.add(new Context(path, docBase));
            }
        }
        return result;
    }

    public static String getService(){
        Element service = serverDocument.select("Service").first();
        return service.attr("name");
    }

    public static List<Connector> listConnectors(Service service){
        List<Connector> result = new ArrayList<>();
        Elements elements = serverDocument.select("Service");
        for (Element element: elements) {
            String serviceName = element.attr("name");
            if (service.getName().equals(serviceName)){
                Elements connectorElements = element.select("Connector");
                for (Element connector: connectorElements) {
                    Connector c = new Connector(service);
                    c.setPort(Convert.toInt(connector.attr("port")));
                    result.add(c);
                }
            }
        }
        return result;
    }

    public static String getEngineDefaultHost(){
        Element engine = serverDocument.select("Engine").first();
        return engine.attr("defaultHost");
    }

    public static List<Host> listHosts(Engine engine){
        List<Host> result = new ArrayList<>();
        Element engineElement = serverDocument.select("Engine").first();
        Elements children = engineElement.children();
        for (Element host: children) {
            String name = host.attr("name");
            result.add(new Host(name, engine));
        }
        return result;
    }

    /**
     * 获取欢迎页
     * @param context
     * @return
     */
    public static String getWelcomeFile(Context context){
        Elements elements = webDocument.select("welcome-file");
        for (Element element: elements) {
            String fileName = element.text();
            File file = new File(context.getDocBase(), fileName);
            if (file.exists()){
                return file.getName();
            }
        }
        return "index.html";
    }

    public static synchronized String getMimeType(String extension){
        if (mimeTypeMapping.isEmpty()){
            initMimeType();
        }
        String mimeType = mimeTypeMapping.get(extension);
        if (StrUtil.isEmpty(mimeType)){
            mimeType = "text/html";
        }
        return mimeType;
    }

    private static void initMimeType(){
        Elements elements = webDocument.select("mime-mapping");
        for (Element element: elements) {
            String extension = element.select("extension").text();
            String mimeType = element.select("mime-type").text();
            mimeTypeMapping.put(extension, mimeType);
        }
    }

    public static String getWatchedResource(){
        try {
            Elements watchedResource = contextDocument.select("WatchedResource");
            return  watchedResource.text();
        }catch (Exception e){
            e.printStackTrace();
            return "WEB-INF/web.xml";
        }
    }

    public static void parseServletMapping(Document webInfDocument, Map<String, String> urlServletClass,Map<String, String> urlServletName, Map<String, String> servletNameClass){
        Elements urlPatternElements = webInfDocument.select("servlet-mapping url-pattern");
        for (Element urlPatternElement: urlPatternElements) {
            String urlPattern = urlPatternElement.text();
            Element servletNameElement = urlPatternElements.parents().select("servlet-name").first();
            String servletName = servletNameElement.text();
            urlServletName.put(urlPattern, servletName);
        }
        Elements servletNameElements = webInfDocument.select("servlet servlet-name");
        for (Element servletNameElement: servletNameElements) {
            String servletName = servletNameElement.text();
            Element servletClassElement = urlPatternElements.parents().select("servlet-class").first();
            String servletClass = servletClassElement.text();
            servletNameClass.put(servletName, servletClass);
        }
        Set<String> urls = urlServletName.keySet();
        for (String url: urls) {
            String servletName = urlServletName.get(url);
            String servletClass = servletNameClass.get(servletName);
            urlServletClass.put(url, servletClass);
        }
    }

    public static void checkDuplicated(Document webInfDocument, String mapping, String desc) throws WebConfigDuplicatedException {
        Elements elements = webInfDocument.select(mapping);
        Set<String> contents = new HashSet<>();
        for (Element element: elements) {
            if (!contents.contains(element.text())) {
                contents.add(element.text());
            }else {
                throw new WebConfigDuplicatedException(StrUtil.format(desc, element.text()));
            }
        }
    }

    public static void checkDuplicated(Document webInfDocument) throws WebConfigDuplicatedException {
        checkDuplicated(webInfDocument, "servlet-mapping url-pattern", "servlet url:{} 重复");
        checkDuplicated(webInfDocument, "servlet servlet-name", "servlet name:{} 重复");
        checkDuplicated(webInfDocument, "servlet servlet-class", "servlet class:{} 重复");
    }
}
