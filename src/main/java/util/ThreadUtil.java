package util;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zerocoder
 * @Description: 线程工具
 * @Date: 2021/2/12 22:43
 */

public class ThreadUtil {
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 100, 60, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(10));

    public static void run(Runnable r){
        threadPool.execute(r);
    }
}
