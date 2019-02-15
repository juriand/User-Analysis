package bean;

import java.util.ArrayList;

public class Session {
	private int id;
	private String ip;
	private int clusterId;
	private ArrayList<Integer> pageVector;
	private ArrayList<Integer> sequence;
	private ArrayList<Float> distance;
	
	public Session(){
		distance = new ArrayList<Float>();
		pageVector = new ArrayList<Integer>();
		sequence = new ArrayList<Integer>();
	}
	
	public Session(int id,String ip){
		this.id = id;
		this.ip = ip;
		distance = new ArrayList<Float>();
		pageVector = new ArrayList<Integer>();
		sequence = new ArrayList<Integer>();
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

	public ArrayList<Integer> getPageVector() {
		return pageVector;
	}

	public void setPageVector(ArrayList<Integer> pageVector) {
		this.pageVector = pageVector;
	}

	public ArrayList<Integer> getSequence() {
		return sequence;
	}

	public void setSequence(ArrayList<Integer> sequence) {
		this.sequence = sequence;
	}

	public void addPageVector(int f){
		pageVector.add(f);
	}
	
	public void addSequence(int s){
		sequence.add(s);
	}
	
	public int vectorLength(){
		return pageVector.size();
	}
	
	public int sequenceLength(){
		return sequence.size();
	}
	
	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}

	public float getDistance(int i){
		return distance.get(i);
	}

	public void setDistance(int i,float content){
		distance.set(i, content);
	}
	
	public void addDistance(float d){
		distance.add(d);
	}
	
	public float[] vector2Array(){
		float[] array = new float[pageVector.size()];
		for(int i=0;i<pageVector.size();i++){
			array[i] = pageVector.get(i);
		}
		return array;
	}
	
	public float[] first2Vector(){
		float[] array = new float[pageVector.size()];
		for(int i=0;i<pageVector.size();i++){
			array[i] = 0;
		}
		
		for(int i=0;i<Math.min(2, sequence.size());i++){
			array[sequence.get(i)] = 1;
		}
		return array;
	}
	
	public int getVector(int i){
		return pageVector.get(i);
	}
	
	public int getSequence(int i){
		return sequence.get(i);
	}
	
	public int getBrowsingNum(){
		int num = 0;
		for(int i=0;i<pageVector.size();i++){
			if(pageVector.get(i) == 1){
				num++;
			}
		}
		return num;
	}
	
	@Override
	public String toString() {
		String string="";
		
		for(int i=0;i<sequence.size();i++){
			string = string + sequence.get(i)+" ";
		}
		
		return string;
	}

}
