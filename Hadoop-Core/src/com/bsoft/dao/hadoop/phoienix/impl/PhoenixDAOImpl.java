package com.bsoft.dao.hadoop.phoienix.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.bsoft.dao.hadoop.phoienix.PhoienixDAO;
import com.bsoft.util.Bytes;
import com.bsoft.util.XMLHelper;

/**
 * 
 * @author wjg
 * 
 */
public class PhoenixDAOImpl implements PhoienixDAO {

	private static final Log logger = LogFactory.getLog(PhoenixDAOImpl.class);
	private static final String CODING = "GBK";
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	// 获取SEQ值
	@Override
	public long getSEQ(String sn) {
		long seq = jdbcTemplate.queryForObject("SELECT NEXT VALUE FOR SEQ_" + sn,
				Long.class);
		return seq;
	}

	/**
	 * 
	 * @param mpiid
	 *            :mpid
	 * @param st
	 *            :开始查找日期
	 * @param et
	 *            :结束查找日期
	 * @return
	 */
	@Override
	public long getCount(String tableName, String key, String value, String st,
			String et) {
		long rowCount = 0;
		String SQL = "";
		try {
			// 第一次登录时,没有任何参数,找所有记录总数
//			if (key.equals("") && st.equals("") && et.equals("")) {
//				SQL = "SELECT COUNT(*) FROM " + tableName;
//				rowCount = jdbcTemplate.queryForLong(SQL);
//			} else {
			if(key==null || key.equals("")){
				String condition = " WHERE " + " CREATEDATETIME>=? AND CREATEDATETIME<=?";
				SQL = "SELECT COUNT(*) FROM " + tableName + condition;
				rowCount = jdbcTemplate.queryForLong(SQL, new Object[] {st, et });
			}else{
				String condition = " WHERE " + key
						+ " = ? AND CREATEDATETIME>=? AND CREATEDATETIME<=?";
				SQL = "SELECT COUNT(*) FROM " + tableName + condition;
				rowCount = jdbcTemplate.queryForLong(SQL, new Object[] { value,
						st, et });
			}
//			}
			// logger.info(SQL);
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return rowCount;
	}

	@Override
	public Map<String, Object> getList(String tableName,int currentPage, int pageSize, long total, String key,
			String value, String st, String et) {
		// 定义返回的结果
		Map<String, Object> rmap = new HashMap<String, Object>();
//		// 第一个参数为SQL语句，第二参数的RowMapper将每一行结果映射成一个Java对象，方便将其他封装到JavaBean中，第三个参数为占位符值（为可变参数）
		String SQL = "";
		long startpage = currentPage*pageSize;
		if(startpage >= total){
			pageSize = (int)total-pageSize*(currentPage-1);
			startpage = total;
		}
		if(key==null || key.equals("")){
			String condition = " CREATEDATETIME >= '"+ st + "' AND CREATEDATETIME <= '" + et + "'";
			SQL = "SELECT * FROM(SELECT * FROM "
					+ tableName
					+ " WHERE "
					+ condition
					+ ") WHERE ROWID>=(SELECT  MIN(ROWID)  FROM (SELECT ROWID  FROM (SELECT ROWID FROM "
					+ tableName + " WHERE " + condition
					+ ")  LIMIT " + startpage
					+ " ))  LIMIT " + pageSize;
    	}else{
			String condition = key + " = '" + value + "' AND CREATEDATETIME>='"
					+ st + "' AND CREATEDATETIME<='" + et + "'";
			SQL = "SELECT * FROM(SELECT * FROM "
					+ tableName
					+ " WHERE "
					+ condition
					+ ") WHERE ROWID>=(SELECT  MIN(ROWID)  FROM (SELECT ROWID  FROM (SELECT ROWID FROM "
					+ tableName + " WHERE " + condition
					+ ")  DESC LIMIT " + startpage
					+ " ))   LIMIT " + pageSize;
    	}
		List<Map<String, Object>> list = jdbcTemplate.query(SQL,
				new RowMapper() {
					@Override
					public Object mapRow(ResultSet rs, int arg1)
							throws SQLException {
						Map<String, Object> map = new HashMap<String, Object>();
						// 遍历游标
						ResultSetMetaData rsmd = rs.getMetaData();
						int columnCount = rsmd.getColumnCount();
						// 输出列名
						for (int i = 1; i <= columnCount; i++) {
							String key = rsmd.getColumnName(i);
							if(key.equals("ORIGINALBODY")||key.equals("BODY")){
								byte[] bytes = rs.getBytes(i);
								String blob = null;
								try {
									String index = new String(Bytes.unzip(bytes),CODING);
									blob = XMLHelper.xmlFormat(index);
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}finally{
									map.put(key, blob);
								}
							} else {
								String value = rs.getString(i);
								map.put(key, value);
							}
						}
						return map;
					}
				});
		List<Map<String, Object>> tr = new ArrayList<Map<String, Object>>();
		int len = list.size();
		for (int i = len - 1; i >= 0; i--) {
			tr.add(list.get(i));
		}
		list = tr;
		rmap.put("list", list);
		return rmap;
	}
	
	@Override
	public List<Map<String, Object>> getDetailData(String dataSetCode,
			Map<String, String> param) {
		// 组装查询条件
		String condition = "WHERE";
		String tc = "";
		for (Map.Entry<String, String> entry : param.entrySet()) {
			String pKey = entry.getKey();
			String pValue = entry.getValue();
			tc = " " + pKey + "='" + pValue + "' AND";
		}
		if (tc.equals("")) {
			condition = "";
		} else {
			condition = condition + tc;
			// 需要移除最后的AND
			condition = condition.substring(0, condition.length() - 4);
		}
		String SQL = "SELECT * FROM " + dataSetCode.toUpperCase() + " "
				+ condition;

		//logger.info(SQL);

		List<Map<String, Object>> list = jdbcTemplate.query(SQL,
				new RowMapper() {
					@Override
					public Object mapRow(ResultSet rs, int arg1)
							throws SQLException {
						Map<String, Object> map = new HashMap<String, Object>();
						// 遍历游标
						ResultSetMetaData rsmd = rs.getMetaData();
						int columnCount = rsmd.getColumnCount();
						// 输出列名
						for (int i = 1; i <= columnCount; i++) {
							String key = rsmd.getColumnName(i);
							String value = rs.getString(i);
							map.put(key, value);
						}
						return map;
					}
				});
		return list;
	}

	@Override
	public Map<String, Object> getRowData(String EventId) {
		String SQL = "SELECT T.HEADER,T.BODY,T.ORIGINALBODY FROM EHRMAIN T WHERE PK ='"+EventId+"'";
		List<Map<String, Object>> list = jdbcTemplate.query(SQL,
				new RowMapper() {
					@Override
					public Object mapRow(ResultSet rs, int arg1)
							throws SQLException {
						Map<String, Object> map = new HashMap<String, Object>();
						// 遍历游标
						ResultSetMetaData rsmd = rs.getMetaData();
						int columnCount = rsmd.getColumnCount();
						// 输出列名
						for (int i = 1; i <= columnCount; i++) {
							String key = rsmd.getColumnName(i);
							if(key.equals("ORIGINALBODY")||key.equals("BODY")){
								byte[] bytes = rs.getBytes(i);
									map.put(key, bytes);
							} else {
								String value = rs.getString(i);
								map.put(key, value);
							}
						}
						return map;
					}
				});
		return list.get(0);
	}
	
	@Override
	public String getBodyEventId(String EventId) {
		String SQL = "SELECT T.BODY FROM EHRMAIN T WHERE PK ='"+EventId+"'";
		List<Map<String, Object>> list = jdbcTemplate.query(SQL,
				new RowMapper() {
					@Override
					public Object mapRow(ResultSet rs, int arg1)
							throws SQLException {
						Map<String, Object> map = new HashMap<String, Object>();
						// 遍历游标
						ResultSetMetaData rsmd = rs.getMetaData();
						int columnCount = rsmd.getColumnCount();
						// 输出列名
						for (int i = 1; i <= columnCount; i++) {
							String key = rsmd.getColumnName(i);
							byte[] bytes = rs.getBytes(i);
							String index;
							String blob = null;
							try {
								blob = new String(Bytes.unzip(bytes),CODING);
							} catch (UnsupportedEncodingException e) {

								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							map.put(key, blob);
						}
						return map;
					}
				});
		String body = null;
		if(list != null && list.size()>0 ){
			body = list.get(0).get("BODY").toString();
		}
		return body;
	}
}
