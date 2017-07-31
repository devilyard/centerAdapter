package com.bsoft.dao.hadoop.hdfs.impl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.HarFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.springframework.util.Assert;

import com.bsoft.dao.hadoop.hdfs.HdfsWriter;

public class HdfsTextFileWriter extends AbstractHdfsWriter implements HdfsWriter {

	private FileSystem fileSystem;
	private FSDataOutputStream fsDataOutputStream;

	private volatile String charset = "UTF-8";
	
	public HdfsTextFileWriter(FileSystem fileSystem) {
		Assert.notNull(fileSystem, "Hadoop FileSystem must not be null.");
		this.fileSystem = fileSystem;
	}
	

	@Override
	public void write(String message) throws IOException {
		initializeCounterIfNecessary();
		prepareOutputStream();
		if (this.fsDataOutputStream != null)
			copy(getPayloadAsBytes(message), this.fsDataOutputStream);
	}
	

	private void prepareOutputStream() throws IOException {
		boolean found = false;
		Path name = null;
		
		//TODO improve algorithm
		while (!found) {
			name = new Path(getFileName());
			// If it doesn't exist, create it.  If it exists, return false
			if (getFileSystem().createNewFile(name)) {	
				found = true;
				this.resetBytesWritten();
				this.fsDataOutputStream = this.getFileSystem().append(name);
			}
			else {
				if (this.getBytesWritten() >= getRolloverThresholdInBytes()) {
					close();
					incrementCounter();
				}
				else {
					found = true;
				}
			}
		}
	}
	
	public FileSystem getFileSystem() {
		return this.fileSystem;
	}
	
	/**
	 * Simple not optimized copy
	 */
	public void copy(byte[] in, FSDataOutputStream out) throws IOException {
		Assert.notNull(in, "No input byte array specified");
		Assert.notNull(out, "No OutputStream specified");
		out.write(in);	
		incrementBytesWritten(in.length);
	}

	//TODO note, taken from TcpMessageMapper
	/**
	 * Extracts the payload as a byte array.  
	 * @param message
	 * @return
	 */
	private byte[] getPayloadAsBytes(String message) {
		byte[] bytes = null;
		try {
			bytes = ((String) message).getBytes(this.charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return bytes;
	}

	@Override
	public void close() {
		if (fsDataOutputStream != null) {
			IOUtils.closeStream(fsDataOutputStream);
		}
	}

	//创建目录
	public boolean createDir(String path) throws IllegalArgumentException, IOException{
		return fileSystem.mkdirs(new Path(path));
	}
	
	//判断目录是否存在
	public boolean isExist(String path) throws IllegalArgumentException, IOException{
		return fileSystem.exists(new Path(path));
	}
	
	//遍历指定目录下的文件和文件夹信息
	public void listPath(String localPath,String path,String flag) throws FileNotFoundException, IllegalArgumentException, IOException{
		FileStatus[] fs=fileSystem.listStatus(new Path(path));
		Path[] p=FileUtil.stat2Paths(fs);
		for(Path tp:p){
			if(tp.toString().indexOf(flag)>-1){
				System.out.println(tp.toString());
				String f=tp.toString();
				f=f.replace(".xml", "aaaaaaa.xml");
				append(localPath,f);
				break;
			}
			//获取块的信息
			//BlockLocation[] bl=fileSystem.getFileBlockLocations(tp, 0, 0);
			//for(BlockLocation d:bl){
				//System.out.println(d.getHosts());
			//}			
		}
	}
	
	// 上传本地文件到文件系统
	public void uploadLocalFile2HDFS(String s, String d) throws IOException{
		Path src = new Path(s);
		Path dst = new Path(d);
		fileSystem.copyFromLocalFile(src, dst);
	}


	@Override
	//在指定文件后追加文件，目的是小文件集中在一起，减轻NN服务器的内存压力
	public void append(String localPath, String dstPath) {
		this.fileSystem.getConf().get("dfs.client.block.write.replace-datanode-on-failure.policy","NEVER");
		this.fileSystem.getConf().get("dfs.client.block.write.replace-datanode-on-failure.enable","true");

		InputStream in = null;
		FSDataOutputStream out=null;
		try {			
			//要追加的文件流，inpath为文件
            in = new BufferedInputStream(new FileInputStream(localPath));
            out = fileSystem.append(new Path(dstPath));
            IOUtils.copyBytes(in, out, 4096, true);            

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void har() throws Exception {
		HarFileSystem fs = new HarFileSystem();
		fs.initialize(new URI("har:///user/heipark/20120108_15.har"),
				this.fileSystem.getConf());
		FileStatus[] listStatus = fs.listStatus(new Path("sub_dir"));
		for (FileStatus fileStatus : listStatus) {
			System.out.println(fileStatus.getPath().toString());
		}
	} 
	
	//删除指定的文件
	public void deleteByFileName(String[] fileNames) {
		try {
			for(String fileName:fileNames){
				Path path = new Path(getBasePath(),fileName+".xml");
				boolean flag=fileSystem.delete(path);
				System.out.println("文件删除："+path.toString()+",删除结果："+flag);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
