package com.bsoft.service.impl.rpc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import wl.zs.utils.UUID;
import com.bsoft.dao.monodb.MongoDBDAO;
import com.bsoft.service.HBaseService;
import com.bsoft.util.Bytes;

import ctd.annotation.RpcService;

/**
 * 
 * @author wjg
 * 
 */

public class RPCServiceImpl {
	private static final Log logger = LogFactory.getLog(RPCServiceImpl.class);
	private MongoDBDAO mongoDao;
	private HBaseService hbaseService;
	private static final String CODING = "GBK";

	public void setHbaseService(HBaseService hbaseService) {
		this.hbaseService = hbaseService;
	}

	public void setMongoDao(MongoDBDAO mongoDao) {
		this.mongoDao = mongoDao;
	}

	@RpcService
	public String uploadOrginalData(List<Map<String, Object>> list) {
		logger.info("前置机成功上传正确数据"+list.size()+"条");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
		for (Map<String, Object> map : list) {
			byte[] ByteOriginalbody =  (byte[]) map.get("OriginalBody");
			byte[] Bytebody =  (byte[]) map.get("Body");
			if(ByteOriginalbody!= null) {
				try {
					String OriginalBody = new String(Bytes.unzip(ByteOriginalbody),CODING);
					map.put("OriginalBody", OriginalBody);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
				}
			}
			if(Bytebody!= null) {
				try {
					String Body = new String(Bytes.unzip(Bytebody),CODING);
					map.put("Body", Body);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
				}
			}
		//map.put("EventId", getEventId());
		map.put("SystemTime", sdf.format(map.get("SystemTime")));
		map.put("UploadTime", sdf.format(map.get("UploadTime")));
		map.put("EffectiveTime", sdf.format(map.get("EffectiveTime")));
		map.put("ZipType", "NoZip");
		}
		//logger.info("中心适配器预处理数据"+list.size()+"条");
		return mongoDao.save(list);
	}
	
	public static String getEventId() {

		String eventIdStr = UUID.randomUUID().toString();
		String eventId = eventIdStr.substring(0, 8)
		+ eventIdStr.substring(9, 13) + eventIdStr.substring(14, 18)
		+ eventIdStr.substring(19, 23) + eventIdStr.substring(24);
		return eventId;
		}

