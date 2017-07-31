package com.bsoft.service.impl.monodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bsoft.dao.monodb.MongoDBDAO;
import com.bsoft.listener.ListenerProcess;
import com.bsoft.util.DateTimeUtil;

/**
 * 
 * @author wjg
 * 
 */
public class MongoDBServiceImpl implements ListenerProcess {

	private MongoDBDAO mongoDAOService;
	private String collectionName3;
	private String collectionName2;

	public void setCollectMap(Map<String, String> collectMap) {
		this.collectionName2 = collectMap.get("collectionName2").toString();
		this.collectionName3 = collectMap.get("collectionName3").toString();
	}

	public void setMongoDAOService(MongoDBDAO mongoDAOService) {
		this.mongoDAOService = mongoDAOService;
	}

	@Override
	public List<Map<String, Object>> process(List<Map<String, Object>> list) {
		// collectionName3集合对应的LIST定义
		List<Map<String, Object>> list3 = new ArrayList<Map<String, Object>>();
		// collectionName2集合对应的LIST定义
		List<Map<String, Object>> list2 = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> map : list) {
			// 添加CreateDateTime
			map.put("CreateDateTime", DateTimeUtil.getCurrentDateTime());
			Object mpiIdObject = map.get("mpiId");
			// 把MPIID为NULL的,保存到collectionName3集合中
			if (mpiIdObject == null || mpiIdObject.toString().equals("")) {
				list3.add(map);
			} else {
				// MPIID不为NULL的,保存到collectionName2集合中
				list2.add(map);
			}
		}
		// 批量插入指定的集合
		/*if (!list2.isEmpty()) {
			mongoDAOService.save(list2, collectionName2);
		}*/
		if (!list3.isEmpty()) {
			mongoDAOService.save(list3, collectionName3);
		}
		return list;
	}

	@Override
	public List<Map<String, Object>> afterProcess(List<Map<String, Object>> list) {
		// TODO Auto-generated method stub
		return list;
	}

	@Override
	public List<Map<String, Object>> beforeProcess(
			List<Map<String, Object>> list) {
		// TODO Auto-generated method stub
		return list;
	}
}
