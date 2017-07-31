package com.bsoft.service;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author wjg
 *
 */
public interface HDFSService {
	
	//保存处理后的XML文件
    List<Map<String,Object>> saveOriginal(List<Map<String,Object>> list);
    
    //保存原始的XML文件
    List<Map<String,Object>> saveDeal(List<Map<String,Object>> list);

    //@RpcService
    //String getContentByEventID(String EventID) throws Exception;
    
    //@RpcService
    //String deleteByFileName(String title) throws Exception;  
    
}
