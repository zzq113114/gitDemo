package cn.com.bsoft.service.impl;

import cn.com.bsoft.mapper.BDB.Ehr_HealthRecordMapper;
import cn.com.bsoft.mapper.BDB.Ehr_VisitRecordMapper;
import cn.com.bsoft.service.ChildRecordService;
import cn.com.bsoft.service.data.PhridService;
import cn.com.bsoft.util.Common;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 溧水要求，儿童信息查询自己，同时通过idcard当做母亲idcard查询孩子
 */
@Profile("LS")
@Service
public class ChildRecordServiceLsImpl implements ChildRecordService {
    @Autowired
    private Ehr_VisitRecordMapper ehr_VisitRecordMapper;
    @Autowired
    private Ehr_HealthRecordMapper ehr_healthRecordMapper;
    @Autowired
    private PhridService phridService;

    /**
     * 根据儿童身份证或母亲身份证获取孩子信息
     *
     * @param param
     * @return
     */
    @Override
    public List<HashMap<String, Object>> vaccinationRecords(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        //根据儿童身份证或母亲身份证获取孩子信息
        HashMap<String, Object> personInfoById = ehr_healthRecordMapper.getFullPersonInfoById(param);
        if (!CollectionUtils.isEmpty(personInfoById)) {
            String idcard = (String) personInfoById.get("idcard");
            if (Common.isNotEmpty(idcard)) {
                Map<String, Object> map = new HashMap<>();
                map.put("idcard", idcard);
                List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getVaccinationRecordList(map);
                if (!CollectionUtils.isEmpty(tempList)) {
                    result.addAll(tempList);
                }
            }
        }
        return result;
    }

    /**
     * 根据phrid查自己，同时查询孩子
     *
     * @param param
     * @return
     */
    @Override
    public List<HashMap<String, Object>> childBaseInfos(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            map.put("phrid", tmp.get("PHRID"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getETJBQKList(map);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }

        //根据母亲身份证获取孩子信息
        HashMap<String, Object> personInfoById = ehr_healthRecordMapper.getFullPersonInfoById(param);
        if (!CollectionUtils.isEmpty(personInfoById)) {
            String idcard = (String) personInfoById.get("idcard");
            if (Common.isNotEmpty(idcard)) {
                map = new HashMap<>();
                map.put("motherid", idcard);
                List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getETJBQKList(map);
                if (!CollectionUtils.isEmpty(tempList)) {
                    result.addAll(tempList);
                }
            }
        }
        return result;
    }

    @Override
    public List<HashMap<String, Object>> babyVisitRecords(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<HashMap<String, Object>> temp = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            map.put("phrid", tmp.get("PHRID"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getBabyVisitBaseList(map);
            if (!CollectionUtils.isEmpty(tempList)) {
                temp.addAll(tempList);
            }
        }

        //根据母亲身份证获取孩子访视基本信息
        HashMap<String, Object> personInfoById = ehr_healthRecordMapper.getFullPersonInfoById(param);
        if (!CollectionUtils.isEmpty(personInfoById)) {
            String idcard = (String) personInfoById.get("idcard");
            if (Common.isNotEmpty(idcard)) {
                map = new HashMap<>();
                map.put("motherid", idcard);
                List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getBabyVisitBaseList(map);
                if (!CollectionUtils.isEmpty(tempList)) {
                    temp.addAll(tempList);
                }
            }
        }

        map = new HashMap<>();
        for (HashMap<String, Object> tmp : temp) {
            map.put("etfsjbbh", tmp.get("KEY_CODE"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getBabyVisitList(map);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        return result;
    }

    /**
     * 通过母亲身份证号
     *
     * @param param
     * @return
     */
    @Override
    public List<HashMap<String, Object>> birthCertificate(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();

        HashMap<String, Object> personInfoById = ehr_healthRecordMapper.getFullPersonInfoById(param);
        if (!CollectionUtils.isEmpty(personInfoById)) {
            String idcard = (String) personInfoById.get("idcard");
            if (Common.isNotEmpty(idcard)) {
                Map<String, Object> map = new HashMap<>();
                map.put("motherid", idcard);
                List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getbirthCertificateList(map);
                if (!CollectionUtils.isEmpty(tempList)) {
                    result.addAll(tempList);
                }
            }
        }

        return result;
    }
}
