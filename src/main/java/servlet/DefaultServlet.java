package servlet;

import catalina.Context;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import http.Request;
import http.Response;
import util.Constant;
import util.XmlUtil;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @Author: zerocoder
 * @Description: 静态资源加载
 * @Date: 2021/2/15 13:48
 */

public class DefaultServlet extends HttpServlet {
    private static DefaultServlet instance = new DefaultServlet();

    public static DefaultServlet getInstance() {
        return instance;
    }

    private DefaultServlet() {

    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        Request request = (Request) req;
        Response response = (Response) res;
        try {
            Context context = request.getContext();
            String uri = request.getUri();
            if (StrUtil.isEmpty(uri) || context == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            // 欢迎页
            if (Constant.SEPARATOR.equals(uri)) {
                uri = XmlUtil.getWelcomeFile(request.getContext());
            }
            String fileName = StrUtil.removePrefix(uri, Constant.SEPARATOR);
            // 多应用
            File file;
            if ((file = FileUtil.file(context.getDocBase(), fileName)).exists()) {
                byte[] bytes = FileUtil.readBytes(file);
                response.setBody(bytes);
                // 多媒体类型
                String extName = FileUtil.extName(file);
                String mimeType = XmlUtil.getMimeType(extName);
                response.setMimeType(mimeType);
                if (fileName.equals("time.html")) {
                    Thread.sleep(1000);
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setDesc(fileName);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            response.setDesc(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LogFactory.get().error(e);
            e.printStackTrace();
        }
    }
}
