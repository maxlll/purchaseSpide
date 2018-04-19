package wen.liu.spide;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import wen.liu.entity.Const;
import wen.liu.entity.SpideRecord;
import wen.liu.servlet.BasicServlet;
import wen.liu.util.CloseUtil;
import wen.liu.util.FileUtil;

public class RecordManager {
	private static Logger logger = LoggerFactory.getLogger(RecordManager.class);
	private static String recordFilePath=BasicServlet.projectRoot+Const.subPathData+Const.fileNameRecord;
	private static Map<String, SpideRecord> recordMap = new HashMap<String, SpideRecord>();
	private static volatile int finishNum=0;
	private static volatile int cancelNum=0;
	private static volatile int saveNum=0;
	
	public static synchronized SpideRecord readRecord(String type){
		if(recordMap.size()==0){
			FileUtil.readRecordFile(recordMap);
		}
		return recordMap.get(type);
	}
	
	public static synchronized void saveRecord(final SpideRecord record){
		String type=record.getType();
		recordMap.put(type, record);
		finishNum++;
		if(finishNum==(saveNum-cancelNum)){
			OutputStream bos = null;
			try{
				bos = new FileOutputStream(recordFilePath);
				IOUtils.writeLines(recordMap.values(), "\n", bos, Const.charset);
				bos.flush();
				finishNum=0;
				cancelNum=0;
				saveNum=0;
			} catch(Exception e){
				logger.error("保存记录文件出错", e);
			} finally{
				CloseUtil.close(bos);
			}
		}
	}
	
	public static synchronized void cancelSave(){
		cancelNum++;
	}
	
	public static synchronized void addSave(){
		saveNum++;
	}
}
