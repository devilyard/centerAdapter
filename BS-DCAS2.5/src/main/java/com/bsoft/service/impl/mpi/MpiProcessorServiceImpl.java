package com.bsoft.service.impl.mpi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.bsoft.listener.ListenerProcess;
import com.bsoft.mpi.core.MPIException;
import com.bsoft.util.Global;
import com.bsoft.util.XMLHelper;

import ctd.net.rpc.Client;
import ctd.net.rpc.exception.RpcException;
/**
 * 
 * @author wjg
 * 
 */
public class MpiProcessorServiceImpl implements ListenerProcess {
	private static final Log logger = LogFactory
			.getLog(MpiProcessorServiceImpl.class);

	private static final String KEY = "ZJLIST";
	private String serviceWriter;
	private String serviceProvider;

	public void setServiceWriter(String serviceWriter) {
		this.serviceWriter = serviceWriter;
	}

	public void setServiceProvider(String serviceProvider) {
		this.serviceProvider = serviceProvider;
	}
	
	// MPI处理过程
	@Override
	public List<Map<String, Object>> process(List<Map<String, Object>> Orglist) {
		for (Map<String, Object> tmap : Orglist) {
			String EventId = tmap.get("EventId").toString();
			String mpiProcessFlag = "1";
			String mpiId = "";
			String mpiProcessResult = "";
			try {
				// 病人信息转换为map
				Map<String, Object> patientMap = (Map<String, Object>) tmap
						.get(KEY);
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				list = queryMPI(patientMap);
				if (list == null){
					Map<String, Object> map = new HashMap<String, Object>();
					// 调用MPID接口类
					map = submit(patientMap);
					mpiId = String.valueOf(map.get("mpiId"));
				}else if(list.size() > 1) {
					String idCard = String.valueOf(patientMap.get("idCard"));
					if (idCard != null) {
						for (Map<String, Object> map : list) {
							if (idCard.equals(map.get("idCard"))) {
								mpiId = String.valueOf(map.get("mpiId"));
							}
						}
					}
					if ("".equals(mpiId)) {
						mpiId = list.get(0).get("mpiId").toString();
					}
				} else if (list.size() == 1) {
					mpiId = list.get(0).get("mpiId").toString();
				}
			} catch (RpcException e) {//rpc连接异常
				logger.error("eventId:"+EventId+"--"+e.getMessage());
				mpiProcessResult=e.getMessage().toString();
				mpiProcessFlag = "2";
				Global.setRpcflag(false);
			} catch (MPIException e) {//mpi关联失败
				mpiProcessFlag = "2";
				mpiId = "";
				logger.error("MPI Process failed,eventId:"+EventId+"--"+e.getMessage());
				mpiProcessResult=e.getMessage().toString();
				Global.setMpiflag(false);
			}
			tmap.put("mpiId", mpiId);
			tmap.put("mpiProcessFlag", mpiProcessFlag);
			tmap.put("mpiProcessResult", mpiProcessResult);
		}
		return Orglist;
	}
	
	private Map<String, Object> submit(Map<String, Object> patientMap) throws RpcException, MPIException {
		try {
			Map map = (Map) Client.rpcInvoke(serviceWriter, "submitMPI",  new Object[] { patientMap });
			return map;
		} catch (Exception e) {
			if (e.getMessage().indexOf("all provider server offline.retry again later") > 0
					 || e.getMessage().indexOf("not found on server registry") > 0	) {
				throw new RpcException("all provider server offline.retry again later");
			}
			throw new MPIException(e.getMessage());
		}
	}
	
	private List<Map<String, Object>> queryMPI(Map<String, Object> patientMap) throws RpcException, MPIException {
		try {
			return (List<Map<String, Object>>) Client.rpcInvoke(serviceProvider, "getMPI", new Object[] { patientMap });
		} catch (Exception e) {
			if (e.getMessage().indexOf("all provider server offline.retry again later") > 0
					 || e.getMessage().indexOf("not found on server registry") > 0	) {
				throw new RpcException("all provider server offline.retry again later");
			}
			throw new MPIException(e.getMessage());
		}
	}

	@Override
	// 将KEY值删除
	public List<Map<String, Object>> afterProcess(List<Map<String, Object>> list) {
		for (Map<String, Object> map : list) {
			//logger.info(map.get(KEY));
			map.remove(KEY);
			//logger.info(map.get(KEY));
		}
		return list;
	}

	@Override
	// 解析HEADER信息
	public List<Map<String, Object>> beforeProcess(
			List<Map<String, Object>> Orglist) {
		for (Map<String, Object> tmap : Orglist) {
			try {
				// 获取Header信息
				String headerString = tmap.get("Header").toString();
				// 转换为doc
				Document doc = DocumentHelper.parseText(headerString);
				Element element = doc.getRootElement();
				// 获取patient节点
				Element patient = (Element) element
						.selectSingleNode("/Header/Patient");
				if (patient == null) {
					logger.error("can not find node:[Patient]." + headerString);
				} else {
					Map<String, Object> patientMap = XMLHelper.parserXmlPatientInfo(patient);
					tmap.put(KEY, patientMap);
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return Orglist;
	}
}
