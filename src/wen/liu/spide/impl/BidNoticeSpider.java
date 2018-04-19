package wen.liu.spide.impl;


import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import wen.liu.annotation.Crawler;
import wen.liu.entity.BidNotice;
import wen.liu.entity.Const;
import wen.liu.entity.ResultFlag;
import wen.liu.entity.SpideData;
import wen.liu.spide.Spider;
import wen.liu.util.DateUtil;
import wen.liu.util.HtmlUtil;

@Crawler(key=Const.typeBidNotice)
public class BidNoticeSpider extends Spider{
	@Override
	public String getType(){
		return Const.typeBidNotice;
	}
	protected SpideData newInstance(){
		return new BidNotice();
	}
	@Override
	protected String getWebRoot(){
		return "http://wj.gzggzy.cn";
	}
	@Override
	protected String getSubPath(){
		return "/NoticeList.aspx?type=0";
	}
	@Override
	protected String getDataFileName(){
		return Const.dataFileBidNotice;
	}
	
	public static void main(String[] args) {
		try{
			Spider spider = BidNoticeSpider.class.newInstance();
			System.out.println(spider.getClass().getName());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
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
				String timeStr=a.parent().nextElementSibling().ownText().trim();
				String dateStr=StringUtils.substringBefore(timeStr, " ");
				if(StringUtils.isBlank(dateStr)){
					continue;
				}
				if(dateStr.length()<10)
					dateStr = DateUtil.transToStandDate(dateStr, "-");
				if(dateStr.compareTo(lastDate)<0){
					resume=false;
					break;
				}
				String code = a.parent().previousElementSibling().text().trim();
				maxDate=maxDate.compareTo(dateStr)<0?dateStr:maxDate;
				if(StringUtils.contains(excludeMap.get(dateStr), code)){
					continue;
				}
				SpideData notice = newInstance();
				notice.setUrl(rootUrl+"/"+detailUrl);
				notice.setName(title);
				notice.setCode(code);
				String time=StringUtils.substringAfter(timeStr, " ");
				notice.setDate(dateStr+" "+DateUtil.transToStandTime(time, ":"));
				
				crawlDetail(HtmlUtil.getHtmlTextByUrl(rootUrl+"/"+detailUrl), notice);
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
	
	@Override
	public void crawlDetail(Document detailDoc, SpideData data){
		Element txtDiv = detailDoc.getElementsByClass("note_content").first();
		if(txtDiv==null){
			logger.info("日期：{}，编号={} 未获取到详细信息", data.getDate(), data.getCode());
			return;
		}
		BidNotice notice = (BidNotice)data;
		StringBuilder params = new StringBuilder();
		if(!crawlBudget(txtDiv, notice)){
			params.append("采购预算、");
		}
		if(!crawlSubmitTime(txtDiv, notice)){
			params.append("报价时间、");
		}
		if(!crawlPurchaser(txtDiv, notice)){
			params.append("采购单位、");
		}
		String paramStr = params.toString();
		if(paramStr.endsWith("、")){
			paramStr = StringUtils.substringBeforeLast(paramStr, "、");
			logger.info("日期：{}，编号={} 未获取到 "+paramStr, notice.getDate(), notice.getCode());
		}
	}
	
	protected static boolean crawlPurchaser(Element txtDiv, SpideData notice){
		try{
			Element purchaserP=txtDiv.getElementsContainingOwnText("报价时间").first();
			if(purchaserP!=null){
				Element ulEl = purchaserP.nextElementSibling();
				while(ulEl!=null && !StringUtils.equals("ul", ulEl.tagName())){
					ulEl = ulEl.nextElementSibling();
				}
				Element name = ulEl.getElementsContainingOwnText("名称").first();
				if(name!=null){
					notice.setPurchaser(StringUtils.substringAfter(name.wholeText(), "名称："));
				}
				Element address = ulEl.getElementsContainingOwnText("地址").first();
				if(address!=null){
					notice.setAddress(StringUtils.substringAfter(address.wholeText(), "地址："));
				}
				Element contacts = ulEl.getElementsContainingOwnText("联系人").first();
				if(contacts!=null){
					notice.setContacts(StringUtils.substringAfter(contacts.wholeText(), "联系人："));
				}
				Element phoneNoEl = ulEl.getElementsContainingOwnText("联系电话").first();
				if(phoneNoEl!=null){
					String phoneNo = StringUtils.substringAfter(phoneNoEl.wholeText(), "联系电话：");
					phoneNo=StringUtils.replace(phoneNo, "(020)", "");
					phoneNo=StringUtils.replace(phoneNo, "（020）", "");
					phoneNo=StringUtils.replace(phoneNo, "020-", "");
					if(phoneNo.startsWith("020")){
						phoneNo=phoneNo.substring(3);
					}
					notice.setPhoneNo(phoneNo);
				}
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(notice.getPurchaser());
	}
	
	private static boolean crawlSubmitTime(Element txtDiv, BidNotice notice){
		try{
			Element submitEle=txtDiv.getElementsContainingOwnText("报价时间").first();
			if(submitEle!=null){
				String submitTime = submitEle.nextElementSibling().wholeText();
				notice.setSubmitTime(submitTime);
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(notice.getSubmitTime());
	}
	
	private static boolean crawlBudget(Element txtDiv, BidNotice notice){
		try{
			Element budgetEle=txtDiv.getElementsContainingOwnText("采购预算").first();
			if(budgetEle!=null){
				String budget = budgetEle.wholeText();
				budget = StringUtils.substringBetween(budget, "人民币", "元");
				if(budget.endsWith(".00")){
					budget = StringUtils.substringBeforeLast(budget, ".00");
				}
				notice.setBudget(budget);
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(notice.getBudget());
	}
	@Override
	protected Element getMainElement(Document doc){
		if(doc!=null){
			Elements mains = doc.getElementsByClass("xzpp");
			if(mains.size()>0){
				return mains.get(0);
			}
		}
		return null;
	}
}
