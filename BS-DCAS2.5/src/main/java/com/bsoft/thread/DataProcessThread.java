package com.bsoft.thread;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.bsoft.dao.monodb.MongoDBDAO;
import com.bsoft.listener.ListenerProcess;

/**
 * 
 * @author WJG
 * 
 */
public class DataProcessThread extends Thread {

	private static final Logger logger = Logger
			.getLogger(DataProcessThread.class);
	private static final long WAITTIME = 1 * 1000 * 15;// 线程休息时间：毫秒
	private static long MAXID = 1;
	private static MongoDBDAO mongoDAOService;
	private static List<ListenerProcess> listenerProcess;

	public void setListenerProcess(List<ListenerProcess> lp) {
		listenerProcess = lp;
	}

	public void setMongoDAOService(MongoDBDAO mds) {
		mongoDAOService = mds;
	}

	// servlet启动时，线程启动的入口程序
	public void run() {
		boolean flag = true;
		while (flag) {
			// 获取mongodb临时表中的数据:按_id顺序取
			List<Map<String, Object>> list = mongoDAOService.getData();
			// 获取最后一个数组内容
			synchronized (this) {
				if (list.size() > 0) {
					Map<String, Object> maxMap = list.get(list.size() - 1);
					MAXID = Long.parseLong(maxMap.get("_id").toString());
				}
			}
			// 当没有记录时，线程休息
			if (list.size() == 0) {
				try {
					logger.info("no data,thread delay:" + WAITTIME + " (MS)");
					Thread.sleep(WAITTIME);
				} catch (InterruptedException e) {
					logger.equals(e.toString());
				}
			} else {
				try {
					// 监听器处理过程:mpi/mongodb/hbase/hdfs/oracle......存储处理
					for (ListenerProcess listener : listenerProcess) {
						// 业务处理之前:对应实现类的初始化工作
						List<Map<String, Object>> l = listener
								.beforeProcess(list);
						// 业务处理过程
						l = listener.process(l);
						// 业务处理之后:相关资源释放等CLOSE工作
						l = listener.afterProcess(l);
					}
					// 删除数据
					mongoDAOService.removeByID(MAXID);
				} catch (Exception e) {
					logger.error("业务处理时发生异常:[" + e.getMessage() + "]");
				}
			}
		}
	}
}
