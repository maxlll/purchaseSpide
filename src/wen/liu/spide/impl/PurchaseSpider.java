package wen.liu.spide.impl;


import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;

import wen.liu.annotation.Crawler;
import wen.liu.entity.Const;
import wen.liu.entity.PurchaseNotice;
import wen.liu.entity.SpideData;
import wen.liu.spide.Spider;
import wen.liu.util.HtmlUtil;

@Crawler(key=Const.typePurchaseNotice)
public class PurchaseSpider extends Spider{
	@Override
	public String getType(){
		return Const.typePurchaseNotice;
	}
	protected SpideData newInstance(){
		return new PurchaseNotice();
	}
	@Override
	public String getCodeFromTitle(String title){
		if(StringUtils.contains(title, "(") && StringUtils.contains(title, ")")){
			return StringUtils.substringBetween(title, "(", ")").trim();
		}
		return "";
	}
	@Override
	protected String getWebRoot(){
		return "http://www.gzggzy.cn";
	}
	@Override
	protected String getSubPath(){
		return "/cms/wz/view/index/layout2/zfcglist.jsp?siteId=1&channelId=456";
	}
	@Override
	protected String getDataFileName(){
		return Const.dataFilePurchaseNotice;
	}
	
	public static void main(String[] args) {
		String url="http://www.gzggzy.cn/cms/wz/view/index/layout3/index.jsp?siteId=1&infoId=546134&channelId=456";
		PurchaseNotice notice=new PurchaseNotice();
		new PurchaseSpider().crawlDetail(HtmlUtil.getHtmlTextByUrl(url), notice);
		System.out.println(JSON.toJSONString(notice));
	}
	
	@Override
	public void crawlDetail(Document detailDoc, SpideData data){
		Elements txtDivs = detailDoc.getElementsByClass("xx-text");
		if(txtDivs.size()==0){
			logger.info("日期：{}，编号={} 未获取到详细信息", data.getDate(), data.getCode());
			return;
		}
		PurchaseNotice notice = (PurchaseNotice)data;
		Element txtDiv = txtDivs.get(0);
		StringBuilder params = new StringBuilder();
		if(!crawlBudget(txtDiv, notice)){
			params.append("项目预算、");
		}
		if(!crawlSubmitTime(txtDiv, notice)){
			params.append("投标时间、");
		}
		if(!crawlOpenTime(txtDiv, notice)){
			params.append("开标时间、");
		}
		if(!crawlPurchaser(txtDiv, notice)){
			params.append("采购单位、");
		}
		if(!crawlAddress(txtDiv, notice)){
			params.append("采购单位地址、");
		}
		if(!crawlContactsAndPhoneNo(txtDiv, notice)){
			params.append("联系人和电话、");
		}
		String paramStr = params.toString();
		if(paramStr.endsWith("、")){
			paramStr = StringUtils.substringBeforeLast(paramStr, "、");
			logger.info("日期：{}，编号={} 未获取到 "+paramStr, notice.getDate(), notice.getCode());
		}
	}
	
	private static boolean crawlSubmitTime(Element txtDiv, PurchaseNotice notice){
		try{
			Elements submitEles=txtDiv.getElementsContainingOwnText("投标文件时间");
			Element submitEle=null;
			if(submitEles.size()==0){
				submitEles=txtDiv.getElementsContainingOwnText("响应文件时间");
			}
			if(submitEles.size()==0){
				submitEles=txtDiv.getElementsContainingText("投标文件");
				for(Element ele:submitEles){
					Element next=ele.nextElementSibling();
					if(next!=null&&StringUtils.equals("span", next.tagName())&&StringUtils.contains(next.text(), "时间：")){
						submitEle=next;
						break;
					}
				}
			}else{
				submitEle=submitEles.get(0);
			}
			if(submitEle!=null){
				String submitTime = submitEle.wholeText();
				submitTime += HtmlUtil.getAfterSiblingText(submitEle, "p");
				int index = submitTime.indexOf("：");
				if(index>-1){
					submitTime=submitTime.substring(index+1);
				}
				if(submitTime.indexOf("（")>0&&submitTime.indexOf("）")>0){
					submitTime=StringUtils.substringBefore(submitTime, "（")+StringUtils.substringAfter(submitTime, "）");
				}
				submitTime=StringUtils.replacePattern(submitTime, " | |。", "");
				submitTime=StringUtils.replacePattern(submitTime, "年|月", "-");
				submitTime=StringUtils.replace(submitTime, "日", " ");
				notice.setSubmitTime(submitTime);
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(notice.getSubmitTime());
	}
	
	private static boolean crawlOpenTime(Element txtDiv, PurchaseNotice notice){
		try{
			Elements openEles=txtDiv.getElementsContainingOwnText("开标时间");
			if(openEles.size()>0){
				Element openEle = openEles.get(0);
				String openTime = openEle.wholeText()+HtmlUtil.getAfterSiblingText(openEle, "p");
				if(openTime.contains("：")){
					openTime=StringUtils.substringAfterLast(openTime, "：");
				}
				openTime=StringUtils.replacePattern(openTime, " | |。", "");
				openTime=StringUtils.replacePattern(openTime, "年|月", "-");
				openTime=StringUtils.replace(openTime, "日", " ");
				notice.setOpenTime(openTime);
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(notice.getOpenTime());
	}
	
	private static boolean crawlBudget(Element txtDiv, PurchaseNotice notice){
		try{
			Elements budgetEles=txtDiv.getElementsContainingOwnText("预算金额");
			if(budgetEles.size()>0){
				Element budgetEle = budgetEles.get(0);
				String budget = HtmlUtil.getAfterSiblingText(budgetEle, "p");
				if(StringUtils.contains(budget, "人民币")){
					notice.setBudget(StringUtils.substringBetween(budget, "人民币", "元").trim());
				}else{
					budget = "";
					Element parent = budgetEle.parent();
					while(parent!=null && !StringUtils.equals(parent.tagName(), "p")){
						parent = parent.parent();
					}
					if(parent!=null){
						Element nextp = parent.nextElementSibling();
						String txt = "";
						while(nextp!=null&&StringUtils.contains((txt=nextp.wholeText()), "子项目")){
							budget += StringUtils.substringBetween(txt, "人民币", "元").trim()+",";
							nextp = nextp.nextElementSibling();
						}
						if(budget.endsWith(",")){
							budget=budget.substring(0, budget.length()-1);
						}
						notice.setBudget(budget);
					}
				}
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(notice.getBudget());
	}
}
