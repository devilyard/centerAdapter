package com.bsoft.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bsoft.dao.hadoop.hbase.impl.HBaseDAOImpl;
import com.bsoft.dao.hadoop.phoienix.impl.PhoienixDAOImpl;
import com.bsoft.dao.sqlite.impl.SqliteDAOImpl;
import com.bsoft.util.DateTimeUtil;
import com.bsoft.util.FileCollector;
import com.bsoft.util.Global;

import ctd.util.ServletUtils;

/**
 * 
 *@author wjg
 * @类功能说明:
 */
@Controller
public class MVCConroller extends JSONOutputMVCConroller {

	private static final Logger logger = Logger
			.getLogger(MVCConroller.class);

	@Autowired
	@Qualifier("phoienixDAOService")
	PhoienixDAOImpl phoienixDao;

	@Autowired
	@Qualifier("sqliteDao")
	SqliteDAOImpl sqliteDao;
	@Autowired
	@Qualifier("hbaseDAOService")
	HBaseDAOImpl HBaseDAO;
	
	/**
	 * 
	 * @方法说明：获取hbase总表里的记录
	 * @param page 页数
	 * @param rows 行数
	 * @param st 开始时间
	 * @param et 结束时间
	 * @param key
	 * @param value
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "html/getHBaseData.action", method = RequestMethod.GET)
	public void getHBaseData(
			@RequestParam(value = "page") Integer page,
			@RequestParam(value = "rows") int rows,
			@RequestParam(value = "st", required = false) String st,
			@RequestParam(value = "et", required = false) String et,
			@RequestParam(value = "key", required = false) String key,
			@RequestParam(value = "value", required = false) String value,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		HashMap<String, Object> resData = new HashMap<String, Object>();
		// 指定表名
		String tableName = FileCollector.getHbaseTable().get("T");
		// 获取数据列表结果
		List<Map<String, Object>> rowsList = new ArrayList<Map<String, Object>>();
		if(value==null){
			value="";
		}
		value = URLDecoder.decode(value, "UTF-8");
		int currentPage = page;// 当前页码
		int pageSize = rows;// 每页显示数据量
		// 总的记录数
		long pageNum = 0;
		try {
			// 开始日期4 
			if (st == null || st.equals("")) {
				st = "1900-01-01 00:00:01";
			} else {
				st = st + " 00:00:01";
			}
			// 结束日期
			if (et == null || et.equals("")) {
				et = DateTimeUtil.getCurrentDateTime();
			} else {
				et = et + " 23:59:59";
			}
			// 查询的具体字段类型
			if (key == null || key.equals("")) {
				key = "";
			}
			// 查询的具体字段对应的值
			if (value == null || value.equals("")) {
				value = "";
			}
			//long startTime = System.currentTimeMillis();//获取当前时间
			//pageNum = HBaseDAO.getHBaseDataCount("EHRMAIN", "RECORD", null, null);
			//long endTime = System.currentTimeMillis();
			//System.out.println("程序运行时间："+(endTime-startTime)+"ms");
			// 获取记录总数
			pageNum = phoienixDao.getCount(tableName, key, value, st, et);
			// 获取记录行明细
			Map<String, Object> returnMap = phoienixDao.getList(tableName,
					currentPage, pageSize, pageNum,key, value, st, et);
			if (!returnMap.isEmpty()) {
				rowsList = (List<Map<String, Object>>) returnMap.get("list");
				int i = (page-1)*rows+1;
				for(Map<String, Object> map :rowsList){
					map.put("PAGEID",i);
					i= i+1;
					if("1".equals(map.get("SEXCODE"))){
						map.put("SEXCODE", "男");
					}else{
						map.put("SEXCODE", "女");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			resData.put("total", pageNum);
			resData.put("rows", rowsList);
			boolean gzip = ServletUtils.isAcceptGzip(request);
			jsonOutput(response, resData, gzip);
		}
	}
	
	/**
	 * 
	 * @方法说明：
	 * @param page
	 * @param rows
	 * @param st
	 * @param et
	 * @param key
	 * @param value
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "html/getHBaseErrorData.action", method = RequestMethod.GET)
	public void getHBaseErrorData(
			@RequestParam(value = "page") Integer page,
			@RequestParam(value = "rows") int rows,
			@RequestParam(value = "st", required = false) String st,
			@RequestParam(value = "et", required = false) String et,
			@RequestParam(value = "key", required = false) String key,
			@RequestParam(value = "value", required = false) String value,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		HashMap<String, Object> resData = new HashMap<String, Object>();
		// 指定表名
		String tableName = FileCollector.getHbaseTable().get("F");
		// 获取数据列表结果
		List<Map<String, Object>> rowsList = new ArrayList<Map<String, Object>>();
		if(value==null){
			value="";
		}
		value = URLDecoder.decode(value, "UTF-8");
		int currentPage = page;// 当前页码
		int pageSize = rows;// 每页显示数据量
		// 总的记录数
		long pageNum = 0;
		try {
			// 开始日期
			if (st == null || st.equals("")) {
				st = "1900-01-01 00:00:01";
			} else {
				st = st + " 00:00:01";
			}
			// 结束日期
			if (et == null || et.equals("")) {
				et = DateTimeUtil.getCurrentDateTime();
			} else {
				et = et + " 23:59:59";
			}
			// 查询的具体字段类型
			if (key == null || key.equals("")) {
				key = "";
			}
			// 查询的具体字段对应的值
			if (value == null || value.equals("")) {
				value = "";
			}
			// 获取记录总数
			pageNum = phoienixDao.getCount(tableName, key, value, st, et);
			// 获取记录行明细
			Map<String, Object> returnMap = phoienixDao.getList(tableName,
					currentPage, pageSize, pageNum,key, value, st, et);
			if (!returnMap.isEmpty()) {
				int i = (page-1)*rows+1;
				rowsList = (List<Map<String, Object>>) returnMap.get("list");
				for(Map<String, Object> map :rowsList){
					map.put("PAGEID",i);
					i= i+1;
					if("1".equals(map.get("SEXCODE"))){
						map.put("SEXCODE", "男");
					}else{
						map.put("SEXCODE", "女");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			resData.put("total", pageNum);
			resData.put("rows", rowsList);
			boolean gzip = ServletUtils.isAcceptGzip(request);
			jsonOutput(response, resData, gzip);
		}
	}
	/**
	 * 
	 * @方法说明：
	 * @param dbname
	 * @param zk
	 * @param dbid
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="html/saveDbConfig.action", method = RequestMethod.GET)
	public void saveDbConfig(
			@RequestParam(value="dbname") String dbname,
			@RequestParam(value="zk") String zk,
			@RequestParam(value="dbid") String dbid,
			HttpServletRequest request,HttpServletResponse response) throws IOException{
		HashMap<String, Object> resData = new HashMap<String, Object>();
		try {
			dbid=URLDecoder.decode(dbid,"UTF-8");
			dbname=URLDecoder.decode(dbname,"UTF-8");
			zk=URLDecoder.decode(zk,"UTF-8");
			sqliteDao.saveDbConfig(dbid,dbname,zk);
			resData.put("success", 200);
			resData.put("message", "保存成功");
		} catch (Exception e) {
			logger.error(e.toString());
			resData.put("success", 201);
			resData.put("message", "发生异常，保存失败");
		}
		boolean gzip = ServletUtils.isAcceptGzip(request);
		jsonOutput(response, resData, gzip);
	}
	/**
	 * 
	 * @方法说明：
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="html/getDbConfig.action", method = RequestMethod.GET)
	public void getDbConfig(
			HttpServletRequest request,HttpServletResponse response) throws IOException{
		HashMap<String, Object> resData = new HashMap<String, Object>();
		List<Map<String, Object>> maplist = new ArrayList<Map<String, Object>>();
		try {
			maplist = sqliteDao.getDbConfig();
			resData.put("list", maplist);
			resData.put("success", 200);
			resData.put("message", "查询成功");
		} catch (Exception e) {
			logger.error(e.toString());
			resData.put("success", 201);
			resData.put("message", "发生异常，查询失败");
		}
		boolean gzip = ServletUtils.isAcceptGzip(request);
		jsonOutput(response, resData, gzip);
	}
	/**
	 * 
	 * @方法说明：
	 * @param dbid
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="html/deleteDbConfig.action", method = RequestMethod.GET)
	public void deleteDbConfig(
			@RequestParam(value="dbid") String dbid,
			HttpServletRequest request,HttpServletResponse response) throws IOException{
		HashMap<String, Object> resData = new HashMap<String, Object>();
		dbid = URLDecoder.decode(dbid, "UTF-8");
		try {
			sqliteDao.deleteDbConfig(dbid);
			resData.put("success", 200);
			resData.put("message", "删除成功");
		} catch (Exception e) {
			logger.error(e.toString());
			resData.put("success", 201);
			resData.put("message", "发生异常，删除失败");
		}
		boolean gzip = ServletUtils.isAcceptGzip(request);
		jsonOutput(response, resData, gzip);
	}
	/**
	 * 
	 * @方法说明：
	 * @param dbid
	 * @param dbname
	 * @param zk
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="html/editDbConfig.action", method = RequestMethod.GET)
	public void editDbConfig(
			@RequestParam(value="dbid") String dbid,
			@RequestParam(value="dbname") String dbname,
			@RequestParam(value="zk") String zk,
			HttpServletRequest request,HttpServletResponse response) throws IOException{
		HashMap<String, Object> resData = new HashMap<String, Object>();
		dbid = URLDecoder.decode(dbid, "UTF-8");
		dbname = URLDecoder.decode(dbname, "UTF-8");
		zk = URLDecoder.decode(zk, "UTF-8");
		try {
			sqliteDao.editDbConfig(dbid,dbname,zk);
			resData.put("success", 200);
			resData.put("message", "修改成功");
		} catch (Exception e) {
			logger.error(e.toString());
			resData.put("success", 201);
			resData.put("message", "发生异常，修改失败");
		}
		boolean gzip = ServletUtils.isAcceptGzip(request);
		jsonOutput(response, resData, gzip);
	}
	/**
	 * 
	 * @方法说明：
	 * @param dbid
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="html/getTables.action", method = RequestMethod.GET)
	public void getTables(
			@RequestParam(value="dbid") String dbid,
			HttpServletRequest request,HttpServletResponse response) throws IOException{
		HashMap<String, Object> resData = new HashMap<String, Object>();
		List<Map<String, Object>> maplist = new ArrayList<Map<String, Object>>();
		try {
			String zk = sqliteDao.getByID(dbid);
			maplist = sqliteDao.getTables(dbid,zk);
			resData.put("list", maplist);
			resData.put("success", 200);
			resData.put("message", "获取表信息成功");
		} catch (Exception e) {
			logger.error(e.toString());
			resData.put("success", 201);
			resData.put("message", "发生异常，获取表信息失败");
		}
		boolean gzip = ServletUtils.isAcceptGzip(request);
		jsonOutput(response, resData, gzip);
	}
	/**
	 * 
	 * @方法说明：
	 * @param sql
	 * @param dbid
	 * @param pageNum
	 * @param pageSize
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value="html/executeSQL.action", method = RequestMethod.GET)
	public void executeSQL(
			@RequestParam(value="sql") String sql,
			@RequestParam(value="dbid") String dbid,
			@RequestParam(value="pageNum") int pageNum,
			@RequestParam(value="pageSize") int pageSize,
			HttpServletRequest request,HttpServletResponse response) throws IOException{
		HashMap<String, Object> resData = new HashMap<String, Object>();
		Map<String, Object> hibernatemap = new HashMap<String, Object>();
		Map<String, Object> jdbcmap = new HashMap<String, Object>();
		dbid = URLDecoder.decode(dbid, "UTF-8");
		sql = URLDecoder.decode(sql, "UTF-8");
		Global.setSqlflag(true);
		try {
			if(dbid!="" && dbid!=null){
				String zk = sqliteDao.getByID(dbid);
				//获取总记录数
				jdbcmap = sqliteDao.getMessByJDBC(sql,zk);
				hibernatemap = sqliteDao.executeSQL(zk,sql,pageNum,pageSize,jdbcmap);
				resData.put("success", 200);
				resData.put("hibernatedata", hibernatemap);
				resData.put("jdbcdata", jdbcmap);
				resData.put("message", "执行SQL成功");
			}else{
				resData.put("success", 201);
				resData.put("message", "请选择数据源");
			}
		} catch (Exception e) {
			logger.error(e.toString());
			resData.put("success", 201);
			resData.put("message", e.toString());
		}
		if(!Global.getSqlflag()){
			resData.put("success", 201);
			resData.put("message", "SQL出错，执行失败");
		}
		boolean gzip = ServletUtils.isAcceptGzip(request);
		jsonOutput(response, resData, gzip);
	}
}
