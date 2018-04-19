package wen.liu.entity;

import org.apache.poi.common.usermodel.HyperlinkType;

import wen.liu.annotation.ExcelData;

import com.alibaba.fastjson.annotation.JSONField;

@ExcelData(type=Const.typeEmallNotice)
public class EmallNotice extends SupplyNotice{
	@Override
	@JSONField(serialize=false)
	public String[] getTitleCells() {
		return new String[]{"编号","订单金额","采购单位","供应商","成交时间"};
	}

	@Override
	@JSONField(serialize=false)
	public String[] getCellValues() {
		return new String[]{code,turnover,purchaser,supplier,date};
	}
	@Override
	@JSONField(serialize=false)
	public ExcelLink getLink(int colNum) {
		if(colNum==0){
			return new ExcelLink(HyperlinkType.URL, url);
		}
		return null;
	}
	@Override
	@JSONField(serialize=false)
	public int[] getWidths() {
		return new int[]{6000,4000,9500,9000,6000};
	}
	@Override
	@JSONField(serialize=false)
	public short getHeight() {
		return (short)650;
	}
	@Override
	@JSONField(serialize=false)
	public String getSheetName() {
		return "电子商城订单";
	}
	@Override
	@JSONField(serialize=false)
	public int getSheetIndex(){
		return 3;
	}
}
