package cn.com.bsoft.service.data;

import cn.com.bsoft.mapper.BDB.IndexRecordMapper;
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
public class TransFusionRecordService {
    private static final Logger log = Logger.getLogger(TransFusionRecordService.class);
    private final String RES_URL = "/transFusionRecord/transFusionMain.do";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private IndexRecordMapper indexRecordMapper;
    @Autowired
    private PhridService phridService;


    public List<TreeObject> getIndexMenu(String empiid, String sorttype, String filtertype, String caseName) {
        List<TreeObject> indexList = null;
        try {
            indexList = sortTransFusionIndexMenu(empiid, sorttype, filtertype, caseName);
            if (null == indexList || 0 >= indexList.size()) {
                indexList = new ArrayList<TreeObject>();
            }
        } catch (Exception ex) {
            log.info(ex.getLocalizedMessage());
            indexList = new ArrayList<TreeObject>();
        }
        return indexList;
    }

    private List<TreeObject> sortTransFusionIndexMenu(String empiid, String sorttype, String filtertype, String caseName) {
        List<HashMap<String, Object>> res = new ArrayList<HashMap<String, Object>>();
        Map<String, Object> obj = new HashMap<String, Object>();
        List<TreeObject> ns = new ArrayList<TreeObject>();
        try {
            List<Map<String, String>> phridMap = phridService.getPhrIdsAndOrganCodeByEmpiid(empiid, CommonConsts.OP_EM_HP_MARK.ZHU_YUAN);
            if (CollectionUtils.isEmpty(phridMap)) {
                log.warn("查询患者档案编号为空，empiid=" + empiid);
                return ns;
            }
            obj.put("sortType", sorttype);
            obj.put("filterType", filtertype);
            obj.put("caseName", caseName);
            List<HashMap<String, Object>> resTemp = null;
            for (Map<String, String> tmp : phridMap) {
                obj.put("phrid", tmp.get("PHRID"));
                obj.put("organ_code", tmp.get("ORGAN_CODE"));
                resTemp = indexRecordMapper.queryTransFusionIndex(obj);
                if (!CollectionUtils.isEmpty(resTemp)) {
                    res.addAll(resTemp);
                }
            }
            if (0 < res.size()) {
                res = Common.sortUnionList(res, "sj", null, null);
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
                } else if ("blood_case".equalsIgnoreCase(sorttype)) {
                    String temp_case = "";
                    List<TreeObject> nullList = new ArrayList<TreeObject>();
                    List<TreeObject> tList = new ArrayList<TreeObject>();
                    Map<String, Object> temp = null;
                    TreeObject tTemp = null;
                    TreeObject mTemp = null;
                    for (int i = 0; i < res.size(); i++) {
                        temp = res.get(i);
                        tTemp = new TreeObject();
                        if (Common.isEmpty((String) temp.get("blood_case"))) {
                            setChildTreeObject(temp, tTemp, sdf);
                            nullList.add(tTemp);
                        } else {
                            if (Common.isEmpty(temp_case)) {
                                temp_case = (String) temp.get("blood_case");
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                            } else {
                                if (temp_case.equals((String) temp.get("blood_case"))) {
                                    setChildTreeObject(temp, tTemp, sdf);
                                    tList.add(tTemp);
                                } else {
                                    //先将之前的list存入children字段并放入map
                                    mTemp = new TreeObject();
                                    mTemp.setName(temp_case + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(temp_case, mTemp);
                                    //读取当前temp_name下是否有记录存在，存在则读取children
                                    temp_case = (String) temp.get("blood_case");
                                    if (null != mMap.get(temp_case)) {
                                        mTemp = mMap.get(temp_case);
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
                                mTemp.setName(temp_case + "(" + tList.size() + "次)");
                                mTemp.setChildren(tList);
                                mMap.put(temp_case, mTemp);
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
        if (Common.isNotEmpty((String) temp.get("blood_case")) && !"null".equalsIgnoreCase(((String) temp.get("blood_case")).trim())) {
            transTemp = (String) temp.get("blood_case");
        } else {
            transTemp = "无输血原因";
        }
        tTemp.setName(sjTemp + "  " + temp.get("cname_blood_type") + "  " + temp.get("cname_rh") + "  " + transTemp);
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
