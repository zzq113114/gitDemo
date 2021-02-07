package cn.com.bsoft.service.impl;

import cn.com.bsoft.mapper.BDB.Ehr_HealthRecordMapper;
import cn.com.bsoft.mapper.BDB.Ehr_VisitRecordMapper;
import cn.com.bsoft.service.ChildRecordService;
import cn.com.bsoft.service.data.PhridService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Profile(value = {"DEF","TX"})
@Service
public class ChildRecordServiceDefImpl implements ChildRecordService {
    @Autowired
    private Ehr_VisitRecordMapper ehr_VisitRecordMapper;
    @Autowired
    private PhridService phridService;
    @Autowired
    private Ehr_HealthRecordMapper ehr_healthRecordMapper;

    /**
     * 根据phrid查询儿童接种
     *
     * @param param
     * @return
     */
    @Override
    public List<HashMap<String, Object>> vaccinationRecords(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
//            param.put("organ_code", tmp.get("ORGAN_CODE"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getVaccinationRecordList(param);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        return result;
    }

    /**
     * 根据phrid查询儿童基本信息
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
            if (!org.springframework.util.CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
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
            if (!org.springframework.util.CollectionUtils.isEmpty(tempList)) {
                temp.addAll(tempList);
            }
        }

        map = new HashMap<>();
        for (HashMap<String, Object> tmp : temp) {
            map.put("etfsjbbh", tmp.get("KEY_CODE"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getBabyVisitList(map);
            if (!org.springframework.util.CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        return result;
    }

    /**
     * 根据姓名性别出生日期
     *
     * @param param
     * @return
     */
    @Override
    public List<HashMap<String, Object>> birthCertificate(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
//        Map<String, Object> personSimpleData = ehr_healthRecordMapper.findPersonSimpleData(param);
//        if (!org.springframework.util.CollectionUtils.isEmpty(personSimpleData)) {
//            result = ehr_VisitRecordMapper.getbirthCertificateList(personSimpleData);
//        }

        Map<String, Object> map = new HashMap<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            map.put("phrid", tmp.get("PHRID"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getbirthCertificateList(map);
            if (!org.springframework.util.CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }

        return result;
    }
}
