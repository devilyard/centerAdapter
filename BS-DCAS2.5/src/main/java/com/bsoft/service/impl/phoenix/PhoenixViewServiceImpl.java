package com.bsoft.service.impl.phoenix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bsoft.dao.monodb.MongoDBDAO;
import com.bsoft.listener.ListenerProcess;
import com.bsoft.service.DataProcessService;
import com.bsoft.service.PhoenixViewService;
import com.bsoft.util.DateTimeUtil;

import ctd.net.rpc.Client;

/**
 * 
 * @author zhuzz
 * 
 */
public class PhoenixViewServiceImpl implements PhoenixViewService {

	private static final Log logger = LogFactory
			.getLog(PhoenixViewServiceImpl.class);
	private static String filePath;
	private static String specialCharacter;
	private static String phoenixViewFileName;
	private static String phoenixSEQFileName;
	private static String SERVICENAME;
	private static String METHOD;
	private static StringBuffer TARGETDATASETSQL;
	private static String filenameTemp;// 文件路径+名称
	
	public static void setSERVICENAME(String sERVICENAME) {
		SERVICENAME = sERVICENAME;
	}

	public static void setMETHOD(String mETHOD) {
		METHOD = mETHOD;
	}
	
	public static String getPhoenixViewFileName() {
		return phoenixViewFileName;
	}

	public static void setPhoenixViewFileName(String phoenixViewFileName) {
		PhoenixViewServiceImpl.phoenixViewFileName = phoenixViewFileName;
	}
	
	public static String getPhoenixSEQFileName() {
		return phoenixSEQFileName;
	}

	public static void setPhoenixSEQFileName(String phoenixSEQFileName) {
		PhoenixViewServiceImpl.phoenixSEQFileName = phoenixSEQFileName;
	}

	public static String getFilePath() {
		return filePath;
	}

	public static void setFilePath(String filePath) {
		PhoenixViewServiceImpl.filePath = filePath;
	}

	public static String getSpecialCharacter() {
		return specialCharacter;
	}

	public static void setSpecialCharacter(String specialCharacter) {
		PhoenixViewServiceImpl.specialCharacter = specialCharacter;
	}

	@Override
	public void phoenixView(List dateSetList) throws Exception {
		HashMap<String, Object> backData = new HashMap<String, Object>();
		String timestamp = DateTimeUtil.getCurrentDateTimeSSSFormatter();
		String sqlViewFileName = phoenixViewFileName + timestamp;
		String sqlSeqFileName = phoenixSEQFileName + timestamp;
		createFile(
				sqlViewFileName,
				new StringBuffer(
						"CREATE VIEW EHRMAIN (PK VARCHAR PRIMARY KEY,RECORD.ROWID UNSIGNED_LONG,RECORD.UPLOADTIME VARCHAR,RECORD.ORIGINALBODY VARCHAR,RECORD.PROCESSFLAG VARCHAR,RECORD.CREATEDATETIME VARCHAR,RECORD.BODY VARCHAR,RECORD.HEADER VARCHAR,RECORD.EVENTID VARCHAR,RECORD.MPIID VARCHAR,RECORD.MPIPROCESSRESULT VARCHAR,DOCINFO.RECORDCLASSIFYING VARCHAR,DOCINFO.RECORDTITLE VARCHAR,DOCINFO.EFFECTIVETIME VARCHAR,DOCINFO.AUTHORORGANIZATION VARCHAR,DOCINFO.SOURCEID VARCHAR,DOCINFO.VERSIONNUMBER VARCHAR,DOCINFO.AUTHORID VARCHAR,DOCINFO.AUTHOR VARCHAR,DOCINFO.SYSTEMTIME VARCHAR,PATIENT.PERSONNAME VARCHAR,PATIENT.SEXCODE VARCHAR,PATIENT.BIRTHDAY VARCHAR,PATIENT.REGISTEREDPERMANENT VARCHAR,PATIENT.ADDRESSTYPECODE VARCHAR,PATIENT.ADDRESS VARCHAR,PATIENT.CERTIFICATENO VARCHAR,PATIENT.CERTIFICATETYPECODE VARCHAR,PATIENT.CARDNO VARCHAR,PATIENT.CARDTYPECODE VARCHAR,PATIENT.CONTACTNO VARCHAR,PATIENT.NATIONALITYCODE VARCHAR,PATIENT.NATIONCODE VARCHAR,PATIENT.RHBLOODCODE VARCHAR,PATIENT.MARITALSTATUSCODE VARCHAR,PATIENT.STARTWORKDATE VARCHAR,PATIENT.WORKCODE VARCHAR,PATIENT.EDUCATIONCODE VARCHAR,PATIENT.INSURANCECODE VARCHAR,PATIENT.IDCARD VARCHAR,PATIENT.INSURANCETYPE VARCHAR);"));
		createFile(
				sqlViewFileName,
				new StringBuffer(
						"CREATE VIEW EHRERROR (PK VARCHAR PRIMARY KEY,RECORD.ROWID UNSIGNED_LONG,RECORD.UPLOADTIME VARCHAR,RECORD.CREATEDATETIME VARCHAR,DOCINFO.RECORDCLASSIFYING VARCHAR,DOCINFO.RECORDTITLE VARCHAR,DOCINFO.EFFECTIVETIME VARCHAR,DOCINFO.AUTHORORGANIZATION VARCHAR,DOCINFO.SOURCEID VARCHAR,DOCINFO.VERSIONNUMBER VARCHAR,DOCINFO.AUTHORID VARCHAR,DOCINFO.AUTHOR VARCHAR,DOCINFO.SYSTEMTIME VARCHAR,PATIENT.PERSONNAME VARCHAR,PATIENT.SEXCODE VARCHAR,PATIENT.BIRTHDAY VARCHAR,PATIENT.REGISTEREDPERMANENT VARCHAR,PATIENT.ADDRESSTYPECODE VARCHAR,PATIENT.ADDRESS VARCHAR,PATIENT.CERTIFICATENO VARCHAR,PATIENT.CERTIFICATETYPECODE VARCHAR,PATIENT.CARDNO VARCHAR,PATIENT.CARDTYPECODE VARCHAR,PATIENT.CONTACTNO VARCHAR,PATIENT.NATIONALITYCODE VARCHAR,PATIENT.NATIONCODE VARCHAR,PATIENT.RHBLOODCODE VARCHAR,PATIENT.MARITALSTATUSCODE VARCHAR,PATIENT.STARTWORKDATE VARCHAR,PATIENT.WORKCODE VARCHAR,PATIENT.IDCARD VARCHAR,PATIENT.EDUCATIONCODE VARCHAR,PATIENT.INSURANCECODE VARCHAR,PATIENT.INSURANCETYPE VARCHAR);"));
		createFile(sqlSeqFileName, new StringBuffer("CREATE SEQUENCE SEQ_EHRMAIN;"));
		createFile(sqlSeqFileName, new StringBuffer("CREATE SEQUENCE SEQ_EHRERROR;"));
		for (int i = 0; i < dateSetList.size(); i++) {
			String dataSetString = (String) dateSetList.get(i);
			StringBuffer combination = new StringBuffer("PK VARCHAR PRIMARY KEY,BODY.ROWID UNSIGNED_LONG");
			backData = (HashMap<String, Object>) Client.rpcInvoke(SERVICENAME,METHOD,new Object[]{dataSetString});
			for (Iterator j = backData.keySet().iterator(); j.hasNext();) {
				String index = (String) j.next();
				String[] tszf = specialCharacter.split(",");
				for (int k = 0; k < tszf.length; k++) {
					if (index.equals(tszf[k])) {
						index = "";
					}
				}
				if(index!=""){
					combination.append(", BODY.").append(index).append(" VARCHAR");
				}
			}
			TARGETDATASETSQL = new StringBuffer("CREATE VIEW ").append(dataSetString.toUpperCase()).append("(").append(combination).append(");");
			createFile(sqlSeqFileName, new StringBuffer("CREATE SEQUENCE SEQ_").append(dataSetString).append(";"));
			createFile(sqlViewFileName, TARGETDATASETSQL);
		}
	}
	
