package cn.com.bsoft.service.data;

import cn.com.bsoft.mapper.BDB.*;
import cn.com.bsoft.util.Common;
import cn.com.bsoft.util.CommonConsts;
import cn.com.bsoft.util.TreeObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OptRecordService {
    private static final Logger log = Logger.getLogger(OptRecordService.class);
    private final String ZY = "住院";
    private final String ZY_URL = "/iptRecord/iptMain.do";
    private final String MZ_URL = "/optRecord/optMain.do";
    private final int[] ageArr = {0, 3, 9, 8, 25, 15};
    @Value("${env_flag}")
    public String ENV_FLAG = "";
    @Autowired
    private IndexRecordMapper indexRecordMapper;
    @Autowired
    private Opt_RecordMapper optRecordMapper;
    @Autowired
    private Drugs_RecordMapper drugsRecordMapper;
    @Autowired
    private Test_RecordMapper testRecordMapper;
    @Autowired
    private Examination_RecordMapper examinationRecordMapper;
    @Autowired
    private Cost_RecordMapper costRecordMapper;
    @Value("${YXURL}")
    public String yxUrl = "";
    @Autowired
    private PhridService phridService;
    @Autowired
    private Ehr_HealthRecordMapper ehrHealthRecordMapper;
    @Autowired
    private MedicalRecordMapper medicalRecordMapper;


    public Map<String, Object> getMainInfo(Map<String, Object> param) {
        Map<String, Object> result = new HashMap<>();
//        UserFormMap userFormMap = (UserFormMap) Common.findUserSession();
        String id = (String) param.get("id");
        if (Common.isNotEmpty(id)) {
            param.put("hid", id);
            param.put("organ_code", param.get("organcode"));
            List<HashMap<String, Object>> res = optRecordMapper.getReportById(param);
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

    public void getRecordCount(HashMap<String, Object> param, Map<String, Object> result) {
        List<HashMap<String, Object>> result1;
        param.put("organcode", param.get("organ_code"));
        //病历
        result1 = medicalRecordMapper.getMedRecord(param);
        if (null != result1 && 0 < result1.size()) {
            Map<String, Object> resMap = result1.get(0);
            if (Common.isEmpty((String) resMap.get("zs"))
                    && Common.isEmpty((String) resMap.get("xbs"))
                    && Common.isEmpty((String) resMap.get("jws"))
                    && Common.isEmpty((String) resMap.get("tgjc"))
                    && Common.isEmpty((String) resMap.get("fzjc"))) {
                result.put("BL_TOTAL", "0");
            } else {
                result.put("BL_TOTAL", "1");
            }
        } else {
            result.put("BL_TOTAL", "0");
        }

        //处方
        result1 = drugsRecordMapper.getWMSelectList(param);
        if (null != result1 && 0 < result1.size()) {
            result.put("CF_TOTAL", "1");
        } else {
            result1 = drugsRecordMapper.getCMSelectList(param);
            if (null != result1 && 0 < result1.size()) {
                result.put("CF_TOTAL", "1");
            } else {
                result.put("CF_TOTAL", "0");
            }
        }

        //检验
        result1 = testRecordMapper.getSelectList(param);
        if (null != result1 && 0 < result1.size()) {
            if (CommonConsts.TX_ENV.equalsIgnoreCase(ENV_FLAG)) {
                //药敏
                List<String> rptNos = new ArrayList<>();
                for (Map<String, Object> tmp : result1) {
                    rptNos.add((String) tmp.get("key_code"));
                }
                param.put("rptNos", rptNos);
                int bioCount = testRecordMapper.countTestDrugResult(param);
                if (0 < bioCount) {
                    result.put("YM_TOTAL", "1");
                } else {
                    result.put("YM_TOTAL", "0");
                }
            }
            result.put("JY_TOTAL", "1");
        } else {
            result.put("JY_TOTAL", "0");
        }
        //检查
        result1 = examinationRecordMapper.getSelectList(param);
        if (null != result1 && 0 < result1.size()) {
            result.put("JC_TOTAL", "1");
            //影像
            String temStr = null;
            for (Map<String, Object> tmp : result1) {
                temStr = (String) tmp.get("image_uidaddr");
                if (Common.isNotEmpty(temStr)) {
                    break;
                }
            }
            if (Common.isNotEmpty(temStr)) {
                result.put("YX_TOTAL", "1");
            } else {
                result.put("YX_TOTAL", "0");
            }

        } else {
            result.put("JC_TOTAL", "0");
            result.put("YX_TOTAL", "0");
        }

        //费用
        result1 = costRecordMapper.getSelectList(param);
        if (null != result1 && 0 < result1.size()) {
            result.put("PJ_TOTAL", "1");
        } else {
            result.put("PJ_TOTAL", "0");
        }
    }

    /**
     * 获取菜单列表
     *
     * @param empiid
     * @param sorttype
     * @param filtertype
     * @param zdmc
     * @return
     */
    public List<TreeObject> getIndexMenu(String empiid, String sorttype, String filtertype, String zdmc) {
        List<TreeObject> indexList = null;
        try {
            indexList = sortIndexMenu2(empiid, sorttype, filtertype, zdmc);
            if (null == indexList || 0 >= indexList.size()) {
                indexList = new ArrayList<TreeObject>();
            }
        } catch (Exception ex) {
            log.info(ex.getLocalizedMessage());
            indexList = new ArrayList<TreeObject>();
        }
        return indexList;
    }

    private String checkSj(HashMap<String, Object> m, String sorttype) {
        String flag = null;
        if ("sj".equalsIgnoreCase(sorttype)) {
            Date obj = (Date) m.get("sj");
            if (obj == null) {
                return "无日期";
            }
            try {
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
        } else if ("zdmc".equalsIgnoreCase(sorttype)) {
            String obj = (String) m.get("zdmc");
            if (obj == null) {
                return "/";
            } else {
                flag = obj;
            }
        } else if ("smzq".equalsIgnoreCase(sorttype)) {

        } else {
            String obj = (String) m.get("organ_name");
            if (obj == null) {
                return "无医疗机构名称";
            } else {
                flag = obj;
            }
        }
        return flag;
    }

    private List<TreeObject> sortIndexMenu2(String empiid, String sorttype, String filtertype, String zdmc) {
        Map<String, Object> obj = new HashMap<String, Object>();
        List<TreeObject> ns = new ArrayList<TreeObject>();
        try {
            if (!"smzq".equals(sorttype)) {
                obj.put("sortType", sorttype);
            } else {
                obj.put("sortType", "sj");
            }
            obj.put("filterType", filtertype);
            obj.put("zdmc", zdmc);
            List<HashMap<String, Object>> result1 = new ArrayList<>();
            List<HashMap<String, Object>> result2 = new ArrayList<>();
            List<HashMap<String, Object>> result3 = new ArrayList<>();
            List<HashMap<String, Object>> result4 = new ArrayList<>();
            List<Map<String, String>> phridMap = null;

            if ("mz".equalsIgnoreCase((String) obj.get("filterType"))) {
                phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.MEN_ZHEN);
                if (!CollectionUtils.isEmpty(phridMap)) {
                    for (Map<String, String> tmp : phridMap) {
                        obj.put("phrid", tmp.get("PHRID"));
                        obj.put("organ_code", tmp.get("ORGAN_CODE"));
                        result3 = indexRecordMapper.queryOptIndexRecord(obj);
                        if (!CollectionUtils.isEmpty(result3)) {
                            result4.addAll(result3);
                        }
                    }
                } else {
                    log.warn("患者档案编号为空-门诊，empiid=" + empiid);
                }

            } else if ("zy".equalsIgnoreCase((String) obj.get("filterType"))) {
                phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.ZHU_YUAN);
                if (!CollectionUtils.isEmpty(phridMap)) {
                    for (Map<String, String> tmp : phridMap) {
                        obj.put("phrid", tmp.get("PHRID"));
                        obj.put("organ_code", tmp.get("ORGAN_CODE"));
                        result2 = indexRecordMapper.queryIptIndexRecord(obj);
                        if (!CollectionUtils.isEmpty(result2)) {
                            result4.addAll(result2);
                        }
                    }
                } else {
                    log.warn("患者档案编号为空-住院，empiid=" + empiid);
                }
            } else {
                phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.MEN_ZHEN);
                if (!CollectionUtils.isEmpty(phridMap)) {
                    for (Map<String, String> tmp : phridMap) {
                        obj.put("phrid", tmp.get("PHRID"));
                        obj.put("organ_code", tmp.get("ORGAN_CODE"));
                        result3 = indexRecordMapper.queryOptIndexRecord(obj);
                        if (!CollectionUtils.isEmpty(result3)) {
                            result4.addAll(result3);
                        }
                    }
                } else {
                    log.warn("患者档案编号为空-门诊，empiid=" + empiid);
                }
                phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.ZHU_YUAN);
                if (!CollectionUtils.isEmpty(phridMap)) {
                    for (Map<String, String> tmp : phridMap) {
                        obj.put("phrid", tmp.get("PHRID"));
                        obj.put("organ_code", tmp.get("ORGAN_CODE"));
                        result2 = indexRecordMapper.queryIptIndexRecord(obj);
                        if (!CollectionUtils.isEmpty(result2)) {
                            result4.addAll(result2);
                        }
                    }
                } else {
                    log.warn("患者档案编号为空-住院，empiid=" + empiid);
                }
            }


            if (null != result4 && 0 < result4.size()) {
                if ("sj".equalsIgnoreCase((String) obj.get("sortType"))) {
                    result4 = Common.sortUnionList(result4, (String) obj.get("sortType"), null, null);
                } else {
                    result4 = Common.sortUnionList(result4, (String) obj.get("sortType"), "sj", null);
                }
                //部分icd10代码隐私处理
                for (HashMap<String, Object> res : result4) {
                    //res.put("zdmc", Common.formatZdmc((String) res.get("zdmc"), (String) res.get("zddm")));
                    if (!Common.checkZdmc((String) res.get("zdmc"), (String) res.get("zddm"))) {
                        result1.add(res);
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                List<TreeObject> finalNs = new ArrayList<TreeObject>();
                Map<String, List<HashMap<String, Object>>> listMap = result1.stream().collect(Collectors.groupingBy(m -> checkSj(m, sorttype), LinkedHashMap::new, Collectors.toList()));
                listMap.forEach((key, value) -> {
                    TreeObject mTemp = new TreeObject();
                    List<TreeObject> tList = new ArrayList<TreeObject>();
                    mTemp.setName(key + "(" + value.size() + "次)");
                    value.forEach(m -> {
                        TreeObject temp = new TreeObject();
                        setChildTreeObject(m, temp, sdf);
                        tList.add(temp);
                    });
                    mTemp.setChildren(tList);
                    finalNs.add(mTemp);
                });
                return finalNs;
//                } else if ("smzq".equalsIgnoreCase(sorttype)) {
//                    List<Date> ageDate = new ArrayList<Date>();
//                    Calendar calendar = Calendar.getInstance();
//                    try {
//                        HashMap<String, Object> jm = ehrHealthRecordMapper.findJMByEmpiid(empiid);
//                        if (!CollectionUtils.isEmpty(jm)) {
//                            calendar.setTime(sdf.parse((String) jm.get("birthday")));
//                            for (int i : ageArr) {
//                                calendar.add(Calendar.YEAR, i);
//                                ageDate.add(calendar.getTime());
//                            }
//                        }
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//
//                    Long tempDate = null, tempDate1 = null, tempDate2 = null, tempDate3 = null, tempDate4 = null, tempDate5 = null, tempDate6 = null;
//                    if (!CollectionUtils.isEmpty(ageDate)) {
//                        tempDate = ageDate.get(1).getTime();
//                        tempDate1 = ageDate.get(2).getTime();
//                        tempDate2 = ageDate.get(3).getTime();
//                        tempDate3 = ageDate.get(4).getTime();
//                        tempDate4 = ageDate.get(5).getTime();
//                        tempDate5 = ageDate.get(6).getTime();
//                        tempDate6 = ageDate.get(7).getTime();
//                    }
//
//                    List<TreeObject> nullList = new ArrayList<TreeObject>();
//                    List<TreeObject> tList = new ArrayList<TreeObject>();
//                    String flag = "";
//                    Map<String, Object> temp = null;
//                    TreeObject tTemp = null;
//                    TreeObject mTemp = null;
//                    for (int i = 0; i < result1.size(); i++) {
//                        temp = result1.get(i);
//                        tTemp = new TreeObject();
//                        if (null == (Date) temp.get("sj") || CollectionUtils.isEmpty(ageDate)) {
//                            setChildTreeObject(temp, tTemp, sdf);
//                            nullList.add(tTemp);
//                        } else {
//                            if (((Date) temp.get("sj")).getTime() <= tempDate) {
//                                if (Common.isNotEmpty(flag) && !"婴幼儿期".equals(flag)) {
//                                    mTemp = new TreeObject();
//                                    mTemp.setName(flag + "(" + tList.size() + "次)");
//                                    mTemp.setChildren(tList);
//                                    mMap.put(flag, mTemp);
//                                    tList = new ArrayList<TreeObject>();
//                                }
//                                setChildTreeObject(temp, tTemp, sdf);
//                                tList.add(tTemp);
//                                flag = "婴幼儿期";
//                            } else if (((Date) temp.get("sj")).getTime() > tempDate && ((Date) temp.get("sj")).getTime() <= tempDate1) {
//                                if (Common.isNotEmpty(flag) && !"学龄（前）期".equals(flag)) {
//                                    mTemp = new TreeObject();
//                                    mTemp.setName(flag + "(" + tList.size() + "次)");
//                                    mTemp.setChildren(tList);
//                                    mMap.put(flag, mTemp);
//                                    tList = new ArrayList<TreeObject>();
//                                }
//                                setChildTreeObject(temp, tTemp, sdf);
//                                tList.add(tTemp);
//                                flag = "学龄（前）期";
//                            } else if (((Date) temp.get("sj")).getTime() > tempDate1 && ((Date) temp.get("sj")).getTime() <= tempDate2) {
//                                if (Common.isNotEmpty(flag) && !"青春期".equals(flag)) {
//                                    mTemp = new TreeObject();
//                                    mTemp.setName(flag + "(" + tList.size() + "次)");
//                                    mTemp.setChildren(tList);
//                                    mMap.put(flag, mTemp);
//                                    tList = new ArrayList<TreeObject>();
//                                }
//                                setChildTreeObject(temp, tTemp, sdf);
//                                tList.add(tTemp);
//                                flag = "青春期";
//                            } else if (((Date) temp.get("sj")).getTime() > tempDate2 && ((Date) temp.get("sj")).getTime() <= tempDate3) {
//                                if (Common.isNotEmpty(flag) && !"青年期".equals(flag)) {
//                                    mTemp = new TreeObject();
//                                    mTemp.setName(flag + "(" + tList.size() + "次)");
//                                    mTemp.setChildren(tList);
//                                    mMap.put(flag, mTemp);
//                                    tList = new ArrayList<TreeObject>();
//                                }
//                                setChildTreeObject(temp, tTemp, sdf);
//                                tList.add(tTemp);
//                                flag = "青年期";
//                            } else if (((Date) temp.get("sj")).getTime() > tempDate3 && ((Date) temp.get("sj")).getTime() <= tempDate4) {
//                                if (Common.isNotEmpty(flag) && !"中年期".equals(flag)) {
//                                    mTemp = new TreeObject();
//                                    mTemp.setName(flag + "(" + tList.size() + "次)");
//                                    mTemp.setChildren(tList);
//                                    mMap.put(flag, mTemp);
//                                    tList = new ArrayList<TreeObject>();
//                                }
//                                setChildTreeObject(temp, tTemp, sdf);
//                                tList.add(tTemp);
//                                flag = "中年期";
//                            } else if (((Date) temp.get("sj")).getTime() > tempDate4 && ((Date) temp.get("sj")).getTime() <= tempDate5) {
//                                if (Common.isNotEmpty(flag) && !"年轻老年期".equals(flag)) {
//                                    mTemp = new TreeObject();
//                                    mTemp.setName(flag + "(" + tList.size() + "次)");
//                                    mTemp.setChildren(tList);
//                                    mMap.put(flag, mTemp);
//                                    tList = new ArrayList<TreeObject>();
//                                }
//                                setChildTreeObject(temp, tTemp, sdf);
//                                tList.add(tTemp);
//                                flag = "年轻老年期";
//                            } else if (((Date) temp.get("sj")).getTime() > tempDate5 && ((Date) temp.get("sj")).getTime() <= tempDate6) {
//                                if (Common.isNotEmpty(flag) && !"老年期".equals(flag)) {
//                                    mTemp = new TreeObject();
//                                    mTemp.setName(flag + "(" + tList.size() + "次)");
//                                    mTemp.setChildren(tList);
//                                    mMap.put(flag, mTemp);
//                                    tList = new ArrayList<TreeObject>();
//                                }
//                                setChildTreeObject(temp, tTemp, sdf);
//                                tList.add(tTemp);
//                                flag = "老年期";
//                            } else if (((Date) temp.get("sj")).getTime() > tempDate6) {
//                                if (Common.isNotEmpty(flag) && !"长寿老年期".equals(flag)) {
//                                    mTemp = new TreeObject();
//                                    mTemp.setName(flag + "(" + tList.size() + "次)");
//                                    mTemp.setChildren(tList);
//                                    mMap.put(flag, mTemp);
//                                    tList = new ArrayList<TreeObject>();
//                                }
//                                setChildTreeObject(temp, tTemp, sdf);
//                                tList.add(tTemp);
//                                flag = "长寿老年期";
//                            }
//                        }
//
//                        if (i == (result1.size() - 1)) {
//                            if (null != tList && 0 < tList.size()) {
//                                mTemp = new TreeObject();
//                                mTemp.setName(flag + "(" + tList.size() + "次)");
//                                mTemp.setChildren(tList);
//                                mMap.put(flag, mTemp);
//                            }
//                            if (null != nullList && 0 < nullList.size()) {
//                                mTemp = new TreeObject();
//                                mTemp.setName("其他" + "(" + nullList.size() + "次)");
//                                mTemp.setChildren(nullList);
//                                mMap.put("其他", mTemp);
//                            }
//                        }
//                    }
//
//            } else {
//                ns = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.info(ex.getLocalizedMessage());
        }
        return ns;
    }

    private List<TreeObject> sortIndexMenu(String empiid, String sorttype, String filtertype, String zdmc) {
        Map<String, Object> obj = new HashMap<String, Object>();
        List<TreeObject> ns = new ArrayList<TreeObject>();
        try {
            if (!"smzq".equals(sorttype)) {
                obj.put("sortType", sorttype);
            } else {
                obj.put("sortType", "sj");
            }
            obj.put("filterType", filtertype);
            obj.put("zdmc", zdmc);
            List<HashMap<String, Object>> result1 = new ArrayList<>();
            List<HashMap<String, Object>> result2 = new ArrayList<>();
            List<HashMap<String, Object>> result3 = new ArrayList<>();
            List<HashMap<String, Object>> result4 = new ArrayList<>();
            List<Map<String, String>> phridMap = null;

            if ("mz".equalsIgnoreCase((String) obj.get("filterType"))) {
                phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.MEN_ZHEN);
                if (!CollectionUtils.isEmpty(phridMap)) {
                    for (Map<String, String> tmp : phridMap) {
                        obj.put("phrid", tmp.get("PHRID"));
                        obj.put("organ_code", tmp.get("ORGAN_CODE"));
                        result3 = indexRecordMapper.queryOptIndexRecord(obj);
                        if (!CollectionUtils.isEmpty(result3)) {
                            result4.addAll(result3);
                        }
                    }
                } else {
                    log.warn("患者档案编号为空-门诊，empiid=" + empiid);
                }

            } else if ("zy".equalsIgnoreCase((String) obj.get("filterType"))) {
                phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.ZHU_YUAN);
                if (!CollectionUtils.isEmpty(phridMap)) {
                    for (Map<String, String> tmp : phridMap) {
                        obj.put("phrid", tmp.get("PHRID"));
                        obj.put("organ_code", tmp.get("ORGAN_CODE"));
                        result2 = indexRecordMapper.queryIptIndexRecord(obj);
                        if (!CollectionUtils.isEmpty(result2)) {
                            result4.addAll(result2);
                        }
                    }
                } else {
                    log.warn("患者档案编号为空-住院，empiid=" + empiid);
                }
            } else {
                phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.MEN_ZHEN);
                if (!CollectionUtils.isEmpty(phridMap)) {
                    for (Map<String, String> tmp : phridMap) {
                        obj.put("phrid", tmp.get("PHRID"));
                        obj.put("organ_code", tmp.get("ORGAN_CODE"));
                        result3 = indexRecordMapper.queryOptIndexRecord(obj);
                        if (!CollectionUtils.isEmpty(result3)) {
                            result4.addAll(result3);
                        }
                    }
                } else {
                    log.warn("患者档案编号为空-门诊，empiid=" + empiid);
                }
                phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.ZHU_YUAN);
                if (!CollectionUtils.isEmpty(phridMap)) {
                    for (Map<String, String> tmp : phridMap) {
                        obj.put("phrid", tmp.get("PHRID"));
                        obj.put("organ_code", tmp.get("ORGAN_CODE"));
                        result2 = indexRecordMapper.queryIptIndexRecord(obj);
                        if (!CollectionUtils.isEmpty(result2)) {
                            result4.addAll(result2);
                        }
                    }
                } else {
                    log.warn("患者档案编号为空-住院，empiid=" + empiid);
                }
            }


            if (null != result4 && 0 < result4.size()) {
                if ("sj".equalsIgnoreCase((String) obj.get("sortType"))) {
                    result4 = Common.sortUnionList(result4, (String) obj.get("sortType"), null, null);
                } else {
                    result4 = Common.sortUnionList(result4, (String) obj.get("sortType"), "sj", null);
                }
                //部分icd10代码隐私处理
                for (HashMap<String, Object> res : result4) {
                    //res.put("zdmc", Common.formatZdmc((String) res.get("zdmc"), (String) res.get("zddm")));
                    if (!Common.checkZdmc((String) res.get("zdmc"), (String) res.get("zddm"))) {
                        result1.add(res);
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                LinkedHashMap<String, TreeObject> mMap = new LinkedHashMap<String, TreeObject>();
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
                    for (int i = 0; i < result1.size(); i++) {
                        temp = result1.get(i);
                        tTemp = new TreeObject();
                        if (null == (Date) temp.get("sj")) {
                            setChildTreeObject(temp, tTemp, sdf);
                            nullList.add(tTemp);
                        } else {
                            if (0 >= thrM.compareTo((Date) temp.get("sj"))) {
                                if (Common.isNotEmpty(flag) && !"近三个月".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "近三个月";
                            } else if (0 < thrM.compareTo((Date) temp.get("sj")) && 0 >= halfYear.compareTo((Date) temp.get("sj"))) {
                                if (Common.isNotEmpty(flag) && !"三个月前".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "三个月前";
                            } else if (0 < halfYear.compareTo((Date) temp.get("sj")) && 0 >= Year.compareTo((Date) temp.get("sj"))) {
                                if (Common.isNotEmpty(flag) && !"六个月前".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "六个月前";
                            } else if (0 < Year.compareTo((Date) temp.get("sj"))) {
                                if (Common.isNotEmpty(flag) && !"一年前".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "一年前";
                            }
                        }

                        if (i == (result1.size() - 1)) {
                            if (null != tList && 0 < tList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName(flag + "(" + tList.size() + "次)");
                                mTemp.setChildren(tList);
                                mMap.put(flag, mTemp);
                            }
                            if (null != nullList && 0 < nullList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName("无日期" + "(" + nullList.size() + "次)");
                                mTemp.setChildren(nullList);
                                mMap.put("无日期", mTemp);
                            }
                        }
                    }
                } else if ("smzq".equalsIgnoreCase(sorttype)) {
                    List<Date> ageDate = new ArrayList<Date>();
                    Calendar calendar = Calendar.getInstance();
                    try {
                        HashMap<String, Object> jm = ehrHealthRecordMapper.findJMByEmpiid(empiid);
                        if (!CollectionUtils.isEmpty(jm)) {
                            calendar.setTime(sdf.parse((String) jm.get("birthday")));
                            for (int i : ageArr) {
                                calendar.add(Calendar.YEAR, i);
                                ageDate.add(calendar.getTime());
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Long tempDate = null, tempDate1 = null, tempDate2 = null, tempDate3 = null, tempDate4 = null, tempDate5 = null, tempDate6 = null;
                    if (!CollectionUtils.isEmpty(ageDate)) {
                        tempDate = ageDate.get(1).getTime();
                        tempDate1 = ageDate.get(2).getTime();
                        tempDate2 = ageDate.get(3).getTime();
                        tempDate3 = ageDate.get(4).getTime();
                        tempDate4 = ageDate.get(5).getTime();
                        tempDate5 = ageDate.get(6).getTime();
                        tempDate6 = ageDate.get(7).getTime();
                    }

                    List<TreeObject> nullList = new ArrayList<TreeObject>();
                    List<TreeObject> tList = new ArrayList<TreeObject>();
                    String flag = "";
                    Map<String, Object> temp = null;
                    TreeObject tTemp = null;
                    TreeObject mTemp = null;
                    for (int i = 0; i < result1.size(); i++) {
                        temp = result1.get(i);
                        tTemp = new TreeObject();
                        if (null == (Date) temp.get("sj") || CollectionUtils.isEmpty(ageDate)) {
                            setChildTreeObject(temp, tTemp, sdf);
                            nullList.add(tTemp);
                        } else {
                            if (((Date) temp.get("sj")).getTime() <= tempDate) {
                                if (Common.isNotEmpty(flag) && !"婴幼儿期".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "婴幼儿期";
                            } else if (((Date) temp.get("sj")).getTime() > tempDate && ((Date) temp.get("sj")).getTime() <= tempDate1) {
                                if (Common.isNotEmpty(flag) && !"学龄（前）期".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "学龄（前）期";
                            } else if (((Date) temp.get("sj")).getTime() > tempDate1 && ((Date) temp.get("sj")).getTime() <= tempDate2) {
                                if (Common.isNotEmpty(flag) && !"青春期".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "青春期";
                            } else if (((Date) temp.get("sj")).getTime() > tempDate2 && ((Date) temp.get("sj")).getTime() <= tempDate3) {
                                if (Common.isNotEmpty(flag) && !"青年期".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "青年期";
                            } else if (((Date) temp.get("sj")).getTime() > tempDate3 && ((Date) temp.get("sj")).getTime() <= tempDate4) {
                                if (Common.isNotEmpty(flag) && !"中年期".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "中年期";
                            } else if (((Date) temp.get("sj")).getTime() > tempDate4 && ((Date) temp.get("sj")).getTime() <= tempDate5) {
                                if (Common.isNotEmpty(flag) && !"年轻老年期".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "年轻老年期";
                            } else if (((Date) temp.get("sj")).getTime() > tempDate5 && ((Date) temp.get("sj")).getTime() <= tempDate6) {
                                if (Common.isNotEmpty(flag) && !"老年期".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "老年期";
                            } else if (((Date) temp.get("sj")).getTime() > tempDate6) {
                                if (Common.isNotEmpty(flag) && !"长寿老年期".equals(flag)) {
                                    mTemp = new TreeObject();
                                    mTemp.setName(flag + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(flag, mTemp);
                                    tList = new ArrayList<TreeObject>();
                                }
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                                flag = "长寿老年期";
                            }
                        }

                        if (i == (result1.size() - 1)) {
                            if (null != tList && 0 < tList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName(flag + "(" + tList.size() + "次)");
                                mTemp.setChildren(tList);
                                mMap.put(flag, mTemp);
                            }
                            if (null != nullList && 0 < nullList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName("其他" + "(" + nullList.size() + "次)");
                                mTemp.setChildren(nullList);
                                mMap.put("其他", mTemp);
                            }
                        }
                    }

                } else if ("zdmc".equalsIgnoreCase(sorttype)) {
                    String temp_zdmc = "";
                    List<TreeObject> nullList = new ArrayList<TreeObject>();
                    List<TreeObject> tList = new ArrayList<TreeObject>();
                    Map<String, Object> temp = null;
                    TreeObject tTemp = null;
                    TreeObject mTemp = null;
                    for (int i = 0; i < result1.size(); i++) {
                        temp = result1.get(i);
                        tTemp = new TreeObject();
                        if (Common.isEmpty((String) temp.get("zdmc"))) {
                            setChildTreeObject(temp, tTemp, sdf);
                            nullList.add(tTemp);
                        } else {
                            if (Common.isEmpty(temp_zdmc)) {
                                temp_zdmc = (String) temp.get("zdmc");
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                            } else {
                                if (temp_zdmc.equals((String) temp.get("zdmc"))) {
                                    setChildTreeObject(temp, tTemp, sdf);
                                    tList.add(tTemp);
                                } else {
                                    //先将之前的list存入children字段并放入map
                                    mTemp = new TreeObject();
                                    mTemp.setName(temp_zdmc + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(temp_zdmc, mTemp);
                                    //读取当前temp_name下是否有记录存在，存在则读取children
                                    temp_zdmc = (String) temp.get("zdmc");
                                    if (null != mMap.get(temp_zdmc)) {
                                        mTemp = mMap.get(temp_zdmc);
                                        tList = mTemp.getChildren();
                                        if (null == tList || 0 >= tList.size()) {
                                            tList = new ArrayList<TreeObject>();
                                        }
                                    } else {
                                        //如果不存在记录，重新new一个children的list
                                        tList = new ArrayList<TreeObject>();
                                    }
                                    setChildTreeObject(temp, tTemp, sdf);
                                    tList.add(tTemp);
                                }
                            }
                        }

                        if (i == (result1.size() - 1)) {
                            if (null != tList && 0 < tList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName(temp_zdmc + "(" + tList.size() + "次)");
                                mTemp.setChildren(tList);
                                mMap.put(temp_zdmc, mTemp);
                            }
                            if (null != nullList && 0 < nullList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName("/" + "(" + nullList.size() + "次)");
                                mTemp.setChildren(nullList);
                                mMap.put("/", mTemp);
                            }
                        }
                    }
                } else {
                    String temp_name = "";
                    List<TreeObject> nullList = new ArrayList<TreeObject>();
                    List<TreeObject> tList = new ArrayList<TreeObject>();
                    Map<String, Object> temp = null;
                    TreeObject tTemp = null;
                    TreeObject mTemp = null;
                    for (int i = 0; i < result1.size(); i++) {
                        temp = result1.get(i);
                        tTemp = new TreeObject();
                        if (Common.isEmpty((String) temp.get("organ_name"))) {
                            setChildTreeObject(temp, tTemp, sdf);
                            nullList.add(tTemp);
                        } else {
                            if (Common.isEmpty(temp_name)) {
                                temp_name = (String) temp.get("organ_name");
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                            } else {
                                if (temp_name.equals((String) temp.get("organ_name"))) {
                                    setChildTreeObject(temp, tTemp, sdf);
                                    tList.add(tTemp);
                                } else {
                                    //先将之前的list存入children字段并放入map
                                    mTemp = new TreeObject();
                                    mTemp.setName(temp_name + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(temp_name, mTemp);
                                    //读取当前temp_name下是否有记录存在，存在则读取children
                                    temp_name = (String) temp.get("organ_name");
                                    if (null != mMap.get(temp_name)) {
                                        mTemp = mMap.get(temp_name);
                                        tList = mTemp.getChildren();
                                        if (null == tList || 0 >= tList.size()) {
                                            tList = new ArrayList<TreeObject>();
                                        }
                                    } else {
                                        //如果不存在记录，重新new一个children的list
                                        tList = new ArrayList<TreeObject>();
                                    }
                                    setChildTreeObject(temp, tTemp, sdf);
                                    tList.add(tTemp);
                                }
                            }
                        }

                        if (i == (result1.size() - 1)) {
                            if (null != tList && 0 < tList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName(temp_name + "(" + tList.size() + "次)");
                                mTemp.setChildren(tList);
                                mMap.put(temp_name, mTemp);
                            }
                            if (null != nullList && 0 < nullList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName("无医疗机构名称" + "(" + nullList.size() + "次)");
                                mTemp.setChildren(nullList);
                                mMap.put("无医疗机构名称", mTemp);
                            }
                        }
                    }
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
        } catch (Exception ex) {
            ex.printStackTrace();
            log.info(ex.getLocalizedMessage());
        }
        return ns;
    }

    private void setChildTreeObject(Map<String, Object> temp, TreeObject tTemp, SimpleDateFormat sdf) {
        String sjTemp;
        String zdmcTemp;
        if (null != temp.get("sj")) {
            sjTemp = sdf.format((Date) temp.get("sj"));
        } else {
            sjTemp = "";
        }
        if (Common.isNotEmpty((String) temp.get("zdmc")) && !"null".equalsIgnoreCase(((String) temp.get("zdmc")).trim())) {
            zdmcTemp = (String) temp.get("zdmc");
        } else {
            zdmcTemp = "/";
        }
        tTemp.setName(sjTemp + "  " + temp.get("record_type") + "  " + zdmcTemp);
        BigDecimal idbd;
        if (null != temp.get("hid")) {
            if (temp.get("hid") instanceof BigDecimal) {
                idbd = (BigDecimal) temp.get("hid");
            } else if (temp.get("hid") instanceof Integer) {
                idbd = new BigDecimal((Integer) temp.get("hid"));
            } else if (temp.get("hid") instanceof String) {
                tTemp.setHid((String) temp.get("hid"));
                idbd = new BigDecimal("0");
            } else {
                idbd = new BigDecimal("0");
            }
        } else {
            idbd = new BigDecimal("0");
        }
        tTemp.setId(idbd.intValue());
        tTemp.setOrganCode((String) temp.get("organ_code"));
        if (ZY.equals(temp.get("record_type"))) {
            tTemp.setResUrl(ZY_URL);
        } else {
            tTemp.setResUrl(MZ_URL);
        }
    }

    public Map<String, Integer> getOptRecordList(Map<String, Object> obj) {
        // 获取登录的bean
        Map<String, Integer> ageMap = new HashMap<>();
        if (null != obj && Common.isNotEmpty((String) obj.get("birthday")) && Common.isNotEmpty((String) obj.get("empiid"))) {
            obj.put("sortType", "sj");
            String empiid = (String) obj.get("empiid");
//            List<Map<String, String>> phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) obj.get("empiid"));
//            if (CollectionUtils.isEmpty(phridMap)) {
//                log.warn("查询患者档案编号为空，empiid=" + obj.get("empiid"));
//                return ageMap;
//            }
            List<HashMap<String, Object>> result1 = new ArrayList<>();
            List<HashMap<String, Object>> result2 = new ArrayList<>();
            List<HashMap<String, Object>> result3 = new ArrayList<>();

            List<Map<String, String>> phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) obj.get("empiid"), CommonConsts.OP_EM_HP_MARK.MEN_ZHEN);
            if (!CollectionUtils.isEmpty(phridMap)) {
                for (Map<String, String> tmp : phridMap) {
                    obj.put("phrid", tmp.get("PHRID"));
                    obj.put("organ_code", tmp.get("ORGAN_CODE"));
                    result3 = indexRecordMapper.queryOptIndexRecord(obj);
                    if (!CollectionUtils.isEmpty(result3)) {
                        for (HashMap<String, Object> res : result3) {
                            if (!Common.checkZdmc((String) res.get("zdmc"), (String) res.get("zddm"))) {
                                result1.add(res);
                            }
                        }
                    }
                }
            }
            phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.ZHU_YUAN);
            if (!CollectionUtils.isEmpty(phridMap)) {
                for (Map<String, String> tmp : phridMap) {
                    obj.put("phrid", tmp.get("PHRID"));
                    obj.put("organ_code", tmp.get("ORGAN_CODE"));
                    result2 = indexRecordMapper.queryIptIndexRecord(obj);
                    if (!CollectionUtils.isEmpty(result2)) {
                        for (HashMap<String, Object> res : result2) {
                            if (!Common.checkZdmc((String) res.get("zdmc"), (String) res.get("zddm"))) {
                                result1.add(res);
                            }
                        }
                    }
                }
            }

//            for (Map<String, String> tmp : phridMap) {
//                obj.put("phrid", tmp.get("PHRID"));
//                obj.put("organ_code", tmp.get("ORGAN_CODE"));
//                List<HashMap<String, Object>> result3 = indexRecordMapper.queryOptIndexRecord(obj);
//                List<HashMap<String, Object>> result2 = indexRecordMapper.queryIptIndexRecord(obj);
//                if (!CollectionUtils.isEmpty(result2)) {
//                    result1.addAll(result2);
//                }
//                if (!CollectionUtils.isEmpty(result3)) {
//                    result1.addAll(result3);
//                }
//            }
            if (null != result1 && 0 < result1.size()) {
                result1 = Common.sortUnionList(result1, (String) obj.get("sortType"), null, null);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                List<Date> ageDate = new ArrayList<Date>();
                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(sdf.parse((String) obj.get("birthday")));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date now = new Date();
                for (int i : ageArr) {
                    calendar.add(Calendar.YEAR, i);
                    if (0 <= sdf.format(now).compareTo(sdf.format(calendar.getTime()))) {
                        ageDate.add(calendar.getTime());
                    } else {
                        break;
                    }
                }
                Date tempDate;
                Date tempDate2;
                for (int i = ageDate.size() - 1; i >= 0; i--) {
                    tempDate = ageDate.get(i);
                    Integer count = 0;
                    if (i == (ageDate.size() - 1)) {
                        for (HashMap<String, Object> temp : result1) {
                            if (0 >= sdf.format(tempDate).compareTo(sdf.format(temp.get("sj"))))
                                count += 1;
                            else
                                continue;
                        }
                    } else {
                        tempDate2 = ageDate.get(i + 1);
                        for (HashMap<String, Object> temp : result1) {
                            if (0 >= sdf.format(tempDate).compareTo(sdf.format(temp.get("sj"))) &&
                                    0 < sdf.format(tempDate2).compareTo(sdf.format(temp.get("sj"))))
                                count += 1;
                            else
                                continue;
                        }
                    }
                    ageMap.put(String.valueOf(i), count);
                }
            } else {
                ageMap = new HashMap<>();
            }
        } else {
            ageMap = new HashMap<>();
        }
        return ageMap;
    }

    public Map<String, Object> optMainPage(Map<String, Object> param) {
        Map<String, Object> result = new HashMap<>();
//        UserFormMap userFormMap = (UserFormMap) Common.findUserSession();
        String id = (String) param.get("id");
        if (Common.isNotEmpty(id)) {
            param.put("hid", id);
            param.put("organ_code", param.get("organcode"));
            List<HashMap<String, Object>> res = optRecordMapper.getReportById(param);
            if (null != res && 0 < res.size()) {
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
