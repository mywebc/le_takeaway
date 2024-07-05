package com.chenxiaolani.le_takeaway.filter;

import com.alibaba.fastjson.JSON;
import com.chenxiaolani.le_takeaway.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录检查过滤器
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    // 专门做路径匹配的工具类
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 1. 从session中获取用户id
        // 2. 判断用户id是否存在
        // 3. 如果存在，放行
        // 4. 如果不存在，跳转到登录页面
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取请求的url
        String url = request.getRequestURI();
        log.info("本次拦截的请求...{}", url);

        // 不需要登录的url
        String[] urls = new String[]{"/employee/login", "/employee/logout", "/backend/**", "/front/**",};

        boolean match = check(urls, url);
        // 如果是不需要登录的页面，直接放行
        if (match) {
            log.info("本次请求{}不用处理", url);
            filterChain.doFilter(request, response);
            return;
        }
        // 如果是其他页面，判断是否登录
        Object employee = request.getSession().getAttribute("employee");
        if (employee != null) {
            // 已经登录，放行
            log.info("用户已经登录，ID为{}", employee);
            filterChain.doFilter(request, response);
            return;
        }
        log.info("用户未登录");
        // 通过输出流的方式返回json数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     * 判断uri是否在urls中
     *
     * @param urls
     * @param uri
     * @return
     */
    public Boolean check(String[] urls, String uri) {
        for (String url : urls) {
            if (PATH_MATCHER.match(url, uri)) {
                return true;
            }
        }
        return false;
    }
}