	public static boolean createFile(String fileName,StringBuffer filecontent){
		Boolean bool = false;
		filenameTemp = filePath + fileName + ".sql";// 文件路径+名称+文件类型
		File file = new File(filenameTemp);
		try {
			if (!file.exists()) {
				file.createNewFile();
				bool = true;
				logger.info("success create file,the file is " + filenameTemp);
			}
			// 创建文件成功后，写入内容到文件里
			writeFileContent(filenameTemp, filecontent);
		} catch (Exception e) {
			logger.error(e.toString());
		}

		return bool;
	}
	
	 public static boolean writeFileContent(String filepath, StringBuffer newstr) throws IOException {
	        Boolean bool = false;
	        String filein = newstr+"\r\n";//新写入的行，换行
	        String temp  = "";
	        FileInputStream fis = null;
	        InputStreamReader isr = null;
	        BufferedReader br = null;
	        FileOutputStream fos  = null;
	        PrintWriter pw = null;
	        try {
	            File file = new File(filepath);//文件路径(包括文件名称)
	            //将文件读入输入流
	            fis = new FileInputStream(file);
	            isr = new InputStreamReader(fis);
	            br = new BufferedReader(isr);
	            StringBuffer buffer = new StringBuffer();
	            
	            //文件原有内容
	            for(int i=0;(temp =br.readLine())!=null;i++){
	                buffer.append(temp);
	                // 行与行之间的分隔符 相当于“\n”
	                buffer = buffer.append(System.getProperty("line.separator"));
	            }
	            buffer.append(filein);
	            
	            fos = new FileOutputStream(file);
	            pw = new PrintWriter(fos);
	            pw.write(buffer.toString().toCharArray());
	            pw.flush();
	            bool = true;
	        } catch (Exception e) {
	        	logger.error(e.toString());
	        }finally {
	            //不要忘记关闭
	            if (pw != null) {
	                pw.close();
	            }
	            if (fos != null) {
	                fos.close();
	            }
	            if (br != null) {
	                br.close();
	            }
	            if (isr != null) {
	                isr.close();
	            }
	            if (fis != null) {
	                fis.close();
	            }
	        }
	        return bool;
	    }

}
