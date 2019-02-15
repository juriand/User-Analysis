package clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Map.Entry;

import bean.Category;
import bean.HisRecord;
import bean.User;
import dataAnalysis.MyHashTable;
import dataAnalysis.Node;
import util.RecordUtil;
import util.TimeUtil;

import java.util.StringTokenizer;

public class Preprocessing {
	ArrayList<String> record;
	ArrayList<User> trainUser;
	ArrayList<User> testUser;
	HashMap<String,User> userMap;
//	MyHashTable userMap;
	ArrayList<HisRecord> hisRecords;
	
	int layer;
	int trainSize;
	int testSize;
	
	Random random;
	
	TimeUtil timeUtil;
	RecordUtil recordUtil;
	
	public Preprocessing(int layer,int trainSize, int testSize){
		this.layer = layer;
		this.trainSize = trainSize;
		this.testSize = testSize;
		
		record = new ArrayList<String>();
		trainUser = new ArrayList<User>();
		testUser = new ArrayList<User>();
		userMap = new HashMap<String,User>();
//		userMap = new MyHashTable(2000);
		hisRecords = new ArrayList<HisRecord>();
		
		random = new Random();
		timeUtil = new TimeUtil();
		recordUtil = new RecordUtil();
	}
	
	public void readDataSet(){
		File file = new File("data/access_log_Jul95");
		BufferedReader reader = null;
		
		try{
			reader = new BufferedReader(new FileReader(file));
			String temp = null;
			while((temp = reader.readLine()) != null){
				String code = recordUtil.getCode(temp);
				String status= recordUtil.getStatus(temp);
				String url = recordUtil.getUrl(code);

				if(code.startsWith("GET") && 
						(url.endsWith("html") || url.endsWith("jsp")) && 
						status.startsWith(" 200") ){
					record.add(temp);
				}
			}
			reader.close();
		}catch(IOException e){
			
		}
		
		System.out.println("===========finish read data===========");
	}
	
	public void readHisRecord(){
		File file = new File("data/history.txt");
		BufferedReader reader = null;
		
		try{
			reader = new BufferedReader(new FileReader(file));
			String temp = null;
			while((temp = reader.readLine()) != null){
				StringTokenizer st = new StringTokenizer(temp);
				String cate = st.nextToken();
				String page = st.nextToken();
				int status = Integer.parseInt(st.nextToken());
				
				HisRecord tempHis = new HisRecord(cate,page,status);
				hisRecords.add(tempHis);
			}
			reader.close();
		}catch(IOException e){
			
		}
	}

	public void userIdentify(){			
		for(int i=0;i<record.size();i++){
			String ip = recordUtil.getIp(record.get(i));
			
			if(!userMap.containsKey(ip)){
				User user = new User(0,ip);
				userMap.put(ip, user);
			}
			
			String timeStmp = recordUtil.getTimeStamp(record.get(i));
			String pageSize = recordUtil.getPageSize(record.get(i));
			
			StringTokenizer st2 = new StringTokenizer(record.get(i),"\"");
			st2.nextToken();
			String code = "";
			if(st2.hasMoreTokens()){
				code = st2.nextToken();
			} 
			
			StringTokenizer st3 = new StringTokenizer(code);
			String url = "";	
			if(st3.hasMoreTokens()){
				st3.nextToken();
			}
			if(st3.hasMoreTokens()){
				url = st3.nextToken();
			}
			
			StringTokenizer st4 = new StringTokenizer(url,"/");
			int l=0;
			if((st4.countTokens() - 1) <= layer){
				l = st4.countTokens() - 2;
			}else{
				l = layer - 1;
			}
			for(int j=0;j<l;j++){
				st4.nextToken();
			}
			
			String category = st4.nextToken();
			String page = "";
			while(st4.hasMoreTokens()){
				page = st4.nextToken();
			}
			if(page.contains(".html")){
				StringTokenizer st5 = new StringTokenizer(page,".");
				page = st5.nextToken();
			}

			if((!category.endsWith(".html")) && !page.equals("")){
				String[] temp = new String[4];
				temp[0] = category;
				temp[1] = page;
				temp[2] = timeStmp;
				temp[3] = pageSize;
				
				userMap.get(ip).addPage(temp);
			}
		}
		
		System.out.println("============finish identify user=============");
	}
	
