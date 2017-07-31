package com.bsoft.service.impl.rpc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bsoft.dao.hadoop.phoienix.impl.PhoienixDAOImpl;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ctd.annotation.RpcService;

/**
 * 
 *@author zhuzz
 * @类功能说明:访问原始数据
 */

public class RPCRawDataServiceImpl {
	
	@Autowired
	@Qualifier("phoienixDAOService")
	PhoienixDAOImpl phoienixDao;
	
	@RpcService
	public Map<String, Object> getRawData(String EventId) {
		Map<String, Object> reMap = new HashMap<String, Object>();
		reMap = phoienixDao.getRowData(EventId);
		return reMap;
		
	}
	
	@RpcService
	public String getDocByEventId(String EventId){
		String body = null;
		String bodyXml = phoienixDao.getBodyEventId(EventId);
		if(bodyXml != null){
			try {
				Document document = DocumentHelper.parseText(bodyXml);
				Element rootElement = document.getRootElement();
				body = ((Element)rootElement.elements().get(0)).asXML();
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}
		return body;
	}
}
