package cn.com.bsoft.service.data;

import cn.com.bsoft.entity.HostFormMap;
import cn.com.bsoft.mapper.BDB.Drugs_RecordMapper;
import cn.com.bsoft.mapper.BDB.In_HospitalRecordMapper;
import cn.com.bsoft.mapper.BDB.IndexRecordMapper;
import cn.com.bsoft.mapper.BDB.Opt_RecordMapper;
import cn.com.bsoft.service.CommonService;
import cn.com.bsoft.util.Common;
import cn.com.bsoft.util.CommonConsts;
import cn.com.bsoft.util.TreeObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class DrugsRecordService {

    private static final Logger log = Logger.getLogger(DrugsRecordService.class);
    private final String RES_URL = "/drugsRecord/drugsMain.do";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private IndexRecordMapper indexRecordMapper;
    @Autowired
    private Drugs_RecordMapper drugsRecordMapper;
    @Autowired
    private Opt_RecordMapper optRecordMapper;
    @Autowired
    private In_HospitalRecordMapper inHospitalRecordMapper;
    @Autowired
    private PhridService phridService;
    @Autowired
    private CommonService commonService;


    public List<HashMap<String, Object>> getSelectList(Map<String, Object> param) {
        List<HashMap<String, Object>> result1 = null;
        List<HashMap<String, Object>> result2 = null;
        try {
            String filter = (String) param.get("filterType");
            if ("wm".equalsIgnoreCase(filter)) {
                result1 = drugsRecordMapper.getWMSelectList(param);
            } else if ("cm".equalsIgnoreCase(filter)) {
                result2 = drugsRecordMapper.getCMSelectList(param);
            } else {
                result1 = drugsRecordMapper.getWMSelectList(param);
                result2 = drugsRecordMapper.getCMSelectList(param);
            }
            if (null != result1 && 0 < result1.size()) {
                if (null != result2 && 0 < result2.size()) {
                    result1.addAll(result2);
                }
            } else {
                result1 = result2;
            }
            if (null == result1 || 0 >= result1.size()) {
                result1 = new ArrayList<HashMap<String, Object>>();
            } else {
                result1 = Common.sortUnionList(result1, "sj", null, null);
            }
        } catch (Exception ex) {
            log.info(ex.getLocalizedMessage());
            result1 = new ArrayList<HashMap<String, Object>>();
        }
        return result1;
    }


    public HashMap<String, Object> getRecordList(Map<String, Object> param) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        String id = (String) param.get("drugcode");
        if (Common.isNotEmpty(id)) {
            List<HashMap<String, Object>> data1 = new ArrayList<HashMap<String, Object>>();
            param.put("drugCode", id);
            param.put("empiid", param.get("empiid"));
            param.put("filterType", param.get("recordcode"));
            String sj_from = null;
            String sj_to = null;
            if (null != param.get("sj")) {
                try {
                    if ("1".equals(param.get("sj"))) {
                        sj_from = Common.phaseDateToString(0, -3, 0);
                    } else if ("2".equals(param.get("sj"))) {
                        sj_from = Common.phaseDateToString(0, -6, 0);
                        sj_to = Common.phaseDateToString(0, -3, 0);
                    } else if ("3".equals(param.get("sj"))) {
                        sj_from = Common.phaseDateToString(-1, 0, 0);
                        sj_to = Common.phaseDateToString(0, -6, 0);
                    } else if ("4".equals(param.get("sj"))) {
                        sj_to = Common.phaseDateToString(-1, 0, 0);
                    }
                } catch (Exception ex) {
                    log.info(ex.getLocalizedMessage());
                }

            }
            param.put("sj_from", sj_from);
            param.put("sj_to", sj_to);
            param.put("sj", param.get("sj"));
            data1 = getOptAndIptList(param);
            //部分icd10代码隐私处理
            for (HashMap<String, Object> res : data1) {
                if(!Common.checkZdmc((String) res.get("name"), (String) res.get("zddm"))){
                    data.add(res);
                }
            }
        }
        result.put("draw", param.get("draw"));
        if (null == result || 0 >= result.size()) {
            result.put("recordsFiltered", 0);
            result.put("data", new ArrayList<LinkedHashMap<String, Object>>());
        } else {
            result.put("recordsFiltered", data.size());
            result.put("data", data);
        }
        return result;
    }

    public List<TreeObject> getIndexMenu(String empiid, String sorttype, String filtertype, String drugName) {
        List<TreeObject> indexList = null;
        try {
            if ("cf".equalsIgnoreCase(sorttype)) {
                indexList = sortPrescriptionIndexMenu(empiid, filtertype);
            } else {
                indexList = sortDrugIndexMenu(empiid, sorttype, filtertype, drugName);
            }
            if (null == indexList || 0 >= indexList.size()) {
                indexList = new ArrayList<TreeObject>();
            }
        } catch (Exception ex) {
            log.info(ex.getLocalizedMessage());
            indexList = new ArrayList<TreeObject>();
        }
        return indexList;
    }

    private List<HashMap<String, Object>> getOptAndIptList(Map<String, Object> param) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        try {
            if (Common.isNotEmpty((String) param.get("sj_from"))) {
                param.put("sj_from", Common.dateToStamp(param.get("sj_from") + " 00:00:00"));
            }
            if (Common.isNotEmpty((String) param.get("sj_to"))) {
                param.put("sj_to", Common.dateToStamp(Common.appointDateToString(param.get("sj_to"), 0, 0, 1) + " 00:00:00"));
            }
        } catch (ParseException ex) {
            param.put("sj_to", null);
        }
        List<HashMap<String, Object>> resList = new ArrayList<>();

        List<HashMap<String, Object>> jobs = getDrugList(param);

        List<Future<HashMap<String, Object>>> futureList = new ArrayList<>();
        if (null != jobs && 0 < jobs.size()) {
            for (HashMap<String, Object> job : jobs) {
                futureList.add(threadPool.submit(new Callable<HashMap<String, Object>>() {
                    public HashMap<String, Object> call() throws Exception {
                        List<HashMap<String, Object>> temp2;
                        job.put("organ_code", job.get("ORGAN_CODE"));
                        job.put("hid", job.get("HID"));
                        HashMap<String, Object> temp = new HashMap<>();
                        if (CommonConsts.PATIENT_INHOSPITAL.equals((String) job.get("OH_MARK"))) {
                            temp2 = inHospitalRecordMapper.findRecordById(job);
                            if (null != temp2 && 0 < temp2.size()) {
                                temp = temp2.get(0);
                                temp.put("record_type", "住院");
                            }
                        } else {
                            temp2 = optRecordMapper.findRecordById(job);
                            if (null != temp2 && 0 < temp2.size()) {
                                temp = temp2.get(0);
                                if (CommonConsts.PATIENT_JZ.equals((String) job.get("OH_MARK")))
                                    temp.put("record_type", "急诊");
                                else
                                    temp.put("record_type", "门诊");
                            }
                        }
                        return temp;
                    }
                }));
            }
        }

        Common.convertFutureList(futureList, resList);
        threadPool.shutdown();
        resList = Common.sortUnionList(resList, "jzsj", null, null);
        return resList;
    }

    private List<HashMap<String, Object>> getDrugList(Map<String, Object> param) {
        List<HashMap<String, Object>> jobs1 = new ArrayList<>();
        List<HashMap<String, Object>> jobs2 = null;
        List<HashMap<String, Object>> jobs3 = null;
        List<Map<String, String>> phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) param.get("empiid"),CommonConsts.OP_EM_HP_MARK.MEN_ZHEN);
        if (!CollectionUtils.isEmpty(phridMap)) {
            if ("wm".equalsIgnoreCase((String) param.get("filterType"))) {
                for (Map<String, String> tmp : phridMap) {
                    param.put("phrid", tmp.get("PHRID"));
                    param.put("organ_code", tmp.get("ORGAN_CODE"));
                    jobs3 = drugsRecordMapper.getWMCodeList(param);
                    if (!CollectionUtils.isEmpty(jobs3)) {
                        //部分icd10代码隐私处理
                        for (HashMap<String, Object> res : jobs3) {
                            if(!Common.checkZdmc((String) res.get("zdmc"), (String) res.get("zddm"))){
                                jobs1.add(res);
                            }
                        }
                    }
                }
            } else if ("cm".equalsIgnoreCase((String) param.get("filterType"))) {
                for (Map<String, String> tmp : phridMap) {
                    param.put("phrid", tmp.get("PHRID"));
                    param.put("organ_code", tmp.get("ORGAN_CODE"));
                    jobs3 = drugsRecordMapper.getCMCodeList(param);
                    if (!CollectionUtils.isEmpty(jobs3)) {
                        //部分icd10代码隐私处理
                        for (HashMap<String, Object> res : jobs3) {
                            if(!Common.checkZdmc((String) res.get("zdmc"), (String) res.get("zddm"))){
                                jobs1.add(res);
                            }
                        }
                    }
                }
            } else {
                for (Map<String, String> tmp : phridMap) {
                    param.put("phrid", tmp.get("PHRID"));
                    param.put("organ_code", tmp.get("ORGAN_CODE"));
                    jobs3 = drugsRecordMapper.getWMCodeList(param);
                    jobs2 = drugsRecordMapper.getCMCodeList(param);
                    if (!CollectionUtils.isEmpty(jobs3)) {
                        //部分icd10代码隐私处理
                        for (HashMap<String, Object> res : jobs3) {
                            if(!Common.checkZdmc((String) res.get("zdmc"), (String) res.get("zddm"))){
                                jobs1.add(res);
                            }
                        }
                    }
                    if (!CollectionUtils.isEmpty(jobs2)) {
                        //部分icd10代码隐私处理
                        for (HashMap<String, Object> res : jobs2) {
                            if(!Common.checkZdmc((String) res.get("zdmc"), (String) res.get("zddm"))){
                                jobs1.add(res);
                            }
                        }
                    }
                }
            }
        }
        if (null == jobs1 || 0 >= jobs1.size()) {
            jobs1 = new ArrayList<HashMap<String, Object>>();
        }
        List<HashMap<String, Object>> resList = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> result1 = null;
        List<HashMap<String, Object>> result2 = null;
        Map<String, Object> temp = new HashMap<>();
        for (HashMap<String, Object> job : jobs1) {
            List<HashMap<String, Object>> iptOptList;
            temp.put("hid", job.get("HID"));
            temp.put("organ_code", job.get("ORGAN_CODE"));
            if (CommonConsts.PATIENT_INHOSPITAL.equals((String) job.get("RECORD_TYPE"))) {
                iptOptList = inHospitalRecordMapper.getReportById(temp);
            } else {
                iptOptList = optRecordMapper.getReportById(temp);
            }
            if (null == iptOptList || 0 >= iptOptList.size()) {
                result1 = new ArrayList<>();
            } else {
                job.put("drugName", param.get("drugName"));
                job.put("drugCode", param.get("drugCode"));
                result1 = indexRecordMapper.queryOneWMDrugIndex(job);
                result2 = indexRecordMapper.queryOneCMDrugIndex(job);
                if (null != result1 && 0 < result1.size()) {
                    if (null != result2 && 0 < result2.size()) {
                        result1.addAll(result2);
                    }
                } else {
                    result1 = result2;
                }
            }
            if (!CollectionUtils.isEmpty(result1)) {
                resList.addAll(result1);
            }
        }
        if (null != resList && 0 < resList.size()) {
            String sorttype = (String) param.get("sortType");
            if (Common.isNotEmpty(sorttype)) {
                String finalSorttype = sorttype.toUpperCase();
                Collections.sort(resList, new Comparator<HashMap<String, Object>>() {
                    public int compare(HashMap<String, Object> arg0, HashMap<String, Object> arg1) {
                        if ("SJ".equalsIgnoreCase(finalSorttype)) {
                            //比较日期
                            if (null == (Date) arg0.get("SJ") && null == (Date) arg1.get("SJ")) {
                                return 0;
                            } else if (null == (Date) arg1.get("SJ") && null != (Date) arg0.get("SJ")) {
                                return -1;
                            } else if (null != (Date) arg1.get("SJ") && null == (Date) arg0.get("SJ")) {
                                return 1;
                            } else {
                                return ((Date) arg1.get("SJ")).compareTo((Date) arg0.get("SJ"));
                            }
                        } else {
                            //第一次比较
                            int i;
                            if (Common.isEmpty((String) arg0.get(finalSorttype)) && Common.isEmpty((String) arg1.get(finalSorttype))) {
                                i = 0;
                            } else if (Common.isEmpty((String) arg0.get(finalSorttype)) && Common.isNotEmpty((String) arg1.get(finalSorttype))) {
                                i = -1;
                            } else if (Common.isNotEmpty((String) arg0.get(finalSorttype)) && Common.isEmpty((String) arg1.get(finalSorttype))) {
                                i = 1;
                            } else {
                                i = ((String) arg0.get(finalSorttype)).compareTo((String) arg1.get(finalSorttype));
                            }
                            //如果第一次比较相同进行第二次比较
                            if (i == 0) {
                                if (null == (Date) arg1.get("SJ") && null == (Date) arg0.get("SJ")) {
                                    return 0;
                                } else if (null == (Date) arg1.get("SJ") && null != (Date) arg0.get("SJ")) {
                                    return -1;
                                } else if (null != (Date) arg1.get("SJ") && null == (Date) arg0.get("SJ")) {
                                    return 1;
                                } else {
                                    return ((Date) arg1.get("SJ")).compareTo((Date) arg0.get("SJ"));
                                }
                            } else {
                                return i;
                            }
                        }
                    }
                });
            }
        }
