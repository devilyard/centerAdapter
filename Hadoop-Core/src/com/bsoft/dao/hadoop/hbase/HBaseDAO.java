package com.bsoft.dao.hadoop.hbase;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Put;

/**
 * 
 * @author wjg
 *
 */

public interface HBaseDAO {

	// 把数据保存到HBASE中
	public void save(String table,final List<Put> list);

	// 分页获取HBASE内容
	public Map<String, Object> getHBaseData(String tableName,String nowTime, String startRow,
			Integer currentPage, Integer pageSize) throws IOException;

	// 获取HBASE中的总数量
	public long getHBaseDataCount(String table,String familyName,String startRow, String endRow);

	// 根据行键获取具体内容
	public Map<String, Object> getDataByRowKey(String tableName,
			String rowkeyName);

	// 根据表名，对应列族结构创建HBASE库表信息
	public void createHtable(String htablName, List<String> columenFamily);
	
	public void testHashAndCreateTable() throws Exception;

}
