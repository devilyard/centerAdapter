package com.bsoft.dao.redis;

import java.util.List;
import java.util.Map;

import ctd.annotation.RpcService;

/**
 * 
 * @author wjg
 *
 */
public interface RedisDAO {
	
	// 把数据保存到Redis中
	@RpcService
	public Map<String,String> save(Map<String,Object> map);

	// 批量保存数据到Redis中
	@RpcService
	public Map<String,String> save(final List<Map<String,Object>> list);
	
	// 把数据保存到队列中
	@RpcService
	public String saveValueToQueue(String value) throws Exception;
}
