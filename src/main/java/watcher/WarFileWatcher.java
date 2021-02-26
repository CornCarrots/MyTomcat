package watcher;

import catalina.Host;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.log.LogFactory;
import util.Constant;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * @Author: zerocoder
 * @Description: war文件动态部署
 * @Date: 2021/2/25 23:07
 */

public class WarFileWatcher {

    private WatchMonitor monitor;

    private Host host;

    public WarFileWatcher(Host host) {
        this.host = host;
        Watcher monitor = new WarMonitor();
        this.monitor = WatchUtil.createAll(Constant.WEBAPPS_FOLDER, 1, monitor);
        this.monitor.setDaemon(true);
    }

    public void start(){
        this.monitor.start();
    }

    class WarMonitor extends SimpleWatcher {

        private void dealWith(WatchEvent<?> event){
            synchronized (WarFileWatcher.class){
                String fileName = event.context().toString();
                if (fileName.toLowerCase().equals(".war") && event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)){
                    LogFactory.get().info("{} check reload file:{}", WarFileWatcher.this,fileName);
                    File file = FileUtil.file(Constant.WEBAPPS_FOLDER, fileName);
                    host.loadWar(file);
                    LogFactory.get().info("{} check file:{} reload", WarFileWatcher.this, fileName);
                }
            }
        }


        @Override
        public void onCreate(WatchEvent<?> watchEvent, Path path) {
            dealWith(watchEvent);
        }

        @Override
        public void onModify(WatchEvent<?> watchEvent, Path path) {
            dealWith(watchEvent);
        }

        @Override
        public void onDelete(WatchEvent<?> watchEvent, Path path) {
            dealWith(watchEvent);
        }

        @Override
        public void onOverflow(WatchEvent<?> watchEvent, Path path) {
            dealWith(watchEvent);
        }
    }
}
