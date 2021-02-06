package cn.com.bsoft.service.data;

import cn.com.bsoft.mapper.BDB.*;
import cn.com.bsoft.service.ChildRecordService;
import cn.com.bsoft.util.Common;
import cn.com.bsoft.util.CommonConsts;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublicHealthService {
    private static final Logger log = Logger.getLogger(HealthInfoService.class);
    @Autowired
    private Ehr_VisitRecordMapper ehr_VisitRecordMapper;
    @Autowired
    private Ipt_RecordMapper ipt_recordMapper;
    @Autowired
    private PhridService phridService;
    @Autowired
    private ChildRecordService childRecordService;
    @Autowired
    private IndexRecordMapper indexRecordMapper;

    /**
     * 第一次产前随访
     *
     * @param param
     * @return
     */
    public List<HashMap<String, Object>> FirstPrenatalfollowupList(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
//            param.put("organ_code", tmp.get("ORGAN_CODE"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getFirstPrenatalfollowupList(param);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }


    /**
     * 第2-5次产前随访
     *
     * @param param
     * @return
     */
    public List<HashMap<String, Object>> prenatalfollowupList(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getPrenatalfollowupList(param);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }

    /**
     * 产后随访
     *
     * @param param
     * @return
     */
    public List<HashMap<String, Object>> postpartumFollowup(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
//            param.put("organ_code", tmp.get("ORGAN_CODE"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getPostpartumFollowupList(param);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }

    /**
     * 产后42天随访
     *
     * @param param
     * @return
     */
    public List<HashMap<String, Object>> postpartum42DayFollowup(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
//            param.put("organ_code", tmp.get("ORGAN_CODE"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getPostpartum42DayFollowupList(param);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }

    /**
     * 出生医学证明
     *
     * @param param
     * @return
     */
    public List<HashMap<String, Object>> birthCertificate(Map<String, Object> param) {
        return childRecordService.birthCertificate(param);
    }

    /**
     * 婚前医学检查
     *
     * @param param
     * @return
     */
    public List<HashMap<String, Object>> premaritalExam(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
//            param.put("organ_code", tmp.get("ORGAN_CODE"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getPremaritalExamList(param);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }

    /**
     * 预防接种
     *
     * @param param
     * @return
     */
    public List<HashMap<String, Object>> vaccinationRecords(Map<String, Object> param) {
        List<HashMap<String, Object>> result = childRecordService.vaccinationRecords(param);

        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }

    public List<HashMap<String, Object>> deliveryInfo(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
//            param.put("organ_code", tmp.get("ORGAN_CODE"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getDeliveryInfoList(param);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }

    public List<HashMap<String, Object>> HighRiskMaternalInfo(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
//            param.put("organ_code", tmp.get("ORGAN_CODE"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getHighRiskMaternalInfoList(param);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }

    public List<HashMap<String, Object>> InfectionInfo(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
//            param.put("organ_code", tmp.get("ORGAN_CODE"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getInfectionInfoList(param);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }

    /**
     * 获取儿童基本信息
     *
     * @param param
     * @return
     */
    public List<HashMap<String, Object>> childBaseInfos(Map<String, Object> param) {
        List<HashMap<String, Object>> result = childRecordService.childBaseInfos(param);

        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }

    /**
     * 获取新生儿访视
     *
     * @param param
     * @return
     */
    public List<HashMap<String, Object>> babyVisitRecords(Map<String, Object> param) {
        List<HashMap<String, Object>> result = childRecordService.babyVisitRecords(param);

        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }

    /**
     * 孕产妇登记
     *
     * @param param
     * @return
     */
    public List<HashMap<String, Object>> maternalRegistration(Map<String, Object> param) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
            List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getPregnancyCardList(param);
            if (!CollectionUtils.isEmpty(tempList)) {
                result.addAll(tempList);
            }
        }
        if (!CollectionUtils.isEmpty(result)) {
            result = Common.sortUnionList(result, "sj", null, null);
        }
        return result;
    }

    /**
     * 门诊既往史数量
     *
     * @param map
     * @return
     */
    public int getMzjwsCount(Map<String, Object> map) {
        List<Map<String, String>> phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) map.get("empiid"),
                CommonConsts.OP_EM_HP_MARK.MEN_ZHEN);
        if (CollectionUtils.isEmpty(phridMap)) {
            return 0;
        }
        Map<String, Object> param = new HashMap<>();
        List<HashMap<String, Object>> result = new ArrayList<>();
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
            param.put("organ_code", tmp.get("ORGAN_CODE"));
            result.addAll(indexRecordMapper.queryOptIndexRecord(param));
        }
        return result.size();
    }

    /**
     * 住院既往史数量
     *
     * @param map
     * @return
     */
    public int getZyjwsCount(Map<String, Object> map) {
        List<Map<String, String>> phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) map.get("empiid"),
                CommonConsts.OP_EM_HP_MARK.ZHU_YUAN);
        if (CollectionUtils.isEmpty(phridMap)) {
            return 0;
        }
        Map<String, Object> param = new HashMap<>();
        List<HashMap<String, Object>> result = new ArrayList<>();
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
            param.put("organ_code", tmp.get("ORGAN_CODE"));
            result.addAll(indexRecordMapper.queryIptIndexRecord(param));
        }
        return result.size();
    }

    /**
     * 计免接种信息
     *
     * @param map
     * @return
     */
    public Map<String, Object> getVaccinationRecordList(Map<String, Object> map) {
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) map.get("empiid"));
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        if (CollectionUtils.isEmpty(phridMap)) {
            result.put("draw", map.get("draw"));
            result.put("data", new HashMap<String, Object>());
            return result;
        }
        List<HashMap<String, Object>> records = new ArrayList<>();
        for (Map<String, String> obj : phridMap) {
            param.put("phrid", obj.get("PHRID"));
            records.addAll(ehr_VisitRecordMapper.getVaccinationInfoList(param));
        }
        for (Map<String, Object> obj : records) {
            String jzrq = (String) obj.get("jzrq");
            switch (jzrq) {
                case "1980-01-01": obj.put("jzrq", "已种同效苗");break;
                case "1981-01-01": obj.put("jzrq", "不详");break;
                case "1982-01-01": obj.put("jzrq", "拒种");break;
                case "1983-01-01": obj.put("jzrq", "禁忌");break;
                case "1984-01-01": obj.put("jzrq", "超期不种");break;
                case "1985-01-01": obj.put("jzrq", "已种");break;
                case "1986-01-01": obj.put("jzrq", "已患");break;
            }
        }
        result.put("data", records);
        result.put("draw", map.get("draw"));
        result.put("recordsFiltered", records.size());
        return result;
    }

    /**
     * 获取计免接种信息表头信息
     *
     * @param empiid
     * @return
     */
    public Map<String, Object> getVaccinationRecordHeader(String empiid) {
        Map<String, Object> param = new HashMap<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid(empiid);
        if (CollectionUtils.isEmpty(phridMap)) {
            return new HashMap<>();
        }
        List<HashMap<String, Object>> headers = new ArrayList<>();
        for (Map<String, String> obj : phridMap) {
            String phrid = obj.get("PHRID");
            param.put("phrid", phrid);
            List<HashMap<String, Object>> header = ehr_VisitRecordMapper.getVaccinationHeader(param);
            // 查询是否有禁忌症状
            String childId = ehr_VisitRecordMapper.getChildIDByPhrid(phrid);
            if (Common.isNotEmpty(childId)) {
                String jjzbx = ehr_VisitRecordMapper.getJJZBByChildId(childId);
                for (Map<String, Object> h: header) {
                    h.put("sfyjj", jjzbx);
                }
            }
            headers.addAll(header);
        }
        Map<String, Object> res = Common.mergeMaps(headers);
        String sex = (String) res.get("sex");
        String hjlx = (String) res.get("hjlx");
        if ("1".equals(sex)) {
            res.put("sex", "男");
        } else if ("2".equals(sex)) {
            res.put("sex", "女");
        } else {
            res.put("sex", "未知");
        }

        if ("1".equals(hjlx)) {
            res.put("hjlx", "本地户口");
        } else if ("2".equals(hjlx)) {
            res.put("hjlx", "非本地户口");
        } else if ("3".equals(hjlx)) {
            res.put("hjlx", "非本地户口居住1年以上");
        } else {
            res.put("hjlx", "未知");
        }
        return res;
    }
}
