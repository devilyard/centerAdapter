package com.bsoft.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.MD5Hash;

/**
 * 
 * @author wjg
 * 
 */
public class HBaseUtil {
	// 协调器注册:hbase0.92之后新增加的一个高性能统计表行数的并行计算方式
	public final static String coprocessClassName = "org.apache.hadoop.hbase.coprocessor.AggregateImplementation";

	// cell 数据版本号控制 :主键rowkey相同的条件下，默认保留1000个版本
	public static final int VERSION = 1000;

	// 明细表和数据集表存放时的列族定义：存放XML中BODY解析出来的字段
	public final static String BODY = "BODY";

	//主表和错误表记录的RECORD列族名称
	public final static String RECORD = "RECORD";

	// 每张表,自定义一个自增加的ROWID,类似于MYSQL或则ORACLE的SEQ
	// 目的是为了更快速地进行分页排序
	public final static String ROWID = "ROWID";

	// 构建PUT列表时的标识符
	public final static String HBASEPUT = "HBASEPUT";

	// 设定数据集长度,目的使为了ROWKEY有规律
	public final static int TL = 32;

	public static String getEventID() {
		String s = UUID.randomUUID().toString();
		s = s.replaceAll("-", "");
		return s;
	}

	// 行建设计器
	public static String getRowKey(String eventId, String datetime) {
		String rowKey = "";
		byte[] bytes = Bytes.toBytes(eventId);
		String hashPrefix = MD5Hash.getMD5AsHex(bytes).substring(0, 4);
		// System.out.println(hashPrefix);
		// byte[] bytes2 = Bytes.toBytes(hashPrefix);
		// rowkey取md5(userid)的前四位+userid.前四位用来散列userid,避免写入热点。缺点，不支持顺序scan
		// userId.
		// byte[] rowkey = Bytes.add(bytes2, bytes);
		// System.out.println(rowkey);
		// 可通过rowkey逆推得到 userid
		// System.out.println(Bytes.toLong(rowkey, 4, rowkey.length - 4));
		rowKey = hashPrefix + "-" + datetime;
		return rowKey;
	}

	// 获取简单的行建：也就是EVENTID的值
	public static String getSingleRowKey(Map<String, Object> map) {
		String eventId=map.get("EventId").toString();
		return eventId;
	}

	// 复杂行建设计
	// MPIID#时间#EVENTID
	public static String getComplexRowKey1(Map<String, Object> map) {
		// String RecordClassifying = map.get("RecordClassifying").toString();
		// RecordClassifying = getNewRecordClassifying(RecordClassifying);
		String dateTime = map.get("CreateDateTime").toString();
		dateTime = DateTimeUtil.formatDateTime(dateTime);
		dateTime = DateTimeUtil.getNChar(dateTime, 8);
		String EventId = map.get("EventId").toString();
		String MPIID = getNullMPIID1(map.get("mpiId").toString());
		// String rowKey = RecordClassifying + "|" + dateTime + "|" + EventId;
		String rowKey = MPIID + "#" + dateTime + "#" + EventId;
		return rowKey;
	}

	// 如果MPIID为NULL，则用#来替代，目的是为了保证ROWKEY的定长
	public static String getNullMPIID1(String MPIID) {
		if (MPIID == null || MPIID.equals("")) {
			for (int i = 0; i < TL; i++) {
				// 为什么设定为#代替,因为在ASCII中的排序,#比Aa~Zz|数字之类的都要小
				// 而MPIID是字母+数字的混合体
				MPIID = MPIID + "#";
			}
			return MPIID;
		} else {
			return MPIID;
		}
	}

	public static void main(String[] args) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("RecordClassifying", "Ipt_MedicalRecordPage");
		map.put("CreateDateTime", DateTimeUtil.getCurrentDateTimeSSS());
		map.put("EventId", "EventId");
		getComplexRowKey1(map);
	}

}