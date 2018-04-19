package wen.liu.spide.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import wen.liu.annotation.Crawler;
import wen.liu.entity.Const;
import wen.liu.entity.ResultFlag;
import wen.liu.entity.SpideData;
import wen.liu.entity.SupplyNotice;
import wen.liu.spide.Spider;
import wen.liu.util.HtmlUtil;

@Crawler(key=Const.typeSupplyNotice)
public class SupplyNoticeSpider extends Spider{
	protected int days=2;
	
	@Override
	protected String getType() {
		return Const.typeSupplyNotice;
	}
	@Override
	protected String getWebRoot() {
		return "http://xygy.gzggzy.cn";
	}
	@Override
	protected String getSubPath() {
		return "/OrderXinList.aspx";
	}
	@Override
	protected String getDataFileName() {
		return Const.dataFileSupplyNotice;
	}
	@Override
	protected SpideData newInstance() {
		return new SupplyNotice();
	}
	@Override
	protected int getKeepDays(){
		return days;
	}
	
	public static void main(String[] args) {
		try{
			new SupplyNoticeSpider().start();
		}catch(Exception e){
			e.printStackTrace();
		}
//		System.exit(0);
	}
	
	@Override
	protected String crawl(String rootUrl, String subUrl, List<SpideData> notices, String lastDate, Map<String,String> excludeMap){
		return crawlSupplyNotice(rootUrl, subUrl, notices, lastDate, excludeMap, 1);
	}
	private String crawlSupplyNotice(String rootUrl, String subUrl, List<SpideData> notices, 
			String lastDate, Map<String,String> excludeMap, int pageIndex){
		Document doc = null;
		if(pageIndex>1){
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("ScriptManager1", "UpdatePanel1|Pager");
			dataMap.put("__EVENTTARGET", "Pager");
			dataMap.put("__EVENTARGUMENT", String.valueOf(pageIndex));
			dataMap.put("__ASYNCPOST", "true");
			dataMap.put("__VIEWSTATE", paramViewstate);
			dataMap.put("__EVENTVALIDATION", paramValidation);
			doc = HtmlUtil.getHtmlTextByPost(rootUrl + subUrl, dataMap);
		}else{
			doc = HtmlUtil.getHtmlTextByUrl(rootUrl + subUrl);
		}
		if(doc==null){
			return lastDate;
		}
		Element table=null;
		if(pageIndex>1){
			Elements tables = doc.getElementsByTag("table");
			if(tables.size()>0){
				table=tables.get(tables.size()-1);
			}
		}else{
			Element mainDiv = doc.getElementById("UpdatePanel1");
			if(mainDiv==null){
				return lastDate;
			}
			table = mainDiv.children().last();
		}
		ResultFlag resultFlag=crawlListPage(rootUrl, table, notices, excludeMap, lastDate);
		if(resultFlag.isResume()){
			pageIndex++;
			crawlSupplyNotice(rootUrl, subUrl, notices, lastDate, excludeMap, pageIndex);
		}
		return resultFlag.getMaxDate();
	}
	@Override
	protected ResultFlag crawlListPage(String rootUrl, Element table, List<SpideData> notices, Map<String,String> excludeMap, String lastDate){
		String maxDate=lastDate;
		boolean resume=table!=null;
		if(resume){
			Elements trs=table.getElementsByTag("tr");
			for(int i=0;i<trs.size();i++){
				Elements tds = trs.get(i).children();
				if(tds.size()<7){
					continue;
				}
				String dateStr = tds.get(4).text().trim();
				if(dateStr.compareTo(lastDate)<0){
					resume=false;
					break;
				}
				maxDate=maxDate.compareTo(dateStr)<0?dateStr:maxDate;
				Elements as = tds.get(0).getElementsByTag("a");
				if(as.size()==0){
					continue;
				}
				Element a = as.get(0);
				String detailUrl = a.attr("href");
				String code = a.ownText().trim();
				if(StringUtils.contains(excludeMap.get(dateStr), code)){
					continue;
				}
				SupplyNotice supplyNotice = (SupplyNotice)newInstance();
				supplyNotice.setCode(code);
				supplyNotice.setDate(dateStr);
				supplyNotice.setUrl(rootUrl+detailUrl);
				supplyNotice.setType(tds.get(1).text().trim());
				supplyNotice.setPurchaser(tds.get(2).text().trim());
				supplyNotice.setSupplier(tds.get(3).text().trim());
				supplyNotice.setTurnover(tds.get(5).text().trim());
				notices.add(supplyNotice);
				if(StringUtils.isNotBlank(excludeMap.get(dateStr))){
					excludeMap.put(dateStr, excludeMap.get(dateStr)+","+code);
				}else{
					excludeMap.put(dateStr, code);
				}
			}
		}
		return new ResultFlag(resume, maxDate);
	}
	
	@Override
	protected void crawlDetail(Document detailDoc, SpideData data) {
	}
	private static final String paramViewstate="/wEPDwULLTE0MTE3MzI3NDYPZBYCAgMPZBYEAgUPZBYCZg9kFgQCCw8QZGQWAWZkAg0PEGRkFg"
			+ "BkAgcPZBYCZg9kFgICAw8PFgQeC1JlY29yZGNvdW50AqHJCR4QQ3VycmVudFBhZ2VJbmRleAICZGRkCOhYXWqUTIqwsuBoVP1FUzPulmy8L8BAZhLfFkFsgx0=";
	private static final String paramValidation="/wEWDAKOtYS7DQKP/sziDAKN/eiMCwKN/dD1BwKKlOGrDAKNovL8CAKd5I/lCgKNi6WLBgKS"
			+ "i6WLBgKTi6WLBgKln/PuCgL057e1DH788OM13At9j6xJC3v6lDtKiqlm4nujiN/cma2QAeVr";
}
