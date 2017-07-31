package com.bsoft.service;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

import com.bsoft.servlet.HttpDispatcher;

import ctd.dictionary.support.XMLDictionary;
import ctd.net.rpc.Client;
import ctd.util.xml.XMLHelper;

public class GetDataSetXmlFileService {
	private static Log logger = LogFactory.getLog(GetDataSetXmlFileService.class);
	private static String SERVICENAME;
	private static String METHOD;
	private static String PARAMS;
	private static String SSDEVERSION;
	private static String PATHNAME;

	public static void setSSDEVERSION(String sSDEVERSION) {
		SSDEVERSION = sSDEVERSION;
	}

	public void setSERVICENAME(String sERVICENAME) {
		SERVICENAME = sERVICENAME;
	}

	public void setMETHOD(String mETHOD) {
		METHOD = mETHOD;
	}

	public void setPARAMS(String pARAMS) {
		PARAMS = pARAMS;

	}

	public void setPATHNAME(String pATHNAME) {
		PATHNAME = pATHNAME;
		if (SSDEVERSION.equals("2.3")) {
			Document doc = null;
			try {
				XMLDictionary dic = (XMLDictionary) Client.rpcInvoke(
						SERVICENAME, METHOD, new Object[] { PARAMS });
				doc = dic.getDefineDoc();
				File file = new File(PATHNAME);
				XMLHelper.putDocument(new FileOutputStream(file), doc);
				logger.info("获取平台数据集文件成功");
			} catch (Exception e) {
				 logger.info("获取平台数据集文件失败！reason："+e.toString());
			}
		} else {
			Document doc;
			try {
				doc = (Document) Client.rpcInvoke(SERVICENAME, METHOD,
						new Object[] { PARAMS });
				File file = new File(PATHNAME);
				XMLHelper.putDocument(new FileOutputStream(file), doc);
				logger.info("获取平台数据集文件成功");
			} catch (Exception e) {
				logger.info("获取平台数据集文件失败！reason："+e.toString());
			}

		}
	}
}

