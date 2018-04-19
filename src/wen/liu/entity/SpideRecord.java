package wen.liu.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

public class SpideRecord {
	@JSONField(ordinal=0)
	private String type;
	@JSONField(ordinal=1)
	private String lastDate;
	@JSONField(ordinal=2)
	private int total;
	@JSONField(ordinal=3)
	private String excludes;
	
	public SpideRecord(){}
	public SpideRecord(String type){
		this.type=type;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLastDate() {
		return lastDate;
	}
	public void setLastDate(String lastDate) {
		this.lastDate = lastDate;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public String getExcludes() {
		return excludes;
	}
	public void setExcludes(String excludes) {
		this.excludes = excludes;
	}
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
