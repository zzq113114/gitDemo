package cn.com.bsoft.service.data;

import cn.com.bsoft.mapper.BDB.*;
import cn.com.bsoft.util.Common;
import cn.com.bsoft.util.CommonConsts;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 个人健康首页
 */
@Service
public class HealthInfoService {
    private static final Logger log = Logger.getLogger(HealthInfoService.class);
    @Value("${env_flag}")
    public String ENV_FLAG = "";
    @Autowired
    private Ehr_HealthRecordMapper ehrHealthRecordMapper;
    @Autowired
    private Ehr_PersonHistroyMapper ehr_PersonHistroyMapper;
    @Autowired
    private Ehr_VisitRecordMapper ehr_VisitRecordMapper;
    @Autowired
    private IndexRecordMapper indexRecordMapper;
    @Autowired
    private Examination_RecordMapper examination_recordMapper;
    @Autowired
    private PhridService phridService;
    @Value("${mbs_flag}")
    public String mbsFlag = "";
    @Autowired
    public MedicalRecordMapper medicalRecordMapper;
    @Autowired
    public Operation_RecordMapper operation_recordMapper;
    @Autowired
    public TransFusion_RecordMapper transFusion_recordMapper;
    @Autowired
    public In_HospitalRecordMapper in_hospitalRecordMapper;

    /**
     * 获取健康问题
     *
     * @param empiid
     * @return
     */
    public Map<String, Object> getPersonHistory(String empiid) {
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid(empiid);
        List<Map<String, String>> phridMapJM = new ArrayList<>();
        List<HashMap<String, Object>> dataList2 = new ArrayList<>();
        List<HashMap<String, Object>> dataList3 = new ArrayList<>();
        List<HashMap<String, Object>> dataList4 = new ArrayList<>();
        List<HashMap<String, Object>> dataList6 = new ArrayList<>();
        List<HashMap<String, Object>> dataList7 = new ArrayList<>();
        List<HashMap<String, Object>> dataList8 = new ArrayList<>();
        int gxyCount = 0;
        int tnbCount = 0;
        //公卫部分
        if (!CollectionUtils.isEmpty(phridMap)) {
            Map<String, Object> param = new HashMap<String, Object>();
            for (Map<String, String> tmp : phridMap) {
                param.put("phrid", tmp.get("PHRID"));
                param.put("organ_code", tmp.get("ORGAN_CODE"));
                //既往疾病史
                List<HashMap<String, Object>> temp2 = ehr_PersonHistroyMapper.findJWJBSById(param);
                if (!CollectionUtils.isEmpty(temp2)) {
                    for (HashMap<String, Object> res : temp2) {
                        if(!Common.checkZdmc((String) res.get("name"), (String) res.get("zddm"))){
                            dataList2.add(res);
                        }
                    }
                    //dataList2.addAll(temp2);
                }

                List<HashMap<String, Object>> temp3 = ehr_PersonHistroyMapper.findJWSSSById(param);
                if (!CollectionUtils.isEmpty(temp3)) {
                    dataList3.addAll(temp3);
                }

                List<HashMap<String, Object>> temp4 = ehr_PersonHistroyMapper.findJWSXSById(param);
                if (!CollectionUtils.isEmpty(temp4)) {
                    dataList4.addAll(temp4);
                }

                List<HashMap<String, Object>> temp6 = ehr_PersonHistroyMapper.findJZSById(param);
                if (!CollectionUtils.isEmpty(temp6)) {
                    dataList6.addAll(temp6);
                }

                List<HashMap<String, Object>> temp7 = ehr_PersonHistroyMapper.findJZYCSById(param);
                if (!CollectionUtils.isEmpty(temp7)) {
                    dataList7.addAll(temp7);
                }
                List<HashMap<String, Object>> temp8 = ehr_PersonHistroyMapper.findJWZYSById(param);
                if (!CollectionUtils.isEmpty(temp8)) {
                    dataList8.addAll(temp8);
                }
                if (!"0".equals(mbsFlag)) {
                    int gxytemp = ehr_PersonHistroyMapper.findGXYCount(param);
                    int tnmtemp = ehr_PersonHistroyMapper.findTNBCount(param);
                    gxyCount += gxytemp;
                    tnbCount += tnmtemp;
                }
            }
        }
        //医疗部分
        phridMapJM = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.MEN_ZHEN);
        if (!CollectionUtils.isEmpty(phridMapJM)) {
            Map<String, Object> param = new HashMap<String, Object>();
            for (Map<String, String> tmp : phridMapJM) {
                param.put("phrid", tmp.get("PHRID"));
                param.put("organ_code", tmp.get("ORGAN_CODE"));
                //既往疾病史
                List<HashMap<String, Object>> temp2 = medicalRecordMapper.getMdisList(param);
                if (!CollectionUtils.isEmpty(temp2)) {
                    //部分icd10代码隐私处理
                    for (HashMap<String, Object> res : temp2) {
                        if(!Common.checkZdmc((String) res.get("name"), (String) res.get("zddm"))){
                            dataList2.add(res);
                        }
                    }
                    //dataList2.addAll(temp2);
                }
            }
        }

