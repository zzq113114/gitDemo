package cn.com.bsoft.service;

import cn.com.bsoft.adapter.AdapterUtil;
import cn.com.bsoft.entity.PatientAuthFormMap;
import cn.com.bsoft.entity.RoleFormMap;
import cn.com.bsoft.entity.UserFormMap;
import cn.com.bsoft.mapper.AuthMapper;
import cn.com.bsoft.mapper.BDB.Ehr_HealthRecordMapper;
import cn.com.bsoft.mapper.RoleMapper;
import cn.com.bsoft.util.Common;
import cn.com.bsoft.util.CommonConsts;
import cn.com.bsoft.util.GetRequestJsonUtils;
import cn.com.bsoft.util.JsonUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Service
public class PatientAuthService {
    private static final Logger logger = LoggerFactory.getLogger(PatientAuthService.class);
    @Autowired
    private AuthMapper authMapper;
    @Autowired
    private CommonService commonService;
    @Autowired
    private ConfigService configService;

    @Autowired
    private SendMsgService sendMsgService;
    @Autowired
    private Ehr_HealthRecordMapper ehrHealthRecordMapper;
    @Autowired
    private RoleMapper roleMapper;

    /**
     * 验证授权
     *
     * @return
     */
    private boolean checkAuth(String empiid, String ip) {
        String flag = configService.getConfig(ConfigService.AUTH_FLAG);

        //未启用验证或拥有对应角色，不验证
        if (Common.isEmpty(flag) || !"1".equals(flag)) {
            return true;
        } else {
            boolean hasRole = false;
            try {
                String userId =((UserFormMap) Common.findUserSession()).get("id")+"";
                RoleFormMap map = new RoleFormMap();
                map.put("userId", userId);
                List<RoleFormMap> roleFormMaps = roleMapper.seletUserRole(map);
                for (RoleFormMap role : roleFormMaps) {
                    if (CommonConsts.AUTH_ROLE.equalsIgnoreCase((String) role.get("roleKey"))) {
                        hasRole = true;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                hasRole = false;
            }
            if (hasRole) {
                return true;
            }
        }
        try {
            if (!Common.isEmpty(empiid) && !Common.isEmpty(ip)) {
                Map<String, Object> authData = getAuthFormMap(ip, empiid);
                if (!CollectionUtils.isEmpty(authData)) {
                    Integer status = (Integer) authData.get("status");
                    Long timeActive = (Long) authData.get("time_active");
                    if (status != null && status == 1) {
                        if (timeActive != null && timeActive > System.currentTimeMillis()) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkAuth(HttpServletRequest request, HttpServletResponse response, String empiid) {
        try {
            String ip = Common.getRealIp(request);
            if (!checkAuth(empiid, ip)) {
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put("uri", request.getServletPath());
                requestMap.put("method", request.getMethod());
                requestMap.put("params", AdapterUtil.MapTransfrom(request.getParameterMap()));
                response.sendRedirect(request.getContextPath() + "/vcode.do?callback=" + Common.encodeUrlParam(Common.encodeName(JsonUtils.mapToJson(requestMap))) + "&empiid=" + Common.encodeUrlParam(empiid));
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkAuth(HttpServletRequest request, HttpServletResponse response, String phrid, String organCode) {
        String empiid = commonService.getEmpiidByPhridAndOrganCode(phrid, organCode);
        return checkAuth(request, response, empiid);
    }

    public boolean checkAuth(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> paramMap = GetRequestJsonUtils.getRequestJsonMap(request);
        String empiid = (String) paramMap.get("empiid");
        if (Common.isNotEmpty(empiid)) {
            return checkAuth(request, response, empiid);
        } else {
            String phrid = (String) paramMap.get("phrid");
            String organcode = (String) paramMap.get("organcode");
            return checkAuth(request, response, phrid, organcode);
        }
    }

    /**
     * 获取验证码
     *
     * @param ip
     * @param empiid
     * @throws Exception
     */
    public boolean getVCode(String ip, String empiid) {
        try {
            Map<String, Object> authData = getAuthFormMap(ip, empiid);
            PatientAuthFormMap formMap = new PatientAuthFormMap();
            String vcode = getVCode(6);
            formMap.put("vcode", vcode);

            int minute = getActiveTime(ConfigService.TIME_VCODE_ACTIVE, 30);

            formMap.put("vcode_active_time", Common.dateToStamp(Common.appointTimeToString(new Date(), 0, 0, 0, 0, minute, 0)));
            formMap.put("time", Common.formatDateTimeToString(new Date()));
            if (!CollectionUtils.isEmpty(authData)) {
                formMap.put("id", authData.get("id"));
                authMapper.editEntity(formMap);
            } else {
                formMap.put("ip", ip);
                formMap.put("empiid", empiid);
                authMapper.addEntity(formMap);
            }
            //发送短信
            Map<String, Object> jm = ehrHealthRecordMapper.findJMByEmpiid(empiid);
            String mobile = null;
            if (CollectionUtils.isEmpty(jm)) {
                logger.info("发送验证码失败,JM数据为空====》{}", empiid);
                return false;
            }
            mobile = (String) jm.get("mobile");
            if (Common.isEmpty(mobile)) {
                logger.info("发送验证码失败,JM表mobile为空====》{}", empiid);
                return false;
            }
            setVcodeMsg(vcode, mobile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取最新一条验证信息(默认当天)
     *
     * @param ip
     * @param empiid
     * @return
     */
    private Map<String, Object> getAuthFormMap(String ip, String empiid) {
        Map<String, Object> map = new HashMap<>();
        map.put("ip", ip);
        map.put("empiid", empiid);
        Date now = new Date();
        map.put("sj_to", Common.formatDateTimeToString(now));
        int hour = getActiveTime(ConfigService.TIME_AUTH_ACTIVE, 0);
        if (hour == 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                    0, 0, 0);
            map.put("sj_from", Common.formatDateTimeToString(calendar.getTime()));
        } else {
            map.put("sj_from", Common.appointTimeToString(now, 0, 0, 0, -hour, 0, 0));
        }
        return authMapper.getAuthData(map);
    }

    /**
     * 获取验证码
     *
     * @return
     */
    private String getVCode(int size) {
        String str = "0123456789";
//        String str="ABCDEFGHJKLMNPQRSTUVWXY0123456789";
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            char ch = str.charAt(new Random().nextInt(str.length()));
            sb.append(ch);
        }
        String vcode = sb.toString();
        logger.info("生成验证码======>" + vcode);
        return vcode;
    }

    /**
     * 校验验证码
     *
     * @param
     * @return
     */
    public int checkVCode(HttpServletRequest request) {
        try {
            Map<String, Object> paramMap = GetRequestJsonUtils.getRequestJsonMap(request);
            if (!CollectionUtils.isEmpty(paramMap)) {
                String empiid = (String) paramMap.get("empiid");
                String code = (String) paramMap.get("code");
                if (Common.isEmpty(code) || Common.isEmpty(empiid)) {
                    return 0;
                }
                String ip = Common.getRealIp(request);
                Map<String, Object> authData = getAuthFormMap(ip, empiid);
                if (!CollectionUtils.isEmpty(authData)) {
                    Long activeTime = (Long) authData.get("vcode_active_time");
                    String vcode = (String) authData.get("vcode");
                    if (Common.isEmpty(vcode) || activeTime == null || activeTime < System.currentTimeMillis()) {
                        return -1;
                    }
                    if (code.equalsIgnoreCase(vcode)) {
                        PatientAuthFormMap map = new PatientAuthFormMap();
                        map.put("id", authData.get("id"));
                        map.put("status", 1);
                        int hour = getActiveTime(ConfigService.TIME_AUTH_ACTIVE, 24);
                        map.put("time_active", Common.dateToStamp(Common.appointTimeToString(new Date(), 0, 0, 0, hour, 0, 0)));
                        authMapper.editEntity(map);
                        return 1;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 发送短信验证码
     *
     * @param vcode
     */
    public boolean setVcodeMsg(String vcode, String mobile) {
        String content = "尊敬的用户，您的健康档案信息正在被调阅，验证码是：" + vcode + "，请不要把验证码泄露给其他人。";
        return sendMsgService.sendMsg(content, mobile);
    }

    /**
     * 获取默认时间
     *
     * @param code
     * @param def
     * @return
     */
    private int getActiveTime(String code, int def) {
        String timeActive = configService.getConfig(code);
        int time = def;
        if (Common.isNotEmpty(timeActive)) {
            try {
                time = Integer.parseInt(timeActive);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return time;
    }

}
