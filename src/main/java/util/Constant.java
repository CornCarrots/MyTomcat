package util;

import cn.hutool.system.SystemUtil;

import java.io.File;

/**
 * @Author: zerocoder
 * @Description:
 * @Date: 2021/2/12 18:33
 */

public class Constant {
    public static final String RESPONSE_HEAD = "HTTP/1.1 {} {}\r\n" + "Content-Type: {}\r\n\r\n";

    public static final File WEBAPPS_FOLDER = new File(SystemUtil.get("user.dir"), "webapps");

    public static final File ROOT_FOLDER = new File(WEBAPPS_FOLDER, "ROOT");
}
