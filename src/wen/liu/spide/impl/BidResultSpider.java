package wen.liu.spide.impl;


import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import wen.liu.annotation.Crawler;
import wen.liu.entity.BidResult;
import wen.liu.entity.Const;
import wen.liu.entity.ResultFlag;
import wen.liu.entity.SpideData;
import wen.liu.spide.Spider;
import wen.liu.util.DateUtil;
import wen.liu.util.HtmlUtil;

@Crawler(key=Const.typeBidResult)
public class BidResultSpider extends Spider{
	@Override
	public String getType(){
		return Const.typeBidResult;
	}
	protected SpideData newInstance(){
		return new BidResult();
	}
	@Override
	protected String getWebRoot(){
		return "http://wj.gzggzy.cn";
	}
	@Override
	protected String getSubPath(){
		return "/NoticeList.aspx?type=5";
	}
	@Override
	protected String getDataFileName(){
		return Const.dataFileBidResult;
	}
	
	public static void main(String[] args) {
		try{
			new BidResultSpider().start();
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
				if(dateStr.length()<10)
					dateStr = DateUtil.transToStandDate(dateStr, "-");
				if(StringUtils.isBlank(dateStr)){
					continue;
				}
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
		BidResult notice = (BidResult)data;
		StringBuilder params = new StringBuilder();
		if(!crawlPurchaser(txtDiv, notice)){
			params.append("本项目采购人、");
		}
		String paramStr = params.toString();
		if(paramStr.endsWith("、")){
			paramStr = StringUtils.substringBeforeLast(paramStr, "、");
			logger.info("日期：{}，编号={} 未获取到 "+paramStr, notice.getDate(), notice.getCode());
		}
	}
	
	protected static boolean crawlPurchaser(Element txtDiv, BidResult notice){
		try{
			Element supplierEl = txtDiv.getElementsContainingOwnText("供应商名称").first();
			if(supplierEl!=null){
				String supplierTxt = supplierEl.wholeText();
				String supplier = StringUtils.substringBetween(supplierTxt, "：", "；");
				notice.setSupplier(supplier);
			}else if(txtDiv.getElementsContainingText("项目采购失败").size()>0){
				notice.setSupplier("项目采购失败");
			}
			Element purchaserEl = txtDiv.getElementsContainingOwnText("本项目采购人").first();
			if(purchaserEl!=null){
				String purchaserTxt = purchaserEl.wholeText();
				String purchaser = StringUtils.substringBetween(purchaserTxt, "：", "；");
				notice.setPurchaser(purchaser);
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(notice.getPurchaser());
	}
	@Override
	protected Element getMainElement(Document doc){
		if(doc!=null){
			return doc.getElementsByClass("xzpp").first();
		}
		return null;
	}
}
