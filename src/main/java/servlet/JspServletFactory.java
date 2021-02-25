package servlet;

import catalina.Context;
import classloader.JspClassLoader;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import http.Request;
import http.Response;
import util.Constant;
import util.JspUtil;
import util.XmlUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @Author: zerocoder
 * @Description: jsp处理器
 * @Date: 2021/2/23 23:41
 */

public class JspServletFactory extends HttpServlet {
    private static JspServletFactory instance = new JspServletFactory();

    private JspServletFactory() {
    }

    public static JspServletFactory getInstance() {
        return instance;
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Request request = (Request) req;
        Response response = (Response) resp;
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
            File jspFile;
            if ((file = FileUtil.file(request.getRealPath(fileName))).exists()) {
                jspFile = file;
                String path = context.getPath();
                String subFolder;
                if (Constant.SEPARATOR.equals(path)) {
                    subFolder = "_";
                }
                else {
                    subFolder = StrUtil.subAfter(path, "/", false);
                }
                String servletClassPath = JspUtil.getServletClassPath(uri, subFolder);
                File servletClassFile = new File(servletClassPath);
                if (!servletClassFile.exists()) {
                    JspUtil.compileFile(context, jspFile);
                }else if(jspFile.lastModified() > servletClassFile.lastModified()){
                    JspUtil.compileFile(context, jspFile);
                    JspClassLoader.invalidJspClassLoader(uri, context);
                }
                // 多媒体类型
                String extName = FileUtil.extName(file);
                String mimeType = XmlUtil.getMimeType(extName);
                response.setMimeType(mimeType);

                // 获取类
                JspClassLoader jspClassLoader = JspClassLoader.getJspClassLoader(uri, context);
                String servletClassName = JspUtil.getServletClassName(uri, subFolder);
                // 加载类
                Class<?> jspClass = jspClassLoader.loadClass(servletClassName);
                HttpServlet servlet = context.getServletByPool(jspClass);
                // 使用类
                servlet.service(request, response);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setDesc(fileName);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            response.setDesc(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LogFactory.get().error(e);
        }
    }
}
