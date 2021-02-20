package util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.SystemUtil;

import java.io.File;

/**
 * @Author: zerocoder
 * @Description:
 * @Date: 2021/2/12 18:33
 */

public class Constant {
    /**
     * 响应头
     */
    public static final String RESPONSE_HEAD = "HTTP/1.1 {} {}\r\n" + "Content-Type: {}\r\n\r\n";

    /**
     * webapps路径
     */
    public static final File WEBAPPS_FOLDER = new File(SystemUtil.get("user.dir"), "webapps");

    /**
     * ROOT路径
     */
    public static final File ROOT_FOLDER = new File(WEBAPPS_FOLDER, "ROOT");

    /**
     * 配置文件路径
     */
    public static final File CONFIG_FOLDER = new File(SystemUtil.get("user.dir"), "conf");

    /**
     * server.xml路径
     */
    public static final File SERVER_XML = new File(CONFIG_FOLDER, "server.xml");

    /**
     * web.xml路径
     */
    public static final File WEB_XML = new File(CONFIG_FOLDER, "web.xml");

    public static final File CONTEXT_XML = new File(CONFIG_FOLDER, "context.xml");

    /**
     * 分隔符
     */
    public static final String SEPARATOR = "/";

}
