package wen.liu.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import wen.liu.entity.Const;
import wen.liu.entity.SpideRecord;
import wen.liu.servlet.BasicServlet;

public class FileUtil {
	private static Logger logger=LoggerFactory.getLogger(FileUtil.class);
	public static List<String> readLines(String filepath){
		return readLines(filepath, 1, 0 , false);
	}
	
	public static List<String> readLines(String filepath,int from, int end, boolean desc){
		LinkedList<String> lines=new LinkedList<String>();
		from=from>0?from:1;
		end = end<from?Integer.MAX_VALUE:end;
		if(end>=from){
			BufferedReader br = null;
			try{
				br=new BufferedReader(new InputStreamReader(new FileInputStream(filepath), Const.charset));
				int lineNo=0;
				String line="";
				while((line=br.readLine())!=null && ++lineNo<=end){
					if(lineNo>=from&&lineNo<=end){
						if(desc){
							lines.addFirst(line);
						}else{
							lines.addLast(line);
						}
					}
				}
			}catch(Exception e){
				logger.error("文件："+filepath+" 读取异常", e);
			}finally{
				CloseUtil.close(br);
			}
		}
		return lines;
	}
	
	public static void readRecordFile(Map<String, SpideRecord> recordMap){
		if(recordMap==null){
			return;
		}

		String recordPath=BasicServlet.projectRoot+Const.subPathData+Const.fileNameRecord;
		if(!new File(recordPath).exists()){
			return;
		}
		try {
			List<String> lines = IOUtils.readLines(new FileInputStream(recordPath));
			if(lines!=null&&lines.size()>0){
				for(String line:lines){
					SpideRecord record=JSONObject.parseObject(line, SpideRecord.class);
					recordMap.put(record.getType(), record);
				}
			}
		} catch (Exception e) {
			logger.error("记录文件："+recordPath+" 读取异常", e);
		}
	}
}
