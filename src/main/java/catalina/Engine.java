package catalina;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import util.XmlUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Author: zerocoder
 * @Description: 引擎
 * @Date: 2021/2/13 16:37
 */

public class Engine {
    private Host defaultHost;

    private List<Host> hosts;

    private Service service;

    public Engine(Service service) {
        TimeInterval timer = DateUtil.timer();
        this.service = service;
        String engineDefaultHost = XmlUtil.getEngineDefaultHost();
        LogFactory.get().info("[load Engine] default host is {}", engineDefaultHost);
        hosts = XmlUtil.listHosts(this);
        if (CollUtil.isEmpty(hosts)){
            throw new RuntimeException("hosts is empty!");
        }
        defaultHost = getHost(engineDefaultHost);
        if (defaultHost == null){
            throw new RuntimeException("[load Engine] default host doesn't exists!");
        }
        LogFactory.get().info("[load Engine] has finished in {} ms", timer.intervalMs());
    }

    public Host getHost(String hostName){
        Optional<Host> optionalHost = hosts.stream().filter(host -> host.getName().equals(hostName)).findFirst();
        return optionalHost.orElse(null);
    }

    public Host getDefaultHost() {
        return defaultHost;
    }

    public Service getService() {
        return service;
    }
}
