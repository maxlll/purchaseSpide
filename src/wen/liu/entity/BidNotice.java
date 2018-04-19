package wen.liu.entity;

import org.apache.poi.common.usermodel.HyperlinkType;

import wen.liu.annotation.ExcelData;

import com.alibaba.fastjson.annotation.JSONField;

@ExcelData(type=Const.typeBidNotice)
public class BidNotice extends PurchaseNotice{
	@Override
	@JSONField(serialize=false)
	public String[] getTitleCells() {
		return new String[]{
				"发布时间","项目名称","项目预算","报价时间","采购单位","单位地址","联系人","联系电话"
		};
	}
	@Override
	@JSONField(serialize=false)
	public String[] getCellValues() {
		return new String[]{date,name,budget,submitTime,purchaser,address,contacts,phoneNo};
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
		return new int[]{5300,7500,3000,7000,6000,6000,2200,3500};
	}
	@Override
	@JSONField(serialize=false)
	public short getHeight() {
		return (short)800;
	}
	@Override
	@JSONField(serialize=false)
	public String getSheetName() {
		return "竞价公告";
	}
	@Override
	@JSONField(serialize=false)
	public int getSheetIndex(){
		return 4;
	}
}
