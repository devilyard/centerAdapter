package com.bsoft.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;

import com.bsoft.dao.hadoop.phoienix.impl.PhoienixDAOImpl;
import com.bsoft.util.Bytes;

/**
 * 
 * @author wjg
 * 
 */
public class XMLHelper {
	private static final Log logger = LogFactory.getLog(XMLHelper.class);
	private static final String SIMPLEDATE = "yyyy-M-d H:mm:ss";
	private static final String CODING = "GBK";

	// 处理复杂节点信息
	public static Map<String, Object> getMap(Map<String, Object> map) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String key = entry.getKey().toString();
			Object value = entry.getValue();
			if (value instanceof List) {
				List<Map<String, Object>> l = (List<Map<String, Object>>) value;
				if (l.size() >= 1) {
					Map<String, Object> tmp = l.get(0);
					Iterator tit = tmp.entrySet().iterator();
					while (tit.hasNext()) {
						Map.Entry entry1 = (Map.Entry) tit.next();
						String key1 = entry1.getKey().toString();
						Object value1 = entry1.getValue();
						returnMap.put(key1, value1);
					}
				}
			} else {
				returnMap.put(key, value);
			}
		}
		return returnMap;
	}

	// 根据hbase列族对应的key-value，去xml中获取对应的节点信息
	// 第一个key为列族名，第二个key为列族下对应列名和列值
	public static Map<String, Object> getValues(List<String> columnList,
			Map<String, Object> map) {
		// 定义返回的MAP
		Map<String, Object> rmap = new HashMap<String, Object>();
		try {
			// 主表存放的内容
			Map<String, Map<String, Object>> T = new HashMap<String, Map<String, Object>>();
			// 数据集表存放的内容
			Map<String, Map<String, Object>> DT1 = new HashMap<String, Map<String, Object>>();
			// 数据集明细表存放的内容
			Map<String, List<Map<String, Object>>> DT2 = new HashMap<String, List<Map<String, Object>>>();
			// 1、首先从XML中找寻Header节点，并解析
			String xml = map.get("Header").toString();
			// 转换为doc
			Document doc = DocumentHelper.parseText(xml);
			Element element = doc.getRootElement();
			// 获取patient节点
			Element patient = (Element) element
					.selectSingleNode("/Header/Patient");
			// 病人信息转换为map
			Map<String, Object> patientMap = XMLHelper
					.parserXmlPatientInfo(patient);
			T.put("PATIENT", getMap(patientMap));
			// 2、其次从XML中找寻DOCINFO节点，并解析
			xml = map.get("Header").toString();
			Map<String, Object> docInfoMap = parserXmlDocInfo(xml,
					"/Header/DocInfo");
			T.put("DOCINFO", getMap(docInfoMap));
			// 3、再次从map中找寻RECORD信息
			Map<String, Object> recordMap = new HashMap<String, Object>();
			for (String column : columnList) {
				Object value;
				if(column.equals("OriginalBody")||column.equals("Body")){
					value = Bytes.zip(map.get(column).toString().getBytes(CODING));
				}else{
					value = map.get(column);
				}
				recordMap.put(column, value);
			}
			recordMap.put("ProcessFlag", "0");
			T.put("RECORD", recordMap);
			rmap.put("T", T);
			// 4\最后从body需要全部解包放入HBASE
			String RecordClassifying = map.get("RecordClassifying").toString();
			String OriginalBody = map.get("OriginalBody").toString();
			// body信息转换为map
			Map<String, Object> OriginalBodyMap = parserXmlBodyInfo(
					OriginalBody, "/Request/Body/" + RecordClassifying);
			// 如果配置文件中未指定特定解包的ITEM，则默认全部解包存入到HBASE
			Map<String, Object> bodyMap = new HashMap<String, Object>();
			Iterator it = OriginalBodyMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String bodyKey = entry.getKey().toString();
				Object bodyValue = entry.getValue();
				if (bodyValue instanceof List) {
					// 证明是BODY下的子节点,也就是明细表的内容
					List<Map<String, Object>> value = (List<Map<String, Object>>) bodyValue;
					DT2.put(bodyKey, value);
				} else {
					bodyMap.put(bodyKey, bodyValue);
				}
			}
			DT1.put(RecordClassifying.toUpperCase(), bodyMap);
			if (!DT1.isEmpty()) {
				rmap.put("DT1", DT1);
			}
			if (!DT2.isEmpty()) {
				rmap.put("DT2", DT2);
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return rmap;
	}

	// 解析doctor数据
	public static Map<String, Object> parserXmlDocInfo(String docInfo,
			String xpath) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			// 将字符串转为XML
			Document doc = DocumentHelper.parseText(docInfo);
			Node root = doc.getRootElement();
			List<?> list = root.selectNodes(xpath);
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
					String key = element.getName();
					String value = element.getText();
					map.put(key, value);
				}
			}
			if (map.isEmpty()) {
				logger.info("xml中没有这个节点信息：" + xpath);
				logger.info("=======================================");
				logger.info("xml详细信息：" + docInfo);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return map;
	}

	// 解析OriginalBody数据
	public static Map<String, Object> parserXmlBodyInfo(String OriginalBody,
			String xpath) {
		// 存放BODY的信息
		Map<String, Object> map = new HashMap<String, Object>();
		// 存放BODY中还有子节点信息的节点信息
		List<Map<String, Object>> tl = new ArrayList<Map<String, Object>>();
		try {
			// 将字符串转为XML
			Document doc = DocumentHelper.parseText(OriginalBody);
			Node root = doc.getRootElement();
			List<?> list = root.selectNodes(xpath);
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
					// 判断节点是否还有子节点
					if (!element.elements().isEmpty()) {
						Element eee = (Element) element.elements().get(0);
						String MX = eee.getQName().getName();
						// 子节点处理
						List<Map<String, Object>> v = processSubElement(
								element, tl);
						if (v != null && !v.isEmpty()) {
							map.put(MX, v);
						}
					} else {
						String key = element.getName();
						String value = element.getText();
						map.put(key, value);
					}
				}
			}
			if (map.isEmpty()) {
				logger.info("xml中没有这个节点信息：" + xpath);
				logger.info("=======================================");
				logger.info("xml详细信息：" + OriginalBody);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return map;
	}

	// 遍历子元素
	private static List<Map<String, Object>> processSubElement(Element element,
			List<Map<String, Object>> list) {
		if (!element.elements().isEmpty()) {
			Iterator<?> it = element.elementIterator();
			Map<String, Object> map = new HashMap<String, Object>();
			while (it.hasNext()) {
				Element element1 = (Element) it.next();
				if (!element1.elements().isEmpty()) {
					processSubElement(element1, list);
				} else {
					String key = element1.getName();
					String value = element1.getText();
					map.put(key, value);
				}
			}
			if (!map.isEmpty()) {
				list.add(map);
			}
			return list;
		} else {
			return null;
		}
	}

	// 将指定XML节点信息转换为map结构
	public static Map<String, Object> parserXmlPatientInfo(Element patient) {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] fields = new String[] { "PersonName", "Birthday", "SexCode",
				"ContactNo", "RegisteredPermanent", "Address",
				"NationalityCode", "NationCode", "BloodTypeCode",
				"RhBloodCode", "MaritalStatusCode", "StartWorkDate",
				"WorkCode", "EducationCode", "InsuranceCode", "InsuranceType",
				"WorkPlace" };
		for (String fieldName : fields) {
			Element fieldElement = (Element) patient
					.selectSingleNode(fieldName);
			if (fieldElement != null && !"".equals(fieldElement.getText())) {
				String field = fieldName.substring(0, 1).toLowerCase()
						+ fieldName.substring(1);
				map.put(field, fieldElement.getText());
			}
		}
		buildCard(patient, map);
		buildIdCard(patient, map);
		return map;
	}
	
	private static void buildIdCard(Element patient, Map<String, Object> map) {
		Element idCard = (Element) patient.selectSingleNode("IdCard");
		if (idCard == null) {
			return;
		}
		Element idType = (Element) patient.selectSingleNode("IdType");
		if (idType == null || idType.getText().isEmpty()) {
			map.put("idCard", idCard.getText());
		} else {
			Map<String, Object> certificatesmap = new HashMap<String, Object>();
			certificatesmap.put("certificateNo", idCard.getText());
			certificatesmap.put("certificateTypeCode", idType.getText());
			List<Map<String, Object>> certificateslist = new ArrayList<Map<String, Object>>();
			certificateslist.add(certificatesmap);
			map.put("certificates", certificateslist);
		}
	}
	
	private static void buildCard(Element patient, Map<String, Object> map) {
		Element cardNo = (Element) patient.selectSingleNode("CardNo");
		if (cardNo == null || cardNo.getText().isEmpty()) {
			return;
		}
		Element cardType = (Element) patient.selectSingleNode("CardType");
		if (cardType == null || cardType.getText().isEmpty()) {
			map.put("cardNo", cardNo.getText());
		} else {
			Map<String, Object> cardsmap = new HashMap<String, Object>();
			cardsmap.put("cardNo", cardNo.getText());
			cardsmap.put("cardTypeCode", cardType.getText());
			List<Map<String, Object>> cardslist = new ArrayList<Map<String, Object>>();
			cardslist.add(cardsmap);
			map.put("cards", cardslist);
		}
	}
	
	/**
	 * @方法说明：xml格式化
	 * @param xml
	 * @return
	 */
	public static String xmlFormat(String xml) {
		SAXReader reader = new SAXReader();
		StringWriter stringWriter = null;
		XMLWriter writer = null;
		try {
			Document document = reader.read(new StringReader(xml));
			if (document != null) {
				stringWriter = new StringWriter();
				OutputFormat format = OutputFormat.createPrettyPrint();
				format.setEncoding("UTF-8");
				format.setIndent(true);
				writer = new XMLWriter(stringWriter, format);
				writer.write(document);
				writer.flush();
				xml = stringWriter.getBuffer().toString();
			}
		} catch (Exception e) {
			logger.error(PhoienixDAOImpl.class, e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					logger.error(PhoienixDAOImpl.class, e);
				}
			}
		}
		return xml;
	}
}
