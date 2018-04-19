package wen.liu.spide;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wen.liu.annotation.Crawler;
import wen.liu.entity.Const;
import wen.liu.servlet.BasicServlet;

public class SpiderManager {
	private static Logger logger=LoggerFactory.getLogger(SpiderManager.class);
	private static final String classPath = "WEB-INF/classes/";
	private static final String packageName = "wen.liu.spide.impl";
	private static Map<String, Spider> threadMap = new HashMap<String, Spider>();
	private static Thread execute = null;
	
	public static String getSpiderDataFilename(String type){
		if(threadMap.containsKey(type)){
			return threadMap.get(type).getDataFileName();
		}
		return "";
	}
	
	public static Set<String> getAllSpiderType(){
		return threadMap.keySet();
	}
	
	public synchronized static void startSpiders(){
		if(execute==null || !execute.isAlive()){
			execute = new Thread(Const.tNameSpiderManager){
				@Override
				public void run(){
					while(true){
						loadSpider();
						try {
							Thread.sleep(4*60*1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			execute.start();
		}
	}
	
	private static void loadSpider() {
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
				Crawler crawler = clazz.getDeclaredAnnotation(Crawler.class);
				if(crawler!=null && Spider.class.isAssignableFrom(clazz)){
					String key = crawler.key();
					Spider oldSpider = threadMap.get(key);
					if(oldSpider!=null && clazz!=oldSpider.getClass()){
						Crawler oldCrawler = oldSpider.getClass().getDeclaredAnnotation(Crawler.class);
						int oVersion = oldCrawler.version();
						int version = crawler.version();
						if(version>oVersion){
							Spider spider = (Spider)clazz.newInstance();
							spider.setName(key);
							threadMap.put(key, spider);
							if(oldSpider.isAlive()){
								oldSpider.replaceBy(spider);
							}else{
								spider.start();
							}
						}
					}else if(oldSpider==null || !oldSpider.isAlive()){
						Spider spider = (Spider)clazz.newInstance();
						threadMap.put(key, spider);
						spider.setName(key);
						spider.start();
					}
				}
			} catch (Exception e) {
				logger.error("加载Spider时出现异常", e);
			}
		}
	}
	
	public static int spiderNum(){
		return threadMap.size();
	}
}
