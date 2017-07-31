package com.bsoft.dao.oracle.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.bsoft.dao.oracle.OracleDAO;
import com.bsoft.util.Bytes;
import com.bsoft.util.DateTimeUtil;
import com.bsoft.util.Global;

/**
 * 
 * @author wjg
 * 
 */
public class OracleDAOImpl implements OracleDAO {

	private static final Log logger = LogFactory.getLog(OracleDAOImpl.class);
	
	// JAVA换行标志
	private static final String HHFLAG = "\r\n";

	private HibernateTemplate hibernateTemplate;
	private String entityName;
	private static final String CODING = "GBK";

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
	/**
	 * 1、根据用户名获取该名下所有的表信息 2、根据表信息获取该表对应的结构 3、创建SQL 4、执行SQL
	 */
	public static void main(String[] args) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");// 加入oracle的驱动，“”里面是驱动的路径
			String url = "jdbc:oracle:thin:@localhost:1521:orcl";// 数据库连接，oracle代表链接的是oracle数据库；thin:@MyDbComputerNameOrIP代表的是数据库所在的IP地址（可以保留thin:）；1521代表链接数据库的端口号；ORCL代表的是数据库名称
			String UserName = "blood";// 数据库用户登陆名 ( 也有说是 schema 名字的 )
			String Password = "blood";// 密码
			conn = DriverManager.getConnection(url, UserName, Password);

