package wen.liu.entity;

import org.apache.poi.common.usermodel.HyperlinkType;

import wen.liu.annotation.ExcelData;

import com.alibaba.fastjson.annotation.JSONField;

@ExcelData(type=Const.typePurchaseResult)
public class PurchaseResult extends SpideData{
	@JSONField(ordinal=3)
	private String bidAmount;
	@JSONField(ordinal=4)
	private String supplier;

	public String getBidAmount() {
		return bidAmount;
	}
	public void setBidAmount(String bidAmount) {
		this.bidAmount = bidAmount;
	}
	public String getSupplier() {
		return supplier;
	}
	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}
	
	@Override
	@JSONField(serialize=false)
	public String[] getTitleCells() {
		return new String[]{
				"发布日期","项目名称","中标金额","中标供应商","采购单位","单位地址","联系人","联系电话"
		};
	}
	@Override
	@JSONField(serialize=false)
	public String[] getCellValues() {
		return new String[]{date,name,bidAmount,supplier,purchaser,address,contacts,phoneNo};
	}
	@Override
	@JSONField(serialize=false)
	public ExcelLink getLink(int colNum) {
		if(colNum==1){
			return new ExcelLink(HyperlinkType.URL, url);
		}
		return null;
	}
	@Override
	@JSONField(serialize=false)
	public int[] getWidths() {
		return new int[]{3000,9000,3500,6500,6500,6500,2500,3500};
	}
	@Override
	@JSONField(serialize=false)
	public short getHeight() {
		return (short)800;
	}
	@Override
	@JSONField(serialize=false)
	public String getSheetName() {
		return "采购结果公告";
	}
	@Override
	@JSONField(serialize=false)
	public int getSheetIndex(){
		return 1;
	}
}
