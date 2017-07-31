package com.bsoft.service.impl.hbase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.bsoft.dao.hadoop.hbase.HBaseDAO;
import com.bsoft.dao.hadoop.phoienix.PhoienixDAO;
import com.bsoft.listener.ListenerProcess;
import com.bsoft.service.HBaseService;
import com.bsoft.util.DateTimeUtil;
import com.bsoft.util.FileCollector;
import com.bsoft.util.HBaseUtil;
import com.bsoft.util.XMLHelper;

/**
 * 
 *@author wjg
 * @类功能说明:
 */
public class HBaseServiceImpl implements ListenerProcess, HBaseService {

	private static final Log logger = LogFactory.getLog(HBaseServiceImpl.class);

	// 保存批处理的putlist结果：数据集名称表记录。
	private ThreadLocal<Map<String, List<Put>>> EHRPutList = new ThreadLocal<Map<String, List<Put>>>();

	private HBaseDAO hbaseDAOService;
	private PhoienixDAO phoienixDAOService;

	public void setPhoienixDAOService(PhoienixDAO phoienixDAOService) {
		this.phoienixDAOService = phoienixDAOService;
	}

	public void setHbaseDAOService(HBaseDAO hbaseDAOService) {
		this.hbaseDAOService = hbaseDAOService;
	}

	FileCollector fileCollector;

