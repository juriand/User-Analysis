package clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import twolevel.Twolevel;
import util.PageUtil;
import bean.Cluster;
import bean.HisRecord;
import bean.User;
import bean.Category;
import bean.Session;

public class Hierclustering {
	private int finalCluNum;
	private int layer;
	private int trainSize;
	private int testSize;
	
	private ArrayList<Cluster> hierclusters;
	private ArrayList<Session> dataSet;
	private ArrayList<Session> testSet;
	private ArrayList<HisRecord> hisRecords;
	private ArrayList<float[]> center;
	
	private Preprocessing preprocess;

	private PageUtil pageUtil;
	
	public Hierclustering(int finalCluNum, int layer, int trainSize, int testSize){
		if(finalCluNum <= 0){
			this.finalCluNum = 1;
		}else{
			this.finalCluNum = finalCluNum;
		}
		this.layer = layer;
		this.trainSize = trainSize;
		this.testSize = testSize;
	}
	
	public void init(){
		center = new ArrayList<float[]>();
		hisRecords = new ArrayList<HisRecord>();
		preprocess = new Preprocessing(layer,trainSize,testSize);
		preprocess.dataPreprocessing();
		pageUtil = new PageUtil(preprocess.getCategoryList());
		
		if(dataSet == null || dataSet.size() == 0){
			dataSet = initData(dataSet, preprocess.getTrainUser(),false);
			testSet = initData(testSet, preprocess.getTestUser(),true);
		}
		
		initCluster();
	}
	
	public ArrayList<Session> initData(ArrayList<Session> dataSet, ArrayList<User> userMap, boolean isTest){
		dataSet = new ArrayList<Session>();
		User user = new User(0,"");

		for(int i=0;i<userMap.size();i++){
			if(isTest){
				User user1 = userMap.get(i);
				user.setIp(user1.getIp());
				user.setId(user1.getId());
				
				//delete last page for predict
				for(int h=0;h<user1.getPageList().size()-1;h++){
					String[] tempPage = new String[2];
					tempPage[0] = user1.getPageList().get(h)[0];
					tempPage[1] = user1.getPageList().get(h)[1];
					user.addPage(tempPage);
				}
				
			}else{
				user = userMap.get(i);
			}
			
			//Session sequence
			Session session = new Session(user.getId(),user.getIp());
			for(int j=0;j<user.getCategoryList().size();j++){
				session.addSequence(pageUtil.Cate2Num(user.getCategoryList().get(j)));
			}
			
			//Session vector
			for(Category c:pageUtil.getCates()){
				if(user.havePage(c.getCname())){
					session.addPageVector(1);
				}else{
					session.addPageVector(0);
				}	
			}
			dataSet.add(session);
		}

		return dataSet;
	}

	public void initCluster() {
		hierclusters = new ArrayList<Cluster>();

		for(int i=0;i<dataSet.size();i++){
			Cluster c = new Cluster(i);
			dataSet.get(i).setClusterId(i);
			c.addDatas(dataSet.get(i));
			hierclusters.add(c);
		}
	}
	
	public int getMaxCommSeq(Session x, Session y){
		int[][] dp = new int[x.sequenceLength()][y.sequenceLength()];
		
		if(x.getSequence(0) == y.getSequence(0)){
			dp[0][0] = 1;
		}else{
			dp[0][0] = 0;
		}
		
		for(int i=1;i<x.sequenceLength();i++){
			int temp;
			if(x.getSequence(i) == y.getSequence(0)){
				temp = 1;
			}else{
				temp = 0;
			}
			dp[i][0] = Math.max(dp[i-1][0], temp);
		}
		
		for(int j=1;j<y.sequenceLength();j++){
			int temp;
			if(y.getSequence(j) == y.getSequence(0)){
				temp = 1;
			}else{
				temp = 0;
			}
			dp[0][j] = Math.max(dp[0][j-1], temp);
		}
		
		for(int i = 1;i<x.sequenceLength();i++){
			for(int j=1;j<y.sequenceLength();j++){
				dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
				if(x.getSequence(i) == y.getSequence(j)){
					dp[i][j] = Math.max(dp[i][j], dp[i-1][j-1]+1);
				}
			}
		}
		
		//get sequence from dp
		int m = x.sequenceLength()-1;
		int n = y.sequenceLength()-1;
		int maxCommSeq = 0;
		float[] res = new float[dp[m][n]];
		int index = res.length-1;
		while(index >= 0){
			if( n > 0 && dp[m][n] == dp[m][n-1]){
				n--;
			}else if( m > 0 && dp[m][n] == dp[m-1][n]){
				m--;
			}else{
				res[index--] = x.getSequence(m);
				m--;
				n--;
				maxCommSeq++;
			}
		}
		dp = null;
		
		return maxCommSeq;
		
	}
	
