package com.bsoft.service.impl.oracle;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.bsoft.dao.oracle.OracleDAO;
import com.bsoft.listener.ListenerProcess;
import com.bsoft.util.XMLHelper;

/**
 * 
 * @author wjg
 * 
 */
public class OracleServiceImpl implements ListenerProcess {
	private static final Log logger = LogFactory
			.getLog(OracleServiceImpl.class);

	private static final String CODING = "GBK";

	private OracleDAO oracleDAOService;

	public void setOracleDAOService(OracleDAO oracleDAOService) {
		this.oracleDAOService = oracleDAOService;
	}

	@Override
	public List<Map<String, Object>> process(List<Map<String, Object>> list) {
		// 批量插入指定的表
		try {
			List<Map<String, Object>> maplist1 = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> maplist2 = new ArrayList<Map<String, Object>>();
			for(int i=0; i<list.size(); i++){
				String mpiid = (String)list.get(i).get("mpiId");
				String eventid = (String)list.get(i).get("EventId");
				if(mpiid==null || mpiid.length()==0){
					continue;
				}
				if(oracleDAOService.existEventid(eventid)==true){
					maplist1.add(list.get(i));
				}else{
					maplist2.add(list.get(i));
				}
			}
			//诺数据库中已存在eventid，执行更新操作
			if(maplist1!=null && maplist1.size()!=0){
				oracleDAOService.update(maplist1);
			}
			//诺数据库中不存在eventid，执行保存操作
			if(maplist2!=null && maplist2.size()!=0){
				oracleDAOService.save(maplist2);
			}
		} catch (Exception e) {
			logger.error("oracle process exception: " + e.toString());
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
		return list;
	}
}
