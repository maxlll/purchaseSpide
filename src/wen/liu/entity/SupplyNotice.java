package wen.liu.entity;

import org.apache.poi.common.usermodel.HyperlinkType;

import wen.liu.annotation.ExcelData;

import com.alibaba.fastjson.annotation.JSONField;

@ExcelData(type=Const.typeSupplyNotice)
public class SupplyNotice extends SpideData{
	@JSONField(ordinal=3)
	private String type;
	@JSONField(ordinal=4)
	protected String supplier;
	@JSONField(ordinal=5)
	protected String turnover;

	public String getSupplier() {
		return supplier;
	}

	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}

	public String getTurnover() {
		return turnover;
	}

	public void setTurnover(String turnover) {
		this.turnover = turnover;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	@JSONField(serialize=false)
	public String[] getTitleCells() {
		return new String[]{"编号","类型","采购单位","供应商","日期","成交总金额"};
	}

	@Override
	@JSONField(serialize=false)
	public String[] getCellValues() {
		return new String[]{code,type,purchaser,supplier,date,turnover};
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
		return new int[]{6000,4000,9000,9000,3500,4000};
	}
	@Override
	@JSONField(serialize=false)
	public short getHeight() {
		return (short)650;
	}
	@Override
	@JSONField(serialize=false)
	public String getSheetName() {
		return "协议供货";
	}
	@Override
	@JSONField(serialize=false)
	public int getSheetIndex(){
		return 2;
	}
}
