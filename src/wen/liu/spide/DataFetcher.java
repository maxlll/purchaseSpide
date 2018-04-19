package wen.liu.spide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import wen.liu.entity.Const;
import wen.liu.entity.ExcelEntity;
import wen.liu.entity.Page;
import wen.liu.entity.PageResult;
import wen.liu.entity.SpideRecord;
import wen.liu.servlet.BasicServlet;
import wen.liu.util.FileUtil;

public class DataFetcher {
	private static Logger logger = LoggerFactory.getLogger(DataFetcher.class);
	
	public static PageResult<String> fetchDatas(String type, Page page){
		int pageSize=page.getPageSize();
		int pageNo=page.getPageNo();
		SpideRecord record=RecordManager.readRecord(type);
		int total=0;
		if(record!=null){
			total=record.getTotal();
			page.setTotal(total);
			int pageCount = total%pageSize==0?total/pageSize:total/pageSize+1;
			page.setPageCount(pageCount);
		}
		int from=pageSize*(pageNo-1)+1;
		int end=pageSize*pageNo;
		if(total>0){
			int tmp=total-from+1;
			from=total-end+1;
			end=tmp;
		}
		String dataFilename = SpiderManager.getSpiderDataFilename(type);
		String filePath=BasicServlet.projectRoot+Const.subPathData+dataFilename;
		List<String> lines=FileUtil.readLines(filePath, from, end, true);
		if(lines.size()==0){
			page.setTotal(0);
			page.setPageCount(0);
		}
		return new PageResult<String>(lines, page);
	}
	
	public static Map<String, List<ExcelEntity>> getAllPageData(int pageSize){
		Map<String, List<ExcelEntity>> dataMap=new HashMap<String, List<ExcelEntity>>();
		Set<String> types=EntityManager.getAllEntityTypes();
		for(String type:types){
			List<ExcelEntity> excelDatas=getPageData(type, pageSize);
			if(excelDatas!=null && excelDatas.size()>0){
				dataMap.put(type, excelDatas);
			}
		}
		return dataMap;
	}
	
	public static List<ExcelEntity> getAllData(String type){
		return transData(FileUtil.readLines(getDataFilePath(type)), type);
	}
	
	public static List<ExcelEntity> getPageData(String type, int pageSize){
		if(pageSize<1){
			return getAllData(type);
		}else{
			SpideRecord record=RecordManager.readRecord(type);
			if(record!=null){
				int end=record.getTotal();
				int from=end-pageSize+1;
				List<String> lines=FileUtil.readLines(getDataFilePath(type), from, end, true);
				return transData(lines, type);
			}
		}
		return null;
	}
	
	public static List<ExcelEntity> transData(List<String> lines, String type){
		if(lines!=null && lines.size()>0){
			List<ExcelEntity> excelEntities = new ArrayList<ExcelEntity>(lines.size());
			Class<? extends ExcelEntity> clazz = EntityManager.getExcelClass(type);
			for(String line:lines){
				try{
					excelEntities.add(JSONObject.parseObject(line, clazz));
				}catch(Exception e){
					logger.error("json转换异常："+line, e);
				}
			}
			return excelEntities;
		}
		return null;
	}
	
	private static String getDataFilePath(String type){
		return BasicServlet.projectRoot+Const.subPathData+SpiderManager.getSpiderDataFilename(type);
	}
}
