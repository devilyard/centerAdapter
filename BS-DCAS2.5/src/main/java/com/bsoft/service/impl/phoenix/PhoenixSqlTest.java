package com.bsoft.service.impl.phoenix;



import java.util.UUID;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.MD5Hash;


public class PhoenixSqlTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			testHashAndCreateTable();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

		/*String url = "jdbc:phoenix:10.10.2.155:2181";  
	    String driver = "org.apache.phoenix.jdbc.PhoenixDriver";  
	    Connection connection = null;  
        try {  
            Class.forName(driver);  
            connection = DriverManager.getConnection(url);  
            System.out.println("-----------Connect HBase success..--------------");  
        } catch (ClassNotFoundException e) {  
            e.printStackTrace();  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }
        String sql = "select * FROM EHRMAIN";  
        ResultSet resultSet = null;  
        try {  
            Statement statement = connection.createStatement();  
            resultSet = statement.executeQuery(sql);
            List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
            
			// 遍历游标
			ResultSetMetaData rsmd = resultSet.getMetaData();
			int columnCount = rsmd.getColumnCount();
			// 输出列名
			while (resultSet != null && resultSet.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				for (int i = 1; i <= columnCount; i++) {
					String key = rsmd.getColumnName(i);
					String value = resultSet.getString(i);
					map.put(key, value);
				}
				list.add(map);
				System.out.println(map.size());
				
			}
            while (resultSet != null && resultSet.next()) {  
                System.out.println("stuid: " + resultSet.getString(1) + "\tname: " + resultSet.getString(2)  
                 + "\tage: " + resultSet.getString(3) + "\tscore: " + resultSet.getString(4) + "\tclassid: " + resultSet.getString(5));  
            }  
        } catch (SQLException e) {  
            e.printStackTrace();  
        }

	} */
	
	public static void testHashAndCreateTable() throws Exception{  
		HashChoreWoker worker = new HashChoreWoker(100,3);  
        byte [][] splitKeys = worker.calcSplitKeys();
          
        HBaseAdmin admin = new HBaseAdmin(HBaseConfiguration.create());  
        TableName tableName = TableName.valueOf("hash_split_table1");  
          
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
/*	public static   byte[][] calcSplitKeys() {  
        byte[][] splitKeys = new byte[20 - 1][];  
        for(int i = 1; i < 20 ; i ++) {  
            splitKeys[i-1] = Bytes.toBytes((long)i);  
        }  
        return splitKeys;  
    }*/
	
	
}
