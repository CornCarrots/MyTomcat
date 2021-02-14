package http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @Author: zerocoder
 * @Description: 响应体
 * @Date: 2021/2/12 16:14
 */

public class Response {
    private StringWriter stringWriter;

    private PrintWriter printWriter;

    private String mimeType;

    private Integer code;

    private String desc;

    private byte[] body;

    public Response(Integer code, String desc) {
        this("text/html", code, desc);
    }

    public Response(String mimeType, Integer code, String desc) {
        this.mimeType = mimeType;
        this.code = code;
        this.desc = desc;
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    public static Response success(){
        return new Response(200, "OK");
    }

    public static Response notFound(String filename){
        Response res =  new Response(404, "Not Found");
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

    public static Response error(Exception e){
        Response res =  new Response(500, "Server error");
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
                "\t\t\t\t<div>错误：" + e.getMessage() +
                "</div>\n" +
                "\t\t\t</div>\n" +
                "\t\t</div>\n" +
                "\t</body>\n" +
                "</html>\n" +
                "\n";
        res.getPrintWriter().println(html);
        return res;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public byte[] getBody(){
        return this.body;
//        String content = stringWriter.toString();
//        return content.getBytes(StandardCharsets.UTF_8);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setBody(byte[] body) {
        this.body = body;
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
}
