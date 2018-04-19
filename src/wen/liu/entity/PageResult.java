package wen.liu.entity;

import java.util.ArrayList;
import java.util.List;

public class PageResult<T> {
	private List<T> datas=new ArrayList<T>();
	private Page page;
	
	public PageResult(){}
	public PageResult(List<T> datas, Page page){
		this.datas=datas;
		this.page=page;
	}
	
	public List<T> getDatas() {
		return datas;
	}
	public void setDatas(List<T> datas) {
		this.datas = datas;
	}
	public Page getPage() {
		return page;
	}
	public void setPage(Page page) {
		this.page = page;
	}
}
