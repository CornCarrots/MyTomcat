package util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import http.Request;
import http.Response;
import http.StandardSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: zerocoder
 * @Description: session管理器
 * @Date: 2021/2/23 13:57
 */

public class SessionManager {
    /**
     * session池
     */
    private static Map<String, StandardSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * session默认超时时间
     */
    private static int defaultTimeout = getDefaultTimeout();

    static {
        checkSessionTimeOut();
    }

    /**
     * 获取失效时间
     * @return
     */
    private static int getDefaultTimeout(){
        int time = 30;
        try {
            Document document = Jsoup.parse(Constant.WEB_XML, StandardCharsets.UTF_8.name());
            Element select = document.selectFirst("session-config session-timeout");
            if (ObjectUtil.isNotNull(select)){
                time = Convert.toInt(select.text());
            }
        }catch (Exception ignored){
        }
        return time;
    }

    /**
     * 每隔3秒检查过期session
     */
    public static void checkSessionTimeOut(){
        Runnable runnable = new Runnable() {
            @lombok.SneakyThrows
            @Override
            public void run() {
                while (true){
                    List<String> timeoutJsessionIds = new ArrayList<>();
                    Set<String> jessionIds = sessionMap.keySet();
                    for (String jessionId: jessionIds) {
                        StandardSession standardSession = sessionMap.get(jessionId);
                        int maxInactiveInterval = standardSession.getMaxInactiveInterval();
                        long lastAccessedTime = standardSession.getLastAccessedTime();
                        long interval = System.currentTimeMillis() - lastAccessedTime;
                        if (interval > maxInactiveInterval * 1000){
                            timeoutJsessionIds.add(jessionId);
                        }
                    }
                    if (CollectionUtil.isNotEmpty(timeoutJsessionIds)){
                        timeoutJsessionIds.forEach(jessionId -> sessionMap.remove(jessionId));
                    }
                    Thread.sleep(3000);
                }
            }
        };
        ThreadUtil.run(runnable);
    }

    /**
     * 生成sessionId
     * 一：生成随机数
     * 二：MD5加密
     * 三：大写
     * @return
     */
    public static synchronized String generateSessionId(){
        String res;
        byte[] bytes = RandomUtil.randomBytes(16);
        res = new String(bytes);
        res = SecureUtil.md5(res);
        res = res.toUpperCase();
        return res;
    }

    /**
     * 创建session
     * @param request
     * @param response
     * @return
     */
    public static HttpSession createSession(Request request, Response response){
        ServletContext servletContext = request.getServletContext();
        String sessionId = generateSessionId();
        StandardSession session = new StandardSession(sessionId, servletContext);
        session.setMaxInactiveInterval(defaultTimeout);
        session.setLastAccessTime(DateUtil.date().getTime());
        sessionMap.put(sessionId, session);
        createCookieBySession(session, request, response);
        return session;
    }


    /**
     * 获取session 没有则创建
     * @param sessionId
     * @param request
     * @param response
     * @return
     */
    public static HttpSession getSession(String sessionId, Request request, Response response){
        if (StrUtil.isEmpty(sessionId)){
            return createSession(request, response);
        }
        StandardSession standardSession = sessionMap.get(sessionId);
        if (ObjectUtil.isNull(standardSession)){
            return createSession(request, response);
        }
        standardSession.setLastAccessTime(System.currentTimeMillis());
        createCookieBySession(standardSession, request, response);
        return standardSession;
    }

    /**
     * 根据session创建cookie
     * @param session
     * @param request
     * @param response
     */
    public static void createCookieBySession(HttpSession session, Request request, Response response){
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setMaxAge(session.getMaxInactiveInterval());
        cookie.setPath(request.getContext().getPath());
        response.addCookie(cookie);
    }

}
