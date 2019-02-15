package bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Cluster {
	private int id;
	private ArrayList<Session> datas;
	
	public Cluster(){
		datas = new ArrayList<Session>();
	}
	
	public Cluster(int id){
		this.id = id;
		datas = new ArrayList<Session>();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<Session> getDatas() {
		return datas;
	}

	public void setDatas(ArrayList<Session> datas) {
		this.datas = datas;
	}
	
	public void addDatas(Session d){
		datas.add(d);
	}
	
	public int size(){
		return datas.size();
	}
	
	public void removeAllData(){
		datas.clear();
	}

	public void add(Session session) {
		datas.add(session);	
	}
	
	public void combine(Cluster c){
		for(int i=0;i<c.size();i++){
			c.getDatas().get(i).setClusterId(this.id);
			this.addDatas(c.getDatas().get(i));
		}
		
		c.removeAllData();
	}
	
	public float[] getWeight(){
		int singleLength = datas.get(0).getPageVector().size();
		//Credit weight
		float[] PMed = new float[singleLength];
		int numOfOne = 0;
		for(int i=0;i<singleLength;i++){
			for(int j=0;j<datas.size();j++){
				if(datas.get(j).getPageVector().get(i) == 1){
					numOfOne++;
				}
			}
			
			if(numOfOne/singleLength > 0.5){
				PMed[i] = 1;
			}else if(numOfOne/singleLength < 0.5){
				PMed[i] = 0;
			}else{
				PMed[i] = (float) 0.5;
			}
			numOfOne = 0;
		}
		
		float[] pa = new float[datas.size()];
		float sum = 0;
		for(int j=0;j<datas.size();j++){
			for(int i=0;i<singleLength;i++){
				sum = sum + (float)Math.pow(datas.get(j).getVector(i) - PMed[i], 2);
			}
			
			pa[j] = (float) (1/(1 + Math.sqrt(sum)));
		}
		
		//Browsing frequency weight
		float[] weight = new float[datas.size()];
		float sumBrowsingNum = 0;
		for(int j=0;j<datas.size();j++){
			sumBrowsingNum = sumBrowsingNum + datas.get(j).getBrowsingNum()*pa[j];
		}
		
		for(int j=0;j<datas.size();j++){
			weight[j] = (float)datas.get(j).getBrowsingNum()* pa[j] / sumBrowsingNum;
		}
		
		return weight;
	}
	
	public Session get(int i){
		return datas.get(i);
	}

	@Override
	public String toString() {
		String print = "";
		for(int i=0;i<datas.size();i++){
			if(!datas.get(i).toString().equals("")){
				print= print +"{"+datas.get(i).toString()+"}"+"\n";
			}	
		}
		
		return print;
	}

}
