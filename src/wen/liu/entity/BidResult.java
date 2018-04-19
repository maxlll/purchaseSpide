package wen.liu.entity;

import org.apache.poi.common.usermodel.HyperlinkType;

import wen.liu.annotation.ExcelData;

import com.alibaba.fastjson.annotation.JSONField;

@ExcelData(type=Const.typeBidResult)
public class BidResult extends SupplyNotice{

	@Override
	@JSONField(serialize=false)
	public String[] getTitleCells() {
		return new String[]{"日期","项目名称","成交供应商","采购单位"};
	}

	@Override
	@JSONField(serialize=false)
	public String[] getCellValues() {
		return new String[]{date,name,supplier,purchaser};
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
		return new int[]{5500,10000,9300,10000};
	}
	@Override
	@JSONField(serialize=false)
	public short getHeight() {
		return (short)800;
	}
	@Override
	@JSONField(serialize=false)
	public String getSheetName() {
		return "竞价结果公告";
	}
	@Override
	@JSONField(serialize=false)
	public int getSheetIndex(){
		return 5;
	}
}
