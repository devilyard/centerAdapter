package com.bsoft.dao.monodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.query.Criteria;

/**
 * 
 * @author wjg
 *
 */
public interface MongoDBDAO {
	//Add by 王建刚 20140212
	//根据身份证号，或则姓名，性别，查找对应的EVENTID
	public List<Map> getEventIDListByParameter(Map p);	

	Map getStructSearchDetail(String id);

	long getStructSearchCount(Criteria criteria);
	
	
	
	
	//保存数据
	public String save(List<Map<String,Object>> list,String tableName);
	
	public String save(List<Map<String,Object>> list);
	
	//从临时集合中获取数据
	public List<Map<String,Object>> getData(String tableName);
	
	public List<Map<String,Object>> getData();
	
	public List<Map<String,Object>> getData(int page, int rows, int nextstart, String tableName);
	
	public List<Map<String,Object>> getData(String tableName,ArrayList list);
	
	public String getBody(String EventID);
	
	//获取集合长度
	public Integer getCount(String tableName);
	
	//删除数据
	public void removeByID(long id);
	
	public void removeByID(long id,String tableName);
	
	public void removeInID(long id,String tableName);
}
