package cn.com.bsoft.service.data;

import cn.com.bsoft.controller.data.ExamRecordController;
import cn.com.bsoft.mapper.BDB.Examination_RecordMapper;
import cn.com.bsoft.mapper.BDB.IndexRecordMapper;
import cn.com.bsoft.mapper.BDB.PacsPatientMapper;
import cn.com.bsoft.mapper.BDB.PacsStudyMapper;
import cn.com.bsoft.util.Common;
import cn.com.bsoft.util.CommonConsts;
import cn.com.bsoft.util.TreeObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ExamRecordService {
    private static final Logger log = Logger.getLogger(ExamRecordController.class);
    private final String RES_URL = "/examRecord/examMain.do";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private IndexRecordMapper indexRecordMapper;
    @Autowired
    private Examination_RecordMapper examinationRecordMapper;
    @Autowired
    private PacsPatientMapper pacsPatientMapper;
    @Autowired
    private PacsStudyMapper pacsStudyMapper;
    @Autowired
    private PhridService phridService;

    public List<TreeObject> getIndexMenu(String empiid, String sorttype, String filtertype, String projName) {
        List<TreeObject> indexList = null;
        try {
            indexList = sortExamIndexMenu(empiid, sorttype, filtertype, projName);
            if (null == indexList || 0 >= indexList.size()) {
                indexList = new ArrayList<TreeObject>();
            }
        } catch (Exception ex) {
            log.info(ex.getLocalizedMessage());
            indexList = new ArrayList<TreeObject>();
        }
        return indexList;
    }

    public List<HashMap<String, Object>> getSelectList(Map<String, Object> param) {

        List<HashMap<String, Object>> result;
        try {
            result = examinationRecordMapper.getSelectList(param);
            if (null == result || 0 >= result.size()) {
                result = new ArrayList<HashMap<String, Object>>();
            }
        } catch (Exception ex) {
            log.info(ex.getLocalizedMessage());
            result = new ArrayList<HashMap<String, Object>>();
        }
        return result;
    }

    public List<Map<String, Object>> getPacsSelectList(Map<String, Object> param) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<HashMap<String, Object>> list = getSelectList(param);
        for (Map<String, Object> tmp : list) {
            if (Common.isNotEmpty((String) tmp.get("image_uidaddr"))) {
                result.add(tmp);
            }
        }
        return result;
    }

    public List<String> getPacsStudyUids(Map<String, Object> map) {
        List<String> studyUids = new ArrayList<>();
        if (Common.isEmpty((String) map.get("yxh"))) {
            return studyUids;
        }
        List<String> ids = pacsPatientMapper.getIds(map);
        if (CollectionUtils.isEmpty(ids)) {
            return studyUids;
        }
        map.put("patientIds", ids);
        studyUids = pacsStudyMapper.getStudyUids(map);
        return studyUids;
    }

    public String getImageUidaddr(String rptNo, String organcode) {
        Map<String, Object> param = new HashMap<>();
        param.put("rptNo", rptNo);
        param.put("organcode", organcode);
        List<HashMap<String, Object>> list = examinationRecordMapper.getSelectList(param);
        String temStr = (String) list.get(0).get("image_uidaddr");
        return temStr;
    }

    private List<TreeObject> sortExamIndexMenu(String empiid, String sorttype, String filtertype, String projName) {
        List<HashMap<String, Object>> res = new ArrayList<HashMap<String, Object>>();
        Map<String, Object> obj = new HashMap<String, Object>();
        List<TreeObject> ns = new ArrayList<TreeObject>();
        try {
            List<Map<String, String>> phridMap = phridService.getPhrIdsAndOrganCodeByEmpiidOfExam(empiid);
            if (CollectionUtils.isEmpty(phridMap)) {
                log.warn("查询患者档案编号为空，empiid=" + empiid);
                return ns;
            }
            List<HashMap<String, Object>> resTmp = null;
            for (Map<String, String> tmp : phridMap) {
                obj.put("phrid", tmp.get("PHRID"));
                obj.put("organ_code", tmp.get("ORGAN_CODE"));
                obj.put("mark", tmp.get("MARK"));//门急诊标记,2019-3-26 00:11:01
                obj.put("sortType", sorttype);
                obj.put("filterType", filtertype);
                obj.put("projName", projName);
                resTmp = indexRecordMapper.queryExaminationIndex(obj);
                if (!CollectionUtils.isEmpty(resTmp)) {
                    res.addAll(resTmp);
                }
                //如果是门诊，具体业务里门诊、急诊都查一次
                if (CommonConsts.OP_EM_HP_MARK.MEN_ZHEN.equals(tmp.get("MARK"))) {
                    obj.put("mark", CommonConsts.OP_EM_HP_MARK.JI_ZHEN);//门急诊标记,2019-3-26 00:11:01
                    resTmp = indexRecordMapper.queryExaminationIndex(obj);
                    if (!CollectionUtils.isEmpty(resTmp)) {
                        res.addAll(resTmp);
                    }
                }
            }
            if (0 < res.size()) {
                res = Common.sortUnionList(res, "sj", null, null);
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
                    for (int i = 0; i < res.size(); i++) {
                        temp = res.get(i);
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

                        if (i == (res.size() - 1)) {
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
                } else if ("proj_name".equalsIgnoreCase(sorttype)) {
                    String temp_name = "";
                    List<TreeObject> nullList = new ArrayList<TreeObject>();
                    List<TreeObject> tList = new ArrayList<TreeObject>();
                    Map<String, Object> temp = null;
                    TreeObject tTemp = null;
                    TreeObject mTemp = null;
                    for (int i = 0; i < res.size(); i++) {
                        temp = res.get(i);
                        tTemp = new TreeObject();
                        if (Common.isEmpty((String) temp.get("proj_name"))) {
                            setChildTreeObject(temp, tTemp, sdf);
                            nullList.add(tTemp);
                        } else {
                            if (Common.isEmpty(temp_name)) {
                                temp_name = (String) temp.get("proj_name");
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                            } else {
                                if (temp_name.equals((String) temp.get("proj_name"))) {
                                    setChildTreeObject(temp, tTemp, sdf);
                                    tList.add(tTemp);
                                } else {
                                    //先将之前的list存入children字段并放入map
                                    mTemp = new TreeObject();
                                    mTemp.setName(temp_name + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(temp_name, mTemp);
                                    //读取当前temp_name下是否有记录存在，存在则读取children
                                    temp_name = (String) temp.get("proj_name");
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

                        if (i == (res.size() - 1)) {
                            if (null != tList && 0 < tList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName(temp_name + "(" + tList.size() + "次)");
                                mTemp.setChildren(tList);
                                mMap.put(temp_name, mTemp);
                            }
                            if (null != nullList && 0 < nullList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName("其他" + "(" + nullList.size() + "次)");
                                mTemp.setChildren(nullList);
                                mMap.put("其他", mTemp);
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
                    for (int i = 0; i < res.size(); i++) {
                        temp = res.get(i);
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

                        if (i == (res.size() - 1)) {
                            if (null != tList && 0 < tList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName(temp_name + "(" + tList.size() + "次)");
                                mTemp.setChildren(tList);
                                mMap.put(temp_name, mTemp);
                            }
                            if (null != nullList && 0 < nullList.size()) {
                                mTemp = new TreeObject();
                                mTemp.setName("其他" + "(" + nullList.size() + "次)");
                                mTemp.setChildren(nullList);
                                mMap.put("其他", mTemp);
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
        String transTemp;
        if (null != temp.get("sj")) {
            sjTemp = sdf.format((Date) temp.get("sj"));
        } else {
            sjTemp = "";
        }
        if (Common.isNotEmpty((String) temp.get("proj_name")) && !"null".equalsIgnoreCase(((String) temp.get("proj_name")).trim())) {
            transTemp = (String) temp.get("proj_name");
        } else {
            transTemp = "无检查名称";
        }
        tTemp.setName(sjTemp + "  " + transTemp);
        if (null != temp.get("hid")) {
            if (temp.get("hid") instanceof String) {
                tTemp.setHid((String) temp.get("hid"));
            } else {
                tTemp.setHid("");
            }
        } else {
            tTemp.setHid("");
        }
        tTemp.setOrganCode((String) temp.get("organ_code"));
        tTemp.setResUrl(RES_URL);
    }
}
