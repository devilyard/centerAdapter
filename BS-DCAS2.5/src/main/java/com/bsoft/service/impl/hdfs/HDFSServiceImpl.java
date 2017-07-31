package com.bsoft.service.impl.hdfs;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bsoft.dao.hadoop.hdfs.impl.HdfsTextFileReadFactory;
import com.bsoft.dao.hadoop.hdfs.impl.HdfsTextFileWriter;
import com.bsoft.dao.hadoop.hdfs.impl.HdfsWriterFactory;
import com.bsoft.listener.ListenerProcess;
import com.bsoft.service.HDFSService;

/**
 * 
 * @author wjg
 * 
 */
public class HDFSServiceImpl implements ListenerProcess, HDFSService {
	private static final Log logger = LogFactory.getLog(HDFSServiceImpl.class);

	private HdfsTextFileReadFactory hdfsTextFileReadFactory;
	private HdfsWriterFactory hdfsWriterFactory;
	private String orignalPath;
	private String dealPath;

	public void setOrignalPath(String orignalPath) {
		this.orignalPath = orignalPath;
	}

	public void setDealPath(String dealPath) {
		this.dealPath = dealPath;
	}

	public HdfsWriterFactory getHdfsWriterFactory() {
		return hdfsWriterFactory;
	}

	public void setHdfsWriterFactory(HdfsWriterFactory hdfsWriterFactory) {
		this.hdfsWriterFactory = hdfsWriterFactory;
	}

	public HdfsTextFileReadFactory getHdfsTextFileReadFactory() {
		return hdfsTextFileReadFactory;
	}

	public void setHdfsTextFileReadFactory(
			HdfsTextFileReadFactory hdfsTextFileReadFactory) {
		this.hdfsTextFileReadFactory = hdfsTextFileReadFactory;
	}

	// @RpcService
	// public String getContentByEventID(String fileName) throws Exception {
	// this.getAllFilesByDirName(getHdfsTextFileReadFactory().getBasePath());
	// String value = getHdfsTextFileReadFactory().getHdfsContent(fileName);
	// return value;
	// }

	// // 罗列某目录下所有的文件
	// private void getAllFilesByDirName(String path) {
	// try {
	// getHdfsTextFileReadFactory().listAll(path);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	// @RpcService
	// public String deleteByFileName(String title) throws Exception {
	// HdfsTextFileWriter hdfsWriterTrans = (HdfsTextFileWriter)
	// getHdfsWriterFactory()
	// .createWriter();
	// String[] s = title.split(",");
	// hdfsWriterTrans.deleteByFileName(s);
	// return null;
	// }

	@Override
	public List<Map<String, Object>> process(List<Map<String, Object>> list) {
		// 原始数据保存
		saveOriginal(list);
		// 处理后数据保存
		saveDeal(list);
		return list;
	}

	@Override
	public List<Map<String, Object>> saveDeal(List<Map<String, Object>> list) {
		HdfsTextFileWriter hdfsWriterTrans = (HdfsTextFileWriter) getHdfsWriterFactory()
				.createWriter();
		try {
			for (Map<String, Object> map : list) {
				String Header = map.get("Header").toString();
				String Body = map.get("Body").toString();
				String deal = Header + Body;
				// 添加转换后的blob
				hdfsWriterTrans.setBaseFilename(map.get("EventId").toString());
				hdfsWriterTrans.setBasePath(dealPath);
				hdfsWriterTrans.write(deal);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			hdfsWriterTrans.close();
			return list;
		}
	}

	@Override
	public List<Map<String, Object>> saveOriginal(List<Map<String, Object>> list) {
		HdfsTextFileWriter hdfsWriterOriginal = (HdfsTextFileWriter) getHdfsWriterFactory()
				.createWriter();
		try {
			for (Map<String, Object> map : list) {
				String Header = map.get("Header").toString();
				String originalBody = map.get("OriginalBody").toString();
				String originalXML = Header + originalBody;
				// 添加原始blob文件
				hdfsWriterOriginal.setBaseFilename(map.get("EventId")
						.toString());
				hdfsWriterOriginal.setBasePath(orignalPath);
				hdfsWriterOriginal.write(originalXML);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			hdfsWriterOriginal.close();
			return list;
		}
	}

	// 随机获取RecordClassifying名称
	public String getRecordClassifing() {
		java.util.Random random = new java.util.Random();// 定义随机类
		int result = random.nextInt(21);// 返回[0,10)集合中的整数，注意不包括10
		result = result + 1; // +1后，[0,10)集合变为[1,11)集合，满足要求
		String[] RecordClassifying = new String[] { "Opt_Register",
				"Opt_Record", "Opt_Recipe", "Opt_Fee", "Ipt_Record",
				"Ipt_AdmissionNote", "Ipt_Advice", "Ipt_SignsRecord",
				"Ipt_LeaveRecord", "Ipt_MedicalRecordPage", "Ipt_Fee",
				"Ipt_Fee_detail", "Pt_LabReport", "Pt_ExamReport",
				"Pt_Operation", "Pt_Diagnosis", "Pt_Transfusion", "Pt_Allergy",
				"Pt_MedicineRely", "Cu_Register", "Pt_DrugCategory",
				"Pt_SurgicalGrade" };
		String r = "";
		r = RecordClassifying[result];
		return r;
	}

	@Override
	public List<Map<String, Object>> afterProcess(List<Map<String, Object>> list) {
		// TODO Auto-generated method stub
		return list;
	}

	@Override
	public List<Map<String, Object>> beforeProcess(
			List<Map<String, Object>> list) {
		// TODO Auto-generated method stub
		return list;
	}
}