//        threadPool.shutdown();
        return resList;
    }

    private List<TreeObject> sortPrescriptionIndexMenu(String empiid, String filtertype) {
        Map<String, Object> obj = new HashMap<>();
        List<TreeObject> ns = new ArrayList<>();
        List<Map<String, String>> phridMap;
        try {
            obj.put("filterType", filtertype);
            List<HashMap<String, Object>> prescriptions = new ArrayList<>();
            phridMap = phridService.getPhrIdsAndOrganCodeByEmpiidOfExam(empiid);
            if (!CollectionUtils.isEmpty(phridMap)) {
                for (Map<String, String> tmp : phridMap) {
                    obj.put("phrid", tmp.get("PHRID"));
                    obj.put("organcode", tmp.get("ORGAN_CODE"));
                    obj.put("mark", tmp.get("MARK"));
                    prescriptions.addAll(this.getSelectList(obj));
                }
                Common.sortUnionList(prescriptions, "sj", null, null);
            } else {
                log.warn("患者档案编号为空，empiid=" + empiid);
                return ns;
            }
            Map<String, List<HashMap<String, Object>>> listMap = prescriptions.stream().collect(Collectors.groupingBy(this::pickUp, LinkedHashMap::new, Collectors.toList()));
            listMap.forEach((key, values) -> {
                TreeObject mTemp = new TreeObject();
                List<TreeObject> tList = new ArrayList<>();
                mTemp.setName(key + "(" + values.size() + "次)");
                values.forEach(value -> {
                    TreeObject temp = new TreeObject();
                    setPrescriptionChildTreeObject(value, temp);
                    tList.add(temp);
                });
                mTemp.setChildren(tList);
                ns.add(mTemp);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            log.info(ex.getLocalizedMessage());
        }
        return ns;
    }

    private String pickUp(HashMap<String, Object> item) {
        String flag = null;
        try {
            Date obj = new SimpleDateFormat("yyyy-MM-dd").parse((String) item.get("sj"));
            if (obj == null) {
                return "无日期";
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date thrM = sdf.parse(Common.phaseDateToString(0, -3, 0));
            Date halfYear = sdf.parse(Common.phaseDateToString(0, -6, 0));
            Date Year = sdf.parse(Common.phaseDateToString(-1, 0, 0));
            if (0 >= thrM.compareTo(obj)) {
                flag = "近三个月";
            } else if (0 < thrM.compareTo(obj) && 0 >= halfYear.compareTo(obj)) {
                flag = "三个月前";
            } else if (0 < halfYear.compareTo(obj) && 0 >= Year.compareTo(obj)) {
                flag = "六个月前";
            } else if (0 < Year.compareTo(obj)) {
                flag = "一年前";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return flag;
    }

    private List<TreeObject> sortDrugIndexMenu(String empiid, String sorttype, String filtertype, String drugName) {
        List<HashMap<String, Object>> res;
        Map<String, Object> obj = new HashMap<String, Object>();
        List<TreeObject> ns = new ArrayList<TreeObject>();
        try {
            obj.put("empiid", empiid);
            obj.put("sortType", sorttype);
            obj.put("filterType", filtertype);
            obj.put("drugName", drugName);
            long startTime = System.currentTimeMillis();    //获取开始时间
            //res = indexRecordMapper.queryDrugIndex(obj);
            res = getDrugList(obj);
            long endTime = System.currentTimeMillis();    //获取结束时间
            log.debug("查询运行时间：" + (endTime - startTime) / 1000 + "s");
            if (null != res && 0 < res.size()) {
                LinkedHashMap<String, TreeObject> mMap = new LinkedHashMap<String, TreeObject>();
                LinkedHashMap<String, List<TreeObject>> tempMap = new LinkedHashMap<String, List<TreeObject>>();
                LinkedHashMap<String, List<TreeObject>> nullMap = new LinkedHashMap<String, List<TreeObject>>();
                if ("sj".equalsIgnoreCase(sorttype)) {
                    Date thrM = sdf.parse(Common.phaseDateToString(0, -3, 0));
                    Date halfYear = sdf.parse(Common.phaseDateToString(0, -6, 0));
                    Date Year = sdf.parse(Common.phaseDateToString(-1, 0, 0));
                    List<TreeObject> nullList = new ArrayList<TreeObject>();
                    List<TreeObject> tList = new ArrayList<TreeObject>();
                    String flag = "";
                    Map<String, Object> temp = null;
                    TreeObject tTemp = null;
                    TreeObject mTemp = null;
                    String mapKey = "";
                    for (int i = 0; i < res.size(); i++) {
                        temp = res.get(i);
                        tTemp = new TreeObject();
                        mapKey = (String) temp.get("RECORD_TYPE") + " " + (String) temp.get("ORGAN_CODE") + " " + (String) temp.get("DRUG_CODE");
                        if (null == (Date) temp.get("SJ")) {
                            setChildTreeObject(temp, tTemp, 0);
                            if (null != nullMap.get(mapKey)) {
                                nullList = nullMap.get(mapKey);
                            } else {
                                nullList = new ArrayList<TreeObject>();
                            }
                            nullList.add(tTemp);
                            nullMap.put(mapKey, nullList);
                        } else {
                            if (0 >= thrM.compareTo((Date) temp.get("SJ"))) {
                                if (Common.isNotEmpty(flag) && !"近三个月".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tempMap.keySet().size() + "种)");
                                    mTemp.setChildren(setChildTreeList(tempMap));
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                    tempMap = new LinkedHashMap<String, List<TreeObject>>();
                                }
                                setChildTreeObject(temp, tTemp, 1);
                                if (null != tempMap.get(mapKey)) {
                                    tList = tempMap.get(mapKey);
                                } else {
                                    tList = new ArrayList<TreeObject>();
                                }
                                tList.add(tTemp);
                                tempMap.put(mapKey, tList);
                                flag = "近三个月";
                            } else if (0 < thrM.compareTo((Date) temp.get("SJ")) && 0 >= halfYear.compareTo((Date) temp.get("SJ"))) {
                                if (Common.isNotEmpty(flag) && !"三个月前".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tempMap.keySet().size() + "种)");
                                    mTemp.setChildren(setChildTreeList(tempMap));
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                    tempMap = new LinkedHashMap<String, List<TreeObject>>();
                                }
                                setChildTreeObject(temp, tTemp, 2);
                                if (null != tempMap.get(mapKey)) {
                                    tList = tempMap.get(mapKey);
                                } else {
                                    tList = new ArrayList<TreeObject>();
                                }
                                tList.add(tTemp);
                                tempMap.put(mapKey, tList);
                                flag = "三个月前";
                            } else if (0 < halfYear.compareTo((Date) temp.get("SJ")) && 0 >= Year.compareTo((Date) temp.get("SJ"))) {
                                if (Common.isNotEmpty(flag) && !"六个月前".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tempMap.keySet().size() + "种)");
                                    mTemp.setChildren(setChildTreeList(tempMap));
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                    tempMap = new LinkedHashMap<String, List<TreeObject>>();
                                }
                                setChildTreeObject(temp, tTemp, 3);
                                if (null != tempMap.get(mapKey)) {
                                    tList = tempMap.get(mapKey);
                                } else {
                                    tList = new ArrayList<TreeObject>();
                                }
                                tList.add(tTemp);
                                tempMap.put(mapKey, tList);
                                flag = "六个月前";
                            } else if (0 < Year.compareTo((Date) temp.get("SJ"))) {
                                if (Common.isNotEmpty(flag) && !"一年前".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tempMap.keySet().size() + "种)");
                                    mTemp.setChildren(setChildTreeList(tempMap));
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                    tempMap = new LinkedHashMap<String, List<TreeObject>>();
                                }
                                setChildTreeObject(temp, tTemp, 4);
                                if (null != tempMap.get(mapKey)) {
                                    tList = tempMap.get(mapKey);
                                } else {
                                    tList = new ArrayList<TreeObject>();
                                }
                                tList.add(tTemp);
                                tempMap.put(mapKey, tList);
                                flag = "一年前";
                            }
                        }

                        if (i == (res.size() - 1)) {
                            if (null != tList && 0 < tList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName(flag + "(" + tempMap.keySet().size() + "种)");
                                mTemp.setChildren(setChildTreeList(tempMap));
                                mMap.put(flag, mTemp);
                                tList = new ArrayList<TreeObject>();
                                tempMap = new LinkedHashMap<String, List<TreeObject>>();
                            }
                            if (null != nullList && 0 < nullList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName("其他" + "(" + nullMap.keySet().size() + "种)");
                                mTemp.setChildren(setChildTreeList(nullMap));
                                mMap.put("其他", mTemp);
                            }
                        }
                    }
                    tList = null;
                    tempMap = null;
                    nullList = null;
                    nullMap = null;
                } else if ("useway_name".equalsIgnoreCase(sorttype)) {
                    String temp_name = "";
                    List<TreeObject> nullList = new ArrayList<TreeObject>();
                    List<TreeObject> tList = new ArrayList<TreeObject>();
                    Map<String, Object> temp = null;
                    TreeObject tTemp = null;
                    TreeObject mTemp = null;
                    String mapKey = "";
                    for (int i = 0; i < res.size(); i++) {
                        temp = res.get(i);
                        tTemp = new TreeObject();
                        mapKey = (String) temp.get("RECORD_TYPE") + " " + (String) temp.get("ORGAN_CODE") + " " + (String) temp.get("DRUG_CODE");
                        if (Common.isEmpty((String) temp.get("USEWAY_NAME"))) {
                            setChildTreeObject(temp, tTemp, null);
                            if (null != nullMap.get(mapKey)) {
                                nullList = nullMap.get(mapKey);
                            } else {
                                nullList = new ArrayList<TreeObject>();
                            }
                            nullList.add(tTemp);
                            nullMap.put(mapKey, nullList);
                        } else {
                            if (Common.isEmpty(temp_name)) {
                                temp_name = (String) temp.get("USEWAY_NAME");
                                setChildTreeObject(temp, tTemp, null);
                                if (null != tempMap.get(mapKey)) {
                                    tList = tempMap.get(mapKey);
                                } else {
                                    tList = new ArrayList<TreeObject>();
                                }
                                tList.add(tTemp);
                                tempMap.put(mapKey, tList);
                            } else {
                                if (temp_name.equals((String) temp.get("USEWAY_NAME"))) {
                                    setChildTreeObject(temp, tTemp, null);
                                    if (null != tempMap.get(mapKey)) {
                                        tList = tempMap.get(mapKey);
                                    } else {
                                        tList = new ArrayList<TreeObject>();
                                    }
                                    tList.add(tTemp);
                                    tempMap.put(mapKey, tList);
                                } else {
                                    mTemp = new TreeObject();
                                    mTemp.setName(temp_name + "(" + tempMap.keySet().size() + "种)");
                                    mTemp.setChildren(setChildTreeList(tempMap));
                                    mMap.put(temp_name, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                    tempMap = new LinkedHashMap<String, List<TreeObject>>();
                                    setChildTreeObject(temp, tTemp, null);
                                    temp_name = (String) temp.get("USEWAY_NAME");
                                    if (null != tempMap.get(mapKey)) {
                                        tList = tempMap.get(mapKey);
                                    } else {
                                        tList = new ArrayList<TreeObject>();
                                    }
                                    tList.add(tTemp);
                                    tempMap.put(mapKey, tList);
                                }
                            }
                        }

                        if (i == (res.size() - 1)) {
                            if (null != tList && 0 < tList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName(temp_name + "(" + tempMap.keySet().size() + "种)");
                                mTemp.setChildren(setChildTreeList(tempMap));
                                mMap.put(temp_name, mTemp);
                            }
                            if (null != nullList && 0 < nullList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName("其他" + "(" + nullMap.keySet().size() + "种)");
                                mTemp.setChildren(setChildTreeList(nullMap));
                                mMap.put("其他", mTemp);
                            }
                        }
                    }
                    tList = null;
                    tempMap = null;
                    nullList = null;
                    nullMap = null;
                } else {
                    String temp_name = "";
                    List<TreeObject> nullList = new ArrayList<TreeObject>();
                    List<TreeObject> tList = new ArrayList<TreeObject>();
                    Map<String, Object> temp = null;
                    TreeObject tTemp = null;
                    TreeObject mTemp = null;
                    String mapKey = "";
                    for (int i = 0; i < res.size(); i++) {
                        temp = res.get(i);
                        tTemp = new TreeObject();
                        mapKey = (String) temp.get("RECORD_TYPE") + " " + (String) temp.get("ORGAN_CODE") + " " + (String) temp.get("DRUG_CODE");
                        if (Common.isEmpty((String) temp.get("FLBM_NAME"))) {
                            setChildTreeObject(temp, tTemp, null);
                            if (null != nullMap.get(mapKey)) {
                                nullList = nullMap.get(mapKey);
                            } else {
                                nullList = new ArrayList<TreeObject>();
                            }
                            nullList.add(tTemp);
                            nullMap.put(mapKey, nullList);
                        } else {
                            if (Common.isEmpty(temp_name)) {
                                temp_name = (String) temp.get("FLBM_NAME");
                                setChildTreeObject(temp, tTemp, null);
                                if (null != tempMap.get(mapKey)) {
                                    tList = tempMap.get(mapKey);
                                } else {
                                    tList = new ArrayList<TreeObject>();
                                }
                                tList.add(tTemp);
                                tempMap.put(mapKey, tList);
                            } else {
                                if (temp_name.equals((String) temp.get("FLBM_NAME"))) {
                                    setChildTreeObject(temp, tTemp, null);
                                    if (null != tempMap.get(mapKey)) {
                                        tList = tempMap.get(mapKey);
                                    } else {
                                        tList = new ArrayList<TreeObject>();
                                    }
                                    tList.add(tTemp);
                                    tempMap.put(mapKey, tList);
                                } else {
                                    mTemp = new TreeObject();
                                    mTemp.setName(temp_name + "(" + tempMap.keySet().size() + "种)");
                                    mTemp.setChildren(setChildTreeList(tempMap));
                                    mMap.put(temp_name, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                    tempMap = new LinkedHashMap<String, List<TreeObject>>();
                                    setChildTreeObject(temp, tTemp, null);
                                    temp_name = (String) temp.get("FLBM_NAME");
                                    if (null != tempMap.get(mapKey)) {
                                        tList = tempMap.get(mapKey);
                                    } else {
                                        tList = new ArrayList<TreeObject>();
                                    }
                                    tList.add(tTemp);
                                    tempMap.put(mapKey, tList);

                                }
                            }
                        }

                        if (i == (res.size() - 1)) {
                            if (null != tList && 0 < tList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName(temp_name + "(" + tempMap.keySet().size() + "种)");
                                mTemp.setChildren(setChildTreeList(tempMap));
                                mMap.put(temp_name, mTemp);
                            }
                            if (null != nullList && 0 < nullList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName("其他" + "(" + nullMap.keySet().size() + "种)");
                                mTemp.setChildren(setChildTreeList(nullMap));
                                mMap.put("其他", mTemp);
                            }
                        }
                    }
                    tList = null;
                    tempMap = null;
                    nullList = null;
                    nullMap = null;
                }
                if (null != mMap && 0 < mMap.size()) {
                    for (String key : mMap.keySet()) {
                        ns.add(mMap.get(key));
                    }
                } else {
                    ns = null;
                }
            } else {
                ns = null;
            }
            long endTime2 = System.currentTimeMillis();    //获取结束时间
            log.debug("逻辑代码运行时间：" + (endTime2 - endTime) / 1000 + "s");
        } catch (Exception ex) {
            log.info(ex.getLocalizedMessage());
        }
        return ns;
    }

    private void setPrescriptionChildTreeObject(Map<String, Object> temp, TreeObject tTemp) {
        HostFormMap host = commonService.getBDAServer();
        String filenameVar = (String) host.get("prefix");
        tTemp.setName(temp.get("ORGAN_NAME") + " " + temp.get("sj"));
        if ("wm".equalsIgnoreCase((String) temp.get("type_desc"))) {
            filenameVar += "xymzcfj_new";
        } else {
            filenameVar += "zymzcfj_new";
        }
        tTemp.setResUrl("http://" + host.get("resUrl") + commonService.getPdfBaseUrl() + filenameVar + ".prpt/generatedContent?userid=admin&password=password&output-target=pageable/pdf&ORGAN_CODE="
                + Common.encodeUrlParam((String) temp.get("ORGAN_CODE")) + "&RX_CODE=" + temp.get("key_code") + "&PHRID=" + temp.get("phrid"));
    }

    private void setChildTreeObject(Map<String, Object> temp, TreeObject tTemp, Integer sj_flag) {
        if (Common.isNotEmpty((String) temp.get("DRUG_NAME"))) {
            tTemp.setName((String) temp.get("DRUG_NAME"));
        } else {
            tTemp.setName("无药品名称");
        }
        if (null != sj_flag) {
            tTemp.setResUrl(RES_URL + "?drugcode=" + temp.get("DRUG_CODE") +
                    "&organcode=" + temp.get("ORGAN_CODE") + "&recordcode=" + temp.get("RECORD_CODE") + "&sj=" + sj_flag);
        } else {
            tTemp.setResUrl(RES_URL + "?drugcode=" + temp.get("DRUG_CODE") +
                    "&organcode=" + temp.get("ORGAN_CODE") + "&recordcode=" + temp.get("RECORbrewD_CODE"));
        }
    }

    private List<TreeObject> setChildTreeList(Map<String, List<TreeObject>> mMap) {
        List<TreeObject> childList = new ArrayList<>();
        List<TreeObject> tList;
        TreeObject mTemp;
        for (String key : mMap.keySet()) {
            mTemp = new TreeObject();
            tList = mMap.get(key);
            mTemp.setName(tList.get(0).getName() + "(" + tList.size() + "次)");
            mTemp.setDescription(tList.get(0).getName());
            mTemp.setResUrl(tList.get(0).getResUrl());
            childList.add(mTemp);
        }
        return childList;
    }
}