			// 1、获取所有表信息
			List<String> tablesList = new ArrayList<String>();
			String sql = "select table_name,LAST_ANALYZED from user_tables";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String table_name = rs.getString("table_name");
				String LAST_ANALYZED=rs.getString("LAST_ANALYZED");
				tablesList.add(table_name);
			}

			// 2、获取该表对应的结构
			Map<String, List<Map<String, String>>> tableInfoMap = new HashMap<String, List<Map<String, String>>>();
			for (String tableName : tablesList) {
				sql = "select COLUMN_NAME,DATA_TYPE,DATA_LENGTH,DATA_PRECISION,DATA_SCALE,NULLABLE,COLUMN_ID from user_tab_columns where table_name =UPPER('"
						+ tableName + "')";
				conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery(sql);
				List<Map<String, String>> fv1 = new ArrayList<Map<String, String>>();
				Map<String, String> fmap = new TreeMap<String, String>();
				// 输出列信息
				while (rs.next()) {
					fmap = new HashMap<String, String>();
					fmap.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));// 获取字段名
					fmap.put("DATA_TYPE", rs.getString("DATA_TYPE")); // 获取数据类型
					fmap.put("DATA_LENGTH", rs.getString("DATA_LENGTH"));// 获取数据长度
					if (rs.getString("DATA_PRECISION") == null) {
						fmap.put("DATA_PRECISION", "");
					} else {
						fmap.put("DATA_PRECISION", rs
								.getString("DATA_PRECISION"));// 获取数据长度
					}
					if (rs.getString("DATA_SCALE") == null) {
						fmap.put("DATA_SCALE", "");
					} else {
						fmap.put("DATA_SCALE", rs.getString("DATA_SCALE"));// 获取数据精度
					}
					fmap.put("NULLABLE", rs.getString("NULLABLE")); // 获取是否为空
					fmap.put("COLUMN_ID", rs.getString("COLUMN_ID")); // 字段序号
					fv1.add(fmap);
				}
				tableInfoMap.put(tableName, fv1);
			}

			// 3、创建可执行SQL
			System.out
					.println("=========================================CREATE SQL START==============================================");
			String createSQLView = "";
			Iterator it = tableInfoMap.keySet().iterator();
			while (it.hasNext()) {
				// 获取表名
				String tableName = it.next().toString();
				List<Map<String, String>> columnList = tableInfoMap
						.get(tableName);
				String tSQL = "-- Create table:" + tableName + " || "
						+ System.currentTimeMillis() + HHFLAG;
				tSQL = tSQL + "create table " + tableName + HHFLAG;
				tSQL = tSQL + "(" + HHFLAG;
				String vSQL = "";
				for (Map<String, String> m : columnList) {
					String COLUMN_NAME = m.get("COLUMN_NAME").toString();
					String DATA_TYPE = m.get("DATA_TYPE").toString();
					DATA_TYPE = DATA_TYPE.toUpperCase();
					String DATA_LENGTH = m.get("DATA_LENGTH").toString();
					String NULLABLE = m.get("NULLABLE").toString();
					// 整理 SQL 语句
					// -- Create table
					// create table BASE_USER
					// (
					// id VARCHAR2(20) not null,
					// name VARCHAR2(50),
					// password VARCHAR2(32),
					// status CHAR(1),
					// createdt DATE,
					// pagecount INTEGER
					// )
					if (NULLABLE.equals("N")) {
						NULLABLE = " NOT NULL";
					} else {
						NULLABLE = "";
					}
					// 过滤特殊字段
					if (DATA_TYPE.equals("BLOB")
							|| DATA_TYPE.equals("BINARY_DOUBLE")
							|| DATA_TYPE.equals("DATE")
							|| (DATA_TYPE.indexOf("TIMESTAMP") > -1)) {
						DATA_TYPE = DATA_TYPE;
					} else {
						DATA_TYPE = DATA_TYPE + "(" + DATA_LENGTH + ")"
								+ NULLABLE;
					}
					vSQL = vSQL + COLUMN_NAME + " " + DATA_TYPE + "," + HHFLAG;
				}
				int lastFlag = vSQL.lastIndexOf(",");
				vSQL = vSQL.substring(0, lastFlag);
				tSQL = tSQL + vSQL + HHFLAG;
				tSQL = tSQL + ");" + HHFLAG;
				// 统一转为换大写
				tSQL = tSQL.toUpperCase();
				createSQLView = createSQLView + tSQL;
			}
			System.out.println(createSQLView);
			System.out
					.println("=========================================CREATE SQL END==============================================");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void save(List<Map<String, Object>> list) {
		// ================================================
		/*
		 * for (Map<String, Object> map : list) {
		 * hibernateTemplate.save(entityName, map); }
		 */
		// ================================================
		/*
		 * SessionFactory sf = hibernateTemplate.getSessionFactory(); Session
		 * session = sf.openSession(); session.getTransaction().begin(); for
		 * (Map<String, Object> map : list) { session.save(entityName, map); }
		 * session.getTransaction().commit();
		 */
		// ===========向固定表插数据========================
		PreparedStatement pst = null;
		Session session = null;
		Connection conn = null;
		try {
			logger.info("数据准备批量插入.");
			session = hibernateTemplate.getSessionFactory().openSession();
			session.setFlushMode(FlushMode.AUTO);
			conn = session.connection();
			conn.setAutoCommit(false);
			int index = 0;
			pst = conn.prepareStatement("insert into "
							+ entityName
							+ " (EventId,RecordClassifying,RecordTitle,EffectiveTime,Authororganization,"
							+ "SourceID,VersionNumber,Author,SystemTime,UploadTime,Header,Body,DocFormat,"
							+ "ZipType,ProcessFlag,OriginalBody,MpiProcessFlag,Mpi,Dcid,CreateTime,"
							+ "Authororganization_text) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
			for (Map<String, Object> map : list) {
				map.put("CreateDateTime", DateTimeUtil.getCurrentDateTime());
				pst.setString(1, (String) map.get("EventId"));
				pst.setString(2, (String) map.get("RecordClassifying"));
				pst.setString(3, (String) map.get("RecordTitle"));
				pst.setTimestamp(4,new Timestamp(sdf.parse(map.get("EffectiveTime").toString()).getTime()));
				pst.setString(5, (String) map.get("Authororganization"));
				pst.setString(6, (String) map.get("SourceID"));
				pst.setString(7, (String) map.get("VersionNumber"));
				pst.setString(8, (String) map.get("Author"));
				pst.setTimestamp(9,new Timestamp(sdf.parse(map.get("SystemTime").toString()).getTime()));
				pst.setTimestamp(10,new Timestamp(sdf.parse(map.get("UploadTime").toString()).getTime()));
				pst.setString(11, (String) map.get("Header"));
				pst.setBytes(12, Bytes.zip(map.get("Body").toString().getBytes(CODING)));
				pst.setString(13, (String) map.get("DocFormat"));
				pst.setString(14, (String) map.get("ZipType"));
				pst.setString(15, (String) map.get("ProcessFlag"));
				pst.setBytes(16, Bytes.zip(map.get("OriginalBody").toString().getBytes(CODING)));
				pst.setString(17, (String) map.get("mpiProcessFlag"));
				pst.setString(18, (String) map.get("mpiId"));
				pst.setString(19, (String) map.get("Dcid"));
				pst.setTimestamp(20,new Timestamp(sdf.parse(map.get("CreateDateTime").toString()).getTime()));
				pst.setString(21, (String) map.get("AUTHORORGANIZATION_TEXT"));
				pst.addBatch();
				// 每1000条记录，提交一次
				index++;
				if (index > 1000) {
					pst.executeBatch();
					conn.commit();
					pst.clearBatch();
					index = 0;
				}
			}
			pst.executeBatch();
			conn.commit();
			pst.clearBatch();
			logger.info("数据批量插入Oracle成功.");
		} catch (SQLException e) {
			logger.error(e.toString());
			Global.setOracleflag(false);
			try {
				if (!conn.isClosed()) {
					conn.rollback();
					conn.setAutoCommit(true);
				}
			} catch (SQLException e1) {
				logger.error(e1.toString());
				Global.setOracleflag(false);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.toString());
			Global.setOracleflag(false);
		} catch (ParseException e) {
			logger.error(e.toString());
			Global.setOracleflag(false);
		} catch (IOException e) {
			logger.error(e.toString());
			Global.setOracleflag(false);
		} finally{
			try {
				pst.close();
				conn.close();
				session.close();
			} catch (SQLException e) {
				logger.error(e.toString());
				Global.setOracleflag(false);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean existEventid(String eventid){
		PreparedStatement pst = null;
		Session session = null;
		Connection conn = null;
		boolean flag = false;
		try {
			session = hibernateTemplate.getSessionFactory().openSession();
			conn = session.connection();
			pst = conn.prepareStatement("select * from " + entityName + " where EventId = ? ");
			pst.setString(1,eventid);
			ResultSet rs = pst.executeQuery();
			if(rs==null || rs.next()==false){
				flag = false;
			}else{
				flag = true;
			}
		} catch (Exception e) {
			logger.error(e.toString());
			Global.setOracleflag(false);
		}finally{
			try {
				pst.close();
				conn.close();
				session.close();
			} catch (SQLException e) {
				logger.error(e.toString());
				Global.setOracleflag(false);
			}
		}
		return flag;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void update(List<Map<String, Object>> list){
		PreparedStatement pst = null;
		Session session = null;
		Connection conn = null;
		try {
			logger.info("数据准备批量更新.");
			session = hibernateTemplate.getSessionFactory().openSession();
			session.setFlushMode(FlushMode.AUTO);
			conn = session.connection();
			conn.setAutoCommit(false);
			int index = 0;
			pst = conn.prepareStatement(" update "
							+ entityName
							+ " set MpiProcessFlag=?,Mpi=? where EventId = ? ");
			for (Map<String, Object> map : list) {
				pst.setString(1, (String) map.get("mpiProcessFlag"));
				pst.setString(2, (String) map.get("mpiId"));
				pst.setString(3, (String) map.get("EventId"));
				pst.addBatch();
				// 每1000条记录，提交一次
				index++;
				if (index > 1000) {
					pst.executeBatch();
					conn.commit();
					pst.clearBatch();
					index = 0;
				}
			}
			pst.executeBatch();
			conn.commit();
			pst.clearBatch();
			logger.info("数据批量更新Oracle成功.");
		} catch (SQLException e) {
			logger.error(e.toString());
			Global.setOracleflag(false);
			try {
				if (!conn.isClosed()) {
					conn.rollback();
					conn.setAutoCommit(true);
				}
			} catch (SQLException e1) {
				logger.error(e1.toString());
				Global.setOracleflag(false);
			}
		}finally{
			try {
				pst.close();
				conn.close();
				session.close();
			} catch (SQLException e) {
				logger.error(e.toString());
				Global.setOracleflag(false);
			}
		}
	}
}