	// 创建hbase表:表的信息来源于XML文件
	public void setFileCollector(FileCollector fileCollector) {
		logger
				.info("====================init hbase tables......=============================");
		this.fileCollector = fileCollector;
		Map<String, String> hbaseTable = fileCollector.getHbaseTable();
		Map<String, TreeMap<String, List<String>>> columnFamily = fileCollector
				.getColumnFamily();
		Iterator it = hbaseTable.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String tableName = entry.getValue().toString();
			// 获得列族名
			Map<String, List<String>> cf = columnFamily.get(tableName);
			Iterator cfit = cf.entrySet().iterator();
			List<String> TColumenFamily = new ArrayList();
			while (cfit.hasNext()) {
				Map.Entry cfentry = (Map.Entry) cfit.next();
				String cfNames = cfentry.getKey().toString();
				TColumenFamily.add(cfNames);
			}
			// 创建的表名采用全部大写
			hbaseDAOService.createHtable(tableName.toUpperCase(),
					TColumenFamily);
		}
		this.createHtable();
		logger
				.info("====================init hbase tables completed!=============================");
	}

	// 创建HBASE表：表的信息来源于平台的数据集信息
	// 如果平台的RPC服务异常，则从本地获取
	private void createHtable() {
		List<String> cf = new ArrayList<String>();
		cf.add(HBaseUtil.BODY);
		Map<String, List<Map<String, Object>>> map = fileCollector
				.getDataSetMXMap();
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			List<Map<String, Object>> list = (List<Map<String, Object>>) entry
					.getValue();
			for (Map<String, Object> m : list) {
				Iterator it1 = m.entrySet().iterator();
				while (it1.hasNext()) {
					Map.Entry entry1 = (Map.Entry) it1.next();
					String tableName = entry1.getKey().toString();
					// 创建的表名采用全部大写
					hbaseDAOService.createHtable(tableName.toUpperCase(), cf);
				}
			}
		}
	}

	// 释放资源,清空缓存
	@Override
	public List<Map<String, Object>> afterProcess(List<Map<String, Object>> list) {
		Map<String, List<Put>> ptList = (Map<String, List<Put>>) EHRPutList
				.get();
		ptList.clear();
		EHRPutList.set(null);
		return list;
	}

	@Override
	public List<Map<String, Object>> process(List<Map<String, Object>> list) {
		logger.info("正在向hbase中批量插入数据......");
		// 插入数据
		for (Map<String, Object> map : list) {
			// HBASE非常重要的设计:rowkey
			// 行建设计不合理，会拖垮整个hbase集群
			// 也不利于快速检索
			// 添加入库时间
			map.put("CreateDateTime", DateTimeUtil.getCurrentDateTime());
			// 行键设计
			String rowkey = HBaseUtil.getSingleRowKey(map);
			Map<String, Object> vmap = (Map<String, Object>) map
					.get(HBaseUtil.HBASEPUT);
			// 批量构建hbase的put列表:整个实现类中最核心的过程方法
			buildPut(rowkey, vmap);
		}
		// 真正向HBASE插入数据
		this.save();
		logger.info("hbase中插入数据过程结束!");
		return list;
	}

	// 插入数据
	private void save() {
		// 真正向HBASE插入数据
		Map<String, List<Put>> map = EHRPutList.get();
		Iterator cf = map.entrySet().iterator();
		while (cf.hasNext()) {
			Map.Entry cfEntry = (Map.Entry) cf.next();
			String table = cfEntry.getKey().toString();
			//待完善，由于无法获取完整的数据集信息，所以需在此创建表
			List<String> list1 = new ArrayList<String>();
			list1.add(HBaseUtil.BODY);
			hbaseDAOService.createHtable(table, list1);
			//
			List<Put> list = (List<Put>) cfEntry.getValue();
			if (list == null || list.size() == 0) {

			} else {
				hbaseDAOService.save(table, list);
			}
		}
	}

	/**
	 * 构建PUT列表:Vmap中包含了HBASE库所需要的任何信息 ----- 整个实现类中最核心的过程方法
	 * 
	 * @param rowkey
	 * @param columenFamily
	 * @param vmap
	 */
	private void buildPut(final String rowkey, Map<String, Object> vmap) {
		// 获取主表信息
		String mainTable = fileCollector.getHbaseTable().get("T").toString()
				.toUpperCase();
		// 所有PUT过程都存放在ThreadLocal中
		Map<String, List<Put>> ptList = (Map<String, List<Put>>) EHRPutList
				.get();
		// 定义行建
		byte[] rk = rowkey.getBytes();
		// 指定时间戳
		long timestamp = System.currentTimeMillis();

		// 1、首先构建主表的PUTLIST
		// 获取存储T表对应PUT信息的集合
		List<Put> mainTablePutList = ptList.get(mainTable);
		if (mainTablePutList == null) {
			// 初次时，开辟存储空间
			mainTablePutList = new ArrayList<Put>();
			ptList.put(mainTable, mainTablePutList);
		}
		Put mainPut = new Put(rk, timestamp);
		Map<String, Map<String, Object>> mainMap = (Map<String, Map<String, Object>>) vmap
				.get("T");
		Iterator it1 = mainMap.entrySet().iterator();
		while (it1.hasNext()) {
			Map.Entry cfEntry1 = (Map.Entry) it1.next();
			// 强转大写
			// 列族名
			String familyName = cfEntry1.getKey().toString().toUpperCase();
			// 对应列族下的列的结构信息
			Map<String, Object> fv = (Map<String, Object>) cfEntry1.getValue();
			Iterator itfv = fv.entrySet().iterator();
			while (itfv.hasNext()) {
				Map.Entry itfvEntry1 = (Map.Entry) itfv.next();
				String column = itfvEntry1.getKey().toString().toUpperCase();
				Object columnValue = itfvEntry1.getValue();
				String columnValueStr = "";
				if(column.equals("ORIGINALBODY")||column.equals("BODY")){
					mainPut.add(Bytes.toBytes(familyName), Bytes.toBytes(column),
							(byte[]) columnValue);
				}else{
					if (columnValue == null) {
						columnValueStr = "";
					} else {
						columnValueStr = columnValue.toString();
					}
					mainPut.add(Bytes.toBytes(familyName), Bytes.toBytes(column),
							Bytes.toBytes(columnValueStr.toString()));
				}
			}
			// 强行添加ROWID
			if (familyName.equals("RECORD")) {
				Long seq = Long.valueOf(phoienixDAOService.getSEQ(mainTable));
				mainPut.add(Bytes.toBytes(familyName), Bytes
						.toBytes(HBaseUtil.ROWID), Bytes.toBytes(seq));
			}
		}
		// 添加主表的PUT结果
		mainTablePutList.add(mainPut);

		// 2、其次构建主数据集表的PUTLIST
		Put dt1Put = new Put(rk, timestamp);
		Map<String, Map<String, Object>> dt1Map = (Map<String, Map<String, Object>>) vmap
				.get("DT1");
		String dataname = null;
		Iterator itdt1 = dt1Map.entrySet().iterator();
		while (itdt1.hasNext()) {
			Map.Entry dtEntry1 = (Map.Entry) itdt1.next();
			// 强转大写
			// 列族名
			String dataSetCode = dtEntry1.getKey().toString().toUpperCase();
			// 开辟存储空间
			List<Put> dt1PutList = ptList.get(dataSetCode);
			if (dt1PutList == null) {
				dt1PutList = new ArrayList<Put>();
				ptList.put(dataSetCode, dt1PutList);
			}
			// 对应列族下的列的结构信息
			Map<String, Object> fv = (Map<String, Object>) dtEntry1.getValue();
			Iterator itfv1 = fv.entrySet().iterator();
			while (itfv1.hasNext()) {
				Map.Entry itfvEntry1 = (Map.Entry) itfv1.next();
				String column = itfvEntry1.getKey().toString().toUpperCase();
				Object columnValue = itfvEntry1.getValue();
				String columnValueStr = "";
				if (columnValue == null) {
					columnValueStr = "";
				} else {
					columnValueStr = columnValue.toString();
				}
				dt1Put.add(Bytes.toBytes(HBaseUtil.BODY),
						Bytes.toBytes(column), Bytes.toBytes(columnValueStr
								.toString()));
			}
			Long seq1 = Long.valueOf(phoienixDAOService.getSEQ(dataSetCode));
			dataname = dataSetCode;
			dt1Put.add(Bytes.toBytes(HBaseUtil.BODY), Bytes
					.toBytes(HBaseUtil.ROWID), Bytes.toBytes(seq1));
			// 添加主数据集表的PUT结果
			dt1PutList.add(dt1Put);
		}

		// 3、最后构建明细数据集表的PUTLIST
		Map<String, List<Map<String, Object>>> dt2Map = (Map<String, List<Map<String, Object>>>) vmap
				.get("DT2");
		if (dt2Map != null) {
			Iterator itdt2 = dt2Map.entrySet().iterator();
			while (itdt2.hasNext()) {
				Map.Entry dtEntry2 = (Map.Entry) itdt2.next();
				// 强转大写
				// 列族名
				String dataSetCode = dtEntry2.getKey().toString().toUpperCase();
				// 开辟存储空间
				List<Put> dt2PutList = ptList.get(dataSetCode);
				if (dt2PutList == null) {
					dt2PutList = new ArrayList<Put>();
					ptList.put(dataSetCode, dt2PutList);
				}
				// 对应列族下的列的结构信息
				List<Map<String, Object>> fvList = (List<Map<String, Object>>) dtEntry2
						.getValue();
				// 遍历
				for (Map<String, Object> mmm : fvList) {
					// MX表的PUT定义
					Put dt2Put = new Put(rk);
					Iterator cfc = mmm.entrySet().iterator();
					while (cfc.hasNext()) {
						Map.Entry cfcEntry = (Map.Entry) cfc.next();
						// 列族下所有column都设置为大写
						String column = cfcEntry.getKey().toString().toUpperCase();
						Object columnValue = cfcEntry.getValue();
						String columnValueStr = "";
						if (columnValue == null) {
							columnValueStr = "";
						} else {
							columnValueStr = columnValue.toString();
						}
						dt2Put.add(Bytes.toBytes(HBaseUtil.BODY), Bytes
								.toBytes(column), Bytes.toBytes(columnValueStr
								.toString()));
					}
					Long seq2 = Long.valueOf(phoienixDAOService.getSEQ(dataname));
					dt2Put.add(Bytes.toBytes(HBaseUtil.BODY), Bytes
							.toBytes(HBaseUtil.ROWID), Bytes.toBytes(seq2));
					dt2PutList.add(dt2Put);
				}
			}
		}
	}

	@Override
	public List<Map<String, Object>> beforeProcess(
			List<Map<String, Object>> list) {
		// 获取本地线程变量并强制转换为List<Put>类型:保存全局表数据
		Map<String, List<Put>> ptList = (Map<String, List<Put>>) EHRPutList
				.get();
		// 线程首次执行此方法时
		if (ptList == null) {
			// 创建一个putList对象，并保存到本地线程变量ThreadLocal中：保存数据集主表数据
			ptList = new HashMap<String, List<Put>>();
			EHRPutList.set(ptList);
		}
		// 获取正确表的信息
		Map<String, List<String>> cfMap = fileCollector.getColumnFamily().get(
				fileCollector.getHbaseTable().get("T").toString());
		List<String> columnList = cfMap.get("RECORD");
		// XML业务处理,解析相应的数据
		for (Map<String, Object> map : list) {
			// 从XML中获取所有节点信息
			Map<String, Object> vmap = XMLHelper.getValues(columnList, map);
			map.put(HBaseUtil.HBASEPUT, vmap);
		}
		return list;
	}

	@Override
	// 保存错误数据
	public void saveErrorData(List<Map<String, Object>> list) {
		// 获取错误表的信息
		String tableName = fileCollector.getHbaseTable().get("F").toString();
		Map<String, List<String>> cfMap = fileCollector.getColumnFamily().get(
				tableName);
		List<String> columnList = cfMap.get("RECORD");
		// 存放插入结果集
		List<Put> putList = new ArrayList<Put>();
		for (Map<String, Object> map : list) {
			// HBASE非常重要的设计:rowkey
			// 行建设计不合理，会拖垮整个hbase集群
			// 也不利于快速检索
			// 添加入库时间
			map.put("CreateDateTime", DateTimeUtil.getCurrentDateTime());
			map.put("mpiId", "");
			// 行键设计
			String rowkey = HBaseUtil.getSingleRowKey(map);
			// 从XML中获取所有节点信息
			Map<String, Object> vmap = XMLHelper.getValues(columnList, map);
			Map<String, Object> patientMap = (Map<String, Object>) ((Map<String, Object>) vmap
					.get("T")).get("PATIENT");
			Map<String, Object> docinfoMap = (Map<String, Object>) ((Map<String, Object>) vmap
					.get("T")).get("DOCINFO");
			Map<String, Object> recordMap = (Map<String, Object>) ((Map<String, Object>) vmap
					.get("T")).get("RECORD");
			Map<String, Map<String, Object>> m = new HashMap<String, Map<String, Object>>();
			m.put("PATIENT", patientMap);
			m.put("DOCINFO", docinfoMap);
			m.put("RECORD", recordMap);
			// 批量构建hbase的Error数据的put列表
			buildPut(rowkey, m, putList,tableName);
		}
		// 真正向HBASE插入数据
		hbaseDAOService.save(tableName, putList);
		logger.info("向hbase错误表中插入数据结束!");
	}

	/**
	 * 
	 * @方法说明：构建错误数据批量插入的PUT列表
	 * @param rowkey
	 * @param map
	 * @param putList
	 * @param tableName
	 * @return
	 */
	private List<Put> buildPut(final String rowkey,
			Map<String, Map<String, Object>> map, List<Put> putList, String tableName) {
		// 定义行建
		byte[] rk = rowkey.getBytes();
		// 指定时间戳
		long timestamp = System.currentTimeMillis();
		Put put = new Put(rk, timestamp);
		// 首先遍历patientMap
		Iterator pm = map.entrySet().iterator();
		while (pm.hasNext()) {
			Map.Entry pmEntry = (Map.Entry) pm.next();
			// 强转大写
			String cf = pmEntry.getKey().toString().toUpperCase();
			Map<String, Object> object = (Map<String, Object>) pmEntry
					.getValue();
			// 遍历
			Iterator pm1 = object.entrySet().iterator();
			while (pm1.hasNext()) {
				Map.Entry pmEntry1 = (Map.Entry) pm1.next();
				// 强转大写
				String column = pmEntry1.getKey().toString().toUpperCase();
				Object value = pmEntry1.getValue();
				if (column.equals("ORIGINALBODY")) {
					put.add(Bytes.toBytes(cf), Bytes.toBytes(column),
							(byte[])value);
				} else {
					if (value == null) {
						value = "";
					} // 数据集表存储
					put.add(Bytes.toBytes(cf), Bytes.toBytes(column),
							Bytes.toBytes(value.toString()));
				}
			}
		}
		// 在RECORD列族下强行添加ROWID
		Long seq = Long.valueOf(phoienixDAOService.getSEQ(tableName));
		put.add(Bytes.toBytes(HBaseUtil.RECORD),
				Bytes.toBytes(HBaseUtil.ROWID), Bytes.toBytes(seq));
		putList.add(put);
		return putList;
	}

	@Override
	public List<Map<String, Object>> getDetailData(String dataSetCode,
			Map<String, String> param) {
		return phoienixDAOService.getDetailData(dataSetCode, param);
	}
}
