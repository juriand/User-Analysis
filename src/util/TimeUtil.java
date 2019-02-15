package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class TimeUtil {
	public long timeBetween(String startTime,String endTime){
		StringTokenizer st1 = new StringTokenizer(startTime,"/");
		String startDay = st1.nextToken();
		st1.nextToken();
		startTime = startDay + "/07/" + st1.nextToken();
		
		StringTokenizer st2 = new StringTokenizer(endTime,"/");
		String endDay = st2.nextToken();
		st2.nextToken();
		endTime = endDay + "/07/" + st2.nextToken();
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = sdf.parse(startTime);
			endDate = sdf.parse(endTime); 
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (endDate.getTime() - startDate.getTime());
	}

	public boolean lessThanTimeoutBetween(String startTime, String endTime) {
		StringTokenizer st1 = new StringTokenizer(startTime,"/");
		String startDay = st1.nextToken();
		st1.nextToken();
		startTime = startDay + "/07/" + st1.nextToken();
		
		StringTokenizer st2 = new StringTokenizer(endTime,"/");
		String endDay = st2.nextToken();
		st2.nextToken();
		endTime = endDay + "/07/" + st2.nextToken();
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = sdf.parse(startTime);
			endDate = sdf.parse(endTime); 
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (endDate.getTime() - startDate.getTime()) < 25.5*60*1000;
	}
}
