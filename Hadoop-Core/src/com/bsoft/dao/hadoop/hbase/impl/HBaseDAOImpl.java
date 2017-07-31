package com.bsoft.dao.hadoop.hbase.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.data.hadoop.hbase.TableCallback;

import com.bsoft.dao.hadoop.hbase.HBaseDAO;

import com.bsoft.util.HBaseUtil;

/**
 * 
 * @author wjg 
 * 
 */
public class HBaseDAOImpl implements HBaseDAO {

	private static final Log logger = LogFactory.getLog(HBaseDAOImpl.class);

	private HbaseTemplate htemplate;

	public void setHtemplate(HbaseTemplate htemplate) {
		this.htemplate = htemplate;
	}

	// 创建表及对应的列族信息
	@Override
	public void createHtable(String htablName, List<String> columenFamily) {
		try {
			HBaseAdmin hBaseAdmin = new HBaseAdmin(htemplate.getConfiguration());
			// 如果存在要创建的表，那么先删除，再创建
		    if (hBaseAdmin.tableExists(htablName)) {
				/*try {  
					hBaseAdmin.disableTable(htablName);  
	            } catch (Exception e) {  
	            }  
				hBaseAdmin.deleteTable(htablName);*/
				return;
			}
			byte [][] splitKeys =null;
			while(true){
				HashChoreWoker worker = new HashChoreWoker(10000,6);  
		        splitKeys = worker.calcSplitKeys();
		        break;
			}
			HTableDescriptor tableDescriptor = new HTableDescriptor(htablName);
			// 注册协作器
			tableDescriptor.addCoprocessor(HBaseUtil.coprocessClassName);
			// 遍历columenFamily
			for (String key : columenFamily) {
				HColumnDescriptor newhcd = new HColumnDescriptor(key);
				// CELL中保存数据的版本数
				newhcd.setMaxVersions(HBaseUtil.VERSION);
				tableDescriptor.addFamily(newhcd);
			}
			//hBaseAdmin.createTable(tableDescriptor);
			hBaseAdmin.createTable(tableDescriptor,splitKeys);
		} catch (MasterNotRunningException e) {
			logger.error(e.toString());
		} catch (ZooKeeperConnectionException e) {
			logger.error(e.toString());
		} catch (IOException e) {
			logger.error(e.toString());
		}
	}
	
	public void testHashAndCreateTable() throws Exception{  
		HashChoreWoker worker = new HashChoreWoker(10000,6);  
        byte [][] splitKeys = worker.calcSplitKeys();
          
        HBaseAdmin admin = new HBaseAdmin(htemplate.getConfiguration());  
        TableName tableName = TableName.valueOf("hash_split_table");  
          
        if (admin.tableExists(tableName)) {  
            try {  
                admin.disableTable(tableName);  
            } catch (Exception e) {  
            }  
            admin.deleteTable(tableName);  
        }  
        HTableDescriptor tableDesc = new HTableDescriptor(tableName);  
        HColumnDescriptor columnDesc = new HColumnDescriptor(Bytes.toBytes("info"));  
        columnDesc.setMaxVersions(1);  
        tableDesc.addFamily(columnDesc);  
        admin.createTable(tableDesc ,splitKeys);  
        admin.close();  
    }
	public  byte[][] calcSplitKeys() {  
        byte[][] splitKeys = new byte[20 - 1][];  
        for(int i = 1; i < 20 ; i ++) {  
            splitKeys[i-1] = Bytes.toBytes((long)i);  
        }  
        return splitKeys;  
    }

	// 插入数据
	@Override
	public void save(String table, final List<Put> list) {
		htemplate.execute(table, new TableCallback<Boolean>() {
			public Boolean doInTable(HTableInterface table) throws Throwable {
				// 默认是开启的,这里设置false，最终通过flushCommits强制刷新到磁盘
				table.setAutoFlush(false);
				// 批量提交
				table.put(list);
				table.flushCommits();
				return true;
			}
		});
	}