        phridMapJM = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.ZHU_YUAN);
        if (!CollectionUtils.isEmpty(phridMapJM)) {
            Map<String, Object> param = new HashMap<String, Object>();
            for (Map<String, String> tmp : phridMapJM) {
                param.put("phrid", tmp.get("PHRID"));
                param.put("organ_code", tmp.get("ORGAN_CODE"));
                //手术史
                List<HashMap<String, Object>> temp3 = operation_recordMapper.getJWOperList(param);
                if (!CollectionUtils.isEmpty(temp3)) {
                    dataList3.addAll(temp3);
                }
                //输血
                List<HashMap<String, Object>> temp4 = transFusion_recordMapper.getJWTransFusionList(param);
                if (!CollectionUtils.isEmpty(temp4)) {
                    dataList4.addAll(temp4);
                }
                //住院
                List<HashMap<String, Object>> temp8 = in_hospitalRecordMapper.getJWInhospitalList(param);
                if (!CollectionUtils.isEmpty(temp8)) {
                    //部分icd10代码隐私处理
                    for (HashMap<String, Object> res : temp8) {
                        //res.put("name", Common.formatZdmc((String) res.get("name"), (String) res.get("zddm")));
                        if(!Common.checkZdmc((String) res.get("name"), (String) res.get("zddm"))){
                            dataList8.add(res);
                        }
                    }
                    //dataList8.addAll(temp8);
                }
            }
        }
        if (!CollectionUtils.isEmpty(dataList2)) {
            dataList2 = Common.sortUnionList(dataList2, "sj", null, null);
        }
        if (!CollectionUtils.isEmpty(dataList3)) {
            dataList3 = Common.sortUnionList(dataList3, "sj", null, null);
        }
        if (!CollectionUtils.isEmpty(dataList4)) {
            dataList4 = Common.sortUnionList(dataList4, "sj", null, null);
        }
        if (!CollectionUtils.isEmpty(dataList6)) {
            for (HashMap<String, Object> item : dataList6) {
                if ("01".equals(item.get("name"))) {
                    item.put("name", "无");
                }
            }
            dataList6 = Common.sortUnionList(dataList6, "sj", null, null);
        }
        if (!CollectionUtils.isEmpty(dataList7)) {
            dataList7 = Common.sortUnionList(dataList7, "sj", null, null);
        }
        if (!CollectionUtils.isEmpty(dataList8)) {
            dataList8 = Common.sortUnionList(dataList8, "sj", null, null);
        }
        HashMap<String, Object> mergelist = new HashMap<>();
        HashMap<String, Object> result = new HashMap<>();
        //mergeList(mergelist, dataList1, "gms");
        mergeList(mergelist, dataList2, "jws");
        mergeList(mergelist, dataList3, "sss");
        mergeList(mergelist, dataList4, "sxs");
        //mergeList(mergelist, dataList5, "wss");
        mergeList(mergelist, dataList6, "jzs");
        mergeList(mergelist, dataList7, "ycbs");
        mergeList(mergelist, dataList8, "zys");

        StringBuilder mbStr = new StringBuilder();
        if (gxyCount > 0) {
            mbStr.append("高血压，");
        }
        if (tnbCount > 0) {
            mbStr.append("糖尿病");
        }
        mergelist.put("mbs", mbStr.toString());//慢病

        result.put("history", mergelist);
        return result;
    }

    /**
     * 获取健康服务信息列表
     *
     * @param param
     * @return
     */
    public Map<String, Object> getHealthServiceList(Map<String, Object> param) {
        String page = (String) param.get("page");
        String rows = (String) param.get("rows");
        if (0 != Integer.parseInt(page) && 0 != Integer.parseInt(rows)) {
            //BDB查询分页
            param.put("offset", (Integer.parseInt(page) - 1) * Integer.parseInt(rows));
        }
        getDates(param);
        List<Map<String, String>> phridMap = null;
        List<HashMap<String, Object>> result1 = new ArrayList<>();
        List<HashMap<String, Object>> result2 = null;
        List<HashMap<String, Object>> result3 = null;
        phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) param.get("empiid"), CommonConsts.OP_EM_HP_MARK.MEN_ZHEN);
        if (!CollectionUtils.isEmpty(phridMap)) {
            for (Map<String, String> tmp : phridMap) {
                param.put("phrid", tmp.get("PHRID"));
                param.put("organ_code", tmp.get("ORGAN_CODE"));
                result3 = ehr_VisitRecordMapper.findOptListById(param);
                if (null != result3 && 0 < result3.size()) {
                    //部分icd10代码隐私处理
                    for (HashMap<String, Object> res : result3) {
                        if(!Common.checkZdmc((String) res.get("name"), (String) res.get("zddm"))){
                            result1.add(res);
                        }
                    }
                }
            }
        }
        phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) param.get("empiid"), CommonConsts.OP_EM_HP_MARK.ZHU_YUAN);
        if (!CollectionUtils.isEmpty(phridMap)) {
            for (Map<String, String> tmp : phridMap) {
                param.put("phrid", tmp.get("PHRID"));
                param.put("organ_code", tmp.get("ORGAN_CODE"));
                result2 = ehr_VisitRecordMapper.findIptListById(param);
                if (null != result2 && 0 < result2.size()) {
                    //部分icd10代码隐私处理
                    for (HashMap<String, Object> res : result2) {
                        if(!Common.checkZdmc((String) res.get("name"), (String) res.get("zddm"))){
                            result1.add(res);
                        }
                    }
                }
            }
        }

        int total = 0;
        List<HashMap<String, Object>> visitData;
        if (null == result1 || 0 >= result1.size()) {
            visitData = new ArrayList<>();
        } else {
            log.info("sortStr:" + param.get("sort"));
            result1 = Common.sortUnionList(result1, (String) param.get("sort"), null, null);
            log.info("total:" + result1.size());
            int offset = (Integer) param.get("offset");
            int limit = (Integer) param.get("offset") + Integer.parseInt(rows);
            total = result1.size();
            if (total >= offset) {
                if (limit > total) {
                    limit = total;
                }
                visitData = result1.subList(offset, limit);
            } else {
                visitData = new ArrayList<>();
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows", visitData);
        result.put("total", total);
        return result;
    }

    /**
     * 获取健康服务饼图信息
     *
     * @param param
     * @return
     */
    public Map<String, Object> getHealthServicePie(Map<String, Object> param) {
        getDates(param);
        List<Map<String, String>> phridMap = null;
        List<HashMap<String, Object>> result1 = new ArrayList<>();
        List<HashMap<String, Object>> result2 = null;
        List<HashMap<String, Object>> result3 = null;
        phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) param.get("empiid"), CommonConsts.OP_EM_HP_MARK.ZHU_YUAN);
        if (!CollectionUtils.isEmpty(phridMap)) {
            for (Map<String, String> tmp : phridMap) {
                param.put("phrid", tmp.get("PHRID"));
                param.put("organ_code", tmp.get("ORGAN_CODE"));
                result2 = ehr_VisitRecordMapper.findIptPieById(param);
                if (!CollectionUtils.isEmpty(result2)) {
                    //部分icd10代码隐私处理
                    for (HashMap<String, Object> res : result2) {
                        if(!Common.checkZdmc((String) res.get("name"), (String) res.get("zddm"))){
                            result1.add(res);
                        }
                    }
                }
            }
        }
        phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) param.get("empiid"), CommonConsts.OP_EM_HP_MARK.MEN_ZHEN);
        if (!CollectionUtils.isEmpty(phridMap)) {
            for (Map<String, String> tmp : phridMap) {
                param.put("phrid", tmp.get("PHRID"));
                param.put("organ_code", tmp.get("ORGAN_CODE"));
                result3 = ehr_VisitRecordMapper.findOptPieById(param);
                if (!CollectionUtils.isEmpty(result3)) {
                    //部分icd10代码隐私处理
                    for (HashMap<String, Object> res : result3) {
                        if(!Common.checkZdmc((String) res.get("name"), (String) res.get("zddm"))){
                            result1.add(res);
                        }
                    }
                }
            }
        }

        List<String> nameList = new ArrayList<String>();
        if (null != result1 && 0 < result1.size()) {
            result1 = Common.sortUnionList(result1, "sj", null, null);
            //住院、门诊相同诊断名合并
            List<HashMap<String, Object>> temp = new ArrayList<>();
            Map<String, HashMap<String, Object>> tempMap = new HashMap<>();
            String tempStr = null;
            HashMap<String, Object> map1 = null;
            for (HashMap<String, Object> map : result1) {
                tempStr = (String) map.get("name");
                map1 = tempMap.get(tempStr);
                if (!CollectionUtils.isEmpty(map1)) {
                    map1.put("value", (long) map.get("value") + (long) map1.get("value"));
                } else {
                    tempMap.put(tempStr, map);
                }
            }
            nameList.addAll(tempMap.keySet());
            for (Map.Entry entry : tempMap.entrySet()) {
                temp.add((HashMap<String, Object>) entry.getValue());
            }
            result1 = Common.sortUnionList(temp, "sj", null, null);
//            for (Map<String, Object> temp : result1) {
//                if (!nameList.contains(temp.get("name"))) {
//                    nameList.add((String) temp.get("name"));
//                }
//            }
        } else {
            nameList = new ArrayList<String>();
            nameList.add("无");
            result1 = new ArrayList<HashMap<String, Object>>();
            HashMap<String, Object> paramTemp = new HashMap<String, Object>();
            paramTemp.put("name", "无");
            paramTemp.put("value", 0);
            result1.add(paramTemp);
        }

        HashMap<String, Object> result = new HashMap<>();
        result.put("dataName", nameList);
        result.put("dataValue", result1);
        return result;
    }

    /**
     * 获取体征信息
     *
     * @param param
     * @return
     */
    public Map<String, Object> getPersonBmi(Map<String, Object> param) {
        List<HashMap<String, Object>> bmiData = null;
        List<Map<String, String>> phridMap = null;
        String empiid = (String) param.get("empiid");
        if (CommonConsts.TX_ENV.equalsIgnoreCase(ENV_FLAG)) {
            phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid,CommonConsts.OP_EM_HP_MARK.TI_JIAN);
            if (!CollectionUtils.isEmpty(phridMap)) {
                bmiData = getTxBMI(param, phridMap);
            }
        } else {
            phridMap = phridService.getJMPhridAndOraganByEmpiid(empiid);
            if (!CollectionUtils.isEmpty(phridMap)) {
                bmiData = getUnionBMI(param, phridMap);
            }
        }
        if (bmiData == null) {
            bmiData = new ArrayList<>();
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("bmi", bmiData);
        return result;
    }

    private List<HashMap<String, Object>> getUnionBMI(Map<String, Object> param, List<Map<String, String>> phridMap) {
        List<HashMap<String, Object>> result1 = new ArrayList<>();
        List<HashMap<String, Object>> result2 = null;
        List<HashMap<String, Object>> result3 = null;
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
//            param.put("organ_code", tmp.get("ORGAN_CODE"));
            result3 = ehr_VisitRecordMapper.findBMIFromGXY(param);
            result2 = ehr_VisitRecordMapper.findBMIFromTNB(param);
            if (!CollectionUtils.isEmpty(result2)) {
                result1.addAll(result2);
            }
            if (!CollectionUtils.isEmpty(result3)) {
                result1.addAll(result3);
            }
        }
        if (null == result1 || 0 >= result1.size()) {
            result1 = new ArrayList<HashMap<String, Object>>();
        } else {
            result1 = Common.sortUnionList(result1, "SJ", null, null);
        }
        return result1;
    }

    private List<HashMap<String, Object>> getTxBMI(Map<String, Object> param, List<Map<String, String>> phridMap) {
        List<HashMap<String, Object>> result1 = null;
        List<HashMap<String, Object>> result2 = new ArrayList<>();
        List<HashMap<String, Object>> result3 = null;
        param.put("sortType", "sj");
        for (Map<String, String> tmp : phridMap) {
            param.put("phrid", tmp.get("PHRID"));
            param.put("organ_code", tmp.get("ORGAN_CODE"));
            result3 = indexRecordMapper.queryCheckupIndex(param);
            if (!CollectionUtils.isEmpty(result3)) {
                result2.addAll(result3);
            }
        }
        if (null != result2 && 0 < result2.size()) {
            result1 = getBMIInfo(result2);
        } else {
            result1 = new ArrayList<>();
        }
        return result1;
    }

    /**
     * 桐乡
     *
     * @param jobs
     * @return
     */
    private List<HashMap<String, Object>> getBMIInfo(List<HashMap<String, Object>> jobs) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        List<HashMap<String, Object>> resList = new ArrayList<>();
        List<Future<HashMap<String, Object>>> futureList = new ArrayList<>();
        if (null != jobs && 0 < jobs.size()) {
            for (HashMap<String, Object> job : jobs) {
                futureList.add(threadPool.submit(new Callable<HashMap<String, Object>>() {
                    public HashMap<String, Object> call() throws Exception {
                        HashMap<String, Object> unionMap = new HashMap<>();
                        List<HashMap<String, Object>> unionList = ehr_VisitRecordMapper.findAllFromExam(job);
                        if (null != unionList && 0 < unionList.size()) {
                            unionMap.put("SJ", job.get("sjstr"));
                            for (HashMap<String, Object> temp : unionList) {
                                if (Common.isNotEmpty((String) temp.get("checkupprojectid"))) {
                                    if ("9200113".equalsIgnoreCase((String) temp.get("checkupprojectid")) ||
                                            "9101012".equalsIgnoreCase((String) temp.get("checkupprojectid"))) {
                                        if (Common.isNotEmpty((String) temp.get("result_name")) &&
                                                0 > ((String) temp.get("result_name")).indexOf("-")) {
                                            unionMap.put("BPCON", temp.get("result_name"));
                                        } else {
                                            unionMap.put("BPCON", "");
                                        }

                                    } else if ("9200114".equalsIgnoreCase((String) temp.get("checkupprojectid")) ||
                                            "9101013".equalsIgnoreCase((String) temp.get("checkupprojectid"))) {
                                        if (Common.isNotEmpty((String) temp.get("result_name")) &&
                                                0 > ((String) temp.get("result_name")).indexOf("-")) {
                                            unionMap.put("BPDIA", temp.get("result_name"));
                                        } else {
                                            unionMap.put("BPDIA", "");
                                        }
                                    } else if ("9101004".equalsIgnoreCase((String) temp.get("checkupprojectid"))) {
                                        if (Common.isNotEmpty((String) temp.get("result_name")) &&
                                                0 > ((String) temp.get("result_name")).indexOf("-")) {
                                            unionMap.put("HEIGHT", temp.get("result_name"));
                                        } else {
                                            unionMap.put("HEIGHT", "");
                                        }
                                    } else if ("9101005".equalsIgnoreCase((String) temp.get("checkupprojectid"))) {
                                        if (Common.isNotEmpty((String) temp.get("result_name")) &&
                                                0 > ((String) temp.get("result_name")).indexOf("-")) {
                                            unionMap.put("WEIGHT", temp.get("result_name"));
                                        } else {
                                            unionMap.put("WEIGHT", "");
                                        }
                                    } else if ("9104001".equalsIgnoreCase((String) temp.get("checkupprojectid")) ||
                                            "8200001".equalsIgnoreCase((String) temp.get("checkupprojectid"))) {
                                        if (Common.isNotEmpty((String) temp.get("result_name")) &&
                                                0 > ((String) temp.get("result_name")).indexOf("-")) {
                                            unionMap.put("FBG", temp.get("result_name"));
                                        } else {
                                            unionMap.put("FBG", "");
                                        }
                                    } else if ("9104002".equalsIgnoreCase((String) temp.get("checkupprojectid")) ||
                                            "8101646".equalsIgnoreCase((String) temp.get("checkupprojectid"))) {
                                        if (Common.isNotEmpty((String) temp.get("result_name")) &&
                                                0 > ((String) temp.get("result_name")).indexOf("-")) {
                                            unionMap.put("FBG_MEAL", temp.get("result_name"));
                                        } else {
                                            unionMap.put("FBG_MEAL", "");
                                        }
                                    } else
                                        continue;
                                } else
                                    continue;
                            }
                        }

                        if (Common.isNotEmpty((String) unionMap.get("HEIGHT"))
                                && Common.isNotEmpty((String) unionMap.get("WEIGHT"))) {
                            double weightVal = 0.0d;
                            double heightVal = 0.0d;
                            try {
                                weightVal = Double.parseDouble((String) unionMap.get("WEIGHT"));
                                heightVal = Double.parseDouble((String) unionMap.get("HEIGHT"));
                            } catch (Exception ex) {
                                log.debug(ex.getLocalizedMessage());
                            }
                            if (0 < weightVal && 0 < heightVal) {
                                DecimalFormat df = new DecimalFormat("#.00");
                                unionMap.put("BMI", df.format((weightVal * 10000) /
                                        (Math.pow(heightVal, 2.0))));
                            } else {
                                unionMap.put("BMI", "0");
                            }

                        } else {
                            unionMap.put("BMI", "");
                        }
                        if (null == unionMap.get("BPCON")) {
                            unionMap.put("BPCON", "");
                        }
                        if (null == unionMap.get("BPDIA")) {
                            unionMap.put("BPDIA", "");
                        }
                        if (null == unionMap.get("HEIGHT")) {
                            unionMap.put("HEIGHT", "");
                        }
                        if (null == unionMap.get("WEIGHT")) {
                            unionMap.put("WEIGHT", "");
                        }
                        if (null == unionMap.get("FBG")) {
                            unionMap.put("FBG", "");
                        }
                        if (null == unionMap.get("FBG_MEAL")) {
                            unionMap.put("FBG_MEAL", "");
                        }
                        return unionMap;
                    }
                }));
            }
        }
        Common.convertFutureList(futureList, resList);
        threadPool.shutdown();
        return resList;
    }

    /**
     * 获取影像信息列表
     *
     * @param param
     * @return
     */
    public Map<String, Object> getPacsList(Map<String, Object> param) {
//        String page = (String) param.get("page");
//        String rows = (String) param.get("rows");
//        if (0 != Integer.parseInt(page) && 0 != Integer.parseInt(rows)) {
//            //BDB查询分页
//            param.put("offset", (Integer.parseInt(page) - 1) * Integer.parseInt(rows));
//        }
//        getDates(param);
//
//        List<Map<String, Object>> pacsImageData = new ArrayList<>();
//        List<Map<String, String>> phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) param.get("empiid"));
//        int sum = 0;
//        int total = 0;
//        if (!CollectionUtils.isEmpty(phridMap)) {
//            for (Map<String, String> tmp : phridMap) {
//                param.put("phrid", tmp.get("PHRID"));
//                param.put("organcode", tmp.get("ORGAN_CODE"));
//                total = examination_recordMapper.getPacsDatasCount(param);
//                sum += total;
//            }
//            if (sum > 0) {
//                for (Map<String, String> tmp : phridMap) {
//                    param.put("phrid", tmp.get("PHRID"));
//                    param.put("organcode", tmp.get("ORGAN_CODE"));
//                    List<Map<String, Object>> tmpList = examination_recordMapper.getPacsDatasByPage(param);
//                    if (!CollectionUtils.isEmpty(tmpList)) {
//                        pacsImageData.addAll(tmpList);
//                    }
//                }
//            }
//        }
        HashMap<String, Object> result = new HashMap<>();
//        result.put("rows", pacsImageData);
//        result.put("total", total);
        return result;
    }

    /**
     * 获取影像饼图
     *
     * @param param
     * @return
     * @throws Exception
     */
    public Map<String, Object> getPacsPie(Map<String, Object> param) {
//        getDates(param);
//        List<Map<String, String>> phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) param.get("empiid"));
//
//        List<Map<String, Object>> result1 = new ArrayList<>();
//        List<Map<String, Object>> temList = null;
//        if (!CollectionUtils.isEmpty(phridMap)) {
//            for (Map<String, String> tmp : phridMap) {
//                param.put("phrid", tmp.get("PHRID"));
//                param.put("organcode", tmp.get("ORGAN_CODE"));
//                if (param.get("yxlx") != null && !"".equals(param.get("yxlx"))) {
//                    temList = examination_recordMapper.findPacsPie(param);
//                } else {
//                    temList = examination_recordMapper.findPacsPieByMethodName(param);
//                }
//                if (!CollectionUtils.isEmpty(temList)) {
//                    result1.addAll(temList);
//                }
//            }
//        }
//
//        List<String> nameList = new ArrayList<String>();
//        if (null != result1 && 0 < result1.size()) {
//            for (Map<String, Object> temp : result1) {
//                if (!nameList.contains(temp.get("name"))) {
//                    nameList.add((String) temp.get("name"));
//                }
//            }
//        } else {
//            nameList.add("无");
//            HashMap<String, Object> paramTemp = new HashMap<String, Object>();
//            paramTemp.put("name", "无");
//            paramTemp.put("value", 0);
//            result1.add(paramTemp);
//        }
        HashMap<String, Object> result = new HashMap<>();
//        result.put("dataName", nameList);
//        result.put("dataValue", result1);
        return result;
    }

    private void mergeList(HashMap<String, Object> datamap, List<HashMap<String, Object>> datalist, String s) {
        Set<Object> set = new LinkedHashSet<>();
        if (datalist != null) {
            for (Map<String, Object> item : datalist) {
                String name = (String) item.get("name");
                if (Common.isNotEmpty(name) && !"-".equals(name)) {
                    set.add(name);
                }
            }
        }
        datamap.put(s, StringUtils.collectionToDelimitedString(set, "，"));

    }

    /**
     * 时间条件
     *
     * @param param
     * @throws ParseException
     */
    private void getDates(Map<String, Object> param) {
        try {
            if (Common.isNotEmpty((String) param.get("sj_from")) || Common.isNotEmpty((String) param.get("sj_to"))) {
                param.put("sj_from", Common.dateToStamp(param.get("sj_from") + " 00:00:00"));
                param.put("sj_to", Common.dateToStamp(Common.appointDateToString(param.get("sj_to"), 0, 0, 1) + " 00:00:00"));
            } else if (Common.isNotEmpty((String) param.get("monthFlag"))) {
                String mf = "-" + param.get("monthFlag");
                String sj_from = Common.phaseDateToString(0, Integer.parseInt(mf), 0);
                param.put("sj_from", Common.dateToStamp(sj_from + " 00:00:00"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getZlDjKey(Map<String, Object> param) {
        return getMbDjKey(param, "ZL");
    }

    public List<HashMap<String, Object>> getZlSelectList(Map<String, Object> param) {
        return getMbSelectList(param, "ZL");
    }

    public Map<String, Object> getJsbDjKey(Map<String, Object> param) {
        return getMbDjKey(param, "JSB");
    }

    public List<HashMap<String, Object>> getJsbSelectList(Map<String, Object> param) {
        return getMbSelectList(param, "JSB");
    }

    public List<HashMap<String, Object>> getLnrjktjSelectList(Map<String, Object> param) {
        return getMbSelectList(param, "LNR");
    }

    public Map<String, Object> getTnbDjKey(Map<String, Object> param) {
        return getMbDjKey(param, "TNB");
    }

    public List<HashMap<String, Object>> getTnbSelectList(Map<String, Object> param) {
        return getMbSelectList(param, "TNB");
    }

    public Map<String, Object> getGxyDjKey(Map<String, Object> param) {
        return getMbDjKey(param, "GXY");
    }

    public List<HashMap<String, Object>> getGxySelectList(Map<String, Object> param) {
        return getMbSelectList(param, "GXY");
    }

    private List<HashMap<String, Object>> getMbSelectList(Map<String, Object> param, String flag) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        if (!CollectionUtils.isEmpty(phridMap)) {
            switch (flag) {
                case "TNB":
                    for (Map<String, String> tmp : phridMap) {
                        param.put("phrid", tmp.get("PHRID"));
//                        param.put("organ_code", tmp.get("ORGAN_CODE"));
                        List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getTnbSelectList(param);
                        if (!CollectionUtils.isEmpty(tempList)) {
                            result.addAll(tempList);
                        }
                    }
                    break;
                case "GXY":
                    for (Map<String, String> tmp : phridMap) {
                        param.put("phrid", tmp.get("PHRID"));
//                        param.put("organ_code", tmp.get("ORGAN_CODE"));
                        List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getGxySelectList(param);
                        if (!CollectionUtils.isEmpty(tempList)) {
                            result.addAll(tempList);
                        }
                    }
                    break;
                case "ZL":
                    for (Map<String, String> tmp : phridMap) {
                        param.put("phrid", tmp.get("PHRID"));
//                        param.put("organ_code", tmp.get("ORGAN_CODE"));
                        List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getZlSelectList(param);
                        if (!CollectionUtils.isEmpty(tempList)) {
                            result.addAll(tempList);
                        }
                    }
                    break;
                case "JSB":
                    for (Map<String, String> tmp : phridMap) {
                        param.put("phrid", tmp.get("PHRID"));
//                        param.put("organ_code", tmp.get("ORGAN_CODE"));
                        List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getJsbSelectList(param);
                        if (!CollectionUtils.isEmpty(tempList)) {
                            result.addAll(tempList);
                        }
                    }
                    break;
                case "LNR":
                    for (Map<String, String> tmp : phridMap) {
                        param.put("phrid", tmp.get("PHRID"));
//                        param.put("organ_code", tmp.get("ORGAN_CODE"));
                        List<HashMap<String, Object>> tempList = ehr_VisitRecordMapper.getLnrSelectList(param);
                        if (!CollectionUtils.isEmpty(tempList)) {
                            result.addAll(tempList);
                        }
                    }
                    break;
            }
            if (null == result || 0 >= result.size()) {
                result = new ArrayList<>();
            }
        }
        return result;
    }

    private Map<String, Object> getMbDjKey(Map<String, Object> param, String flag) {
        List<Map<String, String>> phridMap = phridService.getJMPhridAndOraganByEmpiid((String) param.get("empiid"));
        List<Map<String, Object>> listResult = new ArrayList<>();
        if (!CollectionUtils.isEmpty(phridMap)) {
            Map<String, Object> result = null;
            switch (flag) {
                case "TNB":
                    for (Map<String, String> tmp : phridMap) {
                        param.put("phrid", tmp.get("PHRID"));
//                        param.put("organ_code", tmp.get("ORGAN_CODE"));
                        result = ehr_VisitRecordMapper.getTnbDjKey(param);
                        if (!CollectionUtils.isEmpty(result)) {
                            listResult.add(result);
                        }
                    }
                    break;
                case "GXY":
                    for (Map<String, String> tmp : phridMap) {
                        param.put("phrid", tmp.get("PHRID"));
//                        param.put("organ_code", tmp.get("ORGAN_CODE"));
                        result = ehr_VisitRecordMapper.getGxyDjKey(param);
                        if (!CollectionUtils.isEmpty(result)) {
                            listResult.add(result);
                        }
                    }
                    break;
                case "ZL":
                    for (Map<String, String> tmp : phridMap) {
                        param.put("phrid", tmp.get("PHRID"));
//                        param.put("organ_code", tmp.get("ORGAN_CODE"));
                        result = ehr_VisitRecordMapper.getZlDjKey(param);
                        if (!CollectionUtils.isEmpty(result)) {
                            listResult.add(result);
                        }
                    }
                    break;
                case "JSB":
                    for (Map<String, String> tmp : phridMap) {
                        param.put("phrid", tmp.get("PHRID"));
//                        param.put("organ_code", tmp.get("ORGAN_CODE"));
                        result = ehr_VisitRecordMapper.getJsbDjKey(param);
                        if (!CollectionUtils.isEmpty(result)) {
                            listResult.add(result);
                        }
                    }
                    break;
            }


        }

        if (CollectionUtils.isEmpty(listResult)) {
            Map<String, Object> result = new HashMap<>();
            result.put("KEY_CODE", "");
            result.put("ORGAN_CODE", "");
            return result;
        }

        return listResult.get(0);
    }
/*        if (!CollectionUtils.isEmpty(phrids)) {
            param.put("phrids", phrids);
            switch (flag) {
                case "TNB":
                    result = ehr_VisitRecordMapper.getTnbDjKey(param);
                    break;
                case "GXY":
                    result = ehr_VisitRecordMapper.getGxyDjKey(param);
                    break;
                case "ZL":
                    result = ehr_VisitRecordMapper.getZlDjKey(param);
                    break;
                case "JSB":
                    result = ehr_VisitRecordMapper.getJsbDjKey(param);
                    break;
            }
        }
        if (CollectionUtils.isEmpty(result)) {
            result = new HashMap<>();
            result.put("KEY_CODE", "");
            result.put("ORGAN_CODE", "");
        }
        return result;
    }*/
}
