package bean;

public class HisRecord {
	private int status;
	private String category;
	private String page;
	
	public HisRecord(String category,String page,int status) {
		this.category = category;
		this.page = page;
		this.status = status;
	}

	public HisRecord(String category,String page) {
		this.category = category;
		this.page = page;
		status = 1;
	}

	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void changeStatus(int num){
		if(num > 0){
			if(status == 1){
				return;
			}else{
				status = status + num;
			}
		}else{
			if(status == -2){
				return;
			}else{
				status = status + num;
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof HisRecord)){
			return false;
		}else{
			HisRecord node = (HisRecord)obj;
			
			if(node.getCategory().equals(category) &&
				node.getPage().equals(page)
				){
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String s = category + " " + page + " " + status ;
		
		return s;
	}

	
}
