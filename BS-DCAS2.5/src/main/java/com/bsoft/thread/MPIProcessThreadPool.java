package com.bsoft.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bsoft.listener.ListenerProcess;

/**
 * 
 * @author wjg
 * 
 */
public class MPIProcessThreadPool implements ListenerProcess {
	private static final Log logger = LogFactory
			.getLog(MPIProcessThreadPool.class);	

	// 定义线程池容量
	private static final int NUMBER = 10;

	//提取的数据进行MPI多线程处理
	@Override
	public List<Map<String, Object>> process(List<Map<String, Object>> list) {
		// 创建容量为NUMBER的线程池。
		ExecutorService pool = Executors.newFixedThreadPool(NUMBER);
		// 返回结果
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		try {
			// 定义线程运行结果存储
			List<Future<List<Map<String, Object>>>> futures = new ArrayList<Future<List<Map<String, Object>>>>();
			// MPI多线程使用
			for (Map<String, Object> m : list) {
				try {
					MPIProcess dpt = new MPIProcess(m);
					Future<List<Map<String, Object>>> f = pool.submit(dpt);
					futures.add(f);
				} catch (Exception e) {
					logger.error(e.toString());
				}
			}
			for (Future<List<Map<String, Object>>> f : futures) {
				try {
					// if(f.isDone())
					List<Map<String, Object>> rList = f.get();
					for (Map<String, Object> tm : rList) {
						l.add(tm);
					}
				} catch (Exception e) {
					logger.error(e.toString());
				}
			}
		} catch (Exception e) {
			logger.error("MPI Process thread appear exception:["
					+ e.getMessage() + "]");
		} finally {
			pool.shutdown();
		}
		return l;
	}

	@Override
	public List<Map<String, Object>> afterProcess(List<Map<String, Object>> list) {
		// TODO Auto-generated method stub
		return list;
	}

	@Override
	public List<Map<String, Object>> beforeProcess(
			List<Map<String, Object>> list) {
		// TODO Auto-generated method stub
		return list;
	}
}
