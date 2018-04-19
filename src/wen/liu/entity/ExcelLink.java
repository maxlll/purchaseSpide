package wen.liu.entity;

import org.apache.poi.common.usermodel.HyperlinkType;

public class ExcelLink {
	private HyperlinkType type;
	private String address;
	
	public ExcelLink(HyperlinkType type, String address){
		this.type=type;
		this.address=address;
	}
	
	public HyperlinkType getType() {
		return type;
	}
	public String getAddress() {
		return address;
	}
}
