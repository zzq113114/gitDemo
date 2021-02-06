package cn.com.bsoft.service;

import cn.com.bsoft.entity.HostFormMap;
import cn.com.bsoft.entity.NavbarFormMap;
import cn.com.bsoft.entity.UserFormMap;
import cn.com.bsoft.mapper.BDB.Ehr_HealthRecordMapper;
import cn.com.bsoft.mapper.BDB.Mpi_CardMapper;
import cn.com.bsoft.mapper.HostMapper;
import cn.com.bsoft.mapper.NavbarMapper;
import cn.com.bsoft.util.Common;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommonService {
    @Autowired
    private NavbarMapper navbarMapper;
    @Autowired
    private Ehr_HealthRecordMapper ehrHealthRecordMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private Mpi_CardMapper mpiCardMapper;

    private static String pdfUrl_def = "pdf_def";
    private static String pdfUrl_full = "pdf_ful";
    private static String pdfUrl_def_url = "pdf_def_url";
    private static String pdfUrl_full_url = "pdf_ful_url";

    /**
     * 获取用户菜单
     *
     * @return
     */
    public List<NavbarFormMap> getNavbars() {
        UserFormMap userFormMap = (UserFormMap) Common.findUserSession();
        NavbarFormMap navbarFormMap = new NavbarFormMap();
        navbarFormMap.put("userId", userFormMap.get("id"));
        navbarFormMap.put("pageLevel", "2");
        List<NavbarFormMap> mps = navbarMapper.findRes(navbarFormMap);
        HostFormMap hostFormMap = getBDAServer();
        String hostUrl = hostFormMap.getStr("resUrl");
        String prefix = hostFormMap.getStr("prefix");for(NavbarFormMap nav:mps){
            nav.put("hostUrl", hostUrl);
            nav.put("prefix", prefix);
        }

        return mps;
    }

    /**
     * 获取检索页面菜单
     *
     * @return
     */
    public List<NavbarFormMap> getIndexNavbars() {
        UserFormMap userFormMap = (UserFormMap) Common.findUserSession();
        NavbarFormMap navbarFormMap = new NavbarFormMap();
        navbarFormMap.put("userId", userFormMap.get("id"));
        navbarFormMap.put("pageLevel", "1");
        List<NavbarFormMap> mps = navbarMapper.findRes(navbarFormMap);
        return mps;
    }

    /**
     * 获取居民信息
     *
     * @param empiid
     * @return
     */
    public Map<String, Object> getJMByEmpiid(String empiid) {
        if (Common.isEmpty(empiid)) {
            return new HashMap<>();
        }
//        String viewPrivacy = (String) ((UserFormMap) Common.findUserSession()).get("viewPrivacy");
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("empiid", empiid);
        Map<String, Object> person = ehrHealthRecordMapper.findOneById(param);
        if (person != null && 0 < person.size()) {
//            person.put("idcard", formatIdcard((String) person.get("idcard"), viewPrivacy));
//            person.put("address", formatAddress((String) person.get("address"), viewPrivacy));
//            person.put("homeaddress", formatAddress((String) person.get("homeaddress"), viewPrivacy));
//            person.put("personname", formatName((String) person.get("personname"), viewPrivacy));
//            person.put("mobile", formatMobile((String) person.get("mobile"), viewPrivacy));
            person.put("age", Common.getAgeByBirthDay(person.get("birthday")));
            person.put("sex", Common.formatSex(person.get("sexcode")));
            return person;
        } else {
            return new HashMap<>();
        }
    }

    /**
     * 根据idcard获取empiid
     *
     * @param idcard
     * @return
     */
    public String getEmpiidByIdcard(String idcard) {
        String result = "";
        if (Common.isEmpty(idcard)) {
            return result;
        }
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("idcard", idcard);
        Map<String, Object> person = ehrHealthRecordMapper.findOneById(param);
        if (person != null && 0 < person.size()) {
            return (String) person.get("empiid");
        }
        return result;
    }

    /**
     * 获取患者empiid
     * @param phrid
     * @param organCode
     * @return
     */
    public String getEmpiidByPhridAndOrganCode(String phrid,String organCode) {
        String result = "";
        if (Common.isEmpty(phrid)||Common.isEmpty(organCode)) {
            return result;
        }
        String empiid= ehrHealthRecordMapper.findEmpiidByPhridAndOrganCode(phrid, organCode);
        if (!Common.isEmpty(empiid)) {
            return empiid;
        }
        return result;
    }

    /**
     * 获取empiid（jmjbxx）
     * @param phrid
     * @return
     */
    public String getEmpiidByJMJBXXPhrid(String phrid) {
        String result = "";
        if (Common.isEmpty(phrid)) {
            return result;
        }
        String empiid= ehrHealthRecordMapper.findEmpiidByJMJBXXPhrid(phrid);
        if (!Common.isEmpty(empiid)) {
            return empiid;
        }
        return result;
    }

    /**
     * 根据卡类型和卡号获取empiid
     * @param cardno
     * @param typeCode
     * @return
     */
    public String getEmpiidByCardnoAndTypeCode(String cardno, String typeCode) {
        String result = "";
        if (Common.isEmpty(cardno) || Common.isEmpty(typeCode)) {
            return result;
        }
        String empiid = mpiCardMapper.findEmpiidByCardnoAndTypeCode(cardno, typeCode);
        if (Common.isNotEmpty(empiid)) {
            return empiid;
        }
        return result;
    }

    /**
     * 获取bda报表文件路径
     *
     * @return
     */
    public String getPdfBaseUrl() {
        //默认敏感字段加密
        String flag = pdfUrl_def;
        String viewPrivacy = null;
        try {
            viewPrivacy = (String) ((UserFormMap) Common.findUserSession()).get("viewPrivacy");
        } catch (Exception e) {
        }
        if (Common.isNotEmpty(viewPrivacy) && "1".equals(viewPrivacy)) {
            //完全明文路径
            flag = pdfUrl_full;
        }
        List<HostFormMap> maps = hostMapper.findByAttribute("name", flag, HostFormMap.class);
        return maps.get(0).getStr("resUrl");
    }

    public HostFormMap getBDAServer() {
        //默认敏感字段加密
        String flag = pdfUrl_def_url;
        String viewPrivacy = null;
        try {
            viewPrivacy = (String) ((UserFormMap) Common.findUserSession()).get("viewPrivacy");
        } catch (Exception e) {
        }
        if (Common.isNotEmpty(viewPrivacy) && "1".equals(viewPrivacy)) {
            //完全明文路径
            flag = pdfUrl_full_url;
        }
        List<HostFormMap> maps = hostMapper.findByAttribute("name", flag, HostFormMap.class);
        return maps.get(0);
    }

    /**
     * 设置参数
     * @param request
     * @param model
     */
    public void setParameters(HttpServletRequest request, Model model){
        List<NavbarFormMap> mps = getNavbars();
        String hostUrl = request.getParameter("hostUrl");
        String prefix = request.getParameter("prefix");
        if (Common.isEmpty(hostUrl) || Common.isEmpty(prefix)) {
            HostFormMap hostFormMap = getBDAServer();
            hostUrl = hostFormMap.getStr("resUrl");
            prefix = hostFormMap.getStr("prefix");

        }
        model.addAttribute("hostUrl", hostUrl);
        model.addAttribute("prefix", prefix);
        model.addAttribute("navlist", mps);
        for(NavbarFormMap nav:mps){
            nav.put("hostUrl", hostUrl);
            nav.put("prefix", prefix);
        }
        String empiid = (String) request.getAttribute("empiid");
        if (Common.isEmpty(empiid)) {
            empiid = request.getParameter("empiid");
        }
        model.addAttribute("empiid", empiid);
        model.addAttribute("name", request.getParameter("name"));
    }
}