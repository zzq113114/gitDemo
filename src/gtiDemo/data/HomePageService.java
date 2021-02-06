package cn.com.bsoft.service.data;

import cn.com.bsoft.bean.Ehr_HealthRecordBean;
import cn.com.bsoft.entity.FlagFormMap;
import cn.com.bsoft.entity.UserFormMap;
import cn.com.bsoft.mapper.BDB.*;
import cn.com.bsoft.mapper.FlagMapper;
import cn.com.bsoft.util.Common;
import cn.com.bsoft.util.CommonConsts;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static cn.com.bsoft.util.Common.*;

/**
 * 首页service
 */
@Service
public class HomePageService {

    private static final Logger log = Logger.getLogger(HomePageService.class);

    @Inject
    private Ehr_HealthRecordMapper ehrHealthRecordMapper;
    @Inject
    private Mpi_CardMapper mpiCardMapper;
    @Inject
    private IndexRecordMapper indexRecordMapper;
    @Inject
    private In_HospitalRecordMapper inHospitalRecordMapper;
    @Inject
    private Opt_RecordMapper optRecordMapper;
    @Inject
    private FlagMapper flagMapper;
    @Autowired
    private PhridService phridService;


    /**
     * 列表（分页）
     *
     * @return
     */
    public Map<String, Object> getPersonList(Map<String, Object> obj) {
        Map<String, Object> result = new HashMap<>();
        try {
            UserFormMap userFormMap = (UserFormMap) Common.findUserSession();
            String viewPrivacy = (String) userFormMap.get("viewPrivacy");

            getColFlagStr(userFormMap, obj);

            if (Common.isNotEmpty((String) obj.get("birthday_from"))) {
                obj.put("birthday_from", Common.dateToStamp(obj.get("birthday_from") + " 00:00:00"));
            }
            if (Common.isNotEmpty((String) obj.get("birthday_to"))) {
                obj.put("birthday_to", Common.dateToStamp(Common.appointDateToString(obj.get("birthday_to"), 0, 0, 1)
                        + " 00:00:00"));
            }
            String typeCode = (String) obj.get("typeCode");
            String cardno = (String) obj.get("cardno");
            if (Common.isNotEmpty(cardno) && Common.isNotEmpty(typeCode)) {
                String empiid  = mpiCardMapper.findEmpiidByCardnoAndTypeCode(cardno, typeCode);
                if (Common.isEmpty(empiid)) {
                    // 赋值empiid一个数据库中绝对不存在的值，否则当查询不存在的卡号时，会显示所有记录。
                    empiid = "null";
                }
                obj.put("empiid", empiid);
            }
            List<HashMap<String, Object>> results = ehrHealthRecordMapper.queryPage(obj);
            results = getOptAndIptCount(results, viewPrivacy);
            result.put("draw", obj.get("draw"));
            result.put("recordsFiltered", ehrHealthRecordMapper.countOpertation(obj));
            result.put("data", results);
        } catch (Exception e) {
            log.info(e.getLocalizedMessage());
            result.put("draw", obj.get("draw"));
            result.put("recordsFiltered", 0);
            result.put("data", new ArrayList<Ehr_HealthRecordBean>());
        }
        System.out.println("+++++++++++++++++++++++++++");
        System.out.println(result);
        return result;
    }

    /**
     * 获取居民基本信息
     *
     * @param param
     * @return
     */
    public Map<String, Object> getPersonInfoById(HashMap<String, Object> param) {
        Map<String, Object> temp = ehrHealthRecordMapper.getPersonInfoById(param);

//        String viewPrivacy = (String) ((UserFormMap) Common.findUserSession()).get("viewPrivacy");
//        temp.put("idStr", formatIdcard((String) temp.get("idcard"), viewPrivacy));
//        temp.put("address", formatAddress((String) temp.get("address"), viewPrivacy));
//        temp.put("name", formatName((String) temp.get("name"), viewPrivacy));
//        temp.put("mobile", formatMobile((String) temp.get("mobile"), viewPrivacy));
        return temp;
    }

    /**
     * 根据idcard获取empiid
     *
     * @param idcard
     * @return
     */
    public Map<String, Object> getEmpiids(String idcard) {
        Map<String, Object> result = new HashMap<>();
        if (Common.isEmpty(idcard)) {
            return result;
        }
        try {
            Map<String, Object> obj = new HashMap<>();
            obj.put("idcard", idcard);
            List<String> results = ehrHealthRecordMapper.queryEmpiids(obj);
            result.put("data", results);
        } catch (Exception e) {
            log.info(e.getStackTrace());
            result.put("data", new ArrayList<>());
        }
        return result;
    }