	public int getSetResult(ArrayList<Integer> setX, ArrayList<Integer> setY, int flag){
		if(flag == 1){
			//Get intersection
			int size = 0;
			for(int i=0;i<setX.size();i++){
				if( setX.get(i) == setY.get(i)){
					size++;
				}
			}
			return size;
		}else if(flag == 2){
			//Get union
			int size = setX.size();
			return size;
		}
		return 0;
	}
	
	public float getDistance(Session x, Session y){
		if(x.sequenceLength() == 0 && y.sequenceLength() == 0){
			return 1;
		}
		if(x.sequenceLength() == 0 || y.sequenceLength() == 0){
			return 0;
		}

		int maxSize = Math.max(x.sequenceLength(), y.sequenceLength());
		int maxCommSeq = getMaxCommSeq(x,y);

		int interSet = getSetResult(x.getPageVector(),y.getPageVector(),1);
		int unionSet = getSetResult(x.getPageVector(),y.getPageVector(),2);
		
		float seqSim = (float)maxCommSeq / (float)maxSize;
		float setSim = (float)interSet / (float)unionSet;

		return (float) (seqSim*0.5 + setSim*0.5);
	}
	
	public void calculateAllDistance(){	
		for(int i=0;i<dataSet.size();i++){
			for(int j=i;j<dataSet.size();j++){
				if( i == j){
					dataSet.get(i).addDistance(0);
					continue;
				}
				float distance = getDistance(dataSet.get(i),dataSet.get(j));
				dataSet.get(i).addDistance(distance);
				dataSet.get(j).addDistance(distance);
			}
		}
	}
	
	public void combineCluster(int id1,int id2){
		Cluster clus1 = new Cluster();
		Cluster clus2 = new Cluster();
		
		for(Cluster c:hierclusters){
			if(c.getId() == id1){
				clus1 = c;
			}
			if(c.getId() == id2){
				clus2 = c;
			}
		}
		
		clus1.combine(clus2);
		
		//update distance
		for(int i=0;i<dataSet.size();i++){
			for(int j=i; j<dataSet.size();j++){
				if(dataSet.get(i).getClusterId() == dataSet.get(j).getClusterId()){
					dataSet.get(i).setDistance(j, 0);
					dataSet.get(j).setDistance(i, 0);
				}
			}
		}

	}
	
	public void findMaxDistance(){
		float maxDis = 0;
		int d1 = 0;
		int d2 = 0;
		int clus1 = 0;
		int clus2 = 0;
		
		for(int i=0;i<dataSet.size();i++){
			for(int j=0;j<dataSet.size();j++){
				if(dataSet.get(i).getDistance(j) > maxDis){
					maxDis = dataSet.get(i).getDistance(j);
					d1 = i;
					d2 = j;
				}
			}
		}
		
		clus1 = dataSet.get(d1).getClusterId();
		clus2 = dataSet.get(d2).getClusterId();
		
		if(clus1 == clus2){
			dataSet.get(d1).setDistance(d2, 0);
			dataSet.get(d2).setDistance(d1, 0);
		}else{
			combineCluster(clus1,clus2);
			
			Cluster tempClus = new Cluster();
			for(Cluster c:hierclusters){
				if(c.getId() == clus2){
					tempClus = c;
				}
			}
			hierclusters.remove(tempClus);
		}
	}

