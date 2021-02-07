package cn.com.bsoft.service.data;

import cn.com.bsoft.mapper.BDB.Ehr_HealthRecordMapper;
import cn.com.bsoft.util.Common;
import cn.com.bsoft.util.CommonConsts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * phiid service
 */
@Service
@CacheConfig(cacheNames = "queryCache", keyGenerator = "cacheKeyGenerator")
public class PhridService {
    @Autowired
    private Ehr_HealthRecordMapper ehrHealthRecordMapper;

    /**
     * 获取患者档案编号
     *
     * @param empiid
     * @return
     */
//    @Cacheable
//    public List<String> getPhrIdsByEmpiid(String empiid) {
//        if (Common.isEmpty(empiid)) {
//            return new ArrayList<>();
//        }
//        List<String> phrids = ehrHealthRecordMapper.findPhridByMpiid(empiid);
//        if (CollectionUtils.isEmpty(phrids)) {
//            phrids = new ArrayList<>();
//        }
//        return phrids;
//    }

    /**
     * 获取患者档案编号和机构
     *
     * @param empiid
     * @param mark
     * @return
     */
    @Cacheable
    public List<Map<String, String>> getPhrIdsAndOrganCodeByEmpiid(String empiid, String mark) {
        if (Common.isEmpty(empiid)) {
            return new ArrayList<>();
        }
        List<Map<String, String>> phridMap = ehrHealthRecordMapper.findPhridAndOrganCodeByMpiid(empiid, mark);
        if (CollectionUtils.isEmpty(phridMap)) {
            phridMap = new ArrayList<>();
        }
        return phridMap;
    }

    /**
     * 查询检验检查患者标记患者基本信息
     * @param empiid
     * @return
     */
    @Cacheable
    public List<Map<String, String>> getPhrIdsAndOrganCodeByEmpiidOfExam(String empiid) {
        if (Common.isEmpty(empiid)) {
            return new ArrayList<>();
        }
        List<Map<String, String>> phridMap = new ArrayList<>();
        List<Map<String, String>> phridtTemp = null;
        for (String mark : CommonConsts.EM_MARKS) {
            phridtTemp = ehrHealthRecordMapper.findPhridAndOrganCodeByMpiid(empiid, mark);
            if (!CollectionUtils.isEmpty(phridtTemp)) {
                phridMap.addAll(phridtTemp);
            }
        }
        if (CollectionUtils.isEmpty(phridMap)) {
            phridMap = new ArrayList<>();
        }

        return phridMap;
    }

    /**
     * 获取城乡居民健康档案编号
     *
     * @param empiid
     * @return
     */
//    @Cacheable
//    public List<String> getJMPhrIdsByEmpiid(String empiid) {
//        if (Common.isEmpty(empiid)) {
//            return new ArrayList<>();
//        }
//        List<String> phrids = ehrHealthRecordMapper.findJmPhridByMpiid(empiid);
//        if (CollectionUtils.isEmpty(phrids)) {
//            phrids = new ArrayList<>();
//        }
//        return phrids;
//    }

    /**
     * 获取居民健康档案编号phrid和建档机构
     *
     * @param empiid 患者主索引号
     * @return
     */
    @Cacheable
    public List<Map<String, String>> getJMPhridAndOraganByEmpiid(String empiid) {
        if (Common.isEmpty(empiid)) {
            return new ArrayList<>();
        }
        List<Map<String, String>> maps = ehrHealthRecordMapper.findPhridAndOrganByMpiid(empiid);
        if (CollectionUtils.isEmpty(maps)) {
            maps = new ArrayList<>();
        }
        return maps;
    }

}
