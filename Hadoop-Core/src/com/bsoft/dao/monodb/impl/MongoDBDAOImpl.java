package com.bsoft.dao.monodb.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.bsoft.dao.monodb.MongoDBDAO;
import com.bsoft.util.KeyEntityRWLockManager;
import com.bsoft.util.StringHelper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * 
 * @author wjg
 * 
 */
public class MongoDBDAOImpl implements MongoDBDAO {

	private static final Log logger = LogFactory.getLog(MongoDBDAOImpl.class);
	private static Map<String, AtomicLong> count = new HashMap<String, AtomicLong>();
	private static final KeyEntityRWLockManager lock = new KeyEntityRWLockManager();
	private MongoTemplate mongoTemplate;
	private String collectionName1;
	private String collectionName2;

	public void setCollectMap(Map<String, String> collectMap) {
		this.collectionName1 = collectMap.get("collectionName1").toString();
		this.collectionName2 = collectMap.get("collectionName2").toString();
	}

	// 每次提取的数据条数
	private static int limit = 100;

	public void setSize(int limit1) {
		limit = limit1;
	}

	public void setMongoTemplate(MongoTemplate mt) {
		this.mongoTemplate = mt;
		// 移除_class字段
		((MappingMongoConverter) mongoTemplate.getConverter())
				.setTypeMapper(new DefaultMongoTypeMapper(null));
	}

	@Override
	public Map getStructSearchDetail(String id) {
		// TODO Auto-generated method stub
		return mongoTemplate.findById(id, Map.class);
	}

	@Override
	public long getStructSearchCount(Criteria criteria) {
		return mongoTemplate.count(new Query(criteria), Long.class);
	}

	// Add by 王建刚 20140212
	// 根据身份证号，或则姓名，性别，查找对应的EVENTID
	public List<Map> getEventIDListByParameter(Map p) {
		String key = "";
		String value = "";
		if (p.containsKey("IdCard")) {
			key = "IdCard";
		} else if (p.containsKey("SexCode")) {
			key = "SexCode";
		} else if (p.containsKey("PersonName")) {
			key = "PersonName";
		} else {

		}
		value = (String) p.get(key);
		// 获取接入机构编号
		String[] AUTHORORGANIZATION = new String[] { "getAUTHORORGANIZATIONS()" };
		// 构建查询SQL
		Query searchQuery = new Query();
		// 模糊查询,MONGODB的模糊查询，只能通过正则表达式
		searchQuery.addCriteria(new Criteria(key).regex(".*?" + value + ".*"));
		searchQuery.addCriteria(Criteria.where("AUTHORORGANIZATION").in(
				AUTHORORGANIZATION));
		searchQuery.skip(0);// skip相当于从那条记录开始
		searchQuery.limit(limit);// 从每页显示的记录条数.之所以要设定limit，因为是模糊匹配，怕内存溢出。
		// searchQuery.sort().on("UPLOADTIME", Order.DESCENDING);//
		// 排序:根据UPLOADTIME进行顺序排
		List<Map> list = mongoTemplate.find(searchQuery, Map.class,
				collectionName1);
		return list;
	}

	@Override
	public List<Map<String, Object>> getData(String tableName) {
		/*
		 * // 返回的结果 List<Map<String, Object>> rList = new ArrayList<Map<String,
		 * Object>>(); // 构建查询SQL Query searchQuery = new Query();
		 * //searchQuery.skip(0);// skip相当于从那条记录开始 searchQuery.limit(size);//
		 * 从每页显示的记录条数.之所以要设定limit，因为是模糊匹配，怕内存溢出。 searchQuery.getSortObject();
		 * List<Map> list = mongoTemplate.find(searchQuery, Map.class,
		 * collectionName1); for (Map map : list) { // 类型强转 Map<String, Object>
		 * m = (Map<String, Object>) (map); rList.add(m); } return rList;
		 */
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		DBObject orderBy = new BasicDBObject();
		orderBy.put("_id", 1);
		List<DBObject> list = mongoTemplate.getCollection(tableName).find()
				.sort(orderBy).limit(limit).toArray();
		for (DBObject dbObj : list) {
			resultList.add(dbObj.toMap());
		}
		return resultList;
	}

