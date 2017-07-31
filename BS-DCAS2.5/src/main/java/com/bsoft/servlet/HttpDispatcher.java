package com.bsoft.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bsoft.dao.hadoop.phoienix.impl.PhoienixDAOImpl;
import com.bsoft.dao.sqlite.impl.SqliteDAOImpl;
import com.bsoft.thread.DataProcessThread;

import ctd.net.rpc.Client;
import ctd.net.rpc.config.MethodConfig;
import ctd.net.rpc.config.ServiceConfig;
import ctd.net.rpc.server.AbstractServiceDispather;
import ctd.spring.AppDomainContext;
import ctd.util.JSONUtils;
import ctd.util.context.Context;
import ctd.util.context.ContextUtils;
import ctd.util.converter.ConversionUtils;

@SuppressWarnings("unchecked")
public class HttpDispatcher extends HttpServlet {
	
	private static final long serialVersionUID = -8260076694457868119L;
	private static Log logger = LogFactory.getLog(HttpDispatcher.class);

	public void init(ServletConfig cfg) throws ServletException {
		Connection conn = null;
		String zk = PropertiesUtil.readValue("hbase.zk.quorum");
		String url = "jdbc:phoenix:"+zk;
		try {
			Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
			conn = DriverManager.getConnection(url, "", "");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		DataProcessThread p = new DataProcessThread();
		p.start();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		if (request.getContentType().indexOf("application/json") == -1) {
			return;
		}
		int code = 200;
		String msg = "SUCCUESS";
		HashMap<String, Object> res = new HashMap<String, Object>();
		try {
			HashMap<String, Object> req = parseRequest(request);
			String domain = (String) req.get("domain");
			String beanName = (String) req.get("serviceId");
			String methodDesc = (String) req.get("methodDesc");
			String methodName = (String) req.get("methodName");

			if (domain == null) {
				int i = beanName.indexOf(".");
				if (i > 0) {
					domain = beanName.substring(0, i);
				} else {
					domain = AppDomainContext.getName();
					beanName = domain + "." + beanName;
				}
			}

			Context ctx = new Context();
			ctx.put("calldt", System.currentTimeMillis());
			ContextUtils.setContext(ctx);

			Object result = null;
			ServiceConfig sc = (ServiceConfig) AppDomainContext.getBean("&"
					+ beanName);

			if (sc != null) {
				MethodConfig mc = null;
				if (methodDesc != null) {
					mc = sc.getMethodByDesc(methodDesc);
				} else {
					mc = sc.getMethodByName(methodName);
				}
				Class<?>[] types = mc.getParameterTypes();
				Object[] parameters = convertParameters(
						(List<Object>) req.get("body"), types);

				result = AbstractServiceDispather.instance().call(sc, mc,
						parameters);

			} else {
				sc = AppDomainContext.getRegistry().find(beanName);
				MethodConfig mc = null;
				if (methodDesc != null) {
					mc = sc.getMethodByDesc(methodDesc);
				} else {
					mc = sc.getMethodByName(methodName);
				}
				Class<?>[] types = mc.getParameterTypes();
				Object[] parameters = convertParameters(
						(List<Object>) req.get("body"), types);
				result = Client.rpcInvoke(beanName, mc.getName(), parameters);
			}
			res.put("body", result);
		} catch (Exception e) {

			code = 500;
			if (e.getCause() != null) {
				msg = e.getCause().getMessage();
			} else {
				msg = e.getMessage();
			}
			e.printStackTrace();
		}
		res.put("x-response-code", code);
		res.put("x-response-msg", msg);
		try {
			writeToResponse(response, res);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private Object[] convertParameters(List<Object> lsParameters,
			Class<?>[] types) {
		if (lsParameters == null || types == null) {
			return null;
		}
		Object[] parameters = null;
		int n = types.length;
		if (n > 0) {
			parameters = new Object[n];
			for (int i = 0; i < n; i++) {
				parameters[i] = ConversionUtils.convert(lsParameters.get(i),
						types[i]);
			}
		}
		return parameters;
	}

	public static HashMap<String, Object> parseRequest(
			HttpServletRequest request) {
		try {
			String encoding = request.getCharacterEncoding();
			if (encoding == null) {
				encoding = "UTF-8";
			}
			InputStream ins = request.getInputStream();
			return JSONUtils.parse(ins, HashMap.class);
		} catch (Exception e) {
			logger.error("parseRequest failed:", e);
		}
		return new HashMap<String, Object>();
	}

	public static void writeToResponse(HttpServletResponse response,
			HashMap<String, Object> res) throws IOException {
		response.addHeader("content-type", "text/javascript");
		JSONUtils.write(response.getOutputStream(), res);
	}
}
