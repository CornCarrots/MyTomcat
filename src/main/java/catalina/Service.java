package catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import util.XmlUtil;

import java.util.List;

/**
 * @Author: zerocoder
 * @Description: 服务
 * @Date: 2021/2/13 17:29
 */

public class Service {
    private String name;

    private Engine engine;

    private Server server;

    private List<Connector> connectors;

    public Service(Server server) {
        TimeInterval timer = DateUtil.timer();
        this.server = server;
        name = XmlUtil.getService();
        LogFactory.get().info("[load Service] service name is {}", name);
        engine = new Engine(this);
        connectors = XmlUtil.listConnectors(this);
        LogFactory.get().info("[load Service] has finished in {} ms", timer.intervalMs());
    }

    public String getName() {
        return name;
    }

    public Engine getEngine() {
        return engine;
    }

    public Server getServer() {
        return server;
    }

    public List<Connector> getConnectors() {
        return connectors;
    }

    public void start(){
        init();
    }

    private void init(){
        TimeInterval timer = DateUtil.timer();
        for (Connector connector: connectors) {
            connector.init();
        }
        LogFactory.get().info("Initializing processed in {} ms", timer.intervalMs());
        for (Connector connector: connectors) {
            connector.start();
        }
    }
}
