package com.bsoft.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.bsoft.listener.ListenerProcess;

/**
 * 
 * @author wjg
 * 
 */
public class MPIProcess implements Callable<List<Map<String, Object>>> {

	private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

	private static ListenerProcess mpiProcessorService;

	public void setMpiProcessorService(ListenerProcess mps) {
		mpiProcessorService = mps;
	}

	public MPIProcess() {

	}

	// 构造函数中进行赋值
	public MPIProcess(Map<String, Object> map) {
		list.add(map);
	}

	public List<Map<String, Object>> call() {
		// MPI处理过程
		List<Map<String, Object>> l = mpiProcessorService.beforeProcess(list);
		l = mpiProcessorService.process(l);
		l = mpiProcessorService.afterProcess(l);
		return l;
	}
}
