package clustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import bean.Category;
import bean.Cluster;
import bean.HisRecord;
import bean.Session;
import bean.User;
import twolevel.Twolevel;
import util.PageUtil;

public class Kmeans {
	private int k;
	private int singleLength;
	private int layer;
	private int trainSize;
	private int testSize;
	
	private float[] probability;
	
	private boolean isEnd;
	
	private ArrayList<Session> dataSet;
	private ArrayList<Session> testSet;
	private ArrayList<float[]> center;
	private ArrayList<Cluster> kcluster;
	private ArrayList<HisRecord> hisRecords;
	
	private Preprocessing preprocess;
	private PageUtil pageUtil;
	
	public Kmeans(int layer, int trainSize, int testSize){
		this.layer = layer;
		this.trainSize = trainSize;
		this.testSize = testSize;
	}
	
	public void init(){
		k = 2;
		isEnd = false;

		preprocess = new Preprocessing(layer,trainSize,testSize);
		preprocess.dataPreprocessing();
		hisRecords = preprocess.getHisRecords();
		pageUtil = new PageUtil(preprocess.getCategoryList());
		
		if(dataSet == null || dataSet.size() == 0){
			dataSet = initData(dataSet, preprocess.getTrainUser(),false);
			testSet = initData(testSet, preprocess.getTestUser(),true);
		}
		
		singleLength = pageUtil.getCateNum();
		probability = new float[dataSet.size()];
		
		initCenter();
		initCluster();
		
	}
	
	public ArrayList<Session> initData(ArrayList<Session> dataSet, ArrayList<User> userMap, boolean isTest){
		dataSet = new ArrayList<Session>();
		User user = new User(0,"");
		
		for(int i=0;i<userMap.size();i++){
//			if(isTest){
//				User user1 = userMap.get(i);
//				user.setId(user1.getId());
//				user.setIp(user1.getIp());
//				
//				//delete last page for predict
//				for(int h=0;h<user1.getPageList().size()-1;h++){
//					String[] tempPage = new String[2];
//					tempPage[0] = user1.getPageList().get(h)[0];
//					tempPage[1] = user1.getPageList().get(h)[1];
//					user.addPage(tempPage);
//				}
//				
//			}else{
//				user = userMap.get(i);
//			}
			user = userMap.get(i);
			//Session vector
			Session session = new Session(user.getId(),user.getIp());
			for(Category c:pageUtil.getCates()){
				if(user.havePage(c.getCname())){
					session.addPageVector(1);
				}else{
					session.addPageVector(0);
				}	
			}
			
			for(int j=0;j<user.getPageList().size();j++){
				session.addSequence(pageUtil.Cate2Num(user.getPageList().get(j)[0]));
			}
			dataSet.add(session);
		}
		
		return dataSet;
	}
	
	public void initCenter(){
		float maxProb = 0;
		int maxNo = 0;
		float D,totalD = 0;
		center = new ArrayList<float[]>();
		Random random  = new Random();
		int firstCenterNo = random.nextInt(dataSet.size());
		center.add(dataSet.get(firstCenterNo).vector2Array());
		
		float[] nearestCenter = center.get(0);
		float[] noCenter = {0};
		for(int i=0;i<dataSet.size();i++){
			D = getDistance(nearestCenter,dataSet.get(i).vector2Array());
			totalD = D + getDistance(noCenter,dataSet.get(i).vector2Array());
			probability[i] = (D/totalD);
		}
		
		for(int i=0;i<dataSet.size();i++){
			if(probability[i] > maxProb){
				maxProb = probability[i];
				maxNo = i;
			}
		}
		
		center.add(dataSet.get(maxNo).vector2Array());		
	}
	
	public void initCluster(){
		kcluster = new ArrayList<Cluster>();
		
		for(int i=0;i<k;i++){
			kcluster.add(new Cluster(i));
		}
	}
	
	public int getMinDistanceLocation(float[] distance){
		int minLocation = 0;
		float minDistance = distance[0];
		
		for(int i=1;i<distance.length;i++){
			if(minDistance > distance[i]){
				minDistance = distance[i];
				minLocation = i;
			}
		}
		
		return minLocation;
	}
	
	public float getDistance(float[] center,float[] data){
		float distance = 0;
		
		for(int i=0;i<center.length;i++){
			float a = data[i] - center[i];
			distance = a*a + distance;
		}
		
		return distance;
	}
	
	public void setCluster(){
		float[] distance = new float[k];
		
		for(int i=0;i<dataSet.size();i++){
			for(int j=0;j<k;j++){
				distance[j] = getDistance(center.get(j),dataSet.get(i).vector2Array());
			}
			
			int location = getMinDistanceLocation(distance);
			kcluster.get(location).add(dataSet.get(i));
		}		
	}
	
