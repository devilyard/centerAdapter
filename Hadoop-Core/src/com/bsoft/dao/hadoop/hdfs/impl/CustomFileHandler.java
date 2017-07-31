package com.bsoft.dao.hadoop.hdfs.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

public class CustomFileHandler {

	private static final Log log = LogFactory.getLog(CustomFileHandler.class);

	public File handleFile(File input) {
		log.info("Copying file: " + input.getAbsolutePath());
		return input;
	}
}
