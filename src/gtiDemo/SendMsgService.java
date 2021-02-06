package cn.com.bsoft.service;

/**
 * 短信服务接口
 */
public interface SendMsgService {
    /**
     * 发送短信
     *
     * @param msg
     * @param mobile
     * @return
     */
    boolean sendMsg(String msg, String mobile);
}
