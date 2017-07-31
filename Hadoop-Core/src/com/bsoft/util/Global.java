package com.bsoft.util;

public class Global {

	private static boolean rpcflag;
	private static boolean mpiflag;
	private static boolean mongoflag;
	private static boolean hbaseflag;
	private static boolean hadoopflag;
	private static boolean oracleflag;
	
	private static boolean sqlflag;

	public static boolean getSqlflag() {
		return sqlflag;
	}

	public static void setSqlflag(boolean sqlflag) {
		Global.sqlflag = sqlflag;
	}

	public static void setRpcflag(boolean rpcflag) {
		Global.rpcflag = rpcflag;
	}

	public static boolean getRpcflag() {
		return rpcflag;
	}
	
	public static void setMpiflag(boolean mpiflag) {
		Global.mpiflag = mpiflag;
	}

	public static boolean getMpiflag() {
		return mpiflag;
	}
	
	public static void setMongoflag(boolean mongoflag) {
		Global.mongoflag = mongoflag;
	}

	public static boolean getMongoflag() {
		return mongoflag;
	}
	
	public static void setHbaseflag(boolean hbaseflag) {
		Global.hbaseflag = hbaseflag;
	}

	public static boolean getHbaseflag() {
		return hbaseflag;
	}

	public static void setHadoopflag(boolean hadoopflag) {
		Global.hadoopflag = hadoopflag;
	}

	public static boolean getHadoopflag() {
		return hadoopflag;
	}
	
	public static void setOracleflag(boolean oracleflag) {
		Global.oracleflag = oracleflag;
	}

	public static boolean getOracleflag() {
		return oracleflag;
	}
	
	public static void setAllTrue(){
		Global.rpcflag = true;
		Global.mpiflag = true;
		Global.mongoflag = true;
		Global.hbaseflag = true;
		Global.hadoopflag = true;
		Global.oracleflag = true;
	}
}
