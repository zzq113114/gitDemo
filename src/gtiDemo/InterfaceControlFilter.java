package cn.com.bsoft.shiro.filter;

import cn.com.bsoft.util.Common;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Created by chenzw on 2017/8/31.
 */
public class InterfaceControlFilter extends FormAuthenticationFilter {

    @Override
    public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        Subject subject = getSubject(request, response);
        Session session = subject.getSession();
        if(null != session.getAttribute("InterfaceFlag") && Common.INTERFACE_FLAG.equals((String)session.getAttribute("InterfaceFlag"))){
            return true;
        }else{
            return super.onPreHandle(request,response,mappedValue);
        }
    }
}
