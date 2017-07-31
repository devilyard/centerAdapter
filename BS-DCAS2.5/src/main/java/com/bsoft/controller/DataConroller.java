package com.bsoft.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

import com.bsoft.service.PhoenixViewService;
import com.bsoft.util.FileCollector;

import ctd.util.ServletUtils;

/**
 * Phoenix视图构建页面
 *@author zhuzz
 * @类功能说明:
 */
@Controller
public class DataConroller extends JSONOutputMVCConroller {

	private static final Logger logger = Logger.getLogger(DataConroller.class);
	private static Map<String, List<Map<String, Object>>> resDataSetMXMap = new HashMap<String, List<Map<String, Object>>>();
	@Autowired
	@Qualifier("fileCollector")
	FileCollector fileCollector;

	@Autowired
	@Qualifier("phoenixDataSet")
	PhoenixViewService phoenixViewService;

	/**
	 * 
	 * @方法说明：MVC控制层展示数据集
	 * @param version 数据集版本
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "html/dataSet.action", method = RequestMethod.GET)
	public void getCachesData(@RequestParam(value = "version") String version,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		List<Map<String, Object>> list2011 = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> list2012 = new ArrayList<Map<String, Object>>();
		HashMap<String, Object> resData = new HashMap<String, Object>();
		List<Map<String, Object>> rowsList = new ArrayList<Map<String, Object>>();
		try {
			// 处理数据集map
			resDataSetMXMap = fileCollector.getDataSetMXMap();
			if ("all".equals(version)) {
				list2011 = resDataSetMXMap.get("V2011");
				list2012 = resDataSetMXMap.get("V2012");
			}else if("V2011".equals(version)){
				list2011 = resDataSetMXMap.get("V2011");
				list2012 = null;
			}else{
				list2011 = null;
				list2012 = resDataSetMXMap.get("V2012");
			}
			if (list2011 != null) {
				for (int i = 0; i < list2011.size(); i++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map = list2011.get(i);
					for (Iterator j = map.keySet().iterator(); j.hasNext();) {
						   Map<String, Object> targetMap = new HashMap<String, Object>();
						   Object obj = j.next();
						   targetMap.put("VersionNumber", "V2011");
						   targetMap.put("DataSet", obj);
						   targetMap.put("DataSetName", map.get(obj));
						   rowsList.add(targetMap);
					}
				}
			}
			if (list2012 != null) {
				for (int i = 0; i < list2012.size(); i++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map = list2012.get(i);
					for (Iterator j = map.keySet().iterator(); j.hasNext();) {
						   Map<String, Object> targetMap = new HashMap<String, Object>();
						   Object obj = j.next();
						   targetMap.put("VersionNumber", "V2012");
						   targetMap.put("DataSet", obj);
						   targetMap.put("DataSetName", map.get(obj));
						   rowsList.add(targetMap);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			resData.put("total", rowsList.size());
			resData.put("rows", rowsList);
			boolean gzip = ServletUtils.isAcceptGzip(request);
			jsonOutput(response, resData, gzip);
		}
	}
	
	/**
	 * 
	 * @方法说明：MVC控制层生成Phoenix视图SQL语句
	 * @param list 数据集集合
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "html/getDataSet.action", method = RequestMethod.POST)
	public void checkMpiErrorData(
			@RequestParam(value = "data") List list,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		HashMap<String, Object> resData = new HashMap<String, Object>();
		try {
			phoenixViewService.phoenixView(list);
		} catch (Throwable e) {
			logger.error(e.toString());
			resData.put("errorMessage", e.toString());
		}finally{
			boolean gzip = ServletUtils.isAcceptGzip(request);
			jsonOutput(response, resData, gzip);
		}
	}
}
