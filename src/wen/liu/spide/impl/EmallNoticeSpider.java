package wen.liu.spide.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import wen.liu.annotation.Crawler;
import wen.liu.entity.Const;
import wen.liu.entity.EmallNotice;
import wen.liu.entity.ResultFlag;
import wen.liu.entity.SpideData;
import wen.liu.spide.Spider;
import wen.liu.util.HtmlUtil;

@Crawler(key=Const.typeEmallNotice)
public class EmallNoticeSpider extends Spider{
	protected int days=2;
	
	@Override
	protected String getType() {
		return Const.typeEmallNotice;
	}
	@Override
	protected String getWebRoot() {
		return "http://mall.gzggzy.cn";
	}
	@Override
	protected String getSubPath() {
		return "/frontDealDynamic/dealDynamicHtml";
	}
	@Override
	protected String getDataFileName() {
		return Const.dataFileEmallNotice;
	}
	@Override
	protected SpideData newInstance() {
		return new EmallNotice();
	}
	@Override
	protected int getKeepDays(){
		return days;
	}
	
	public static void main(String[] args) {
		try{
			new EmallNoticeSpider().start();
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
		Document doc=HtmlUtil.getHtmlTextByUrl(rootUrl + subUrl+"?"+createQueryString(pageIndex));
		if(doc==null){
			return lastDate;
		}
		Element table=null;
		Elements targetEls=doc.getElementsContainingOwnText("最新成交信息");
		Element targetEl = targetEls.first();
		if(targetEl!=null&&(targetEl=targetEl.nextElementSibling())!=null){
			Element targetDiv=targetEl.nextElementSibling();
			if(targetDiv!=null && StringUtils.equals(targetDiv.tagName(), "div")){
				table=targetDiv.getElementsByTag("tbody").first();
			}
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
				if(tds.size()<6){
					continue;
				}
				String timeStr = tds.get(5).text().trim();
				String dateStr = StringUtils.substringBefore(timeStr, " ");
				if(dateStr.compareTo(lastDate)<0){
					resume=false;
					break;
				}
				maxDate=maxDate.compareTo(dateStr)<0?dateStr:maxDate;
				Elements as = tds.get(1).getElementsByTag("a");
				if(as.size()==0){
					continue;
				}
				Element a = as.get(0);
				String detailUrl = a.attr("href");
				String code = a.text();
				if(StringUtils.contains(excludeMap.get(dateStr), code)){
					continue;
				}
				EmallNotice emallNotice = (EmallNotice)newInstance();
				emallNotice.setCode(code);
				emallNotice.setDate(timeStr);
				emallNotice.setUrl(detailUrl);
				emallNotice.setTurnover(tds.get(2).text().trim());
				emallNotice.setPurchaser(tds.get(3).text().trim());
				emallNotice.setSupplier(tds.get(4).text().trim());
				notices.add(emallNotice);
				if(StringUtils.isNotBlank(excludeMap.get(dateStr))){
					excludeMap.put(dateStr, excludeMap.get(dateStr)+","+code);
				}else{
					excludeMap.put(dateStr, code);
				}
			}
		}
		return new ResultFlag(resume, maxDate);
	}
	
	private String createQueryString(int pageIndex){
		Date curDate = new Date();
		Calendar ca=Calendar.getInstance();
		ca.setTime(curDate);
		ca.add(Calendar.DAY_OF_YEAR, -(getKeepDays()+1));
		SimpleDateFormat formater = new SimpleDateFormat(format);
		String startDate=formater.format(ca.getTime());
		String endDate=formater.format(curDate);
		return "pageNo="+pageIndex+"&startDate="+startDate+"&endDate="+endDate+"&content=&queryType=1";
	}
	@Override
	protected void crawlDetail(Document detailDoc, SpideData data) {
	}
}