	public ArrayList<User> userSessionIdentify(){
		ArrayList<User> users = new ArrayList<User>();
		Iterator it = userMap.entrySet().iterator();
		
		int userNo = 0;
		int largeIntervalTime = 10;
		int a = 1;
		int b = 1;
		
		float dyAverStayTime = 0;
		float dyThreshold = 16;
		float averPageSize = 0;
		
		//Dynamic timeout
		while(it.hasNext()){
		Entry entry = (Entry) it.next();
		User user = (User)entry.getValue();
//		User user = null;
//		Node curNode=null;
//		int tableNo=0;
//		while(true){
//		if(tableNo >= userMap.size()){
//			break;
//		}
//		
//		curNode = userMap.get(tableNo);
//		if(curNode == null){
//			tableNo++;
//			continue;
//		}
//		user = curNode.getValue();
		
		if(user.getPageList().size() > 0){
			String startTime = user.getPageList().get(0)[2];
			User tempSession = new User(userNo,user.getIp());
			averPageSize = Float.parseFloat(user.getPageList().get(0)[3]);
					
			for(int i=0;i<user.getPageList().size();i++){				
				String endTime = user.getPageList().get(i)[2];
				float stayTime = timeUtil.timeBetween(startTime, endTime)/(float)(60*1000);
				
				if(stayTime <= dyThreshold){
					tempSession.addPage(user.getPageList().get(i));
					averPageSize = (averPageSize + Float.parseFloat(user.getPageList().get(i)[3])) / 2;
					
					if(stayTime > largeIntervalTime){
						dyAverStayTime = a * (dyAverStayTime + stayTime) / 2;
						dyThreshold = dyAverStayTime * b * (Float.parseFloat(user.getPageList().get(i)[3]) / averPageSize);
					}
					
				}else{
					averPageSize = Float.parseFloat(user.getPageList().get(i)[3]);
					dyAverStayTime = 0;
					dyThreshold = 16;
					
					users.add(tempSession);
					userNo++;
					startTime = user.getPageList().get(i)[2];
					tempSession = new User(userNo,user.getIp());
					tempSession.addPage(user.getPageList().get(i));
				}
			}
			users.add(tempSession);
			userNo++;
		}
//		curNode = curNode.next;
	} 
		//Static timeout
//		while(it.hasNext()){
//			Entry entry = (Entry) it.next();
//			User user = (User)entry.getValue();
//			
//			if(user.getPageList().size() > 0){
//				String startTime = user.getPageList().get(0)[2];
//				User tempUser = new User(userNo,user.getIp());
//				for(int i=0;i<user.getPageList().size();i++){				
//					String endTime = user.getPageList().get(i)[2];
//					if(timeUtil.lessThanTimeoutBetween(startTime, endTime)){
//						tempUser.addPage(user.getPageList().get(i));
//					}else{
//						users.add(tempUser);
//						userNo++;
//						startTime = user.getPageList().get(i)[2];
//						tempUser = new User(userNo,user.getIp());
//						tempUser.addPage(user.getPageList().get(i));
//					}
//				}
//				users.add(tempUser);
//				userNo++;
//			}
//		} 
		System.out.println("============finish identify user session=============");
		return users;
	}
	
	public void getSample(ArrayList<User> users){
		ArrayList<Integer> randomNum = new ArrayList<Integer>();
		int num = 0;
		int tempRan = 0;
		while(num < (trainSize + testSize) ){
			tempRan = random.nextInt(users.size());
			if(!randomNum.contains(tempRan) && users.get(tempRan).getRecordList().size() > 2){
				randomNum.add(tempRan);
				num++;
			}else{
				continue;
			}	
		}

		for(int i=0;i<(trainSize + testSize);i++){
			if(i<trainSize){
				trainUser.add(users.get(randomNum.get(i)));
			}else{
				testUser.add(users.get(randomNum.get(i)));
			}
		}
		randomNum.clear();
		randomNum = null;
		System.out.println("===========finish select sample data===========");
	}
	
	public ArrayList<Category> getCategoryList(){
		ArrayList<Category> cates = new ArrayList<Category>();
		
		for(int i=0;i<trainUser.size();i++){
			for(String s:trainUser.get(i).getCategoryList()){
				Category tempCate = new Category(s);
				if(!cates.contains(tempCate)){
					cates.add(tempCate);
				}
			}
		} 
		
		for(int i=0;i<testUser.size();i++){
			for(String s:testUser.get(i).getCategoryList()){
				Category tempCate = new Category(s);
				if(!cates.contains(tempCate)){
					cates.add(tempCate);
				}
			}
		}
		
		return cates;
	}

	public ArrayList<User> getTrainUser() {
		return trainUser;
	}

	public ArrayList<User> getTestUser() {
		return testUser;
	}
	
	public ArrayList<HisRecord> getHisRecords() {
		return hisRecords;
	}

	public void dataPreprocessing(){
		readDataSet();
		readHisRecord();
//		long startTime = System.currentTimeMillis();
		userIdentify();
//		long endTime = System.currentTimeMillis();
//		System.out.println("running time=" + (endTime - startTime)+ "ms");
//		saveUserMap();
		getSample(userSessionIdentify());
	}
	
//	public void saveUserMap(){
//		File file = new File("test/hashtable_x.txt");
//        FileWriter fw = null;
//        BufferedWriter writer = null;
//        try {
//            fw = new FileWriter(file);
//            writer = new BufferedWriter(fw);
//            for(int i=0;i<userMap.size();i++)
//    		{
//    			writer.write((i+1)+"");
//                writer.newLine();
//    		}
//            writer.flush();
//            writer.close();
//            fw.close();
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//        
//        file = new File("test/hashtable_y.txt");
//        fw = null;
//        writer = null;
//        try {
//            fw = new FileWriter(file);
//            writer = new BufferedWriter(fw);
//            for(int i=0;i<userMap.size();i++)
//    		{
//    			writer.write(userMap.getChainLength(i)+"");
//                writer.newLine();
//    		}
//            writer.flush();
//            writer.close();
//            fw.close();
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//	}
}
