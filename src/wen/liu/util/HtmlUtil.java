package wen.liu.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HtmlUtil {
	private static Logger logger=LoggerFactory.getLogger(HtmlUtil.class);
	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run(){
				System.out.println("停止的时候调用");
			}
		});
		long max=Runtime.getRuntime().maxMemory();
		long total=Runtime.getRuntime().totalMemory();
		System.out.println(max/(1024*1024)+" M");
		System.out.println(total);
		System.out.println((max-total)/(1024*1024)+" M");
		long before = Runtime.getRuntime().freeMemory();
		System.out.println(before);
		Runtime.getRuntime().gc();
		long after = Runtime.getRuntime().freeMemory();
		System.out.println(after);
		System.out.println((after-before)/1024+" K");
		System.exit(0);
	}
    
    //根据url从网络获取网页文本
    public static Document getHtmlTextByUrl(String url){
        Document doc = null;
        try { 
        	doc = Jsoup.connect(url).timeout(60000).get(); 
        }catch (Exception e1){ 
        	logger.error("访问异常："+url, e1);
        }
        return doc;
    } 
    
    public static Document getHtmlTextByPost(String url, Map<String, String> dataMap){
        Document doc = null;
        try { 
        	Connection conn = Jsoup.connect(url);
        	conn.timeout(60000);
        	if(dataMap!=null&&dataMap.size()>0){
        		for(String key : dataMap.keySet()){
        			conn.data(key, dataMap.get(key));
        		}
        	}
        	doc = conn.post();
        }catch (Exception e1){ 
        	logger.error("访问异常："+url, e1);
        }
        return doc;
    } 
	
	public static String getAfterSiblingText(Element ele, String parentTagName){
		Element parent=ele.parent();
		int index=ele.siblingIndex();
		while(parent!=null&&!StringUtils.equals(parentTagName, parent.tagName())){
			index=parent.siblingIndex();
			parent=parent.parent();
		}
		StringBuilder txt = new StringBuilder();
		if(parent!=null){
			for(int i=index+1;i<parent.children().size();i++){
				txt.append(parent.child(i).wholeText().trim());
			}
		}
		return txt.toString();
	}
	
	public static String getParentText(Element ele, String parentTagName){
		Element parent=ele.parent();
		while(parent!=null&&!StringUtils.equals(parentTagName, parent.tagName())){
			parent=parent.parent();
		}
		return parent!=null?parent.wholeText().trim():"";
	}

}