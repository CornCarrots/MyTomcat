package http;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @Author: zerocoder
 * @Description: 响应体
 * @Date: 2021/2/12 16:14
 */
@Data
public class Response extends BaseResponse {
    private StringWriter stringWriter;

    private PrintWriter printWriter;

    private String mimeType;

    private Integer code;

    private String desc;

    private byte[] body;

    private List<Cookie> cookies;

    private String redirectPath;

    public Response(Integer code, String desc) {
        this("text/html", code, desc);
    }

    public Response(String mimeType, Integer code, String desc) {
        this.mimeType = mimeType;
        this.code = code;
        this.desc = desc;
        this.cookies = new ArrayList<>();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    public static Response success(){
        return new Response(HttpServletResponse.SC_OK, "OK");
    }

    public static Response notFound(String filename){
        Response res =  new Response(HttpServletResponse.SC_NOT_FOUND, "Not Found");
        String html = "<html>\n" +
                "\t<head>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "\n" +
                "\t\t<link href='//fonts.googleapis.com/css?family=Lato:100' rel='stylesheet' type='text/css'>\n" +
                "        \n" +
                "\n" +
                "\t\t<style>\n" +
                "\t\t\tbody {\n" +
                "\t\t\t\tmargin: 0;\n" +
                "\t\t\t\tpadding: 0;\n" +
                "\t\t\t\twidth: 100%;\n" +
                "\t\t\t\theight: 100%;\n" +
                "\t\t\t\tcolor: #B0BEC5;\n" +
                "\t\t\t\tdisplay: table;\n" +
                "\t\t\t\tfont-weight: 100;\n" +
                "\t\t\t\tfont-family: 'Lato';\n" +
                "\t\t\t}\n" +
                "\n" +
                "\t\t\t.container {\n" +
                "\t\t\t\ttext-align: center;\n" +
                "\t\t\t\tdisplay: table-cell;\n" +
                "\t\t\t\tvertical-align: middle;\n" +
                "\t\t\t}\n" +
                "\n" +
                "\t\t\t.content {\n" +
                "\t\t\t\ttext-align: center;\n" +
                "\t\t\t\tdisplay: inline-block;\n" +
                "\t\t\t}\n" +
                "\n" +
                "\t\t\t.title {\n" +
                "\t\t\t\tfont-size: 42px;\n" +
                "\t\t\t\tmargin-bottom: 40px;\n" +
                "\t\t\t}\n" +
                "\t\t</style>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<div class=\"container\">\n" +
                "\t\t\t<div class=\"content\">\n" +
                "\t\t\t\t<div class=\"title\">404 很抱歉，您查看的页面找不到了！</div>\n" +
                "\t\t\t\t<div>查找页面：" + filename +
                "</div>\n" +
                "\t\t\t</div>\n" +
                "\t\t</div>\n" +
                "\t</body>\n" +
                "</html>\n" +
                "\n";
        res.getPrintWriter().println(html);
        return res;
    }

    public static Response found(){
        return new Response(HttpServletResponse.SC_FOUND, "FOUND");
    }

    public static Response error(String e){
        Response res =  new Response(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        String html = "<html>\n" +
                "\t<head>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "\n" +
                "\t\t<link href='//fonts.googleapis.com/css?family=Lato:100' rel='stylesheet' type='text/css'>\n" +
                "        \n" +
                "\n" +
                "\t\t<style>\n" +
                "\t\t\tbody {\n" +
                "\t\t\t\tmargin: 0;\n" +
                "\t\t\t\tpadding: 0;\n" +
                "\t\t\t\twidth: 100%;\n" +
                "\t\t\t\theight: 100%;\n" +
                "\t\t\t\tcolor: #B0BEC5;\n" +
                "\t\t\t\tdisplay: table;\n" +
                "\t\t\t\tfont-weight: 100;\n" +
                "\t\t\t\tfont-family: 'Lato';\n" +
                "\t\t\t}\n" +
                "\n" +
                "\t\t\t.container {\n" +
                "\t\t\t\ttext-align: center;\n" +
                "\t\t\t\tdisplay: table-cell;\n" +
                "\t\t\t\tvertical-align: middle;\n" +
                "\t\t\t}\n" +
                "\n" +
                "\t\t\t.content {\n" +
                "\t\t\t\ttext-align: center;\n" +
                "\t\t\t\tdisplay: inline-block;\n" +
                "\t\t\t}\n" +
                "\n" +
                "\t\t\t.title {\n" +
                "\t\t\t\tfont-size: 42px;\n" +
                "\t\t\t\tmargin-bottom: 40px;\n" +
                "\t\t\t}\n" +
                "\t\t</style>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<div class=\"container\">\n" +
                "\t\t\t<div class=\"content\">\n" +
                "\t\t\t\t<div class=\"title\">500 很抱歉，服务器出现问题了！</div>\n" +
                "\t\t\t\t<div>错误：" + e +
                "</div>\n" +
                "\t\t\t</div>\n" +
                "\t\t</div>\n" +
                "\t</body>\n" +
                "</html>\n" +
                "\n";
        res.getPrintWriter().println(html);
        return res;
    }

    public byte[] getBody(){
        if (this.body == null){
            String content = stringWriter.toString();
            return content.getBytes(StandardCharsets.UTF_8);
        }
        return this.body;
    }

    @Override
    public String toString() {
        return "Response{" +
                "stringWriter=" + stringWriter +
                ", printWriter=" + printWriter +
                ", mimeType='" + mimeType + '\'' +
                ", code=" + code +
                ", desc='" + desc + '\'' +
                ", body=" + Arrays.toString(body) +
                '}';
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    @Override
    public int getStatus() {
        return code;
    }

    @Override
    public void setStatus(int status) {
        this.code = status;
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    @Override
    public void sendRedirect(String s) throws IOException {
        this.redirectPath = s;
    }

    private static final SimpleDateFormat formater = new SimpleDateFormat("EEE,d MMM yyyy HH:mm:ss 'GMI'", Locale.ENGLISH);

    public String getCookieHeader(){
        if (CollUtil.isEmpty(cookies)){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Cookie cookie: cookies) {
            // cookie信息
            String name = cookie.getName();
            String value = cookie.getValue();
            int maxAge = cookie.getMaxAge();
            String path = cookie.getPath();
            // key-value
            builder.append("\r\n")
                    .append("Set-Cookie:")
                    .append(name)
                    .append("=")
                    .append(value)
                    .append(";");
            // 过期时间
            if (maxAge > 0){
                DateTime expire = DateUtil.offset(DateUtil.date(), DateField.SECOND, maxAge);
                builder.append("Expires=")
                        .append(formater.format(expire))
                        .append(";");
            }
            // 路径
            if (StrUtil.isNotEmpty(path)){
                builder.append("Path=")
                        .append(path);
            }
        }
        return builder.toString();
    }

    public void resetBuffer() {
        //处理底层buffer，设置长度为0
        this.stringWriter.getBuffer().setLength(0);
    }
}
