package wen.liu.spide;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import wen.liu.annotation.ExcelData;
import wen.liu.entity.ExcelEntity;
import wen.liu.servlet.BasicServlet;

public class EntityManager {
	private static Map<String, Class<? extends ExcelEntity>> excelMap=new HashMap<>();
	private static final String classPath = "WEB-INF/classes/";
	private static final String packageName = "wen.liu.entity";
	
	public static Class<? extends ExcelEntity> getExcelClass(String type){
		return excelMap.get(type);
	}
	
	public static Set<String> getAllEntityTypes(){
		return excelMap.keySet();
	}

	@SuppressWarnings("unchecked")
	public static void loadExcelEntity() {
		String parentPath = BasicServlet.projectRoot+classPath+StringUtils.replace(packageName, ".", "/");
		File parent = new File(parentPath);
		if(!parent.exists()){
			return;
		}
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for(String filename : parent.list()){
			if(!filename.endsWith(".class")){
				continue;
			}
			try {
				Class<?> clazz = classLoader.loadClass(packageName+"."+StringUtils.substringBeforeLast(filename, "."));
				ExcelData excelData=clazz.getDeclaredAnnotation(ExcelData.class);
				if(excelData!=null){
					String type=excelData.type();
					excelMap.put(type, (Class<ExcelEntity>)clazz);
				}
			} catch (Exception e) {
			}
		}
	}
}
