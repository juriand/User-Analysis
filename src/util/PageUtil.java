package util;

import java.util.ArrayList;

import bean.Category;

public class PageUtil {
	ArrayList<Category> cates;

	public PageUtil(ArrayList<Category> c){
		this.cates = c;
	}
	
	public int Cate2Num(String s){
		for(int i=0;i<cates.size();i++){
			if(cates.get(i).getCname().equals(s)){
				return i;
			}
		}
		return -1;
	}
	
	public String Num2Cate(int i){
		return cates.get(i).getCname();
	}
	
	public int Page2Num(String s, int cate){
		return cates.get(cate).indexOdfPage(s);
	}
	
	public String Num2Page(int n, int cate){
		return (cates.get(cate).getPage(n));
	}

	public ArrayList<Category> getCates() {
		return cates;
	}

	public void setCates(ArrayList<Category> cates) {
		this.cates = cates;
	}

	public int getCateNum(){
		return cates.size();
	}
	
	
}
