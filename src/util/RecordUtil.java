package util;

import java.util.StringTokenizer;

public class RecordUtil {
//	public String getCategory(String record){
//		
//	}
//	
//	public String getPage(String record){
//		
//	}
	
	public String getTimeStamp(String record){
		StringTokenizer st1 = new StringTokenizer(record,"[");
		st1.nextToken();
		String timeStmp = st1.nextToken();
		StringTokenizer st11 = new StringTokenizer(timeStmp,"]");
		timeStmp = st11.nextToken();
		StringTokenizer st111 = new StringTokenizer(timeStmp);
		timeStmp = st111.nextToken();
		
		return timeStmp;
	}
	
	public String getIp(String record){
		StringTokenizer st = new StringTokenizer(record);
		String ip = st.nextToken();
		
		return ip;
	}
	
	public String getPageSize(String record){
		StringTokenizer st = new StringTokenizer(record,"\"");
		st.nextToken();
		st.nextToken();
		String pageSize = st.nextToken();
		
		StringTokenizer st1 = new StringTokenizer(pageSize);
		st1.nextToken();
		pageSize = st1.nextToken();

		return pageSize;
	}
	
	public String getCode(String record){
		StringTokenizer st = new StringTokenizer(record,"\"");
		st.nextToken();
		String code = "";
		if(st.hasMoreTokens()){
			code = st.nextToken();
		} 
		
		return code;
	}
	
	public String getStatus(String record){
		StringTokenizer st = new StringTokenizer(record,"\"");
		st.nextToken();
		String status = "";
		if(st.hasMoreTokens()){
			st.nextToken();
			status = st.nextToken();
		} 
		
		return status;
	}
	
	public String getUrl(String record){
		StringTokenizer st2 = new StringTokenizer(record);
		String url = "";	
		if(st2.hasMoreTokens()){
			st2.nextToken();
		}
		if(st2.hasMoreTokens()){
			url = st2.nextToken();
		}
		
		return url;
	}
	
	
}
