package wen.liu.entity;

import org.apache.poi.common.usermodel.HyperlinkType;

import wen.liu.annotation.ExcelData;

import com.alibaba.fastjson.annotation.JSONField;

@ExcelData(type=Const.typePurchaseNotice)
public class PurchaseNotice extends SpideData{
	@JSONField(ordinal=3)
	protected String budget;
	@JSONField(ordinal=4)
	protected String submitTime;
	@JSONField(ordinal=5)
	protected String openTime;
	
	public String getBudget() {
		return budget;
	}
	public void setBudget(String budget) {
		this.budget = budget;
	}
	public String getSubmitTime() {
		return submitTime;
	}
	public void setSubmitTime(String submitTime) {
		this.submitTime = submitTime;
	}
	public String getOpenTime() {
		return openTime;
	}
	public void setOpenTime(String openTime) {
		this.openTime = openTime;
	}

	@Override
	@JSONField(serialize=false)
	public String[] getTitleCells() {
		return new String[]{
				"发布日期","项目名称","项目预算","投标文件时间","开标时间","采购单位","单位地址","联系人","联系电话"
		};
	}
	@Override
	@JSONField(serialize=false)
	public String[] getCellValues() {
		return new String[]{date,name,budget,submitTime,openTime,purchaser,address,contacts,phoneNo};
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
		return new int[]{3000,8000,2500,5000,4000,6000,6000,2000,3500};
	}
	@Override
	@JSONField(serialize=false)
	public short getHeight() {
		return (short)800;
	}
	@Override
	@JSONField(serialize=false)
	public String getSheetName() {
		return "采购公告";
	}
	@Override
	@JSONField(serialize=false)
	public int getSheetIndex(){
		return 0;
	}
}
