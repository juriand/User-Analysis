package bean;

import java.util.ArrayList;

public class Category {
	private String cname;
	private ArrayList<String> pages;
	
	public Category(String c){
		cname = c;
		pages = new ArrayList<String>();
	}

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public ArrayList<String> getPages() {
		return pages;
	}

	public void setPages(ArrayList<String> pages) {
		this.pages = pages;
	}
	
	public String getPage(int n){
		return pages.get(n);
	}
	
	public int indexOdfPage(String s){
		return pages.indexOf(s);
	}
	
	public void addPage(String s){
		pages.add(s);
	}
	
	public int size(){
		return pages.size();
	}

	@Override
	public boolean equals(Object obj) {
		Category c = (Category)obj;
		if(c.getCname().equals(this.getCname())){
			return true;
		}else{
			return false;
		}
	}
	
	
}
