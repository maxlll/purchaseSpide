package wen.liu.spide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wen.liu.entity.Const;
import wen.liu.entity.ResultFlag;
import wen.liu.entity.SpideData;
import wen.liu.entity.SpideRecord;
import wen.liu.servlet.BasicServlet;
import wen.liu.util.HtmlUtil;
import wen.liu.util.POIUtil;

public abstract class Spider extends Thread implements Replacable<Spider> {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	protected int days=5;
	protected static final int defaultInterval = 5;
	public static final String format="yyyy-MM-dd";
	private volatile Spider replacer;
	
	@Override
	public void replaceBy(Spider newSpider){
		if(newSpider!=null){
			replacer = newSpider;
		}
	}
	
	@Override
	public void run(){
		while(true){
			long begin = System.currentTimeMillis();
			String type = getType();
			try{
				//1 读取记录文件
				SpideRecord spideRecord = RecordManager.readRecord(type);
				if(spideRecord==null){
					spideRecord = new SpideRecord(type);
				}
				RecordManager.addSave();
				//2 爬取数据
				List<SpideData> datas = crawl(ConfigManager.getRootUrl(type), ConfigManager.getSubPath(type), spideRecord);
				if(datas.size()>0){
					try{
						//3 保存数据
						saveCrawlData(datas, spideRecord!=null, type);
						//4 保存记录数据
						RecordManager.saveRecord(spideRecord);
//						logger.info("本次采集到 "+type+" 类的更新数据 "+datas.size()+" 条，并已保存到文件");
					}catch(Exception e){
					}
				}else{
					RecordManager.cancelSave();
//					logger.info("本次未采集到 "+type+" 类更新数据");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			long end = System.currentTimeMillis();
			
			if(replacer==null){
				try {
					Calendar ca=Calendar.getInstance();
					ca.setTime(new Date());
					int hour=ca.get(Calendar.HOUR_OF_DAY);
					int weekDay=ca.get(Calendar.DAY_OF_WEEK);
					long realInterval=1000*60*60*8;
					if(weekDay==Calendar.SATURDAY || weekDay==Calendar.SUNDAY){
						realInterval=1000*60*60*56;
					}
					if(hour>7){
						long intervalTime = getInterval()*60*1000;
						long spendTime = end-begin;
						realInterval = intervalTime-spendTime;
						if(realInterval<0){
							logger.warn("本次爬取耗时：{} 秒，超过间隔时间：{} 秒", spendTime/1000, intervalTime/1000);
							realInterval = 2*60*1000;
						}
					}
					Thread.sleep(realInterval);
				} catch (InterruptedException e) {
				}
			}
			if(replacer!=null){
				logger.warn("类替换：{} --> {}", this.getClass().getSimpleName(), replacer.getClass().getSimpleName());
				replacer.start();
				break;
			}
		}
	}

	protected List<SpideData> crawl(String root, String subUrl, SpideRecord spideRecord){
		root=StringUtils.isBlank(root)?getWebRoot():root;
		subUrl=StringUtils.isBlank(subUrl)?getSubPath():subUrl;
		String lastDate="";
		int total=0;
		if(spideRecord==null||spideRecord.getTotal()==0){
			Date curDate = new Date();
			Calendar ca=Calendar.getInstance();
			ca.setTime(curDate);
			ca.add(Calendar.DAY_OF_YEAR, -getKeepDays());
			SimpleDateFormat formater = new SimpleDateFormat(format);
			lastDate=formater.format(ca.getTime());
		}else{
			lastDate=spideRecord.getLastDate();
			total=spideRecord.getTotal();
		}
		
		List<SpideData> notices = new ArrayList<SpideData>();
		Map<String,String> excludeMap=new HashMap<String,String>();
		excludeMap.put(lastDate, spideRecord.getExcludes());
		
		String maxDate = crawl(root, subUrl, notices, lastDate, excludeMap);
		spideRecord.setLastDate(maxDate);
		spideRecord.setTotal(notices.size()+total);
		spideRecord.setExcludes(excludeMap.get(maxDate));
		return notices;
	}
	
	protected String crawl(String rootUrl, String subUrl, List<SpideData> notices, String lastDate, Map<String,String> excludeMap){
		String url = "";
		if(!rootUrl.endsWith("/") && !subUrl.startsWith("/")){
			url=rootUrl+"/"+subUrl;
		}else if(rootUrl.endsWith("/") && subUrl.startsWith("/")){
			url=rootUrl+subUrl.substring(1);
		}else{
			url=rootUrl+subUrl;
		}
		Document doc = HtmlUtil.getHtmlTextByUrl(url);
		Element mainDiv = getMainElement(doc);
		if(mainDiv==null){
			return lastDate;
		}
		Element table = getKeyElement(mainDiv);
		if(table==null){
			return lastDate;
		}
		ResultFlag resultFlag=crawlListPage(rootUrl, table, notices, excludeMap, lastDate);
		if(resultFlag.isResume()){
			String nextPageUrl = getNextPageUrl(mainDiv);
			if(StringUtils.isNotBlank(nextPageUrl)){
				crawl(rootUrl, nextPageUrl, notices, lastDate, excludeMap);
			}
		}
		return resultFlag.getMaxDate();
	}
	
	protected String getNextPageUrl(Element mainDiv){
		Element nextPage=mainDiv.getElementsContainingOwnText("下一页").first();
		String nextPageUrl = "";
		if(nextPage!=null){
			nextPageUrl = nextPage.attr("href");
		}
		return nextPageUrl;
	}
	
	protected void saveCrawlData(List<SpideData> datas, boolean append, String type) throws Exception{
		try {
			Collections.sort(datas);
			String excelDir = ConfigManager.getConfig(Const.configExcelDir);
			POIUtil.outExcelEntity(datas, excelDir, Const.periodMonth, true);
			
			String outpath=BasicServlet.projectRoot+Const.subPathData+getDataFileName();
			synchronized(outpath.intern()){
				File dataFile = new File(outpath);
				if(!dataFile.exists()){
					try {
						dataFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				try {
					IOUtils.writeLines(datas, "\n", new FileOutputStream(outpath, append), Const.charset);
				} catch (Exception e) {
					logger.error("保存数据出错", e);
					throw e;
				} finally{
					dataFile=null;
				}
			}
		} catch (Exception e) {
			logger.error("保存数据到Excel出错", e);
			throw e;
		}
	}
	
	protected abstract String getType();
	protected abstract String getWebRoot();
	protected abstract String getSubPath();
	protected abstract String getDataFileName();
	protected abstract SpideData newInstance();
	protected abstract void crawlDetail(Document detailDoc, SpideData data);
	
	protected ResultFlag crawlListPage(String rootUrl, Element table, List<SpideData> notices, Map<String,String> excludeMap, String lastDate){
		String maxDate=lastDate;
		boolean resume=table!=null;
		if(resume){
			Elements trs=table.getElementsByTag("tr");
			for(int i=1;i<trs.size();i++){
				Element tr = trs.get(i);
				Elements as = tr.getElementsByTag("a");
				if(as.size()==0){
					continue;
				}
				Element a = as.get(0);
				String detailUrl = a.attr("href");
				String title=a.text();
				if(StringUtils.contains(title, "废标公告")||StringUtils.contains(title, "失败公告")){
					continue;
				}
				String dateStr=a.parent().nextElementSibling().text().trim();
				if(StringUtils.isBlank(dateStr)){
					continue;
				}
				if(dateStr.compareTo(lastDate)<0){
					resume=false;
					break;
				}
				String code = getCodeFromTitle(title);
				maxDate=maxDate.compareTo(dateStr)<0?dateStr:maxDate;
				if(StringUtils.contains(excludeMap.get(dateStr), code)){
					continue;
				}
				SpideData notice = newInstance();
				notice.setUrl(rootUrl+detailUrl);
				String name = StringUtils.substringBeforeLast(title, "(");
				notice.setName(name);
				notice.setCode(code);
				notice.setDate(dateStr);
				
				crawlDetail(HtmlUtil.getHtmlTextByUrl(rootUrl+detailUrl), notice);
				notices.add(notice);
				if(StringUtils.isNotBlank(excludeMap.get(dateStr))){
					excludeMap.put(dateStr, excludeMap.get(dateStr)+","+code);
				}else{
					excludeMap.put(dateStr, code);
				}
			}
		}
		return new ResultFlag(resume, maxDate);
	}

	protected String getCodeFromTitle(String title){
		if(StringUtils.contains(title, "(") && StringUtils.contains(title, ")")){
			return StringUtils.substringBetween(title, "(", ")").trim();
		}
		return "";
	}
	
	protected Element getMainElement(Document doc){
		if(doc!=null){
			Elements mains = doc.getElementsByClass("left_lb");
			if(mains.size()>0){
				return mains.get(0);
			}
		}
		return null;
	}
	
	protected Element getKeyElement(Element mainDiv){
		if(mainDiv!=null){
			return mainDiv.getElementsByTag("tbody").first();
		}
		return null;
	}
	
	private int getInterval(){
		String intervalStr = ConfigManager.getConfig(Const.configInterval);
		int interval = defaultInterval;
		try{
			int i=Integer.parseInt(intervalStr);
			if(i>0){
				interval=i;
			}
		}catch(Exception e){
			logger.error("配置项："+Const.configInterval+" 配置有误", e);
		}
		return interval;
	}
	
	protected int getKeepDays(){
		String dayStr = ConfigManager.getConfig(Const.configInitDays);
		int keepDays = days;
		try{
			int d = Integer.parseInt(dayStr);
			if(d>0){
				keepDays = d;
			}
		}catch(Exception e){
			logger.error("配置项："+Const.configInitDays+" 配置有误", e);
		}
		return keepDays;
	}
	
	protected static boolean crawlPurchaser(Element txtDiv, SpideData notice){
		try{
			Elements purchasers=txtDiv.getElementsContainingOwnText("采购人名称");
			if(purchasers.size()==0){
				purchasers=txtDiv.getElementsContainingOwnText("招标人名称");
			}
			if(purchasers.size()>0){
				String purchaser = StringUtils.substringAfter(HtmlUtil.getParentText(purchasers.get(0), "p"), "：");
				if(StringUtils.contains(purchaser, "（")&&StringUtils.contains(purchaser, "）")){
					purchaser=StringUtils.substringBefore(purchaser, "（");
				}
				while(purchaser.length()>43&&purchaser.contains("、")){
					purchaser=StringUtils.substringBeforeLast(purchaser, "、");
					purchaser+="等";
				}
				notice.setPurchaser(purchaser);
			}else{
				Elements contactEls = txtDiv.getElementsContainingOwnText("联系事项");
				if(contactEls.size()>0){
					Element contactEl=contactEls.get(0);
					while(contactEl!=null && !StringUtils.equals("p", contactEl.tagName())){
						contactEl=contactEl.parent();
					}
					if(contactEl!=null){
						Element purchaserEl = contactEl.nextElementSibling();
						while(purchaserEl!=null && !StringUtils.contains(purchaserEl.wholeText(), "名称：")){
							purchaserEl = purchaserEl.nextElementSibling();
						}
						if(purchaserEl!=null && StringUtils.contains(purchaserEl.wholeText(), "名称：")){
							String purchaser = StringUtils.substringAfter(purchaserEl.wholeText(), "名称：").trim();
							while(purchaser.length()>43&&purchaser.contains("、")){
								purchaser=StringUtils.substringBeforeLast(purchaser, "、");
								purchaser+="等";
							}
							notice.setPurchaser(purchaser);
							Element addrEl = purchaserEl.nextElementSibling();
							if(addrEl!=null && StringUtils.contains(addrEl.wholeText(), "地址：")){
								String address = StringUtils.substringAfter(addrEl.wholeText(), "地址：");
								if(StringUtils.contains(address, "邮编：")){
									address = StringUtils.substringBefore(address, "邮编：").trim();
								}
								address=StringUtils.replacePattern(address, " | |。", "");
								notice.setAddress(address);
							}
						}
					}
				}
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(notice.getPurchaser());
	}
	
	protected static boolean crawlAddress(Element txtDiv, SpideData notice){
		try{
			Elements addresss=txtDiv.getElementsContainingOwnText("采购人地址");
			if(addresss.size()==0){
				addresss=txtDiv.getElementsContainingOwnText("招标人地址");
			}
			if(addresss.size()>0){
				notice.setAddress(StringUtils.substringAfter(HtmlUtil.getParentText(addresss.get(0), "p"), "："));
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(notice.getAddress());
	}
	
	protected static boolean crawlContactsAndPhoneNo(Element txtDiv, SpideData notice){
		try{
			Elements contactsEls=txtDiv.getElementsContainingOwnText("联系电话");
			if(contactsEls.size()==0){
				contactsEls=txtDiv.getElementsContainingOwnText("采购人联系方式");
			}
			if(contactsEls.size()>0){
				String contactsTxt = HtmlUtil.getParentText(contactsEls.get(0), "p");
				String contacts = "";
				String phoneNo = "";
				if(StringUtils.contains(contactsTxt, "采购人联系方式")){
					contacts = StringUtils.substringBetween(contactsTxt, "：", "，");
					phoneNo = StringUtils.substringAfter(contactsTxt, "，");
				}else{
					contacts = StringUtils.substringBetween(contactsTxt, "：", "联系电话：");
					phoneNo=StringUtils.substringAfterLast(contactsTxt, "：");
				}
				if(StringUtils.isNotBlank(contacts)){
					contacts=StringUtils.replace(contacts, " ", "");
				}
				notice.setContacts(contacts);
				if(StringUtils.isNotBlank(phoneNo)){
					phoneNo=StringUtils.replace(phoneNo, "(020)", "");
					phoneNo=StringUtils.replace(phoneNo, "（020）", "");
					phoneNo=StringUtils.replace(phoneNo, "020-", "");
					if(phoneNo.startsWith("020")){
						phoneNo=phoneNo.substring(3);
					}
				}
				notice.setPhoneNo(phoneNo);
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(notice.getContacts())&&StringUtils.isNotBlank(notice.getPhoneNo());
	}
}
