package com.bsoft.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;

/**
 * 
 * @author wjg
 * 
 */
public class FileCollector {

	private static final Log logger = LogFactory.getLog(FileCollector.class);

	private static final String HBASETABLEFILE = "hbase-table.xml";

	private static final String DATAFILE = "dataset.xml";

	// <table code="EHR-MAIN" type="T">
	// key为T\F，value为CODE
	private static Map<String, String> hbaseTable = new HashMap<String, String>();

	// 第一个key为表名,value也是个map类型【其中key为列族名，value为对应列的名称】
	private static Map<String, TreeMap<String, List<String>>> columnFamily = new TreeMap<String, TreeMap<String, List<String>>>();

	// 定义RPC中获取的数据集明细集合
	private static Map<String, List<Map<String, Object>>> dataSetMXMap = new HashMap<String, List<Map<String, Object>>>();	

	public static Map<String, List<Map<String, Object>>> getDataSetMXMap() {
		return dataSetMXMap;
	}

	public static Map<String, String> getHbaseTable() {
		return hbaseTable;
	}

	public static Map<String, TreeMap<String, List<String>>> getColumnFamily() {
		return columnFamily;
	}

	// 定义遍历文件的开始点
	private static final String ROOT = "/hbase";

	private File path;

	// bean注入时执行
	// 解析HBASE表操作相关的工作
	public void setFilePath(File filePath) {
		this.path = filePath;
		try {
			File f = new File(filePath, HBASETABLEFILE);
			// 读取XML文件,获得document对象
			SAXReader reader = new SAXReader();
			Document doc = reader.read(f);
			Node root = doc.getRootElement();
			List<?> list = root.selectNodes(ROOT);
			Iterator<?> i = list.iterator();
			Node foo;
			DefaultElement be = null;
			while (i.hasNext()) {
				foo = (Node) i.next();
				be = (DefaultElement) foo;
				List eleList = be.elements();
				for (Iterator<org.dom4j.Element> iter = eleList.iterator(); iter
						.hasNext();) {
					Element element = iter.next();
					String tableName = element.attributeValue("tableName");
					String type = element.attributeValue("type");
					List<?> eList = element.elements();
					TreeMap<String, List<String>> tmap = new TreeMap<String, List<String>>();
					if (!eList.isEmpty()) {
						Iterator<?> it = element.elementIterator();
						while (it.hasNext()) {
							Element element1 = (Element) it.next();
							// 获得列族名
							String cf = element1.attributeValue("code");
							List<String> columns = new ArrayList<String>();
							if (element1.elements().isEmpty()) {
								tmap.put(cf, columns);
								continue;
							} else {
								List<?> lList = element1.elements();
								if (!lList.isEmpty()) {
									Iterator<?> lit = element1
											.elementIterator();
									while (lit.hasNext()) {
										Element element2 = (Element) lit.next();
										String columnName = element2
												.attributeValue("code");
										columns.add(columnName);
									}
								}
								tmap.put(cf, columns);
							}
						}
					}
					columnFamily.put(tableName, tmap);
					hbaseTable.put(type, tableName);
				}
			}
			// 远程数据集明细信息获取
			getDataSetMXInfo();
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	// 远程数据集明细信息获取
	// 需要优化:从平台获取.目前是从本地文件：dataset.xml中获取
	private void getDataSetMXInfo() {
		List<Map<String, Object>> list2011 = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> list2012 = new ArrayList<Map<String, Object>>();
		try {
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new File(path, DATAFILE));
			Element element = doc.getRootElement();
			List nodes1 = element.elements("item");
			for (Iterator one = nodes1.iterator(); one.hasNext();) {
				Element elm1 = (Element) one.next();
				Attribute attr1 = elm1.attribute("DataStandardId");
				if (attr1.getText().equals("V2011")) {
					List nodes2 = elm1.elements("item");
					for (Iterator two = nodes2.iterator(); two.hasNext();) {
						Element elm2 = (Element) two.next();
						List nodes3 = elm2.elements("item");
						if (elm2.elements().size() > 0) {
							Map<String, Object> map = new HashMap<String, Object>();
							for (Iterator three = nodes3.iterator(); three
									.hasNext();) {
								Element elm3 = (Element) three.next();
								Attribute key = elm3
										.attribute("CustomIdentify");
								// System.out.print(key.getText());
								Attribute value = elm3.attribute("text");
								// System.out.print(value.getText());
								map.put(key.getText(), value.getText());
							}
							list2011.add(map);
						}
					}
					dataSetMXMap.put(attr1.getText(), list2011);
				} else if (attr1.getText().equals("V2012")) {
					List nodes2 = elm1.elements("item");
					for (Iterator two = nodes2.iterator(); two.hasNext();) {
						Element elm2 = (Element) two.next();
						List nodes3 = elm2.elements("item");
						if (elm2.elements().size() > 0) {
							Map<String, Object> map = new HashMap<String, Object>();
							for (Iterator three = nodes3.iterator(); three
									.hasNext();) {
								Element elm3 = (Element) three.next();
								Attribute key = elm3
										.attribute("CustomIdentify");
								// System.out.print(key.getText());
								Attribute value = elm3.attribute("text");
								// System.out.print(value.getText());
								map.put(key.getText(), value.getText());
							}
							list2012.add(map);
						}
					}
					dataSetMXMap.put(attr1.getText(), list2012);
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}
}