	// 获取表里所有数据
	public List<Map<String, Object>> getAllData(String tableName)
			throws Exception {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		HTable table = new HTable(htemplate.getConfiguration(), tableName);
		Scan scan = new Scan();
		ResultScanner rs = table.getScanner(scan);
		for (Result result : rs) {

			List<Cell> ceList = result.listCells();
			Map<String, Object> map = new HashMap<String, Object>();
			// Map<String, Map<String, Object>> returnMap = new HashMap<String,
			// Map<String, Object>>();
			String row = "";
			if (ceList != null && ceList.size() > 0) {
				for (Cell cell : ceList) {
					row = Bytes.toString(cell.getRowArray(), cell
							.getRowOffset(), cell.getRowLength());
					String value = Bytes.toString(cell.getValueArray(), cell
							.getValueOffset(), cell.getValueLength());
					String family = Bytes.toString(cell.getFamilyArray(), cell
							.getFamilyOffset(), cell.getFamilyLength());
					String quali = Bytes.toString(cell.getQualifierArray(),
							cell.getQualifierOffset(), cell
									.getQualifierLength());
					map.put(family + "_" + quali, value);
				}
				map.put("row", row);
				list.add(map);
			}

			// for(KeyValue kv:result.raw())
			// {
			// System.out.println(new String(kv.getRow())+new
			// String(kv.getValue()));
			// }
		}
		return list;
	}

	// 查询数据
	// 通过表名，开始行键和结束行键获取数据
	private List<Map<String, Object>> find(String tableName, String startRow,
			String stopRow) {
		Scan scan = new Scan();
		if (startRow == null) {
			startRow = "";
		}
		if (stopRow == null) {
			stopRow = "";
		}
		scan.setStartRow(Bytes.toBytes(startRow));
		scan.setStopRow(Bytes.toBytes(stopRow));

		/*
		 * PageFilter filter = new PageFilter(5); scan.setFilter(filter);
		 */
		return htemplate.find(tableName, scan,
				new RowMapper<Map<String, Object>>() {
					public Map<String, Object> mapRow(Result result, int rowNum)
							throws Exception {
						List<Cell> ceList = result.listCells();
						Map<String, Object> map = new HashMap<String, Object>();
						// Map<String, Map<String, Object>> returnMap = new
						// HashMap<String, Map<String, Object>>();
						String row = "";
						if (ceList != null && ceList.size() > 0) {
							for (Cell cell : ceList) {
								row = Bytes.toString(cell.getRowArray(), cell
										.getRowOffset(), cell.getRowLength());
								String value = Bytes.toString(cell
										.getValueArray(),
										cell.getValueOffset(), cell
												.getValueLength());
								String family = Bytes.toString(cell
										.getFamilyArray(), cell
										.getFamilyOffset(), cell
										.getFamilyLength());
								String quali = Bytes.toString(cell
										.getQualifierArray(), cell
										.getQualifierOffset(), cell
										.getQualifierLength());
								map.put(family + "_" + quali, value);
							}
							map.put("row", row);
						}
						return map;
					}
				});
	}

	/**
	 * 根据 rowkey删除一条记录
	 * 
	 * @param tablename
	 * @param rowkey
	 */
	public void deleteByRowKey(String tablename, String rowkey) {
		try {
			HTable table = new HTable(htemplate.getConfiguration(), tablename);
			List list = new ArrayList();
			Delete d1 = new Delete(rowkey.getBytes());
			list.add(d1);
			table.delete(list);
			logger.info("删除行成功! rowkey=" + rowkey);
		} catch (IOException e) {
			logger.error(e.toString());
		}
	}

	/**
	 * 通过表名和key获取一行数据
	 * 
	 * @param tableName
	 * @param rowName
	 * @return
	 */
	public Map<String, Object> getDataByRowKey(String tableName,
			String rowkeyName) {
		return htemplate.get(tableName, rowkeyName,
				new RowMapper<Map<String, Object>>() {
					public Map<String, Object> mapRow(Result result, int rowNum)
							throws Exception {
						List<Cell> ceList = result.listCells();
						Map<String, Object> map = new HashMap<String, Object>();
						if (ceList != null && ceList.size() > 0) {
							for (Cell cell : ceList) {
								String columnFamilyName = Bytes.toString(cell
										.getFamilyArray(), cell
										.getFamilyOffset(), cell
										.getFamilyLength());
								// 对应的key
								String ckey = Bytes.toString(cell
										.getQualifierArray(), cell
										.getQualifierOffset(), cell
										.getQualifierLength());
								// 对应的value
								String cvalue = Bytes.toString(cell
										.getValueArray(),
										cell.getValueOffset(), cell
												.getValueLength());
								map.put(ckey, cvalue);
							}
						}
						return map;
					}
				});
	}

