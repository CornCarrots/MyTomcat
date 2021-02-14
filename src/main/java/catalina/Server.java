package catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: zerocoder
 * @Description: 服务器
 * @Date: 2021/2/13 17:48
 */

public class Server {

    private Service service;

    public Server() {
        TimeInterval timer = DateUtil.timer();
        LogFactory.get().info("[init Server] start...");
        this.service = new Service(this);
        LogFactory.get().info("[int Server] has finished in {} ms", timer.intervalMs());
    }

    public void start() {
        LogFactory.get().info("[start server] start...");
        loadJVM();
        init();
    }

    /**
     * 启动服务器
     */
    private void init(){
        service.start();
    }


    public Service getService() {
        return service;
    }

    /**
     * 打印JVM基本信息
     */
    private void loadJVM() {
        Map<String, String> infos = new LinkedHashMap<>();
        infos.put("Server version", "MyTomcat/1.0.0");
        infos.put("Server built", DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss"));
        infos.put("Server number", "1.0.0");
        infos.put("OS Name\t", SystemUtil.get("os.name"));
        infos.put("OS Version", SystemUtil.get("os.version"));
        infos.put("Architecture", SystemUtil.get("os.arch"));
        infos.put("Java Home", SystemUtil.get("java.home"));
        infos.put("JVM Version", SystemUtil.get("java.runtime.version"));
        infos.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));
        LogFactory.get().info("------------------------------------");
        for (String key : infos.keySet()) {
            LogFactory.get().info(key + "\t" + infos.get(key));
        }
        LogFactory.get().info("------------------------------------");
    }
}