    public List<HashMap<String, Object>> getOptAndIptCount(List<HashMap<String, Object>> jobs, String viewPrivacy) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        List<HashMap<String, Object>> resList = new ArrayList<>();
        List<Future<HashMap<String, Object>>> futureList = new ArrayList<>();
        if (null != jobs && 0 < jobs.size()) {
            for (HashMap<String, Object> job : jobs) {
                futureList.add(threadPool.submit(new Callable<HashMap<String, Object>>() {
                    public HashMap<String, Object> call() throws Exception {
                        Integer total1 = 0,total2 = 0;
                        List<HashMap<String, Object>> result2;
                        List<HashMap<String, Object>> result3;
                        List<Map<String, String>> phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) job.get("empiid"), CommonConsts.OP_EM_HP_MARK.MEN_ZHEN);
                        int sum = 0;
                        if (!CollectionUtils.isEmpty(phridMap)) {
                            for (Map<String, String> tmp : phridMap) {
                                job.put("phrid", tmp.get("PHRID"));
                                job.put("sortType", "sj");
                                job.put("organ_code", tmp.get("ORGAN_CODE"));
                                result2 = indexRecordMapper.queryOptIndexRecord(job);
                                if (null != result2 && 0 < result2.size()) {
                                    for (HashMap<String, Object> res : result2) {
                                        if (!Common.checkZdmc((String) res.get("zdmc"), (String) res.get("zddm"))) {
                                            total1++;
                                        }
                                    }
                                }else{
                                    total1 = 0;
                                }
                                /*total = ehr_VisitRecordMapper.countOptListById(job);
                                if (null == total) {
                                    total = 0;
                                }*/
                                sum += total1;
                                total1 = 0;
                            }
                        }
                        phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid((String) job.get("empiid"), CommonConsts.OP_EM_HP_MARK.ZHU_YUAN);
                        if (!CollectionUtils.isEmpty(phridMap)) {
                            for (Map<String, String> tmp : phridMap) {
                                job.put("phrid", tmp.get("PHRID"));
                                job.put("sortType", "sj");
                                job.put("organ_code", tmp.get("ORGAN_CODE"));
                                result3 = indexRecordMapper.queryIptIndexRecord(job);
                                if (null != result3 && 0 < result3.size()) {
                                    for (HashMap<String, Object> res : result3) {
                                        if (!Common.checkZdmc((String) res.get("zdmc"), (String) res.get("zddm"))) {
                                            total2++;
                                        }
                                    }
                                }else{
                                    total2 = 0;
                                }
                                /*total = ehr_VisitRecordMapper.countIptListById(job);
                                if (null == total) {
                                    total = 0;
                                }*/
                                sum += total2;
                                total2 = 0;
                            }
                        }
//                        job.put("idcard", formatIdcard((String) job.get("idcard"), viewPrivacy));
//                        job.put("homeaddress", formatAddress((String) job.get("homeaddress"), viewPrivacy));
//                        job.put("personname", formatName((String) job.get("personname"), viewPrivacy));
//                        job.put("mobile", formatMobile((String) job.get("mobile"), viewPrivacy));

                        job.put("aboblood", formatAboBlood(job.get("aboblood")));
                        job.put("rhblood", formatRhBlood(job.get("rhblood")));
                        job.put("sex", formatSex(job.get("sex")));
                        job.put("regpermanent", formatRegpermanent(job.get("regpermanent")));
                        job.put("recordCount", sum + "条");

                        return job;
                    }
                }));
            }
        }
        Common.convertFutureList(futureList, resList);
        threadPool.shutdown();
        return resList;
    }


    private void getColFlagStr(UserFormMap userFormMap, Map<String, Object> obj) {
        FlagFormMap flagFormMap = new FlagFormMap();
        flagFormMap.set("userId", userFormMap.get("id"));
        List<FlagFormMap> flagList = flagMapper.findRes(flagFormMap);
        if (null != flagList && 0 < flagList.size()) {
            String flagStr = "";
            for (FlagFormMap temp : flagList) {
                flagStr += " AND " + temp.get("flagKey") + "='" + temp.get("flagValue") + "'";
            }
            obj.put("col_flag", flagStr);
        }
    }


    public Map<String, Object> getRecordList(Map<String, Object> obj) {
        Map<String, Object> result = new HashMap<>(3);
        try {
            UserFormMap userFormMap = (UserFormMap) Common.findUserSession();
            getColFlagStr(userFormMap, obj);
            int limit = Integer.parseInt((String) obj.get("length"));
            int offset = Integer.parseInt((String) obj.get("start"));
            obj.put("limit", limit);
            obj.put("offset", offset);
            if (Common.isNotEmpty((String) obj.get("jzsj_from"))) {
                obj.put("jzsj_from", Common.dateToStamp(obj.get("jzsj_from") + " 00:00:00"));
            }
            if (Common.isNotEmpty((String) obj.get("jzsj_to"))) {
                obj.put("jzsj_to", Common.dateToStamp(
                        Common.appointDateToString(obj.get("jzsj_to"), 0, 0, 1) +
                                " 00:00:00"));
            }
            List<HashMap<String, Object>> results;
            int total;
            if ("99".equals(obj.get("type"))) {
                results = inHospitalRecordMapper.findRecordList(obj);
                total = inHospitalRecordMapper.countRecordById(obj);
            } else {
                results = optRecordMapper.findRecordList(obj);
                total = optRecordMapper.countRecordById(obj);
            }
            //隐藏身份证号后几位
//            for (HashMap<String, Object> res : results) {
//                String idCard = formatIdcard((String) res.get("idcard"), (String) userFormMap.get("viewPrivacy"));
//                res.put("idcard", idCard);
//            }
            //部分icd10代码隐私处理
            for (HashMap<String, Object> res : results) {
                String zdmc = Common.formatZdmc((String) res.get("zdmc"), (String) res.get("zddm"));
                res.put("zdmc", zdmc);
            }
            result.put("draw", obj.get("draw"));
            result.put("recordsFiltered", total);
            result.put("data", results);
        } catch (Exception e) {
            log.info(e.getLocalizedMessage());
            result.put("draw", obj.get("draw"));
            result.put("recordsFiltered", 0);
            result.put("data", new ArrayList<Ehr_HealthRecordBean>());
        }
        return result;
    }

    public List<HashMap<String, Object>> getOrganList(Map<String, Object> obj) {
        List<HashMap<String, Object>> result;
        try {
            String recordType = (String) obj.get("recordType");
            if (Common.isNotEmpty(recordType) && "99".equals(recordType)) {
                result = indexRecordMapper.queryIptOrganList();
            } else {
                result = indexRecordMapper.queryOptOrganList();
            }
        } catch (Exception ex) {
            log.info(ex.getLocalizedMessage());
            result = new ArrayList<>();
        }
        return result;
    }

}
