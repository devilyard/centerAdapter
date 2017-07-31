package com.bsoft.service.impl.phoenix;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.MD5Hash;

public class HashChoreWoker {
	//随机取机数目  
    private int baseRecord;  
    //rowkey生成器  
   // private RowKeyGenerator rkGen;  
    //取样时，由取样数目及region数相除所得的数量.  
    private int splitKeysBase;  
    //splitkeys个数  
    private int splitKeysNumber;  
    //由抽样计算出来的splitkeys结果  
    private byte[][] splitKeys;  
  
    public HashChoreWoker(int baseRecord, int prepareRegions) {  
        this.baseRecord = baseRecord;  
       // rkGen = new HashRowKeyGenerator();  
        splitKeysNumber = prepareRegions - 1;  
        splitKeysBase = baseRecord / prepareRegions;  
    }  
  
    public byte[][] calcSplitKeys() {  
        splitKeys = new byte[splitKeysNumber][];  
        TreeSet<byte[]> rows = new TreeSet<byte[]>(Bytes.BYTES_COMPARATOR);  
        for (int i = 0; i < baseRecord; i++) {  
            rows.add(nextId());  
        }  
        int pointer = 0;  
        Iterator<byte[]> rowKeyIter = rows.iterator();  
        int index = 0;  
        while (rowKeyIter.hasNext()) {  
            byte[] tempRow = rowKeyIter.next();  
            rowKeyIter.remove();  
            if ((pointer != 0) && (pointer % splitKeysBase == 0)) {  
                if (index < splitKeysNumber) {  
                    splitKeys[index] = tempRow;  
                    index ++;  
                }  
            }  
            pointer ++;  
        }  
        rows.clear();  
        rows = null;  
        return splitKeys;  
    }
    public static byte [] nextId(){
    	String eventid = getEventId();
    	byte [] rowkey = Bytes.add(MD5Hash.getMD5AsHex(Bytes.toBytes(eventid))  
                .substring(0, 8).getBytes(),Bytes.toBytes(eventid));
    	return rowkey;
    }
    
    public static String getEventId() {
		String eventIdStr = UUID.randomUUID().toString();
		String eventId = eventIdStr.substring(0, 8)
				+ eventIdStr.substring(9, 13) + eventIdStr.substring(14, 18)
				+ eventIdStr.substring(19, 23) + eventIdStr.substring(24);
		return eventId;
	}
}
