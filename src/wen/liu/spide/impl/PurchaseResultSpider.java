package wen.liu.spide.impl;


import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import wen.liu.annotation.Crawler;
import wen.liu.entity.Const;
import wen.liu.entity.PurchaseResult;
import wen.liu.entity.SpideData;
import wen.liu.spide.Spider;
import wen.liu.util.HtmlUtil;

@Crawler(key=Const.typePurchaseResult)
public class PurchaseResultSpider extends Spider {
	@Override
	protected String getType() {
		return Const.typePurchaseResult;
	}
	@Override
	protected SpideData newInstance(){
		return new PurchaseResult();
	}
	@Override
	protected String getCodeFromTitle(String title) {
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
		return "/cms/html/wz/view/index/layout2/zfcglist_458.html?siteId=1&channelId=458";
	}
	@Override
	protected String getDataFileName(){
		return Const.dataFilePurchaseResult;
	}

	@Override
	public void crawlDetail(Document detailDoc, SpideData data){
		Elements txtDivs = detailDoc.getElementsByClass("xx-text");
		if(txtDivs.size()==0){
			logger.info("日期：{}，编号={} 未获取到详细信息", data.getDate(), data.getCode());
			return;
		}
		PurchaseResult purchaseResult = (PurchaseResult)data;
		Element txtDiv = txtDivs.get(0);
		StringBuilder params = new StringBuilder();
		if(!crawlBidAmount(txtDiv, purchaseResult)){
//			params.append("中标金额、");
		}
		if(!crawlSuppplier(txtDiv, purchaseResult)){
			params.append("中标供应商、");
		}
		if(!crawlPurchaser(txtDiv, purchaseResult)){
			params.append("采购单位、");
		}
		if(!crawlAddress(txtDiv, purchaseResult)){
			params.append("采购单位地址、");
		}
		if(!crawlContactsAndPhoneNo(txtDiv, purchaseResult)){
			params.append("联系人和电话、");
		}
		String paramStr = params.toString();
		if(paramStr.endsWith("、")){
			paramStr = StringUtils.substringBeforeLast(paramStr, "、");
			logger.info("日期：{}，编号={} 未获取到 "+paramStr, purchaseResult.getDate(), purchaseResult.getCode());
		}
	}
	
	private boolean crawlBidAmount(Element txtDiv, PurchaseResult purchaseResult){
		try{
			Elements bidEls = txtDiv.getElementsContainingOwnText("中标金额");
			if(bidEls.size()==0){
				bidEls = txtDiv.getElementsContainingOwnText("成交金额");
			}
			if(bidEls.size()>0){
				String bidAmount = bidEls.get(0).text();
				if(StringUtils.contains(bidAmount, "人民币") && StringUtils.contains(bidAmount, "元")){
					bidAmount = StringUtils.substringBetween(bidAmount, "人民币", "元").trim();
				}else{
					bidAmount = HtmlUtil.getParentText(bidEls.get(0), "p");
					bidAmount = StringUtils.substringBetween(bidAmount, "人民币", "元").trim();
				}
				purchaseResult.setBidAmount(bidAmount);
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(purchaseResult.getBidAmount());
	}
	
	private boolean crawlSuppplier(Element txtDiv, PurchaseResult purchaseResult){
		try{
			Element supplyEl = txtDiv.getElementsContainingOwnText("中标供应商名称").first();
			if(supplyEl==null){
				supplyEl = txtDiv.getElementsContainingOwnText("成交供应商名称").first();
			}
			if(StringUtils.equals("span", supplyEl.tagName())){
				String supplier = StringUtils.substringAfter(supplyEl.wholeText(), "：");
				if(StringUtils.isBlank(supplier)&&supplyEl.nextElementSibling()!=null){
					supplier=supplyEl.nextElementSibling().wholeText().trim();
				}
				purchaseResult.setSupplier(supplier);
			}if(StringUtils.equals("td", supplyEl.tagName())){
				Element tr=supplyEl.parent().nextElementSibling();
				String suppliers = "";
				int size = tr.parent().children().size();
				int c=0;
				while(tr!=null&&tr.children().size()>0&&c++<3){
					suppliers +=tr.child(0).wholeText().trim()+",";
					tr=tr.nextElementSibling();
				}
				if(suppliers.endsWith(",")){
					suppliers=StringUtils.substringBeforeLast(suppliers, ",");
					if(size-1>c){
						suppliers+="等";
					}
				}
				purchaseResult.setSupplier(suppliers);
			}
		}catch(Exception e){
		}
		return StringUtils.isNotBlank(purchaseResult.getSupplier());
	}
}
