package com.bsoft.service.impl.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bsoft.dao.monodb.MongoDBDAO;
import com.bsoft.listener.ListenerProcess;
import com.bsoft.service.DataProcessService;
import com.bsoft.util.Global;

/**
 * 
 * @author zhuzz
 * 
 */
public class DataProcessServiceImpl implements DataProcessService{

	private static final Log logger = LogFactory.getLog(DataProcessServiceImpl.class);
	private static MongoDBDAO mongoDAOService;
	private static String collectionName3;
	private static List<ListenerProcess> listenerProcess;
	
	public void setCollectMap(Map<String, String> collectMap) {
		this.collectionName3 = collectMap.get("collectionName3").toString();
	}
	
	public void setListenerProcess(List<ListenerProcess> lp) {
		listenerProcess = lp;
	}

	public void setMongoDAOService(MongoDBDAO mds) {
		mongoDAOService = mds;
	}

	public void dataProcess(List idlist) {
		List<Map<String,Object>> list = mongoDAOService.getData(collectionName3, (ArrayList)idlist);
		if (list.size() == 0) {
				logger.info("没有查到相关数据！");
		} else {
			try {
				Global.setAllTrue();
				for (ListenerProcess listener : listenerProcess) {
					
					// 业务处理之前:对应实现类的初始化工作
					List<Map<String, Object>> l = listener.beforeProcess(list);
					// 业务处理过程
					l = listener.process(l);
					// 业务处理之后:相关资源释放等CLOSE工作
					l = listener.afterProcess(l);
				}
				if(Global.getOracleflag()==true){
					for(Object id:idlist){
						Long aid =Long.parseLong((String) id);
						mongoDAOService.removeInID(aid, collectionName3);
					}
				}
			} catch (Exception e) {
				Global.setHbaseflag(false);
				logger.error("业务处理时发生异常:[" + e.getMessage() + "]");
			}
		}
	}
}