	public void getRepreCenter(ArrayList<Cluster> clusters){
		for(Cluster c:clusters){
			float[] tempCenter = new float[pageUtil.getCateNum()];
			float[] weight = c.getWeight();
			
			for(int i=0;i<tempCenter.length;i++){
				tempCenter[i] = 0;
				for(int j=0;j<c.size();j++){
					tempCenter[i] = tempCenter[i] + c.get(j).getVector(i)*weight[j];
				}	
			}
			center.add(tempCenter);
		}
	}
	
	public float getDistanceInCluster(float[] center,float[] data){
		float distance = 0;
		
		for(int i=0;i<center.length;i++){
			float a = data[i] - center[i];
			distance = a*a + distance;
		}
		
		return distance;
	}
	
	public void twoLevelPreprcoessing(ArrayList<Session> testData){	
		int clusterNo = 0;
		int requestNum = 0;
		int cacheNum = 0;
		float Hitratio = 0;
		float minDistance = 0;
		
		getRepreCenter(hierclusters);
		ArrayList<ArrayList<User>> testMapForCluster = new ArrayList<ArrayList<User>>();
		for(int i=0;i<hierclusters.size();i++){
			testMapForCluster.add(new ArrayList<User>());
		}
		
		//Assign test users
		for(int i=0;i<testData.size();i++){
			clusterNo = 0;
			minDistance = getDistanceInCluster(center.get(0),testData.get(i).vector2Array());
			for(int j=1;j<center.size();j++){
				if(getDistanceInCluster(center.get(j),testData.get(i).vector2Array()) < minDistance ){
					clusterNo = j;
					minDistance = getDistanceInCluster(center.get(j),testData.get(i).vector2Array());
				}
			}

			User testUser=null;
			for(int n=0;n<preprocess.getTestUser().size();n++){
				if(preprocess.getTestUser().get(n).getId() == testData.get(i).getId()){
					testUser = preprocess.getTestUser().get(n);
				}
			}
			
			testMapForCluster.get(clusterNo).add(testUser);
		}
		
		for(int i=0;i<hierclusters.size();i++){
			if(hierclusters.get(i).size() == 0 || testMapForCluster.get(i).size() ==0){
				continue;
			}
			//Get all users in the cluster
			ArrayList<User> clusterUsers = new ArrayList<User>();
			for(int trainNo=0;trainNo<preprocess.getTrainUser().size();trainNo++){
				for(int sessionNo=0;sessionNo<hierclusters.get(i).getDatas().size();sessionNo++){
					if(preprocess.getTrainUser().get(trainNo).getId() == hierclusters.get(i).getDatas().get(sessionNo).getId()){
						clusterUsers.add(preprocess.getTrainUser().get(trainNo));
					}
				}
			}
			
			Twolevel twoLevel = new Twolevel(testMapForCluster.get(i),clusterUsers,hisRecords);
			twoLevel.predict();
			
			hisRecords = twoLevel.getHisRecords();
			requestNum = requestNum + twoLevel.getRequestNum();
			cacheNum = cacheNum + twoLevel.getCacheNum();

			Hitratio = (float)cacheNum / (float)requestNum;
			System.out.println("No."+i+" Hit ratio:"+Hitratio);	
		}

	}
	
	public void clustering(){
		long startTime = System.currentTimeMillis();
		System.out.println("layer:"+layer+" hierarchical clustering begins");
		
		init();	
		calculateAllDistance();
		
		while(hierclusters.size() > finalCluNum ){
			findMaxDistance();
		}
		
		System.out.println("hierarchical clustering ends");
		System.out.println("two-level predict");
		twoLevelPreprcoessing(testSet);
		
		long endTime = System.currentTimeMillis();
		System.out.println("hierarchical clustering running time=" + (endTime - startTime)+"ms");
	}

	public void setDataSet(ArrayList<Session> dataSet) {
		this.dataSet = dataSet;
	}
	
	public String printCluster(Cluster c) {
		return "[" + c.getId() + "]=" + c.toString();
	}

	public ArrayList<Cluster> getClusters() {
		return hierclusters;
	}
	
}
