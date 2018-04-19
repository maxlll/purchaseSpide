package wen.liu.entity;

public class ResultFlag {
	private boolean resume;
	private String maxDate;
	
	public ResultFlag(){}
	public ResultFlag(boolean resume, String maxDate){
		this.resume=resume;
		this.maxDate=maxDate;
	}
	
	public boolean isResume() {
		return resume;
	}
	public void setResume(boolean resume) {
		this.resume = resume;
	}
	public String getMaxDate() {
		return maxDate;
	}
	public void setMaxDate(String maxDate) {
		this.maxDate = maxDate;
	}
}
