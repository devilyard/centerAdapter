package com.bsoft.test;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

public class HBaseTester {
    
    //测试创建表
    @Test
    public void testCreateTable() throws IOException {
        HBaseUtil.createTable("user", "username","password");
    }
    
    //测试插入数据
    @Test
    public void testInsertData() throws IOException {
        HTable htable = HBaseUtil.getHTable("user");
        List<Put> puts = new ArrayList<Put>();;
        Put put1 = HBaseUtil.getPut("1", "username", "name", "name1");
        puts.add(put1);
        Put put2 = HBaseUtil.getPut("1", "password", "pwd", "123");
        puts.add(put2);
        htable.setAutoFlushTo(false);
        htable.put(puts);
        htable.flushCommits();
        htable.close();
    }
    
    //测试获取一行数据
    @Test
    public void testGetRow() throws IOException {
        Result result = HBaseUtil.getResult("user", "2");

        System.out.println("result is: "+ resultToMap(result));

//        byte[] userName = result.getValue("username".getBytes(), null);
//        byte[] password = result.getValue("password".getBytes(), null);
//        System.out.println("username is: "+Bytes.toString(userName)+" passwd is :"+Bytes.toString(password));
    
    }
    
    //测试条件查询
    @Test
    public void testScan() throws IOException {
        ResultScanner rs = HBaseUtil.getResultScanner("user", "password", "pwd", "123","1","10");
        for(Result r = rs.next(); r!=null;r = rs.next()) {
            System.out.println(resultToMap(r));
        }
    }
    
    public static Map<String, Object> resultToMap(Result result){
		List<Cell> ceList = result.listCells();
		Map<String, Object> map = new HashMap<String, Object>();
		if (ceList != null && ceList.size() > 0) {
			for (Cell cell : ceList) {
//				String columnFamilyName = Bytes.toString(cell
//						.getFamilyArray(), cell.getFamilyOffset(), cell
//						.getFamilyLength());
				// 对应的key
				String ckey = Bytes.toString(cell.getQualifierArray(), cell
						.getQualifierOffset(), cell.getQualifierLength());
				// 对应的value
				String cvalue = Bytes.toString(cell.getValueArray(), cell
						.getValueOffset(), cell.getValueLength());
				map.put(ckey, cvalue);	
			}
		}
		return map;
    }
}