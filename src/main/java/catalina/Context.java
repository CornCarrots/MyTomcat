package catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;

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

    public Context(String path, String docBase) {
        TimeInterval timer = DateUtil.timer();
        this.path = path;
        this.docBase = docBase;
        LogFactory.get().info("[load Context] Deployment of web application path:{}, directory:{} has finished in {} ms", this.path, this.docBase, timer.intervalMs());
    }

    public String getPath() {
        return path;
    }

    public String getDocBase() {
        return docBase;
    }
}
