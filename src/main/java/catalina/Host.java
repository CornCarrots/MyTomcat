package catalina;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.log.LogFactory;
import util.Constant;
import util.XmlUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zerocoder
 * @Description: 虚拟主机
 * @Date: 2021/2/13 12:15
 */

public class Host {

    private String name;

    /**
     * 上下文
     */
    private Map<String, Context> contextMap;

    private Engine engine;

    public Host(String name, Engine engine) {
        TimeInterval timer = DateUtil.timer();
        this.engine = engine;
//        name = XmlUtil.getHost();
        this.name = name;
        LogFactory.get().info("[load Host] host name is {}", name);
        contextMap = new HashMap<>();
        scanContextOnWebappsFolder();
        loadContextOnServerXml();
        LogFactory.get().info("[load Host] has finished in {} ms", timer.intervalMs());
    }

    /**
     * 通过配置文件加载上下文
     */
    private void loadContextOnServerXml(){
        List<Context> contexts = XmlUtil.listContexts(this);
        if (CollUtil.isEmpty(contexts)){
            return;
        }
        for (Context context: contexts) {
            contextMap.put(context.getPath(), context);
        }
    }

    /**
     * 通过文件加载应用上下文
     */
    private void scanContextOnWebappsFolder(){
        File[] files = Constant.WEBAPPS_FOLDER.listFiles();
        if (ArrayUtil.isEmpty(files)){
            return;
        }
        for (File folder: files) {
            if (folder.isDirectory()){
                loadContext(folder);
            }
        }
    }

    private void loadContext(File directory){
        String path = directory.getName();
        if ("ROOT".equals(path)){
            path = Constant.SEPARATOR;
        }else {
            path = Constant.SEPARATOR + path;
        }
        String absolutePath = directory.getAbsolutePath();
        Context context = new Context(path, absolutePath, this, true);
        contextMap.put(path, context);
    }

    public void reload(Context context){
        String docBase = context.getDocBase();
        String path = context.getPath();
        Boolean reloadable = context.getReloadable();
        LogFactory.get().info("reloading context with path[{}] has started", path);
        context.stop();
        contextMap.remove(path);
        Context newContext = new Context(path, docBase, this, reloadable);
        contextMap.put(path, newContext);
        LogFactory.get().info("reloading context with path[{}] has completed", path);
    }

    public String getName() {
        return name;
    }

    public Map<String, Context> getContextMap() {
        return contextMap;
    }


    public Context getContext(String path){
        return contextMap.get(path);
    }

    public Engine getEngine() {
        return engine;
    }
}
