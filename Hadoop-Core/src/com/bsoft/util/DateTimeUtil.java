package com.bsoft.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
/**
 * 
 * @author wjg
 *
 */
public class DateTimeUtil {

	// 获取当前时间
	public static String getCurrentDateTime() {
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		java.util.Date currentTime = new java.util.Date();
		String log_date = formatter.format(currentTime);
		return log_date;
	}
	
	//获取当前时间,包括毫秒
	public static String getCurrentDateTimeSSS() {
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss SSS");
		java.util.Date currentTime = new java.util.Date();
		String log_date = formatter.format(currentTime);
		return log_date;
	}
	
	//获取当前时间,包括毫秒
	public static String getCurrentDateTimeSSSFormatter() {
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
				"yyyyMMddHHmmssSSS");
		java.util.Date currentTime = new java.util.Date();
		String log_date = formatter.format(currentTime);
		return log_date;
	}
	
	//格式化时间
	public static String formatDateTime(String dateTime) {
		dateTime = dateTime.replaceAll("-", "");
		dateTime = dateTime.replaceAll(":", "");
		dateTime = dateTime.replaceAll(" ", "");
		return dateTime;
	}
	
	//截取N位前的字符
	public static String getNChar(String dateTime,int n) {
		dateTime =dateTime.substring(0, n);
		return dateTime;
	}
	
	//获取当天日期N天之后的日期
	public static String getAfterNdaysDate(int n) {
		Date date = new Date();// 取时间
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(calendar.DATE, n);// 把日期往后增加一天.整数往后推,负数往前移动
		date = calendar.getTime(); // 这个时间就是日期往后推一天的结果
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
				"yyyyMMdd");
		String log_date = formatter.format(date);
		return log_date;
	}
	
	//获取指定日期N天之后的日期
	public static String getAfterNdaysDateByDay(String strDate, int n) {
		String result = "";
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			Date date = df.parse(strDate);
			long number = 24 * 60 * 60 * 1000;
			long newnumber = date.getTime();
			result = df.format(new Date(newnumber + n * number));
		} catch (Exception e) {
			System.out.println(e);
		}
		return result;
	}
}
