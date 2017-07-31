package com.bsoft.listener;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author wjg
 * 
 */
// 总接口：MPI处理、插入MONGODB、结果插入HDFS、索引关键信息插入HBASE、保存到ORACLE临时表
public interface ListenerProcess {

	// 业务处理之前的工作
	public List<Map<String, Object>> beforeProcess(
			List<Map<String, Object>> list);

	// 业务处理
	public List<Map<String, Object>> process(List<Map<String, Object>> list);

	// 业务处理之后的工作
	public List<Map<String, Object>> afterProcess(List<Map<String, Object>> list);
}