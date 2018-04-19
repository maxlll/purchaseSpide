package wen.liu.spide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



















import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import wen.liu.entity.Const;
import wen.liu.servlet.BasicServlet;
import wen.liu.util.CloseUtil;

public class ConfigManager {
	private static Logger logger = LoggerFactory.getLogger(ConfigManager.class);
	private static String configPath=BasicServlet.projectRoot+Const.subPathConfig+Const.fileNameConfig;
	private static final String prefixRootUrl="rootUrl.";
	private static final String prefixSubPath="subPath.";
	private static Properties config = new Properties();
	private static volatile long lastLoad = 0;
	
	public static String getRootUrl(String type){
		checkReloadConfig();
		return config.getProperty(prefixRootUrl+type);
	}
	
	public static String getSubPath(String type){
		checkReloadConfig();
		return config.getProperty(prefixSubPath+type);
	}
	
	public static String getConfig(String key){
		checkReloadConfig();
		return config.getProperty(key);
	}
	
	public static void checkReloadConfig(){
		long curr = System.currentTimeMillis();
		if((curr-lastLoad)>60*1000){
			synchronized(config){
				if((curr-lastLoad)>60*1000 ){
					InputStream in = null;
					try{
						config.clear();
						in = new FileInputStream(configPath);
						config.load(in);
						lastLoad=System.currentTimeMillis();
					} catch(Exception e){
						logger.error("读取配置文件出错", e);
					} finally {
						CloseUtil.close(in);
					}
				}
			}
		}
	}
	
	private static Map<String, String> pageEditKey=new HashMap<String, String>();
	static{
		pageEditKey.put(Const.configExcelDir, "保存Excel的文件夹路径");
		pageEditKey.put(Const.configInterval, "数据采集间隔时间，单位：分钟");
	}
	public static JSONArray getPageEdits(){
		JSONArray arr = new JSONArray();
		for(String key:pageEditKey.keySet()){
			JSONObject obj=new JSONObject();
			obj.put("name", pageEditKey.get(key));
			obj.put("key", key);
			String value = getConfig(key);
			if(Const.configExcelDir.equals(key)){
				String osname = System.getProperty("os.name").toLowerCase();
				if(osname.startsWith("windows")){
					value = StringUtils.replace(value, "/", "\\");
				}
			}
			obj.put("value", value);
			arr.add(obj);
		}
		return arr;
	}
	
	/**
	 * 修改配置项，并写入文件
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public static void updateConfig(String key, String value) throws Exception{
		synchronized(config){
			if(Const.configExcelDir.equals(key)){
				value = StringUtils.replace(value, "\\", "/");
			}
			config.put(key, value);
			List<String> lines=new ArrayList<String>();
			sortAdd(lines, config);
			
			OutputStream output=null;
			try{
				output=new FileOutputStream(configPath);
				IOUtils.writeLines(lines, "\n", output, Const.charset);
				output.flush();
			}catch(Exception e){
				logger.error("修改配置文件出错", e);
				throw e;
			}finally{
				CloseUtil.close(output);
			}
		}
	}
	
	/**
	 * 按照一定的规则顺序插入
	 * @param lines
	 * @param p
	 */
	private static void sortAdd(List<String> lines, Properties p){
		Map<String, Integer> sortMap=new HashMap<String, Integer>();
		for(Object nameObj : p.keySet()){
			String name=String.valueOf(nameObj);
			String line=name+"="+p.getProperty(name);
			if(name.startsWith(prefixRootUrl) || name.startsWith(prefixSubPath)){
				String type = StringUtils.substringAfter(name, ".");
				if(sortMap.containsKey(type)){
					int index = sortMap.get(type);
					if(name.startsWith(prefixRootUrl)){
						lines.add(index, line);
						indexChange(sortMap, index-1);
					}else{
						if(index+1==lines.size()){
							lines.add(line+"\n");
						}else{
							lines.add(index+1, line+"\n");
							indexChange(sortMap, index);
						}
					}
				}else{
					lines.add(line+(name.startsWith(prefixSubPath)?"\n":""));
					sortMap.put(type, lines.size()-1);
				}
			}else{
				lines.add(0, line+"\n");
				indexChange(sortMap, -1);
			}
		}
		sortMap=null;
	}
	
	private static void indexChange(Map<String, Integer> sortMap, int index){
		for(String key:sortMap.keySet()){
			int vIndex = sortMap.get(key);
			if(vIndex>index){
				sortMap.put(key, vIndex+1);
			}
		}
	}
	
	public static void main(String[] args) {
		String filepath="f:/excel/test.xlsx";
		System.out.println(new File(filepath).exists());
	}
}
