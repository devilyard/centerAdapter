package com.bsoft.dao.redis.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import com.bsoft.dao.redis.RedisDAO;
import com.bsoft.util.ConstanUtil;

import ctd.annotation.RpcService;
/**
 * 
 * @author wjg
 *
 */
public class RedisDAOImpl implements RedisDAO {

	private RedisTemplate redisTemplate;

	/**
	 * 设置redisTemplate
	 * 
	 * @param redisTemplate
	 *            the redisTemplate to set
	 */
	public void setRedisTemplate(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 给前置机调用:传入map
	 * 
	 */
	@RpcService
	public Map<String, String> save(Map<String, Object> map) {
		Map<String, String> rm = new HashMap<String, String>();
		final String dataSetCode = map.get("dataSetCode").toString();
		final String eventID = map.get("eventID").toString();
		final String origianlData = map.get("origianlData").toString();
		final String dealData = map.get("dealData").toString();
		try {
			redisTemplate.execute(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection)
						throws DataAccessException {
					byte[] key = redisTemplate.getStringSerializer().serialize(
							dataSetCode);
					BoundHashOperations<Serializable, byte[], byte[]> boundHashOperations = redisTemplate
							.boundHashOps(key);
					boundHashOperations.put(redisTemplate.getStringSerializer()
							.serialize("eventID"), redisTemplate
							.getStringSerializer().serialize(eventID));
					boundHashOperations.put(redisTemplate.getStringSerializer()
							.serialize("origianlData"), redisTemplate
							.getStringSerializer().serialize(origianlData));
					boundHashOperations.put(redisTemplate.getStringSerializer()
							.serialize("dealData"), redisTemplate
							.getStringSerializer().serialize(dealData));
					connection.hMSet(key, boundHashOperations.entries());
					return null;
				}
			});
			rm.put("key", ConstanUtil.SUCCESSCODE);
			rm.put("value", ConstanUtil.SUCCESSMESSAGE);
		} catch (Exception e) {
			rm.put("key", ConstanUtil.ERRORCODE);
			rm.put("value", e.toString());
		}
		return rm;
	}

	/**
	 * 批量新增 使用pipeline方式 <br>
	 * ------------------------------<br>
	 * 
	 * @param list
	 *@return
	 */
	@RpcService
	public Map<String, String> save(final List<Map<String, Object>> list) {
		return null;
	}

	@Override
	public String saveValueToQueue(String value) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
