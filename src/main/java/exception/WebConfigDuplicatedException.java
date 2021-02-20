package exception;

/**
 * @Author: zerocoder
 * @Description: servlet重复配置的异常
 * @Date: 2021/2/14 21:54
 */

public class WebConfigDuplicatedException extends Exception{
    public WebConfigDuplicatedException(String msg) {
        super(msg);
    }
}
