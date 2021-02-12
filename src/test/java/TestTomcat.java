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
    private static int port = 10086;
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

    private String getConnectionStr(String url){
        url = StrUtil.format("http://{}:{}{}", ip, port, url);
        return MyBrowserUtil.getConnectionString(url);
    }
}
