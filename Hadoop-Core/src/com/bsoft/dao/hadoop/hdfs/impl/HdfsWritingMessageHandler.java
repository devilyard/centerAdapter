package com.bsoft.dao.hadoop.hdfs.impl;

import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

import com.bsoft.dao.hadoop.hdfs.HdfsWriter;

public class HdfsWritingMessageHandler  implements SmartLifecycle {

	private volatile boolean autoStartup = true;

	private volatile int phase;
	
	protected final Object lifecycleMonitor = new Object();

	private volatile boolean active;
		
	private HdfsWriter hdfsWriter;
	
	private HdfsWriterFactory hdfsWriterFactory;
	
	public HdfsWritingMessageHandler(HdfsWriterFactory hdfsWriterFactory) {
		Assert.notNull(hdfsWriterFactory,
				"HdfsWriterFactory must not be null.");
		this.hdfsWriterFactory = hdfsWriterFactory;
	}
	
	protected void handleMessageInternal(String message) throws Exception {
		doWrite(message);
	}
	
	protected void doWrite(String message) throws Exception {
		try {
			hdfsWriter.write(message);
		} catch (Exception e) {
			throw new Exception("failed to write Message payload to HDFS:"+ e);
		}
	}


	@Override
	public boolean isRunning() {
		return this.active;
	}
	
	@Override
	public void start() {		
		synchronized (this.lifecycleMonitor) {
			this.hdfsWriter = this.hdfsWriterFactory.createWriter();
		}
	}
	
	@Override
	public void stop() {
		synchronized (this.lifecycleMonitor) {
			this.hdfsWriter.close();
		}
	}
	
	@Override
	public void stop(Runnable callback) {
		//TODO
		stop();		
	}
	
	@Override
	public int getPhase() {
		return this.phase;
	}
	
	public void setPhase(int phase) {
		this.phase = phase;
	}
	
	@Override
	public boolean isAutoStartup() {
		return this.autoStartup;
	}
	
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}


}
