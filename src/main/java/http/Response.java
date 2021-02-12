package http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * @Author: zerocoder
 * @Description: 响应体
 * @Date: 2021/2/12 16:14
 */

public class Response {
    private StringWriter stringWriter;

    private PrintWriter printWriter;

    private String contentType;

    private Integer code;

    private String desc;

    public Response() {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        contentType = "text/html";
        code = 200;
        desc = "OK";
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBody(){
        String content = stringWriter.toString();
        return content.getBytes(StandardCharsets.UTF_8);
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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
}
