package bean;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
	private int category;
	private int page;
	private int freNum;
	private ArrayList<TreeNode> childList;
	public TreeNode next;
	public TreeNode parent;
	
	public TreeNode(){
		category = -1;
		freNum = 1;
		childList = new ArrayList<TreeNode>();
		next = null;
		parent = null;
	}
	
	public TreeNode(int c,int p){
		category = c;
		page = p;
		freNum = 1;
		childList = new ArrayList<TreeNode>();
		next = null;
		parent = null;
	}

	public int getFreNum() {
		return freNum;
	}

	public void setFreNum(int freNum) {
		this.freNum = freNum;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void addChild(TreeNode node) {
		childList.add(node);
	}

	public TreeNode getChild(int cate2Num,int page2Num) {
		TreeNode tempNode = null;
		
		for(int i=0;i<childList.size();i++){
			if(childList.get(i).getCategory() == cate2Num &&
					childList.get(i).getPage() == page2Num){
				tempNode = childList.get(i);
			}
		}
		
		return tempNode;
	}
	
	public TreeNode getChildAt(int i) {
		return childList.get(i);
	}
	

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}

	public ArrayList<TreeNode> getChildList() {
		return childList;
	}

	public void setChildList(ArrayList<TreeNode> childList) {
		this.childList = childList;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof TreeNode)){
			return false;
		}else{
			TreeNode node = (TreeNode)obj;
			
			if(node.getFreNum() == this.freNum &&
				node.getCategory() == this.category &&
				node.getPage() == this.page
				){
				return true;
			}
		}
		return false;
	}

	public void removeChild(TreeNode tempNode) {
		childList.remove(tempNode);
	}
	
}
