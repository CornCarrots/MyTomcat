package util;

import java.util.concurrent.*;

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

    public static Object runSync(Callable c) throws ExecutionException, InterruptedException {
        Future future = threadPool.submit(c);
        return future.get();
    }
}
