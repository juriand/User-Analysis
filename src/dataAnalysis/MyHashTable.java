package dataAnalysis;

import bean.User;

public class MyHashTable {
	int size;
	Node[] table;
	
	public MyHashTable(int size){
		this.size = size;
		table = new Node[size];
	}
	
	public void put(String ip, User value){
		Node node  = new Node(ip,value);
		int pos = hash(ip);
		if(table[pos] != null){
			Node curNode = table[pos];
			while(curNode.next != null){
				curNode = curNode.next;
			}
			curNode.next = node;
		}else{
			table[pos] = node;
		}
	}
	
	public int hash(String ip){
		long sum = 0;
		for(int i=0;i<ip.length();i++){
			int a = ip.charAt(i);
			if(a >= 48 && a <= 57){
				a = Character.getNumericValue(ip.charAt(i));
			}
			
			sum = sum + a;
		}
		for(int i=0;i<ip.length();i++){
			int a = ip.charAt(i);
			sum = sum*31 + a;
		}
		sum = sum&0x7FFFFFFF;
		return (int)(sum%size);
	}
	
	public boolean containsKey(String ip){
		int pos  = hash(ip);
		Node curNode = table[pos];
		if(curNode != null){
			while(curNode != null){
				if(curNode.getKey().equals(ip)){
					return true;
				}
				curNode = curNode.next;
			}
		}
		
		return false;
	}
	
	public int size(){
		return size;
	}
	
	public User get(String ip){
		int pos  = hash(ip);
		Node curNode = table[pos];
		if(curNode != null){
			while(curNode != null){
				if(curNode.getKey().equals(ip)){
					return curNode.getValue();
				}
				curNode = curNode.next;
			}
		}
		
		return null;
	}
	
	public Node get(int i){
		return table[i];
	}
	
	public int getChainLength(int i){
		Node curNode  = table[i];
		int num = 0;
		
		while(curNode != null){
			curNode = curNode.next;
			num++;
		}
		
		return num;	
	}
}
