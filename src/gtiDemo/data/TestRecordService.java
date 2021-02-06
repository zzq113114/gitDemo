package cn.com.bsoft.service.data;

import cn.com.bsoft.mapper.BDB.IndexRecordMapper;
import cn.com.bsoft.mapper.BDB.Test_RecordMapper;
import cn.com.bsoft.mapper.NavbarMapper;
import cn.com.bsoft.util.Common;
import cn.com.bsoft.util.TreeObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class TestRecordService {
    private static final Logger log = Logger.getLogger(TestRecordService.class);
    private final String RES_URL = "/testRecord/testMain.do";
    private final String YM_TYPE = "YM";
    private final String TEST_TYPE = "TEST";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private IndexRecordMapper indexRecordMapper;
    @Autowired
    private NavbarMapper navbarMapper;
    @Autowired
    private Test_RecordMapper testRecordMapper;
    @Autowired
    private PhridService phridService;

    public List<TreeObject> getIndexMenu(String empiid, String sorttype, String filtertype, String itemName) {
        List<TreeObject> indexList = null;
        try {
            indexList = sortTestIndexMenu(empiid, sorttype, filtertype, itemName);
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
        List<HashMap<String, Object>> result = null;
        try {
            result = getTestList(param, TEST_TYPE);
            if (null == result || 0 >= result.size()) {
                result = new ArrayList<HashMap<String, Object>>();
            }
        } catch (Exception ex) {
            log.info(ex.getLocalizedMessage());
            result = new ArrayList<HashMap<String, Object>>();
        }
        return result;
    }

    public List<HashMap<String, Object>> getDrugAllergySelectList(Map<String, Object> param) {
        List<HashMap<String, Object>> result = null;
        try {
            result = getTestList(param, YM_TYPE);
            if (null == result || 0 >= result.size()) {
                result = new ArrayList<>();
            }
        } catch (Exception ex) {
            log.info(ex.getLocalizedMessage());
            result = new ArrayList<>();
        }
        return result;
    }

    private List<TreeObject> sortTestIndexMenu(String empiid, String sorttype, String filtertype, String itemName) {
        List<HashMap<String, Object>> res = new ArrayList<HashMap<String, Object>>();
        Map<String, Object> obj = new HashMap<String, Object>();
        List<TreeObject> ns = new ArrayList<TreeObject>();
        try {
            List<Map<String, String>> phridMap = phridService.getPhrIdsAndOrganCodeByEmpiidOfExam(empiid);
            if (CollectionUtils.isEmpty(phridMap)) {
                log.warn("查询患者档案编号为空，empiid=" + empiid);
                return ns;
            }
            obj.put("sortType", sorttype);
            obj.put("filterType", filtertype);
            obj.put("itemName", itemName);
            List<HashMap<String, Object>> resTemp=null;
            for(Map<String,String> tmp:phridMap) {
                obj.put("phrid", tmp.get("PHRID"));
                obj.put("organ_code", tmp.get("ORGAN_CODE"));
                obj.put("mark", tmp.get("MARK"));
                resTemp = indexRecordMapper.queryTestIndex(obj);
                if(!CollectionUtils.isEmpty(resTemp)){
                    res.addAll(resTemp);
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
                } else if ("item_name".equalsIgnoreCase(sorttype)) {
                    String temp_name = "";
                    List<TreeObject> nullList = new ArrayList<TreeObject>();
                    List<TreeObject> tList = new ArrayList<TreeObject>();
                    Map<String, Object> temp = null;
                    TreeObject tTemp = null;
                    TreeObject mTemp = null;
                    for (int i = 0; i < res.size(); i++) {
                        temp = res.get(i);
                        tTemp = new TreeObject();
                        if (Common.isEmpty((String) temp.get("item_name"))) {
                            setChildTreeObject(temp, tTemp, sdf);
                            nullList.add(tTemp);
                        } else {
                            if (Common.isEmpty(temp_name)) {
                                temp_name = (String) temp.get("item_name");
                                setChildTreeObject(temp, tTemp, sdf);
                                tList.add(tTemp);
                            } else {
                                if (temp_name.equals((String) temp.get("item_name"))) {
                                    setChildTreeObject(temp, tTemp, sdf);
                                    tList.add(tTemp);
                                } else {
                                    //先将之前的list存入children字段并放入map
                                    mTemp = new TreeObject();
                                    mTemp.setName(temp_name + "(" + tList.size() + "次)");
                                    mTemp.setChildren(tList);
                                    mMap.put(temp_name, mTemp);
                                    //读取当前temp_name下是否有记录存在，存在则读取children
                                    temp_name = (String) temp.get("item_name");
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
        if (Common.isNotEmpty((String) temp.get("item_name")) && !"null".equalsIgnoreCase(((String) temp.get("item_name")).trim())) {
            transTemp = (String) temp.get("item_name");
        } else {
            transTemp = "无检验名称";
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

    private List<HashMap<String, Object>> getTestList(Map<String, Object> param, String type) {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        List<HashMap<String, Object>> jobs = testRecordMapper.getSelectList(param);
        List<HashMap<String, Object>> resList = new ArrayList<>();
        List<Future<HashMap<String, Object>>> futureList = new ArrayList<>();
        if (null != jobs && 0 < jobs.size()) {
            for (HashMap<String, Object> job : jobs) {
                futureList.add(threadPool.submit(new Callable<HashMap<String, Object>>() {
                    public HashMap<String, Object> call() throws Exception {
                        int temp2 = testRecordMapper.countTestDrugResult(job);
                        if (YM_TYPE.equals(type)) {
                            if (0 < temp2) {
                                return job;
                            } else {
                                return null;
                            }
                        } else {
                            if (0 < temp2) {
                                return null;
                            } else {
                                return job;
                            }
                        }
                    }
                }));
            }
        }

        Common.convertFutureList(futureList, resList);

        threadPool.shutdown();
        return resList;
    }
}
