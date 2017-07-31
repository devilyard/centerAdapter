package com.bsoft.dao.sqlite;

import java.util.List;
import java.util.Map;

public interface SqliteDAO {
	
	//保存数据库配置
	public void saveDbConfig(String dbid, String dbname, String zk);
	
	//获取所有数据库配置
	public List<Map<String, Object>> getDbConfig();
	
	//删除数据库配置
	public void deleteDbConfig(final String dbid);
	
	//修改数据库配置
	public void editDbConfig(final String dbid, final String dbname, final String zk);
	
	//通过数据库编号获取zookeeper
	public String getByID(String dbid);
	
	//获取phoenix表信息
	public List<Map<String, Object>> getTables(String dbid,String zk);
	
	//phoenix执行SQL
	public Map<String, Object> executeSQL(final String zk,final String sql,final int pageNum,final int pageSize,Map<String, Object> jdbcmap);
	
	//phoenix通过jdbc获取列名和总记录数
	public Map<String, Object> getMessByJDBC(String sql,String zk);
	
	//查找用户
	public boolean getUser(String userId);
	
	public boolean validatePassword(String userId, String passWord);
	
}
