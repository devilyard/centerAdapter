package com.bsoft.controller;

import java.io.IOException;
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

import com.bsoft.dao.monodb.MongoDBDAO;
import com.bsoft.service.DataProcessService;
import com.bsoft.util.Global;

import ctd.util.ServletUtils;

/**
 * 
 *@author zhuzz
 * @类功能说明:springmvc控制缓存表查询分页类
 */
@Controller
public class MDBConroller extends JSONOutputMVCConroller {

	private static final Logger logger = Logger
			.getLogger(MDBConroller.class);
	private static final String ADAPTERDATATABLE = "adapterData";
	private static final String MPIERRORDATATABLE = "mpiErrorData";
	@Autowired
	@Qualifier("mongoDAOService")
	MongoDBDAO mongoDAO;
	
	@Autowired
	@Qualifier("dataService")
	DataProcessService dataProcessService;
	
	/**
	 * 
	 * @方法说明：获取缓存表数据
	 * @param page 页数
	 * @param rows 行数
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "html/getData.action", method = RequestMethod.GET)
	public void getCachesData(@RequestParam(value = "page") Integer page,
			@RequestParam(value = "rows") int rows,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		HashMap<String, Object> resData = new HashMap<String, Object>();
		List<Map<String, Object>> rowsList = new ArrayList<Map<String, Object>>();
		Integer currentPage = page;// 当前页码
		Integer pageSize = rows;// 每页显示数据量
		Integer nextstart = (currentPage-1)*pageSize;
		// 总的记录数
		long pageNum = 0;
		try {
			List<Map<String,Object>> returnList = mongoDAO.getData(currentPage, pageSize, nextstart, ADAPTERDATATABLE);
			pageNum = mongoDAO.getCount(ADAPTERDATATABLE);
			if (!returnList.isEmpty()) {
				rowsList = returnList;
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
	 * @方法说明：获取Mpi错误表数据
	 * @param page 页数
	 * @param rows 行数
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "html/getErrorEtlData.action", method = RequestMethod.GET)
	public void getMpiErrorData(@RequestParam(value = "page") Integer page,
			@RequestParam(value = "rows") int rows,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		HashMap<String, Object> resData = new HashMap<String, Object>();
		List<Map<String, Object>> rowsList = new ArrayList<Map<String, Object>>();
		Integer currentPage = page;// 当前页码
		Integer pageSize = rows;// 每页显示数据量
		Integer nextstart = (currentPage-1)*pageSize;
		// 总的记录数
		long pageNum = 0;
		try {
			List<Map<String,Object>> returnList = mongoDAO.getData(currentPage, pageSize, nextstart, MPIERRORDATATABLE);
			pageNum = mongoDAO.getCount("mpiErrorData");
			if (!returnList.isEmpty()) {
				rowsList = returnList;
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
	 * @方法说明：进行mpi重新索引
	 * @param list 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "html/checkEtlData.action", method = RequestMethod.POST)
	public void checkMpiErrorData(
			@RequestParam(value = "data") List list,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		HashMap<String, Object> resData = new HashMap<String, Object>();
		dataProcessService.dataProcess(list);
		if(Global.getMpiflag()==false || Global.getRpcflag()==false || Global.getHbaseflag()==false || Global.getOracleflag()==false){
			resData.put("success", "201");
		}else{
			resData.put("success", "200");
		}
		boolean gzip = ServletUtils.isAcceptGzip(request);
		jsonOutput(response, resData, gzip);
	}
}
