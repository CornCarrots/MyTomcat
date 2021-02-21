package watcher;

import catalina.Context;
import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.log.LogFactory;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * @Author: zerocoder
 * @Description: 文件监听器
 * @Date: 2021/2/20 17:34
 */

public class ContextFileWatcher {

    private WatchMonitor monitor;

    private Boolean isStop;

    private Context context;

    public ContextFileWatcher(Context context) {
        this.context = context;
        this.isStop = false;
        Watcher watcher = new ContextFileMonitor();
        this.monitor = WatchUtil.createAll(context.getDocBase(), Integer.MAX_VALUE, watcher);
        this.monitor.setDaemon(true);
    }

    public void start(){
        monitor.start();
    }

    public void stop(){
        monitor.close();
    }

    class ContextFileMonitor extends SimpleWatcher {
        private void dealWith(WatchEvent<?> event){
            synchronized (ContextFileWatcher.class){
                String fileName = event.context().toString();
                LogFactory.get().info("{} check reload file:{}", ContextFileWatcher.this,fileName);
                if (isStop){
                    return;
                }
                if (fileName.endsWith(".jar") || fileName.endsWith(".class") || fileName.endsWith(".xml")){
                    isStop = true;
                    LogFactory.get().info("{} check file:{} reload", ContextFileWatcher.this, fileName);
                    context.reload();
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