	@RpcService
	public void testSave() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 1; i <= 1; i++) {
			Map<String, Object> m = this.getXML(i);
			list.add(m);
			if (list.size() == 1000) {
				System.out.println("insert data......" + i);
				//this.save(list);
				this.uploadOrginalData(list);
				list.clear();
				System.out.println("success!");
			}
		}
		if (!list.isEmpty()) {
			System.out.println("insert data......");
			//this.save(list);
			this.uploadErrorData(list);
			System.out.println("success!");
		}
	}
	

	// 构造测试数据
	public Map<String, Object> getXML(int i) {
		Map<String, Object> map = new HashMap<String, Object>();

		String Body = "<Body DocFormat=\"bsxml\"> \n"
				+ "    <Opt_Register Name=\"门诊就诊记录表\">\n"
				+ "      <JZLSH de=\"DEX70.07.001.01\">JZLSH</JZLSH>\n"
				+ "      <GHLSH de=\"DEX71.28.001.02\">GHLSH</GHLSH>\n"
				+ "      <SFYY de=\"DEX71.31.001.02\" display=\"\">SFYY</SFYY>\n"
				+ "      <SFFZ de=\"DE05.01.081.00\" display=\"\">SFFZ</SFFZ>\n"
				+ "      <KH de=\"DEX70.01.001.01\">KH</KH>\n"
				+ "      <KLX de=\"DEX70.01.002.01\" display=\"\">KLX</KLX>\n"
				+ "      <MZLB de=\"DEX70.08.001.01\" display=\"\">MZLB</MZLB>\n"
				+ "      <JZKSBM de=\"DE08.10.025.00\" display=\"\">JZKSBM</JZKSBM>\n"
				+ "      <ZDBM de=\"DE05.01.024.00\">ZDBM</ZDBM>\n"
				+ "      <ZDMC de=\"DE05.01.025.00\">ZDMC</ZDMC>\n"
				+ "      <ZS de=\"DE04.01.119.00\">ZS</ZS>\n"
				+ "      <ZZMS de=\"DE04.01.117.00\">ZZMS</ZZMS>\n"
				+ "      <XBS de=\"DE02.10.071.00\">XBS</XBS>\n"
				+ "      <JZRQ de=\"DE06.00.004.00\">JZRQ</JZRQ>\n"
				+ "    </Opt_Register>\n" + "  </Body>";

		String OriginalBody = "<Request> \n"
				+ "  <Header> \n"
				+ "    <DocInfo> \n"
				+ "      <RecordClassifying>Opt_Register</RecordClassifying>  \n"
				+ "      <RecordTitle>Opt_Register</RecordTitle>  \n"
				+ "      <EffectiveTime>2012-05-08T13:03:08</EffectiveTime>  \n"
				+ "      <Authororganization>珠海人民医院</Authororganization>  \n"
				+ "      <SourceID>3999238</SourceID>  \n"
				+ "      <VersionNumber>2011</VersionNumber>  \n"
				+ "      <AuthorID>8888</AuthorID>  \n"
				+ "      <Author>赵欢</Author>  \n"
				+ "      <SystemTime>2012-05-08T13:05:22</SystemTime> \n"
				+ "    </DocInfo>  \n" + "    <Patient> \n"
				+ "      <PersonName>王平"
				+ i
				+ "</PersonName>  \n"
				+ "      <SexCode>1</SexCode>  \n"
				+ "      <Birthday>1963-09-02</Birthday>  \n"
				+ "      <RegisteredPermanent>04</RegisteredPermanent>  \n"
				+ "      <AddressType>02</AddressType>  \n"
				+ "      <Address>珠海</Address>  \n"
				+ "      <IdCard>320219196806167305</IdCard>  \n"
				+ "      <IdType>03</IdType>  \n"
				+ "      <CardNo>1056180</CardNo>  \n"
				+ "      <CardType>02</CardType>  \n"
				+ "      <ContactNo>13387212242</ContactNo>  \n"
				+ "      <NationalityCode>01</NationalityCode>  \n"
				+ "      <NationCode>32</NationCode>  \n"
				+ "      <RhBloodCode>1</RhBloodCode>  \n"
				+ "      <MaritalStatusCode>02</MaritalStatusCode>  \n"
				+ "      <StartWorkDate>1980-03-22</StartWorkDate>  \n"
				+ "      <WorkCode>02</WorkCode>  \n"
				+ "      <EducationCode>03</EducationCode>  \n"
				+ "      <InsuranceCode>03</InsuranceCode>  \n"
				+ "      <InsuranceType>02</InsuranceType>  \n"
				+ "      <WorkPlace>珠海供电局</WorkPlace> \n"
				+ "    </Patient> \n"
				+ "  </Header>  \n"
				+ "  <Body DocFormat=\"bsxml\"> \n"
				+ "    <Opt_Register Name=\"门诊就诊记录表\">\n"
				+ "      <JZLSH de=\"DEX70.07.001.01\">JZLSH</JZLSH>\n"
				+ "      <GHLSH de=\"DEX71.28.001.02\">GHLSH</GHLSH>\n"
				+ "      <SFYY de=\"DEX71.31.001.02\" display=\"\">SFYY</SFYY>\n"
				+ "      <SFFZ de=\"DE05.01.081.00\" display=\"\">SFFZ</SFFZ>\n"
				+ "      <KH de=\"DEX70.01.001.01\">KH</KH>\n"
				+ "      <KLX de=\"DEX70.01.002.01\" display=\"\">KLX</KLX>\n"
				+ "      <MZLB de=\"DEX70.08.001.01\" display=\"\">MZLB</MZLB>\n"
				+ "      <JZKSBM de=\"DE08.10.025.00\" display=\"\">JZKSBM</JZKSBM>\n"
				+ "      <ZDBM de=\"DE05.01.024.00\">ZDBM</ZDBM>\n"
				+ "      <ZDMC de=\"DE05.01.025.00\">ZDMC</ZDMC>\n"
				+ "      <ZS de=\"DE04.01.119.00\">ZS</ZS>\n"
				+ "      <ZZMS de=\"DE04.01.117.00\">ZZMS</ZZMS>\n"
				+ "      <XBS de=\"DE02.10.071.00\">XBS</XBS>\n"
				+ "      <JZRQ de=\"DE06.00.004.00\">JZRQ</JZRQ>\n"
				+ "      <IPT_ADVICEDETAILS>\n"
				+ "      		<IPT_ADVICEDETAIL>\n"
				+ "      				<CFMXHM>14145682</CFMXHM>\n"
				+ "      				<XMBH>15302</XMBH>\n"
				+ "      		</IPT_ADVICEDETAIL>\n"
				+ "      		<IPT_ADVICEDETAIL>\n"
				+ "      				<CFMXHM>14145683</CFMXHM>\n"
				+ "      				<XMBH>15303</XMBH>\n"
				+ "      		</IPT_ADVICEDETAIL>\n"
				+ "      </IPT_ADVICEDETAILS>\n"
				+ "    </Opt_Register>\n"
				+ "  </Body> \n" + "</Request>";

		String Header = "<Header> \n"
				+ "    <DocInfo> \n"
				+ "      <RecordClassifying>Opt_Register</RecordClassifying>  \n"
				+ "      <RecordTitle>Opt_Register</RecordTitle>  \n"
				+ "      <EffectiveTime>2012-05-08T13:03:08</EffectiveTime>  \n"
				+ "      <Authororganization>珠海人民医院</Authororganization>  \n"
				+ "      <SourceID>3999238</SourceID>  \n"
				+ "      <VersionNumber>2011</VersionNumber>  \n"
				+ "      <AuthorID>8888</AuthorID>  \n"
				+ "      <Author>赵欢</Author>  \n"
				+ "      <SystemTime>2012-05-08T13:05:22</SystemTime> \n"
				+ "    </DocInfo>  \n" + "    <Patient> \n"
				+ "      <PersonName>王平"
				+ i
				+ "</PersonName>  \n"
				+ "      <SexCode>1</SexCode>  \n"
				+ "      <Birthday>1963-09-02</Birthday>  \n"
				+ "      <RegisteredPermanent>04</RegisteredPermanent>  \n"
				+ "      <AddressType>02</AddressType>  \n"
				+ "      <Address>珠海</Address>  \n"
				+ "      <IdCard>320219196806167305</IdCard>  \n"
				+ "      <IdType>03</IdType>  \n"
				+ "      <CardNo>3056180</CardNo>  \n"
				+ "      <CardType>03</CardType>  \n"
				+ "      <ContactNo>13387212242</ContactNo>  \n"
				+ "      <NationalityCode>01</NationalityCode>  \n"
				+ "      <NationCode>32</NationCode>  \n"
				+ "      <RhBloodCode>1</RhBloodCode>  \n"
				+ "      <MaritalStatusCode>02</MaritalStatusCode>  \n"
				+ "      <StartWorkDate>1980-03-22</StartWorkDate>  \n"
				+ "      <WorkCode>02</WorkCode>  \n"
				+ "      <EducationCode>03</EducationCode>  \n"
				+ "      <InsuranceCode>03</InsuranceCode>  \n"
				+ "      <InsuranceType>02</InsuranceType>  \n"
				+ "      <WorkPlace>珠海供电局</WorkPlace> \n"
				+ "    </Patient> \n"
				+ "  </Header>";

		map.put("OriginalBody", OriginalBody);
		map.put("Body", Body);
		map.put("Header", Header);
		map.put("RecordClassifying", "Opt_Register");// getRecordClassifing()
		map.put("UploadTime", "2016-11-11 11:11:11");
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replaceAll("-", "");
		map.put("EventId", uuid);
		return map;
	}

	@RpcService
	// 直接入HBASE，不进行缓存处理
	public String uploadErrorData(List<Map<String, Object>> list) {
		logger.info("前置机成功上传错误数据"+list.size()+"条");
		for (Map<String, Object> map : list) {
			byte[] ByteOriginalbody =  (byte[]) map.get("OriginalBody");
			if(ByteOriginalbody!= null) {
				try {
					String OriginalBody = new String(Bytes.unzip(ByteOriginalbody),CODING);
					map.put("OriginalBody", OriginalBody);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
				}
			}
		map.put("ZipType", "NoZip");
		}
		String result = "";
		try {
			hbaseService.saveErrorData(list);
		} catch (Exception e) {
			result = e.toString();
			logger.error(result);
		}
		return result;
	}
	
	@RpcService
	// 获取数据明细:目前暂时只供VIEW使用，但可以通过param进行扩展
	public List<Map<String, Object>> getDetailData(String dataSetCode,
			Map<String, String> param) {
		return hbaseService.getDetailData(dataSetCode, param);
	}
}
