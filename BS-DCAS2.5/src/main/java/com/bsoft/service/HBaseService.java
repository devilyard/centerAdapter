package com.bsoft.service;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author wjg
 * 
 */
public interface HBaseService {
	
	// 直接保存错误信息到HBASE中
	public void saveErrorData(List<Map<String, Object>> list);

	// 获取数据明细:目前暂时只供VIEW使用，但可以通过param进行扩展
	public List<Map<String, Object>> getDetailData(String dataSetCode,
			Map<String, String> param);
}
