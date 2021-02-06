package cn.com.bsoft.service.data;

import cn.com.bsoft.controller.data.CostRecordController;
import cn.com.bsoft.mapper.BDB.Cost_RecordMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CostRecordService {
    private static final Logger log = Logger.getLogger(CostRecordController.class);
    @Autowired
    private Cost_RecordMapper costRecordMapper;

    public List<HashMap<String, Object>> getSelectList(Map<String, Object> param) {
        List<HashMap<String, Object>> result = null;
        try {
            result = costRecordMapper.getSelectList(param);
        } catch (Exception ex) {
            log.info(ex.getLocalizedMessage());
        }
        if (null == result || 0 >= result.size()) {
            result = new ArrayList<HashMap<String, Object>>();
        }
        return result;
    }
}
