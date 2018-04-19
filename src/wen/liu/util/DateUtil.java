package wen.liu.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class DateUtil {
	public static int compare(Date date1,Date date2,int type){
		Calendar ca1 = Calendar.getInstance();
		ca1.setTime(date1);
		Calendar ca2 = Calendar.getInstance();
		ca2.setTime(date2);
		ca1.get(type);
		return ca1.get(type)-ca2.get(type);
	}
	
	public static String transToStandDate(String date, String separater){
		if(StringUtils.isNotBlank(date)){
			String[] parts = date.split(separater);
			if(parts!=null && parts.length==3){
				if(parts[0].length()==2){
					parts[0] ="20"+parts[0];
				}
				if(parts[1].length()==1){
					parts[1] ="0"+parts[1];
				}
				if(parts[2].length()==1){
					parts[2] ="0"+parts[2];
				}
				return StringUtils.join(parts, separater);
			}
		}
		return date;
	}
	
	public static String transToStandTime(String time, String separater){
		if(StringUtils.isNotBlank(time)){
			String[] parts = time.split(separater);
			if(parts!=null && parts.length==3){
				for(int i=0;i<parts.length;i++){
					if(parts[i].length()==1){
						parts[i] ="0"+parts[i];
					}
				}
				return StringUtils.join(parts, separater);
			}
		}
		return time;
	}
}
