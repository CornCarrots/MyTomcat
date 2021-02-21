package util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Author: zerocoder
 * @Description: 浏览器
 * @Date: 2021/2/10 17:43
 */

public class MyBrowserUtil {
    public static void main(String[] args) {
        String url = "http://localhost:10086/";
        System.out.println("get connectionString:");
        String connectionString = getConnectionString(url);
        System.out.println(connectionString);
        System.out.println("-------------------");
        System.out.println("get httpString:");
        String httpString = getHttpString(url);
        System.out.println(httpString);
    }

    /**
     * 字符串的HTTP响应内容
     * @param url
     * @return
     */
    public static String getConnectionString(String url){
        return getConnectionString(url, false);
    }

    private static String getConnectionString(String url, boolean isGzip){
        byte[] connectionBytes = getConnectionBytes(url, isGzip);
        if (ArrayUtil.isEmpty(connectionBytes)){
            return null;
        }
        return new String(connectionBytes, StandardCharsets.UTF_8).trim();
    }

    private static String getConnectionString(String url, Map<String, Object> params, boolean isGet){
        return getConnectionBytes(url, params, isGet);
    }

    /**
     * 二进制的HTTP响应内容
     * @param url
     * @return
     */
    public static byte[] getConnectionBytes(String url){
        return getConnectionBytes(url, false);
    }

    private static byte[] getConnectionBytes(String url, boolean isGzip){
        byte[] response = getHttpBytes(url, isGzip);
        byte[] nextLine = "\r\n\r\n".getBytes();

        int index = -1;
        // 匹配换行
        for (int i = 0; i < response.length - nextLine.length; i++) {
            byte[] temp = Arrays.copyOfRange(response, i, i + nextLine.length);
            if (Arrays.equals(temp, nextLine)){
                index = i;
                break;
            }
        }
        if (index == -1){
            return null;
        }
        //HTTP/1.1 200 OK
        //Content-Type: text/html
        //
        index += nextLine.length;
        // 截取换行之后的html内容
        return Arrays.copyOfRange(response, index, response.length);
    }

    //TODO
    public static String getConnectionBytes(String url, Map<String, Object> params, boolean isGet){
        return "";
    }
    /**
     * 字符串的HTTP响应
     * @param url
     * @return
     */
    public static String getHttpString(String url){
        return getHttpString(url, false);
    }

    private static String getHttpString(String url, boolean isGzip){
        byte[] bytes = getHttpBytes(url, isGzip);
        return new String(bytes).trim();
    }

    /**
     * 二进制的HTTP响应
     * @param url
     * @return
     */
    private static byte[] getHttpBytes(String url){
        return getHttpBytes(url, false);
    }

    private static byte[] getHttpBytes(String url, boolean isGzip){
        byte[] result = null;
        try {
            URL urls = new URL(url);
            Socket client = new Socket();
            int port = urls.getPort();
            if (port == -1){
                port = 80;
            }
            // 客户端建立HTTP连接
            InetSocketAddress address = new InetSocketAddress(urls.getHost(), port);
            client.connect(address, 1000);

            // output:GET/HTTP/1.1
            //Accept:text/html
            //Connection:close
            //User-Agent:browser / java1.8
            //Host:localhost:10086
            StringBuilder builder = new StringBuilder();

            // 请求方式+协议
            String path = urls.getPath();
            if (StrUtil.isEmpty(path)){
                path = Constant.SEPARATOR;
            }
            String firstLine = "GET " + path + " HTTP/1.1\r\n";
            builder.append(firstLine);

            // 请求头
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Host", urls.getHost() + ":" + port);
            requestHeaders.put("Accept", "text/html");
            requestHeaders.put("Connection", "close");
            requestHeaders.put("User-Agent", "browser / java1.8");
            if (isGzip){
                requestHeaders.put("Accept-Encoding", "gzip");
            }
            Set<String> headers = requestHeaders.keySet();
            for (String header: headers) {
                String headerLine = header + ":" + requestHeaders.get(header) + "\r\n";
                builder.append(headerLine);
            }

            // 发送请求
            PrintWriter printWriter = new PrintWriter(client.getOutputStream(), true);
            printWriter.println(builder);
            // 获取响应
            InputStream inputStream = client.getInputStream();
            result = readBytes(inputStream, true);
            // 关闭连接
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = e.toString().getBytes(StandardCharsets.UTF_8);
        }
        return result;
    }

    public static byte[] readBytes(InputStream inputStream, boolean isFully) throws IOException{
        int size = 1024;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[size];
        int len;
        while ((len = inputStream.read(buffer)) > 0){
            outputStream.write(buffer, 0, len);
            // 读取完整的数据
            // 类似一个请求 由于是长连接的，就不需要一次性读取完整数据
            if (!isFully && len <= size){
                break;
            }
        }
//        while (true){
//            len = inputStream.read(buffer);
//            if (-1 == len){
//                break;
//            }
//            outputStream.write(buffer, 0, len);
//            if (!isFully && len <= size){
//                break;
//            }
//        }
        return outputStream.toByteArray();
    }

}