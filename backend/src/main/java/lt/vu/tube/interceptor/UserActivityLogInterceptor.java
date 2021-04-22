package lt.vu.tube.interceptor;

import lt.vu.tube.entity.UserActivityLog;
import lt.vu.tube.repository.UserActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.logging.Logger;

@Component
public class UserActivityLogInterceptor implements HandlerInterceptor {
    private static final Logger logger = Logger.getLogger(UserActivityLogInterceptor.class.getSimpleName());


    private UserActivityLogRepository userActivityLogRepository;

    public UserActivityLogInterceptor() {
    }

    @Autowired
    public UserActivityLogInterceptor(UserActivityLogRepository userActivityLogRepository) {
        this.userActivityLogRepository = userActivityLogRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String methodName = handlerMethod.getMethod().getName();
            String className = handlerMethod.getBean().getClass().getSimpleName();
            userActivityLogRepository.save(
                    //TODO get user and talk about permissions
                    new UserActivityLog(
                            "user",
                            "permission",
                            new Timestamp(System.currentTimeMillis()),
                            className + "." + methodName
                    )
            );
            logger.info("[preHandle][" + request + "]" + "[" + request.getMethod()
                    + "]" + request.getRequestURI() + getParameters(request));
            return true;
        }
        throw new RuntimeException("handler not an instance of HandlerMethod");
    }

    private String getParameters(HttpServletRequest request) {
        StringBuilder posted = new StringBuilder();
        Enumeration<?> e = request.getParameterNames();
        if (e != null) {
            posted.append("?");
        }
        while (e.hasMoreElements()) {
            if (posted.length() > 1) {
                posted.append("&");
            }
            String curr = (String) e.nextElement();
            posted.append(curr).append("=");
            if (curr.contains("password")
                    || curr.contains("pass")
                    || curr.contains("pwd")) {
                posted.append("*****");
            } else {
                posted.append(request.getParameter(curr));
            }
        }
        String ip = request.getHeader("X-FORWARDED-FOR");
        String ipAddr = (ip == null) ? getRemoteAddr(request) : ip;
        if (ipAddr!=null && !ipAddr.equals("")) {
            posted.append("&_psip=" + ipAddr);
        }
        return posted.toString();
    }

    private String getRemoteAddr(HttpServletRequest request) {
        String ipFromHeader = request.getHeader("X-FORWARDED-FOR");
        if (ipFromHeader != null && ipFromHeader.length() > 0) {
            logger.info("ip from proxy - X-FORWARDED-FOR : " + ipFromHeader);
            return ipFromHeader;
        }
        return request.getRemoteAddr();
    }
}
