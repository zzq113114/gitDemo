package cn.com.bsoft.shiro.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class TicketValidateFilter implements Filter {
    private String serviceUrl;
    private String selfUrl;
    private String logonUrl;
    public static final String TICKET = "sso_ticket";
    public static final String BACKCALL_URL = "sso_backCall";

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpSession session = httpServletRequest.getSession();
        String ticket = (String) httpServletRequest.getAttribute(TICKET);
        if (ticket == null || "".equals(ticket)) {
            ticket = httpServletRequest.getParameter(TICKET);
        }
        if (ticket == null || "".equals(ticket)) {
            ticket = (String) session.getAttribute(TICKET);
        }
        if (ticket == null || "".equals(ticket)) {
            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (TICKET.equals(cookie.getName())) {
                        ticket = cookie.getValue();
                        break;
                    }
                }
            }
        }
        if (ticket == null || "".equals(ticket)) {
            //httpServletResponse.sendRedirect(logonUrl + "?" + BACKCALL_URL + "=" + selfUrl);
            httpServletResponse.sendRedirect(logonUrl);
            return;
        }
        String result = null;
        try {
            result = getURLByPost(serviceUrl, TICKET + "=" + ticket + "&" + BACKCALL_URL + "=" + selfUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> resMap = str2Map(result);
        if (resMap == null || (Integer) resMap.get("code") != 200) {
            httpServletResponse.sendRedirect(logonUrl + "?" + BACKCALL_URL + "=" + selfUrl);
            return;
        }
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (TICKET.equals(cookie.getName()) && !ticket.equals(cookie.getValue())) {
                    cookie.setValue(ticket);
                    break;
                }
            }
        } else {
            Cookie cookie = new Cookie(TICKET, ticket);
            httpServletResponse.addCookie(cookie);
        }
        session.setAttribute(TICKET, ticket);
        session.setAttribute("uid", httpServletRequest.getAttribute("uid"));
        session.setAttribute("urt", httpServletRequest.getAttribute("urt"));
//		Map<String, Object> body= (Map<String, Object>) resMap.get("body");
//		if(body != null && body.size() > 0){
//			session.setAttribute("uid", body.get("uid"));
//		}
        chain.doFilter(request, response);
    }


    public static Map<String, Object> str2Map(String text) {
        JSON json = (JSON) JSONArray.parse(text);
        Map<String, Object> map = JSONArray.toJavaObject(json, Map.class);
        return map;
    }

    /**
     * post方式请求http服务
     *
     * @param urlStr
     * @param params name=yxd&age=25
     * @return
     * @throws Exception
     */
    public static String getURLByPost(String urlStr, String params) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        PrintWriter printWriter = new PrintWriter(conn.getOutputStream());
        printWriter.write(params);
        printWriter.flush();
        BufferedReader in = null;
        StringBuilder sb = new StringBuilder();
        try {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String str = null;
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                conn.disconnect();
                if (in != null) {
                    in.close();
                }
                if (printWriter != null) {
                    printWriter.close();
                }
            } catch (IOException ex) {
                throw ex;
            }
        }
        return sb.toString();
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void setSelfUrl(String selfUrl) {
        this.selfUrl = selfUrl;
    }

    public void setLogonUrl(String logonUrl) {
        this.logonUrl = logonUrl;
    }
}
