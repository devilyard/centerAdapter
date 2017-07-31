package com.bsoft.servlet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class PropertiesUtil {
	private static final Log logger = LogFactory.getLog(PropertiesUtil.class);
	private static String propertiesUrl = "../hadoop.properties";
	private static Properties props = new Properties();
	static {
		try {
			InputStream is = HttpDispatcher.class.getClassLoader().getResourceAsStream(propertiesUrl);
			props.load(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String readValue(String key) {
		String value = props.getProperty(key);
		return value;
	}

	@SuppressWarnings("rawtypes")
	public static void readProperties() {
		try {
			Enumeration en = props.propertyNames();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				String Property = props.getProperty(key);
				logger.info(key + Property);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeProperties(String parameterName,
			String parameterValue) {
		try {
			OutputStream fos = new FileOutputStream(propertiesUrl);
			props.setProperty(parameterName, parameterValue);
			props.store(fos, "Update '" + parameterName + "' value");
		} catch (IOException e) {
			System.err.println("Visit " + propertiesUrl + " for updating "
					+ parameterName + " value error");
		}
	}

}
