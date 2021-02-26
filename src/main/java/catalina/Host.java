package catalina;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import lombok.Data;
import util.Constant;
import util.XmlUtil;
import watcher.WarFileWatcher;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zerocoder
 * @Description: 虚拟主机
 * @Date: 2021/2/13 12:15
 */
@Data
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
        // 静态加载war
        scanWarOnWebAppsFolder();
        // 动态加载war
        WarFileWatcher warFileWatcher = new WarFileWatcher(this);
        warFileWatcher.start();
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

    public Context getContext(String path){
        return contextMap.get(path);
    }

    /**
     * 文件夹加载上下文
     * @param folder
     */
    public void loadFolder(File folder){
        String path = folder.getName();
        if (path.equals("ROOT")){
            path = "/";
        }
        else {
            path = "/" + path;
        }
        String docBase = folder.getAbsolutePath();
        Context context = new Context(docBase, path, this, false);
        contextMap.put(path, context);
    }

    /**
     * 解压war包，加载对应上下文
     * @param warFile
     */
    public void loadWar(File warFile){
        String fileName = warFile.getName();
        String folderName = StrUtil.subBefore(fileName, ".", true);
        Context context = getContext("/" + folderName);
        // war包加载过上下文了
        if (context != null){
            return;
        }
        // war包有解析过了
        File contextFolder = FileUtil.file(Constant.WEBAPPS_FOLDER, folderName);
        if (contextFolder.exists()){
            if (!FileUtil.isDirEmpty(contextFolder)) {
                loadContext(contextFolder);
                return;
            }
        }else {
            contextFolder.mkdir();
        }
        // 因为解压只能在本目录，所以需要先移动war文件到指定应用目录下
        File tempWarFile = FileUtil.file(contextFolder, fileName);
        FileUtil.copyFile(warFile, tempWarFile);
        // 解压
        String command = "jar xvf "+ fileName;
        Process process = RuntimeUtil.exec(null, contextFolder, command);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 再删除临时文件
        tempWarFile.delete();
        loadFolder(contextFolder);
    }

    /**
     * 静态部署，处理war文件
     */
    private void scanWarOnWebAppsFolder() {
        File folder = FileUtil.file(Constant.WEBAPPS_FOLDER);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if(file.getName().toLowerCase().endsWith(".war"))
                    loadWar(file);
            }
        }
    }
}
