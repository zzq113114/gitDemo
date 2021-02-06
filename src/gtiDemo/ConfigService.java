package cn.com.bsoft.service;

import cn.com.bsoft.mapper.ConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@CacheConfig(cacheNames = "cache", keyGenerator = "cacheKeyGenerator")
public class ConfigService {
    @Autowired
    private ConfigMapper configMapper;

    /**
     * 短信授权标志
     */
    public static final String AUTH_FLAG = "AUTH_FLAG";
    /**
     * 患者授权有效期，单位小时，默认当天
     */
    public static final String TIME_AUTH_ACTIVE = "TIME_AUTH_ACTIVE";
    /**
     * 验证码超时时间，单位分钟，默认30
     */
    public static final String TIME_VCODE_ACTIVE = "TIME_VCODE_ACTIVE";

    /**
     * 获取config
     *
     * @return
     */
    @Cacheable(key = "'SYS_CONFIG_MAP'")
    public Map<String, String> getAllConfigs() {
        Map<String, String> result = new HashMap<>();
        List<Map<String, String>> datas = configMapper.getAllDatas();
        for (Map<String, String> tmp : datas) {
            result.put(tmp.get("KEY"), tmp.get("VALUE"));
        }
        if (CollectionUtils.isEmpty(result) || result.get(AUTH_FLAG) == null) {
            //默认不验证
            result.put(AUTH_FLAG, "0");
        }
        return result;
    }

    /**
     * 清除缓存
     */
    @CacheEvict(key = "'SYS_CONFIG_MAP'")
    public void cleanCache() {
    }

    /**
     * 根据key获取配置
     *
     * @param key
     * @return
     */
    public String getConfig(String key) {
        return getAllConfigs().get(key);
    }
}
