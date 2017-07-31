package com.bsoft.dao.sqlite.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.jdbc.core.JdbcTemplate;

import com.bsoft.dao.sqlite.SqliteDAO;
import com.bsoft.util.Bytes;
import com.bsoft.util.Global;

public class SqliteDAOImpl implements SqliteDAO {
	
	private static final Log logger = LogFactory.getLog(SqliteDAOImpl.class);
	private static final String CODING = "GBK";
	
	private JdbcTemplate jdbcTemplate;
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate=jdbcTemplate;
	}

	@Override
	public void saveDbConfig(String dbid, String dbname, String zk) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
		String datestr = sdf.format(new Date());
		String sql = "insert into CA_DATASOURCE(DBID,DBNAME,ZK,CREATEDATE) values('"+dbid+"','"+dbname+"','"+zk+"','"+datestr+"')";
		jdbcTemplate.update(sql);
	}

	@Override
	public List<Map<String, Object>> getDbConfig() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String sql = "select * from CA_DATASOURCE";
		list = jdbcTemplate.queryForList(sql);
		return list;
	}

	@Override
	public void deleteDbConfig(String dbid) {
		String sql = "delete from CA_DATASOURCE where dbid='"+dbid+"'";
		jdbcTemplate.update(sql);
	}

	@Override
	public void editDbConfig(String dbid, String dbname, String zk) {
		String sql = "update CA_DATASOURCE set dbname = '"+dbname+"',zk='"+zk+"' where dbid = '"+dbid+"'";
		jdbcTemplate.update(sql);
	}
	
	@Override
	public String getByID(String dbid){
		Map<String, Object> map = new HashMap<String, Object>();
		String sql = "select ZK from CA_DATASOURCE where dbid='"+dbid+"'";
		map = jdbcTemplate.queryForMap(sql);
		String zk = map.get("ZK").toString();
		return zk;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> executeSQL(final String zk,final String sql,final int pageNum,final int pageSize,Map<String, Object> jdbcmap){
		Map<String, Object> retuMap = new HashMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int titlelength = (Integer) jdbcmap.get("titlelength");
		Map<String, Object> title = (Map<String, Object>) jdbcmap.get("title");
		int startpage=0;
		if(pageNum>1){
			startpage=(pageNum-1)*pageSize;
		}
		Configuration cfg = new Configuration();
		cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.DB2400Dialect")
		   .setProperty("hibernate.connection.url", "jdbc:phoenix:"+zk)
		   .setProperty("hibernate.connection.driver_class", "org.apache.phoenix.jdbc.PhoenixDriver")
		   .setProperty("hibernate.connection.username", "")
		   .setProperty("hibernate.connection.password", "")
		   .setProperty("hibernate.show_sql", "true")
		   .setProperty("hibernate.format_sql", "true")
		   .setProperty("hibernate.jdbc.fetch_size", "100");
        SessionFactory sf = cfg.buildSessionFactory();  
        Session session = sf.openSession();  
        SQLQuery query = session.createSQLQuery(sql);
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		for(int i=1; i<=titlelength; i++){
			String column = (String)title.get(i+"");
			if(column.equals("ORIGINALBODY")){
				query.addScalar("ORIGINALBODY",StandardBasicTypes.BINARY);
			}else if(column.equals("BODY")){
				query.addScalar("BODY",StandardBasicTypes.BINARY);
			}else{
				query.addScalar(column);
			}
		}
		query.setFirstResult(startpage);
		query.setMaxResults(pageSize);
		list = query.list();
		for (Map<String, Object> map : list) {
			if(map.containsKey("ORIGINALBODY")){
				byte[] bytes = (byte[]) map.get("ORIGINALBODY");
				String OriginalBody = null;
				try {
					OriginalBody = new String(Bytes.unzip(bytes),CODING);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					map.put("ORIGINALBODY", OriginalBody);
				}
			}
			if(map.containsKey("BODY")){
				byte[] bytes = (byte[]) map.get("BODY");
				String Body = null;
				try {
					Body = new String(Bytes.unzip(bytes),CODING);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					map.put("BODY", Body);
				}
			}
		}
        retuMap.put("list", list);
        session.close();
        sf.close();
		return retuMap;
	}
	
	public Map<String, Object> getMessByJDBC(String sql,String zk){
		Map<String, Object> retuMap = new HashMap<String, Object>();
		Map<String, Object> title = new LinkedHashMap<String, Object>();
		ResultSet rs = null;  
		Statement st = null;  
		Connection conn = null; 
		String countsql = "select count(*) as count from ("+sql+")";
		String titlesql = sql+" limit 0";
		long count = 0;
		try {
			String url;
			url = "jdbc:phoenix:"+zk;
			Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
			conn = DriverManager.getConnection(url,"","");
			st = conn.createStatement();
			rs = st.executeQuery(countsql);
	        while (rs.next()) {
	        	count = (Long) rs.getObject(1);
	        }
	        rs = null;
	        rs = st.executeQuery(titlesql);
	        ResultSetMetaData md = rs.getMetaData();
	        int columnCount = md.getColumnCount();
	        for(int i=1; i<=columnCount; i++){
	        	title.put(i+"", md.getColumnName(i));
	        }
	        retuMap.put("pageTotal", count);
	        retuMap.put("titlelength", columnCount);
	        retuMap.put("title", title);
		} catch (ClassNotFoundException e) {
			logger.error(e.toString());
		} catch (SQLException e) {
			logger.error(e.toString());
			Global.setSqlflag(false);
		}finally{
			try {
				if(rs!=null){
					rs.close();
					rs=null;
				}
				if(st!=null){
					st.close();
					st=null;
				}
				if(conn!=null){
					conn.close();
					conn=null;
				}
			} catch (SQLException e) {
				logger.error(e.toString());
				Global.setSqlflag(false);
			}
		}
		return retuMap;
	}
	
	public List<Map<String, Object>> getTables(String dbid,String zk){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		ResultSet rs = null;  
		Statement st = null;  
		Connection conn = null; 
		try {
			String sql = "select distinct table_name from system.catalog order by table_name";
			String url = "jdbc:phoenix:"+zk;;
			Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
			conn = DriverManager.getConnection(url,"","");
			st = conn.createStatement();
			rs=st.executeQuery(sql);
	        int a=0;
	        while (rs.next()) {
	        	a++;
	        	Map<String, Object> map = new HashMap<String, Object>();
	        	map.put("tb"+a, rs.getObject(1));
	            list.add(map);
	        }
		} catch (ClassNotFoundException e) {
			logger.error(e.toString());
		} catch (SQLException e) {
			logger.error(e.toString());
		}finally{
			try {
				if(rs!=null){
					rs.close();
					rs=null;
				}
				if(st!=null){
					st.close();
					st=null;
				}
				if(conn!=null){
					conn.close();
					conn=null;
				}
			} catch (SQLException e) {
				logger.error(e.toString());
			}
		}
		return list;
	}
	
	public String getNewSql(String sql,long pageNum,long pageSize,long pageTotal){
		String newsql="";
		sql=sql.toLowerCase();
		String nospacesql=sql.trim().replace(" ", "");
		long endpage = pageNum*pageSize;
		long startpage = (pageNum-1)*pageSize;
		if(pageNum*pageSize>pageTotal){
			endpage = pageTotal;
		}
		int selectindex = sql.indexOf("select");
		//只支持select查询
		if(selectindex==-1){
			return "";
		}
		int orderbyindex = nospacesql.indexOf("orderby");
		if(nospacesql.indexOf("count(")!=-1 || nospacesql.indexOf("sum(")!=-1 || nospacesql.indexOf("max(")!=-1 || nospacesql.indexOf("min(")!=-1 || nospacesql.indexOf("avg(")!=-1){
			newsql=sql;
			return newsql;
		}
		if(orderbyindex==-1){
			sql = sql+" order by rowid";
		}
		newsql="select * from ("+sql+") limit "+endpage+" offset "+startpage;
		return newsql;
	}

	@Override
	public boolean getUser(String userId) {
		String sql = "select count(*) from user where  username = '"+userId+"'";
		int index = jdbcTemplate.queryForInt(sql);
		if(index == 0){
			return false;
		}
		return true;
	}

	@Override
	public boolean validatePassword(String userId, String passWord) {
		String sql = "select count(*) from user where username = '"+userId+"' and password= '"+passWord+"'";
		int index = jdbcTemplate.queryForInt(sql);
		if(index == 0){
			return false;
		}
		return true;
	}
}