	@Override
	public List<Map<String, Object>> getData(int page, int rows, int nextstart,
			String tableName) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		DBObject orderBy = new BasicDBObject();
		orderBy.put("_id", 1);
		List<DBObject> list = mongoTemplate.getCollection(tableName).find()
				.sort(orderBy).skip(nextstart).limit(rows).toArray();
		for (DBObject dbObj : list) {
			resultList.add(dbObj.toMap());
		}
		return resultList;
	}

	@Override
	public Integer getCount(String tableName) { 

		return mongoTemplate.getCollection(tableName).find().count();
	}

	@Override
	public List<Map<String, Object>> getData(String tableName, ArrayList list) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		DBObject queryObj = new BasicDBObject();
		List dlist = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			DBObject dbObject = new BasicDBObject();
			dbObject.put("_id", Long.parseLong((String) list.get(i)));
			dlist.add(dbObject);
		}
		queryObj.put("$or", dlist);

		List<DBObject> dblist = mongoTemplate.getCollection(tableName)
				.find(queryObj).toArray();
		for (DBObject dbObj1 : dblist) {
			resultList.add(dbObj1.toMap());
		}

		return resultList;
	}

	@Override
	public List<Map<String, Object>> getData() {
		return getData(collectionName1);
	}

	@Override
	public String save(List<Map<String, Object>> list, String tableName) {
		String s = "";
		try {
			for (Map<String, Object> map : list) {
				// 添加主键
				map.put("_id", getIdCount(tableName));
			}
			if (list.size() >= 1) {
				mongoTemplate.insert(list, tableName);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return s;
	}

	/**
	 * id自增主键计算方法
	 * 
	 * @return
	 */
	private long getIdCount(String collectionName) {
		String core = mongoTemplate.getCollection(collectionName).getName();
		AtomicLong counter = null;
		boolean isLock = lock.tryReadLock(core);
		try {
			counter = count.get(core);
		} finally {
			if (isLock) { 
				lock.readUnlock(core);
			}
		}
		if (counter == null) {
			try {
				lock.writeLock(core);
				counter = count.get(core);
				if (counter == null) {
					long i = getDBMaxId(collectionName);
					counter = new AtomicLong(i);
					count.put(core, counter);
				}
			} finally {
				lock.writeUnlock(core);
			}
		}
		return counter.incrementAndGet();
	}

	/**
	 * 获取表中id的最大值
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private long getDBMaxId(String collectionName) {
		long result = 0;
		DBObject orderBy = new BasicDBObject();
		orderBy.put("_id", -1);
		List<DBObject> list = mongoTemplate.getCollection(collectionName)
				.find().sort(orderBy).limit(1).toArray();
		if (list.size() > 0) {
			Map<String, Object> obj = list.get(0).toMap();
			Object idObj=obj.get("_id");
			result = Long.parseLong(idObj.toString());
		}
		return result;
	}

	@Override
	public String save(List<Map<String, Object>> list) {
		return save(list, collectionName1);
	}

	@Override
	public void removeByID(long id, String tableName) {
		// 插入完之后，原始集合中删除相应的记录
		Query query = new Query(Criteria.where("_id").lte(id));
		mongoTemplate.remove(query, tableName);
	}

	@Override
	public void removeByID(long id) {
		removeByID(id, collectionName1);
	}

	@Override
	public void removeInID(long id, String tableName) {
		DBObject dbObject = new BasicDBObject();
		dbObject.put("_id", id);
		mongoTemplate.getCollection(tableName).remove(dbObject);
	}

	@Override
	public String getBody(String EventID) {
		Map map = new HashMap();
		DBObject refObject = new BasicDBObject();
		refObject.put("EventId", EventID);
		DBObject keysObject = new BasicDBObject();
		keysObject.put("Body", "1");
		keysObject.put("_id", "0");
		DBObject dbObj1=mongoTemplate.getCollection(collectionName2).find(refObject, keysObject).limit(1).next();
		map = dbObj1.toMap();
		String result = StringHelper.ParseBodyString(map.get("Body").toString());
		return result;
	}

}
