package com.bsoft.dao.oracle;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author wjg
 *
 */
public interface OracleDAO {

	// 保存数据
	public void save(List<Map<String, Object>> list);
	
	//查询eventid是否已存在
	public boolean existEventid(String eventid);

	//更新数据
	public void update(List<Map<String, Object>> list);
}
