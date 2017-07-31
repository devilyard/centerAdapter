package com.bsoft.dao.hadoop.hdfs.impl;

import org.apache.hadoop.fs.FileSystem;
import org.springframework.util.Assert;

import com.bsoft.dao.hadoop.hdfs.HdfsWriter;

public class HdfsTextFileWriterFactory implements HdfsWriterFactory {

	private FileSystem fileSystem;

	public static final String DEFAULT_BASE_FILENAME = "data";
	public static final String DEFAULT_BASE_PATH = "/data/";
	public static final String DEFAULT_FILE_SUFFIX = "log";
	public static long DEFAULT_ROLLOVER_THRESHOLD_IN_BYTES = 10*1024*1024; //10MB
	
	private long rolloverThresholdInBytes = DEFAULT_ROLLOVER_THRESHOLD_IN_BYTES; 

	private String baseFilename = DEFAULT_BASE_FILENAME;
	private String basePath = DEFAULT_BASE_PATH;
	private String fileSuffix = DEFAULT_FILE_SUFFIX;

	public HdfsTextFileWriterFactory(FileSystem fileSystem) {
		Assert.notNull(fileSystem, "Hadoop FileSystem must not be null.");
		this.fileSystem = fileSystem;
		//System.out.println(this.fileSystem.getConf().get("dfs.support.append"));
		//this.fileSystem.getConf().setBoolean("dfs.support.append", true);
		//System.out.println(this.fileSystem.getConf().get("dfs.support.append"));	
		//System.out.println(this.fileSystem.getConf().get("dfs.client.block.write.replace-datanode-on-failure.policy"));
		//System.out.println(this.fileSystem.getConf().get("dfs.client.block.write.replace-datanode-on-failure.enable"));
		//this.fileSystem.getConf().set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");
		//this.fileSystem.getConf().set("dfs.client.block.write.replace-datanode-on-failure.enable", "true");
		//System.out.println(this.fileSystem.getConf().get("dfs.client.block.write.replace-datanode-on-failure.policy"));
		//System.out.println(this.fileSystem.getConf().get("dfs.client.block.write.replace-datanode-on-failure.enable"));
	}

	public String getFileSuffix() {
		return fileSuffix;
	}

	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	public String getBaseFilename() {
		return baseFilename;
	}

	public void setBaseFilename(String baseFilename) {
		this.baseFilename = baseFilename;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	
	public long getRolloverThresholdInBytes() {
		return rolloverThresholdInBytes;
	}

	public void setRolloverThresholdInBytes(long rolloverThresholdInBytes) {
		this.rolloverThresholdInBytes = rolloverThresholdInBytes;
	}
	

	@Override
	public HdfsWriter createWriter() {
		HdfsTextFileWriter textFileWriter = new HdfsTextFileWriter(fileSystem);
		textFileWriter.setBasePath(basePath);
		textFileWriter.setBaseFilename(baseFilename);
		textFileWriter.setFileSuffix(fileSuffix);
		textFileWriter.setRolloverThresholdInBytes(rolloverThresholdInBytes);
		return textFileWriter;
	}

}
