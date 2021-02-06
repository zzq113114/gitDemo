package cn.com.bsoft.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 儿童相关记录（各地不同情况）
 */
public interface ChildRecordService {

    /**
     * 预防接种
     *
     * @param param
     * @return
     */
    List<HashMap<String, Object>> vaccinationRecords(Map<String, Object> param);

    /**
     * 获取儿童基本信息
     *
     * @param param
     * @return
     */
    List<HashMap<String, Object>> childBaseInfos(Map<String, Object> param);

    /**
     * 获取新生儿访视
     *
     * @param param
     * @return
     */
    List<HashMap<String, Object>> babyVisitRecords(Map<String, Object> param);

    /**
     * 出生医学证明
     *
     * @param param
     * @return
     */
    List<HashMap<String, Object>> birthCertificate(Map<String, Object> param);
}
