package com.bsoft.dao.hadoop.hdfs;


import java.io.IOException;

public interface HdfsWriter {

	void write(String message) throws IOException;
	
	void append(String localPath, String dstPath);
	
	void close();
}
