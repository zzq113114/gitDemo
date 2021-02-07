package cn.com.bsoft.service.impl;


import cn.com.bsoft.service.SendMsgService;
import cn.com.bsoft.util.WebServiceClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 使用webservice方式发送
 */
@Profile(value = "TX")
@Service
public class SendMsgWSService implements SendMsgService {
    private static final Logger logger = LoggerFactory.getLogger(SendMsgWSService.class);
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
    @Value("${sms.server.telNum}")
    String telNum = "";

    @Override
    public boolean sendMsg(String msg, String mobile) {
        try {
            logger.info("发送短信：====》{}", "开始发送短信" + Url);
            // 创建动态客户端
            int pwd = Integer.parseInt(mobile.substring(mobile.length() - 4)) * 3 + 3023;
            String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><infos><info><msg_id><![CDATA[-1]]></msg_id>" +
                    "<password><![CDATA[" + pwd + "]]></password><src_tele_num><![CDATA[" + telNum + "]]></src_tele_num>" +
                    "<dest_tele_num><![CDATA[" + mobile + "]]></dest_tele_num>" +
                    "<msg><![CDATA[" + msg + "]]></msg></info></infos>";
            logger.info("发送短信：====》{}", message);
            Object object = WebServiceClientUtils.send(Url, "sendmsg", account, message);
            logger.info("返回数据:====>{}", object);
//            if("<state><![CDATA[0]]></state>"){
//String ss = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><infos><info><msg_id><![CDATA[18758126840]]></msg_id><state><![CDATA[0]]></state></info></infos>";
//            }


            return true;
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
