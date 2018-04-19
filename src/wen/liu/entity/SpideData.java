package wen.liu.entity;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

public abstract class SpideData implements ExcelEntity,Comparable<SpideData>{
	@JSONField(ordinal=0)
	protected String date;
	@JSONField(ordinal=1)
	protected String name;
	@JSONField(ordinal=2)
	protected String code;
	
	@JSONField(ordinal=6)
	protected String purchaser;
	@JSONField(ordinal=7)
	protected String address;
	@JSONField(ordinal=8)
	protected String contacts;
	@JSONField(ordinal=9)
	protected String phoneNo;
	@JSONField(ordinal=10)
	protected String url;

	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getPurchaser() {
		return purchaser;
	}
	public void setPurchaser(String purchaser) {
		this.purchaser = purchaser;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getContacts() {
		return contacts;
	}
	public void setContacts(String contacts) {
		this.contacts = contacts;
	}
	public String getPhoneNo() {
		return phoneNo;
	}
	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String toJSONString(){
		return JSON.toJSONString(this);
	}
	@Override
	public String toString(){
		return toJSONString();
	}
	@Override
	public int compareTo(SpideData data){
		if(data==null || StringUtils.isBlank(data.getDate()))
			return 1;
		return this.getDate().compareTo(data.getDate());
	}
}
