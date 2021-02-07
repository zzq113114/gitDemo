package cn.com.bsoft.service.impl;


import cn.com.bsoft.service.SendMsgService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 短信发送
 */
@Profile(value = {"DEF","TC"})
@Service
public class SendDefHttpService implements SendMsgService {


    @Override
    public boolean sendMsg(String msg, String mobile) {
        return true;
    }
}
