package com.bsoft.dao.hadoop.phoienix;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author wjg
 * 
 */
public interface PhoienixDAO {

	// 获取序列值
	public long getSEQ(String seqName);

	// 获取序列值
	public Map<String, Object> getRowData(String EventId);

	// 获取指定表中的记录总数
	public long getCount(String tableName, String key, String value, String st,
			String et);

	// 获取指定表中的记录明细
	public Map<String, Object> getList(String tableName, int currentPage, int pageSize, long total, String key,
			String value, String st, String et);
	
	// 获取数据明细:目前暂时只供VIEW使用，但可以通过param进行扩展
	public List<Map<String, Object>> getDetailData(String dataSetCode,
			Map<String, String> param);
	
	public String getBodyEventId(String EventId);
}
