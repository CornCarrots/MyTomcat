import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import util.MyBrowserUtil;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zerocoder
 * @Description:
 * @Date: 2021/2/12 12:28
 */

public class TestTomcat {
    private static int port = 8087;
    private static String ip = "127.0.0.1";

    @BeforeClass
    public static void before(){
        if (NetUtil.isUsableLocalPort(port)){
            System.out.println("server don't listen port!");
            System.exit(1);
        }
        System.out.println("start test!");
    }

    @Test
    public void test(){
        String html = getConnectionStr("/");
        System.out.println(html);
        Assert.assertEquals(html, "Hello MyTomcat");
    }

    @Test
    public void testHtml(){
        String html = getConnectionStr("/test.html");
        System.out.println(html);
        Assert.assertEquals(html, "Hey! Hello MyTomcat!");
    }

    @Test
    public void testTime() throws InterruptedException {
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(20, 20, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(10));
        TimeInterval timeInterval = DateUtil.timer();
        for (int i = 0; i < 3; i++) {
            poolExecutor.execute(() -> getConnectionStr("/time.html"));
        }
        poolExecutor.shutdown();
        poolExecutor.awaitTermination(1, TimeUnit.HOURS);
        long intervalMs = timeInterval.intervalMs();
        Assert.assertTrue(intervalMs < 3000);
    }

    @Test
    public void testWeb(){
        String html = getConnectionStr("/demo/index.html");
        System.out.println(html);
        Assert.assertEquals(html, "Hello Demo!");
    }

    @Test
    public void testWeb2(){
        String html = getConnectionStr("/a/index.html");
        System.out.println(html);
        Assert.assertEquals(html, "Hello Demo 2!");
    }

    @Test
    public void testWeb3(){
        String html = getConnectionStr("/a/b/index.html");
        System.out.println(html);
        Assert.assertEquals(html, "Hello Demo 3!");
    }

    @Test
    public void testJavaweb(){
        String html = getConnectionStr("/javaweb/hello");
        System.out.println(html);
        Assert.assertEquals(html, "Hello javaweb!");
    }

    @Test
    public void testMimeType(){
        String html = getHttpStr("/a/test.txt");
        System.out.println(html);
        containAssert(html, "Content-Type: text/plain");
    }

    @Test
    public void testPNG(){
        byte[] bytes = getConnectionBytes("/a/test.jpg");
        System.out.println(bytes.length);
        Assert.assertEquals(35241, bytes.length);
    }

    @Test
    public void testServlet(){
        String html = getConnectionStr("/j2ee/hello");
        System.out.println(html);
        Assert.assertEquals(html, "Hello Servlet");
    }

    @Test
    public void testHead(){
        String html = getConnectionStr("/test.html");
        System.out.println(html);
        Assert.assertEquals(html, "Hello Servlet");
    }

    private void containAssert(String html, String string){
        boolean any = StrUtil.containsAny(html, string);
        Assert.assertTrue(any);
    }

    private String getConnectionStr(String url){
        url = StrUtil.format("http://{}:{}{}", ip, port, url);
        return MyBrowserUtil.getConnectionString(url);
    }

    private byte[] getConnectionBytes(String url){
        url = StrUtil.format("http://{}:{}{}", ip, port, url);
        return MyBrowserUtil.getConnectionBytes(url);
    }

    private String getHttpStr(String url){
        url = StrUtil.format("http://{}:{}{}", ip, port, url);
        return MyBrowserUtil.getHttpString(url);
    }
}