	// ////////////////////////////////////////////////////////////////HBASE分页实现////////////////////////////////////////////////////////
	private List<Map<String, Object>> getIndexTableInfo(String tableName,
			String startRow, String endRow, Integer currentPage,
			Integer pageSize) throws IOException {
		HTable table = null;
		try {
			table = new HTable(htemplate.getConfiguration(), tableName);
		} catch (Exception e1) {
			logger.error(e1.toString());
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		ResultScanner scanner = null;
		try {
			// 获取最大返回结果数量
			if (pageSize == null || pageSize == 0L)
				pageSize = 100;
			if (currentPage == null || currentPage == 0)
				currentPage = 1;
			// 计算起始页和结束页
			Integer nowPageSize = pageSize + 1;
			// MUST_PASS_ALL(条件 AND) MUST_PASS_ONE（条件OR）
			FilterList filterList = new FilterList(
					FilterList.Operator.MUST_PASS_ALL);
			Filter filter1 = new PageFilter(nowPageSize);

			// 包含C0000的行建信息内容:相当于全表扫描,不靠谱
			// Filter filter2 = new RowFilter(CompareOp.EQUAL,new
			// RegexStringComparator("A0000"));

			filterList.addFilter(filter1);
			// filterList.addFilter(filter2);

			Scan scan = new Scan();
			scan.setFilter(filterList);
			scan.setMaxResultSize(nowPageSize);
			scan.setCaching(10000);// 设置缓存
			// 从大到小排序，也就是把最大时间放在startRow,最小时间放在stopRow
			scan.setStartRow(Bytes.toBytes(endRow));// endRow
			scan.setStopRow(Bytes.toBytes(startRow));// startRow
			scan.setReversed(true);

			scanner = table.getScanner(scan);
			int i = 1;
			// 遍历扫描器对象， 并将需要查询出来的数据row key取出
			for (Result result : scanner) {
				Map<String, Object> communtiyKeysMap = new TreeMap<String, Object>();
				String row = new String(result.getRow());
				// System.out.println(row);
				// MAP是存放一个cell内的信息
				Map<String, Object> map = new HashMap<String, Object>();
				for (Cell cell : result.rawCells()) {
					if (i == nowPageSize) {
						communtiyKeysMap.put("nextStart", row);
						break;
					}
					// ///////////////////////////////////////////////////////////////////////////////////////////////
					// 获取cell的明细
					// 获取列族名
					// String columnFamilyName = Bytes.toString(cell
					// .getFamilyArray(), cell.getFamilyOffset(), cell
					// .getFamilyLength());
					// 对应的key
					String ckey = Bytes.toString(cell.getQualifierArray(), cell
							.getQualifierOffset(), cell.getQualifierLength());
					// 对应的value
					String cvalue = Bytes.toString(cell.getValueArray(), cell
							.getValueOffset(), cell.getValueLength());
					map.put(ckey, cvalue);
					// map.put(columnFamilyName + ":" + ckey, cvalue);
					// ///////////////////////////////////////////////////////////////////////////////////////////////
				}
				// 结果存储
				communtiyKeysMap.put(row, map);
				list.add(communtiyKeysMap);
				// System.out.println(communtiyKeysMap);
				i++;
			}
		} catch (IOException e) {
			logger.error(e.toString());
		} finally {
			if (scanner != null)
				scanner.close();
		}
		return list;
	}

	// 调阅分页处理入口
	@Override
	public Map<String, Object> getHBaseData(String tableName,String startRow, String endRow,
			Integer currentPage, Integer pageSize) throws IOException {
		Map<String, Object> result = new TreeMap<String, Object>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		// 实际查数据的过程
		List<Map<String, Object>> rlist = getIndexTableInfo(tableName,
				startRow, endRow, currentPage, pageSize);
		// Map<String, Object> communtiyKeysMap = new HashMap<String,Object>();
		// 将treeMap结构转换为List，便于jquery框架页面显示
		for (Map<String, Object> communtiyKeysMap : rlist) {
			for (String communitykey : communtiyKeysMap.keySet()) {
				String rowKeyIndex = communitykey;
				Object cellValue = communtiyKeysMap.get(rowKeyIndex);
				if (communitykey.equals("nextStart")) {
					startRow = (String) cellValue;
					// Map<String,Object> tm=new HashMap<String,Object>();
					// tm.put(communitykey, startRow);
					result.put("nextStart", startRow);
					// System.out.println("next page startRow：" + cellValue);
					// continue; // 下一页进行跳转
				} else {
					Map<String, Object> tmap = (Map<String, Object>) cellValue;
					if (!tmap.isEmpty()) {
						tmap.put("OriginalBodyTxt".toUpperCase(), tmap.get("OriginalBody".toUpperCase()));
						tmap.put("BodyTxt".toUpperCase(), tmap.get("Body".toUpperCase()));
						tmap.put("HeaderTxt".toUpperCase(), tmap.get("Header".toUpperCase()));
						tmap.put("mpiProcessResultTxt".toUpperCase(), tmap
								.get("mpiProcessResult".toUpperCase()));
						tmap.put("rowKeyIndex".toUpperCase(), rowKeyIndex);
					}
					for (String key : tmap.keySet()) {
						String value = "";
						if (key.equals("OriginalBody".toUpperCase())) {
							value = "<FONT COLOR=BLUE><U>OriginalBody</U></FONT>";
							tmap.put(key, value);
						} else if (key.equals("Body".toUpperCase())) {
							value = "<FONT COLOR=BLUE><U>Body</U></FONT>";
							tmap.put(key, value);
						} else if (key.equals("Header".toUpperCase())) {
							value = "<FONT COLOR=BLUE><U>Header</U></FONT>";
							tmap.put(key, value);
						} else if (key.equals("mpiProcessResult".toUpperCase())) {
							String v = tmap.get(key).toString();
							if (!v.equals("")) {
								value = "<FONT COLOR=BLUE><U>MPI处理异常内容</U></FONT>";
							}
							tmap.put(key, value);
						}
					}
					if (!tmap.isEmpty()) {
						list.add((Map<String, Object>) cellValue);
					}
				}
			}
		}
		result.put("list", list);

		// System.out.println("第" + currentPage + "页的数据[每页显示数：'" + pageSize+
		// "'条];每页的明细记录：" + communtiyKeysMap);
		/*
		 * for (String communitykey : communtiyKeysMap.keySet()) { String
		 * rowKeyIndex = communitykey; Object cellValue =
		 * communtiyKeysMap.get(rowKeyIndex); if
		 * (communitykey.equals("nextStart")) { startRow = (String) cellValue;
		 * System.out.println("next page startRow：" + cellValue); System.out
		 * .println(
		 * "--------------------------------------------------------------------------"
		 * ); continue; // 下一页进行跳转 } }
		 */
		return result;
	}

	@Override
	public long getHBaseDataCount(String table, String familyName,
			String startRow, String endRow) {
		AggregationClient aggregationClient = new AggregationClient(
				htemplate.getConfiguration());

		Scan scan = new Scan();
		// MUST_PASS_ALL(条件 AND) MUST_PASS_ONE（条件OR）
		// FilterList filterList = new
		// FilterList(FilterList.Operator.MUST_PASS_ALL);

		// Filter filter1 = new PageFilter(nowPageSize);

		// 包含C0000的行建信息内容
		// Filter filter2 = new RowFilter(CompareOp.EQUAL,new
		// RegexStringComparator("A0000"));
		// filterList.addFilter(filter1);

		// scan.setFilter(filterList);
		// scan.setMaxResultSize(nowPageSize);
		// scan.setCaching(10000);//设置缓存
		// 从大到小排序，也就是把最大时间放在startRow,最小时间放在stopRow
		/*
		 * scan.setStartRow(Bytes.toBytes(startRow));// endRow
		 * scan.setStopRow(Bytes.toBytes(endRow));// startRow
		 */// scan.setReversed(true);

		// 指定扫描列族，唯一值
		byte[] CF = Bytes.toBytes(familyName);
		scan.addFamily(CF);
		long rowCount = 0;
		try {
			rowCount = aggregationClient.rowCount(TableName.valueOf(table),
					new LongColumnInterpreter(), scan);
		} catch (Throwable e) {
			logger.error(e.toString());
		}
		return rowCount;
	}
}
