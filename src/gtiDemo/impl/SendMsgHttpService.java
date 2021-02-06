package cn.com.bsoft.service.impl;


import cn.com.bsoft.service.SendMsgService;
import cn.com.bsoft.util.HttpRequestUtil;
import org.apache.commons.collections.map.HashedMap;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 溧水短信发送
 */
@Profile(value = "LS")
@Service
public class SendMsgHttpService implements SendMsgService {
    private static final Logger logger = LoggerFactory.getLogger(SendMsgHttpService.class);
    /**
     * 短信服务器地址
     */
    @Value("${sms.server.url}")
    String Url = "";
    /**
     * 短信服务器代理地址
     */
    @Value("${sms.server.host}")
    String host = "";
    /**
     * 短信服务器账号
     */
    @Value("${sms.server.account}")
    String account = "";
    /**
     * 短信服务器密码
     */
    @Value("${sms.server.password}")
    String password = "";

    @Override
    public boolean sendMsg(String msg, String mobile) {
        Map<String, String> map = new HashedMap();
        map.put("account", account);
        map.put("password", password);
        map.put("mobile", mobile);
        map.put("content", msg);
        try {

            logger.info("发送短信：====》{}", msg);
            String SubmitResult = HttpRequestUtil.httpRequestPost(Url, host, map, "GBK");
            Document doc = DocumentHelper.parseText(SubmitResult);
            Element root = doc.getRootElement();

            String code = root.elementText("code");
//            String msg = root.elementText("msg");
//            String smsid = root.elementText("smsid");
//
//            System.out.println(code);
//            System.out.println(msg);
//            System.out.println(smsid);

            if (!"2".equals(code)) {
                logger.info("短信提交失败：{}", root.elementText("msg"));
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
