package cn.com.bsoft.shiro.filter;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Created by sky on 2016/11/9.
 */

//TODO 后面待实现， 完成用户唯一自有资源权限验证（即用户只能看自己的档案）
public class MyAccessControlFilter extends PermissionsAuthorizationFilter {
    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
        return super.isAccessAllowed(request, response, mappedValue) && checkUserPermission(request, response, mappedValue);
    }

    private boolean checkUserPermission(ServletRequest request, ServletResponse response, Object mappedValue) {
        String userId = SecurityUtils.getSubject().getSession().getAttribute("userSessionId").toString();
        return false;
    }
}
