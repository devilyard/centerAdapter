package com.bsoft.dao.hadoop.hdfs.impl;


import com.bsoft.dao.hadoop.hdfs.HdfsWriter;

public interface HdfsWriterFactory {

	HdfsWriter createWriter();
	
}
