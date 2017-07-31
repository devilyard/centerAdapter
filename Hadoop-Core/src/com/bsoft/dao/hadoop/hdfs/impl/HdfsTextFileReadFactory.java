package com.bsoft.dao.hadoop.hdfs.impl;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.data.hadoop.store.DataStoreReader;
import org.springframework.data.hadoop.store.input.TextFileReader;

public class HdfsTextFileReadFactory{

	//private FileSystem fileSystem;

	public static final String DEFAULT_BASE_FILENAME = "data";
	public static final String DEFAULT_BASE_PATH = "/data/";
	public static final String DEFAULT_FILE_SUFFIX = "log";

	private Configuration configuration;
	private String baseFilename = DEFAULT_BASE_FILENAME;
	private String basePath = DEFAULT_BASE_PATH;
	private String fileSuffix = DEFAULT_FILE_SUFFIX;

	public HdfsTextFileReadFactory() {

	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
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


	public String getHdfsContent(String indexfilename) {
		setBaseFilename(indexfilename);
		Path path = new Path(getFileName());
		String returnvalue="";
		//createTestData(path);
		DataStoreReader<String> reader = new TextFileReader(configuration, path, null, null, null);
		try {
			String s="";
			while ((s=reader.read()) != null) {
				returnvalue+=s;
			}
			while ((s=reader.read()) != null) {
				returnvalue+=s;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return returnvalue;
	}

	public String getFileName() {
		return basePath + baseFilename + "." + fileSuffix;
	}
	
	// 遍历所有目录下的内容
	public void listAll(String dir) throws IOException {
		dir="/import/data/deal";
		FileSystem fs = FileSystem.get(configuration);
		FileStatus[] stats = fs.listStatus(new Path(dir));
		System.out.println("总的文件数："+stats.length);
		for (int i = 0; i < stats.length; ++i) {
			if (stats[i].isFile()) {
				// regular file
				System.out.println(stats[i].getPath().toString());
			} else if (stats[i].isDirectory()) {
				// dir
				System.out.println(stats[i].getPath().toString());
			} else if (stats[i].isSymlink()) {
				// is s symlink in linux
				System.out.println(stats[i].getPath().toString());
			}
		}
		fs.close();
	}

}
