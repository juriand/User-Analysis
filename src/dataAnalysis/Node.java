package dataAnalysis;

import bean.User;

public class Node {
	String key;
	User value;
	public Node next;
	
	public Node(String key,User value){
		this.key = key;
		this.value = value;
		next = null;
	}
	
	public String getKey(){
		return key;
	}
	
	public User getValue(){
		return value;
	}
	
}
