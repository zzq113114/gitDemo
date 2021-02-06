package cn.com.bsoft.service.data;

import cn.com.bsoft.entity.UserFormMap;
import cn.com.bsoft.mapper.BDB.In_HospitalRecordMapper;
import cn.com.bsoft.mapper.BDB.IndexRecordMapper;
import cn.com.bsoft.mapper.BDB.Out_HospitalRecordMapper;
import cn.com.bsoft.util.Common;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IptRecordService {
    @Autowired
    private In_HospitalRecordMapper inHospitalRecordMapper;
    @Autowired
    private Out_HospitalRecordMapper outHospitalRecordMapper;
    @Autowired
    private IndexRecordMapper indexRecordMapper;

    public Map<String, Object> getIptMain(Map<String, Object> param) {
        Map<String, Object> result = new HashMap<String, Object>();
//        UserFormMap userFormMap = (UserFormMap) Common.findUserSession();
        String id = (String) param.get("id");
        if (Common.isNotEmpty(id)) {
            param.put("hid", id);
            param.put("organ_code", param.get("organcode"));
            List<HashMap<String, Object>> res = inHospitalRecordMapper.getReportById(param);
            if (null != res && 0 < res.size()) {
                getRecordCount(res.get(0), result);
//                res.get(0).put("name", Common.formatName((String) res.get(0).get("name"),
//                        (String) userFormMap.get("viewPrivacy")));
                res.get(0).put("age", Common.getAgeByBirthDay(res.get(0).get("birthday")));
                result.put("user", res.get(0));
            }
        }
        return result;
    }

    private void getRecordCount(HashMap<String, Object> param, Map<String, Object> result) {
        int recordCount = 0;
        //病案首页
        recordCount = indexRecordMapper.queryFirstPageCount(param);
        if (0 != recordCount) {
            result.put("BA_TOTAL", "1");
        } else {
            result.put("BA_TOTAL", "0");
        }

        //手术记录
        recordCount = indexRecordMapper.queryOperationCount(param);
        if (0 != recordCount) {
            result.put("SS_TOTAL", "1");
        } else {
            result.put("SS_TOTAL", "0");
        }

        //医嘱
        recordCount = indexRecordMapper.queryDocAdviceCount(param);
        if (0 != recordCount) {
            result.put("YZ_TOTAL", "1");
        } else {
            result.put("YZ_TOTAL", "0");
        }
        //出院
        recordCount = indexRecordMapper.queryOutHospitalCount(param);
        if (0 != recordCount) {
            result.put("CY_TOTAL", "1");
        } else {
            result.put("CY_TOTAL", "0");
        }
    }


    public Map<String, Object> iptMainPage(Map<String, Object> param) {
        Map<String, Object> result = new HashMap<String, Object>();
//        UserFormMap userFormMap = (UserFormMap) Common.findUserSession();
        String id = (String) param.get("id");
        if (Common.isNotEmpty(id)) {
            param.put("hid", id);
            param.put("organ_code", param.get("organcode"));
            List<HashMap<String, Object>> res = inHospitalRecordMapper.getReportById(param);
            List<HashMap<String, Object>> resTemp = outHospitalRecordMapper.findRecordById(param);
            if (null != res && 0 < res.size()) {
                if (null != resTemp && 0 < resTemp.size()) {
                    res.get(0).put("out_hp_dt", resTemp.get(0).get("out_hp_dt"));
                } else {
                    res.get(0).put("out_hp_dt", "");
                }
                getRecordCount(res.get(0), result);
//                res.get(0).put("name", Common.formatName((String) res.get(0).get("name"),
//                        (String) userFormMap.get("viewPrivacy")));
                res.get(0).put("age", Common.getAgeByBirthDay(res.get(0).get("birthday")));
                result.put("user", res.get(0));
                result.put("name", res.get(0).get("name"));
            }
        }
        return result;

    }
}