	public void setNewCenter(){
		//Find independent point
		for(int i=0;i<kcluster.size();i++){
			if(kcluster.get(i).size() == 1){
				kcluster.remove(i);
			}
		}
		
		//Find farthest point
		float maxDis = 0;
		Session c = null;
		for(int i=0;i<k;i++){
			for(int sessionNo=0;sessionNo<kcluster.get(i).size();sessionNo++){
				if(getDistance(center.get(i),kcluster.get(i).get(sessionNo).vector2Array()) > maxDis){
					maxDis = getDistance(center.get(i),kcluster.get(i).get(sessionNo).vector2Array());
					c = kcluster.get(i).get(sessionNo);
				}
			}
		}
		
		//Decide if can be new center
		float sumDis = 0;
		for(int i=0;i<center.size();i++){
			for(int j=i;j<center.size();j++){
				sumDis = sumDis + getDistance(center.get(i),center.get(j));
			}
		}
		sumDis = sumDis / (float)k;
		
		if(maxDis >= 0.5 * sumDis && maxDis < sumDis){
			center.add(c.vector2Array());
			k++;

		}else{
			isEnd = true;
		}
	}
	
	public void getRepreCenter(ArrayList<Cluster> clusters){
		center.clear();
		
		//Weight average	
		for(Cluster c:clusters){
			if(c.size() == 0){
				continue;
			}
			float[] tempCenter = new float[singleLength];
			float[] weight = c.getWeight();
			
			for(int i=0;i<singleLength;i++){
				tempCenter[i] = 0;
				for(int j=0;j<c.size();j++){
					tempCenter[i] = tempCenter[i] + c.get(j).getVector(i)*weight[j];
				}
			}
			center.add(tempCenter);
		}
		
		//Average center 
//		System.out.println("");
//		for(Cluster c:clusters){
//			float[] center = new float[singleLength];
//			for(int i=0;i<singleLength;i++){
//				center[i] = 0;
//				for(int j=0;j<c.size();j++){
//					center[i] = center[i] + c.get(j).getVector(i);
//				}
//				
//				if(c.size() == 0){
//					center[i] = 0;
//				}else{
//					center[i] = center[i]/(float)c.size();
//				}	
//			}
//			clusterSet.put(c.getId(), center);
//		}
	}
	
	public void twoLevelPreprcoessing(ArrayList<Session> testData){	
		int clusterNo = 0;
		int requestNum = 0;
		int cacheNum = 0;
		float Hitratio = 0;
		float minDistance = 0;
		
		getRepreCenter(kcluster);
		ArrayList<ArrayList<User>> testMapForCluster = new ArrayList<ArrayList<User>>();
		for(int i=0;i<kcluster.size();i++){
			testMapForCluster.add(new ArrayList<User>());
		}
		
		//Assign test users
		for(int i=0;i<testData.size();i++){
			clusterNo = 0;
			minDistance = getDistance(center.get(0),testData.get(i).first2Vector());
			for(int j=1;j<center.size();j++){
				if(getDistance(center.get(j),testData.get(i).first2Vector()) < minDistance ){
					clusterNo = j;
					minDistance = getDistance(center.get(j),testData.get(i).first2Vector());
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
		
		for(int i=0;i<kcluster.size();i++){
			//Get all users in the cluster
			if(kcluster.get(i).size() == 0 || testMapForCluster.get(i).size() ==0){
				continue;
			}
			ArrayList<User> clusterUsers = new ArrayList<User>();
			for(int trainNo=0;trainNo<preprocess.getTrainUser().size();trainNo++){
				for(int sessionNo=0;sessionNo<kcluster.get(i).getDatas().size();sessionNo++){
					if(preprocess.getTrainUser().get(trainNo).getId() == kcluster.get(i).getDatas().get(sessionNo).getId()){
						clusterUsers.add(preprocess.getTrainUser().get(trainNo));
					}
				}
			}
			
			Twolevel twoLevel = new Twolevel(testMapForCluster.get(i),clusterUsers,hisRecords);
			twoLevel.setCacheNum(cacheNum);
			twoLevel.setRequestNum(requestNum);
			twoLevel.predict();
			
			hisRecords = twoLevel.getHisRecords();
			requestNum = twoLevel.getRequestNum();
			cacheNum = twoLevel.getCacheNum();	
		}
		
		Hitratio = (float)cacheNum / (float)requestNum;
		System.out.println("\nHit ratio:"+Hitratio);
	}
	
	public void saveHistoryRecord(){
		File file = new File("data/history.txt");
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            
            if(hisRecords.size() > 200){
            	 for(int i=0;i<hisRecords.size();i++){
                 	if(hisRecords.get(i).getStatus() > -2){
                 		writer.write(hisRecords.get(i).toString());
                         writer.newLine();
                 	}
                 }
            }else{
            	for(int i=0;i<hisRecords.size();i++){
            		writer.write(hisRecords.get(i).toString());
                    writer.newLine();
                 }
            }
           
            writer.flush();
            writer.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void kmeans(){
		System.out.println("layer:"+layer+" kmeans begins");
		long startTime = System.currentTimeMillis();
		System.out.println("init");
		init();
		while(true){
			setCluster();
			setNewCenter();
			
			if(isEnd){
				break;
			}

			kcluster.clear();
			initCluster();
		}
		System.out.println("kmeans ends");
		System.out.println("two-level predict");
		twoLevelPreprcoessing(testSet);
		saveHistoryRecord();
		
		long endTime = System.currentTimeMillis();
		System.out.println("running time=" + (endTime - startTime)+ "ms");
		
	}
	
	public void setDataSet(ArrayList<Session> set){
		this.dataSet = set;
	}

	public ArrayList<Cluster> getCluster() {
		return kcluster;
	}
	
	public String printCluster(Cluster c) {
		return "[" + c.getId() + "]=" + c.toString();
	}
}
