package bean;

import java.util.ArrayList;

public class User {
	private int id;
	private String ip;
	private ArrayList<String[]> recordList;
	
	public User(int id,String ip){
		this.id = id;
		this.ip = ip;
		recordList = new ArrayList<String[]>();
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<String[]> getPageList() {
		return recordList;
	}

	public void setPageList(ArrayList<String[]> recordList) {
		this.recordList = recordList;
	}
	
	public void addPage(String[] p){
		recordList.add(p);
	}
	
	public boolean havePage(String cate){
		for(int i=0;i<recordList.size();i++){
			if(recordList.get(i)[0].equals(cate)){
				return true;
			}
		}
		return false;
	}
	
	public boolean havePage(String page,String cate){
		for(int i=0;i<recordList.size();i++){
			if(recordList.get(i)[0].equals(cate) && recordList.get(i)[1].equals(page)){
				return true;
			}
		}
		return false;

	}
	
//	public boolean transfer(String page1, String page2){
//		for(int i=0;i<recordList.size();i++){
//			if(recordList.get(i)[0].equals(page1) && (i + 1) < recordList.size() && recordList.get(i+1)[0].equals(page2)){
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	public boolean transfer(String cate1,String cate2,String page1, String page2){
//		for(int i=0;i<recordList.size();i++){
//			if(recordList.get(i)[0].equals(cate1) && recordList.get(i)[1].equals(page1) 
//					&& (i + 1) < recordList.size() 
//					&& recordList.get(i+1)[0].equals(cate2) && recordList.get(i+1)[1].equals(page2)){
//				return true;
//			}
//		}
//		return false;
//	}
	
//	public boolean haveNext(String cate){
//		for(int i=0;i<recordList.size();i++){
//			if(recordList.get(i)[0].equals(cate) && (i + 1) < recordList.size()){
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	public boolean haveNext(String page,String cate){
//		for(int i=0;i<recordList.size();i++){
//			if(recordList.get(i)[0].equals(cate) && recordList.get(i)[1].equals(page) && (i + 1) < recordList.size()){
//				return true;
//			}
//		}
//		return false;
//	}
	
//	@Override
//	public int hashCode(){
//		int sum = 0;
//		for(int i=0;i<ip.length();i++){
//			int a = ip.charAt(i);
//			sum = sum + a;
//		}
//		return sum%1;
//	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof User && this.id == (((User)obj).getId());
	}

	public ArrayList<String> getCategoryList() {
		ArrayList<String> cates = new ArrayList<String>();
		
		for(int i=0;i<recordList.size();i++){
			cates.add(recordList.get(i)[0]);
		}
		
		return cates;
	}
	
	public ArrayList<String> getAllPageOfCategory(String cname){
		ArrayList<String> pages = new ArrayList<String>();
		
		for(int i=0;i<recordList.size();i++){
			if(recordList.get(i)[0].equals(cname)){
				pages.add(recordList.get(i)[1]);
			}
		}
		return pages;
	}

	public ArrayList<String[]> getTestPageList() {
		ArrayList<String[]> pages = new ArrayList<String[]>();
		
		for(int i=0;i<Math.min(2, recordList.size());i++){
			pages.add(recordList.get(i));
		}
		return pages;
	}

	public ArrayList<String[]> getRecordList() {
		return recordList;
	}

	public void setRecordList(ArrayList<String[]> recordList) {
		this.recordList = recordList;
	}
	
	public String[] getRecord(int i){
		return recordList.get(i);
	}

}
