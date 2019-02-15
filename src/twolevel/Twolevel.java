package twolevel;

import java.util.ArrayList;
import java.util.HashMap;

import bean.Category;
import bean.HisRecord;
import bean.Session;
import bean.Tree;
import bean.TreeNode;
import bean.User;
import util.PageUtil;

public class Twolevel {
	int[][] popPages;
	int[][] C;
	float[][] S;
	float[][] P;
	float[][] P2;
	float[][] R1; 
	float[][] R2; 
	float[][] bayes;
	
	int m;
	int k;
	int matrixNum;
	int bayesNum;
	int requestNum;
	int cacheNum;
	int userRequestNum;
	int userCacheNum;
	
	ArrayList<User> testUsers;
	ArrayList<User> clusterUsers;
	ArrayList<Category> cates;
	ArrayList<HisRecord> hisRecords;
	
	ArrayList<Integer> cateResult;
	ArrayList<String[]> pageResult;
	ArrayList<Integer> position;
	
	PageUtil pageUtil;
	Tree sessionTree;
	HashMap<Integer,TreeNode> nodeMap;
	
	public Twolevel(ArrayList<User> testUsers,ArrayList<User> clusterUsers,ArrayList<HisRecord> hisRecords) {
		this.clusterUsers = clusterUsers;
		this.testUsers = testUsers;
		this.hisRecords = hisRecords;
	}
	
	public void init(){	
//		popPages = new int[2][2];
		cates = new ArrayList<Category>();	
		
		//Add history record of test user
		ArrayList<User> tempUsers = clusterUsers;
		for(int i=0;i<testUsers.size();i++){
			User tempUser = new User(testUsers.get(i).getId(),testUsers.get(i).getIp());
			tempUser.setPageList(testUsers.get(i).getPageList());
			tempUsers.add(tempUser);
		}
		for(int i=0;i<testUsers.size();i++){
			User tempUser = new User(testUsers.get(i).getId(),testUsers.get(i).getIp());
			tempUser.setPageList(testUsers.get(i).getTestPageList());
			clusterUsers.add(tempUser);
		}
		
		//Add all categories
		for(int i=0;i<tempUsers.size();i++){
			for(int j=0;j<tempUsers.get(i).getPageList().size();j++){
				Category temp = new Category(tempUsers.get(i).getPageList().get(j)[0]);
				if(!cates.contains(temp)){
					cates.add(temp);
				}
			}
		}

		//Add all pages
		ArrayList<String> tempPage = new ArrayList<String>();
		for(int i=0;i<cates.size();i++){
			for(int j=0;j<tempUsers.size();j++){
				tempPage = tempUsers.get(j).getAllPageOfCategory(cates.get(i).getCname());
				for(int n=0;n<tempPage.size();n++){
					if(!cates.get(i).getPages().contains(tempPage.get(n))){
						cates.get(i).addPage(tempPage.get(n));
					}
				}
				tempPage = null;		
			}
		}

		pageUtil = new PageUtil(cates);
		
		k = cates.size();
		m = clusterUsers.size();
		
		//INIT MATRIX
		ArrayList<Session> clusterSessions = new ArrayList<Session>();
		for(int i=0;i<m;i++){
			Session session = new Session(clusterUsers.get(i).getId(),clusterUsers.get(i).getIp());
			for(Category c:pageUtil.getCates()){
				if(clusterUsers.get(i).havePage(c.getCname())){
					session.addPageVector(1);
				}else{
					session.addPageVector(0);
				}	
			}
			clusterSessions.add(session);
		}
		
		C = new int[k][m];
		for(int j=0;j<k;j++){
			for(int i=0;i<m;i++){
				C[j][i] = clusterSessions.get(i).getVector(j);
			}
		}
		
		//Init session tree
		buildSessionTree();
		
	}
	
	public void buildSessionTree(){
		sessionTree = new Tree();
		TreeNode curNode = sessionTree.getRoot();
		TreeNode tempNode = null;
		nodeMap = new HashMap<Integer,TreeNode>();
		
		for(int i=0;i<clusterUsers.size();i++){
			for(int j=0;j<clusterUsers.get(i).getRecordList().size();j++){
				int tempCate = pageUtil.Cate2Num(clusterUsers.get(i).getRecordList().get(j)[0]);
				int tempPage = pageUtil.Page2Num(clusterUsers.get(i).getRecordList().get(j)[1], tempCate);
				
				if((tempNode = curNode.getChild(tempCate,tempPage)) != null){
					curNode = tempNode;
					curNode.setFreNum(curNode.getFreNum() + 1);
				}else{
					TreeNode node  = new TreeNode(tempCate,tempPage);
					node.setParent(curNode);
					curNode.addChild(node);
					curNode = node;
					
					if(nodeMap.containsKey(tempCate)){
						tempNode = nodeMap.get(tempCate);
						while(tempNode.next != null){
							tempNode = tempNode.next;
						}
						tempNode.next = curNode;
					}else{
						nodeMap.put(tempCate, curNode);
					}
				}	
			}
			curNode = sessionTree.getRoot();
		}
		
		System.out.println("--------session tree building finish---------");
		
	}
	
	public int getSetResult(int[] setX, int[] setY, int flag){
		if(flag == 1){
			//Get intersection
			int size = 0;
			for(int i=0;i<setX.length;i++){
//				if( (setX[i] == 1) && (setX[i] == setY[i]) ){
//					size++;
//				}
				if(setX[i] == setY[i]){
					size++;
				}
			}
			return size;
		}else if(flag == 2){
			//Get union
			int size = setX.length;
//			int size = 0;
//			for(int i=0;i<setX.length;i++){
//				if( (setX[i] != 0) || (setY[i] != 0) ){
//					size++;
//				}
//			}
			return size;
		}
		return 0;
	}
	
	public float getDistance(int[] x, int[] y){
		if(x.length == 0 && y.length == 0){
			return 1;
		}
		if(x.length == 0 || y.length == 0){
			return 0;
		}
		
		int interSet = getSetResult(x,y,1);
		int unionSet = getSetResult(x,y,2);
		
		float setSim = (float)interSet / (float)unionSet;
		float EuDistance = getEuclideanDistance(x,y);
		
		return (float) (EuDistance*0.5 + setSim*0.5);
	}
	
	public float getEuclideanDistance(int[] data1,int[] data2){
		float distance = 0;
		
		for(int i=0;i<data1.length;i++){
			float a = data2[i] - data1[i];
			distance = a*a + distance;
		}
		
		//NORMALIZATION
		return (float) (1 - Math.sqrt(distance/m));
	}

	public void getSimilarMatrix(){
		S = new float[k][k];
		
		for(int i=0;i<k;i++){
			for(int j=0;j<k;j++){
				S[i][j] = getDistance(C[i],C[j]);
			}
		}
	}
	
	public void getTransferMatrix(){
		P = new float[k][k];
		TreeNode node = null;
		
		//Tree
		for(int i=0;i<k;i++){
			int numi2j = 0;
			int totalNumi2j = 0;
			
			node = nodeMap.get(i);
			while(node != null){
				if(node.getChildList().size() != 0){
					totalNumi2j = totalNumi2j + node.getFreNum();
				}
				node = node.next;
			}
			
			for(int j=0;j<k;j++){
				node = nodeMap.get(j);		
				while(node != null){
					if(node.getParent().getCategory() == i){
						numi2j = numi2j + node.getFreNum();
					}
					node = node.next;
				}
				
				if(numi2j == 0){
					P[i][j] = 0;
				}else{
					P[i][j] = (float)numi2j/(float)totalNumi2j;
				}
				numi2j = 0;
			}		
			totalNumi2j = 0;
		}
	}
	
	public void getRelevantMatrix() {
		P2 = new float[k][k];
		R1 = new float[k][k];
		R2 = new float[k][k];
		float tempSum = 0; 

		//Matrix product
		for(int i=0;i<k;i++){
			for(int j=0;j<k;j++){
				tempSum = 0;
				for(int x=0;x<k;x++){
					tempSum = tempSum + P[i][x] * P[x][j];
				}
				P2[i][j] = tempSum;
			}
		}
		
		for(int i=0;i<k;i++){
			for(int j=0;j<k;j++){
				R1[i][j] = S[i][j] * P[i][j];	
			}
		}
		
		for(int i=0;i<k;i++){
			for(int j=0;j<k;j++){
				R2[i][j] = S[i][j] * P2[i][j];
			}
		}
		
		S = null;
		P = null;
		P2 = null;
	}
	
	public void getBayesMatrix(){
		matrixNum = 0;
		for(int i=0;i<cates.size();i++){
			matrixNum = matrixNum + cates.get(i).size();
		}
		
		bayes = new float[matrixNum][matrixNum];
		
		int cateNo1 = 0;
		int cateNo2 = 0;
		int pageNo1 = 0;
		int pageNo2 = 0;
		
		//Tree
		TreeNode node = null;
		for(int i=0;i<matrixNum;i++,pageNo1++){
			int numi2j = 0;
			int totalNumi2j = 0;
			cateNo2 = 0;
			pageNo2 = 0;
			
			if(pageNo1 >= cates.get(cateNo1).size()){			
				pageNo1 = pageNo1 - cates.get(cateNo1).size();
				cateNo1++;
			}
			node = nodeMap.get(cateNo1);
			while(node != null){
				if(node.getPage() == pageNo1 && node.getChildList().size() != 0){
					totalNumi2j = totalNumi2j + node.getFreNum();
				}
				node = node.next;
			}
			
			for(int j=0;j<matrixNum;j++,pageNo2++){
				if(pageNo2 >= cates.get(cateNo2).size()){				
					pageNo2 = pageNo2 - cates.get(cateNo2).size();
					cateNo2++;
				}
				node = nodeMap.get(cateNo2);
				while(node != null){
					if(node.getParent().getCategory() == cateNo1 &&
						node.getParent().getPage() == pageNo1 &&
						node.getPage() == pageNo2){
						numi2j = numi2j + node.getFreNum();
					}
					node = node.next;
				}
				
				if(numi2j == 0){
					bayes[i][j] = 0;
				}else{
					bayes[i][j] = (float)numi2j / (float)totalNumi2j;
				}	
				numi2j = 0;
			}
			totalNumi2j = 0;
		}
	}
	
	public int[][] getPopularPage(ArrayList<String[]> currPageList){
		//Build popularity model
		Tree popTree = new Tree();		
		ArrayList<int[]> pages = new ArrayList<int[]>();
		int i=0;
		int tempCate,tempPage;
		TreeNode curNode = popTree.getRoot();
		TreeNode tempNode = null;
		
		while(i < currPageList.size()-1){
			int[] record2Num = new int[2];
			record2Num[0] = pageUtil.Cate2Num(currPageList.get(i)[0]);
			record2Num[1] = pageUtil.Page2Num(currPageList.get(i)[1], record2Num[0]);
			pages.add(record2Num);

			if(pages.size() == 2){
				for(int j=1;j>=0;j--){
					tempCate = pages.get(j)[0];
					tempPage = pages.get(j)[1];
					
					if((tempNode = curNode.getChild(tempCate, tempPage)) != null){
						curNode = tempNode;
						curNode.setFreNum(curNode.getFreNum() + 1);
					}else{
						TreeNode node  = new TreeNode(tempCate,tempPage);
						node.setParent(curNode);
						curNode.addChild(node);
						curNode = node;
					}					
				}
				pages.remove(0);
			}
			i++;
			curNode = popTree.getRoot();
		}
		
		//Get most popular pages
		int[][] popPages = new int[2][2];
		tempNode = null;
		i = 0;
		int maxFre = 1;
		curNode = popTree.getRoot();
		for(int n=0;n<curNode.getChildList().size();n++){
			if(curNode.getChildList().get(n).getFreNum() >= maxFre){
				tempNode = curNode.getChildList().get(n);
				maxFre = tempNode.getFreNum();
			}
			
			if(n == curNode.getChildList().size()-1){
				//Choose the most frequent pages
				popPages[i][0] = tempNode.getCategory();
				popPages[i][1] = tempNode.getPage();
				
				if(i<1){
					curNode = tempNode;
					n = 0;
					maxFre = 1;
					tempNode = null;
					i++;
				}
			}
		}
		
		return popPages;
	}
	
	public void predictCategory(int[][] popPages){
		int Ct1 = popPages[0][0];
		int Ct2 = popPages[1][0];
		cateResult = new ArrayList<Integer>();
		
		float[] vector1 = new float[k];
		float[] vector2 = new float[k];		
		for(int i=0;i<k;i++){
			vector1[i] = R1[Ct1][i];
			vector2[i] = R2[Ct2][i];
		}
		
		//Get top10 category
		int tempC1 = 0;
		int tempC2 = 0;
		float maxPro = 0;
		for(int i=0;i<0.7*cates.size();i++){
			for(int j1=0;j1<vector1.length;j1++){
				if(vector1[j1] > maxPro){
					maxPro = vector1[j1];
					tempC1 = j1;
				}
			}
			
			for(int j2=0;j2<vector2.length;j2++){
				if(vector2[j2] > maxPro){
					maxPro = vector2[j2];
					tempC2 = j2;
				}
			}
			
			if(maxPro > (float)0){
				if(vector1[tempC1] >= vector2[tempC2] && !cateResult.contains(tempC1)){
					cateResult.add(tempC1);
					vector1[tempC1] = 0;
					vector2[tempC1] = 0;
				}else if(vector1[tempC1] < vector2[tempC2] && !cateResult.contains(tempC2)){
					cateResult.add(tempC2);
					vector1[tempC2] = 0;
					vector2[tempC2] = 0;
				}
			}

			tempC1 = 0;
			tempC2 = 0;
			maxPro = 0;
		}

		vector1 = null;
		vector2 = null;
	}
	
	public void predictPage(int[][] popPages){
		//Get bayes range
		bayesNum = 0;
		position = new ArrayList<Integer>();
		pageResult = new ArrayList<String[]>();
		
		for(int i=0;i<cateResult.size();i++){
			bayesNum = bayesNum + cates.get(cateResult.get(i)).size();
		}
		
		for(int i=0,pageNo=0,cateNo=0;i<matrixNum;i++,pageNo++){
			if(pageNo >= cates.get(cateNo).size()){			
				pageNo = pageNo - cates.get(cateNo).size();
				cateNo++;
			}
			if(cateResult.contains(cateNo)){
				position.add(i);
			}
		}	

		//Calculate probability
		float[] PpbiAndpa = new float[bayesNum];
		float[] Pafter = new float[bayesNum];
		float Pa = 0;
		
		int PaNo = 0;
		int cateBefore = popPages[0][0];
		int pageBefore = popPages[0][1];
		
		if( cateBefore == -1 || pageBefore == -1){
			return;
		}
		
		for(int i=0;i<cates.size();i++){
			if(cateBefore != i){
				PaNo = PaNo + cates.get(i).size();
			}else{
				break;
			}
		}
		PaNo = PaNo + pageBefore;		
		
		for(int i=0;i<bayesNum;i++){
				PpbiAndpa[i] = bayes[PaNo][position.get(i)];			
		}
		
		for(int i=0;i<matrixNum;i++){
			Pa = Pa + bayes[PaNo][i];
		}
		
		for(int i=0;i<bayesNum;i++){
			Pafter[i] = PpbiAndpa[i] / Pa;
		}
		
		for(int i=0;i<position.size();i++){
			for(int j=0;j<cates.size();j++){
				if(position.get(i) - cates.get(j).size() >= 0){
					position.set(i, position.get(i) - cates.get(j).size() );
				}else{
					String[] temp = new String[3];
					temp[0] = cates.get(j).getCname();
					temp[1] = cates.get(j).getPage(position.get(i));
					temp[2] = Pafter[i]+"";
					
					if(!pageResult.contains(temp)){
						pageResult.add(temp);
					}
					break;
				}
			}
		}
		
		//Modify prediction using history record
		HisRecord tempR = null;
		String[] temp = null;
		for(int i=0;i<pageResult.size();i++){
			temp = pageResult.get(i);
			
			for(int j=0;j<hisRecords.size();j++){
				tempR = hisRecords.get(j);
				if(tempR.getCategory().equals(temp[0]) &&
						tempR.getPage().equals(temp[1])){
					if(tempR.getStatus() >= 0){
						pageResult.get(i)[2] = 1+""; 
					}
				}
			}
		}
	
		System.out.println("");		
	}
	
	public void getResult(ArrayList<String[]> currPageList){
		ArrayList<String[]> result = new ArrayList<String[]>();
			
		int tempJ = 0;
		float maxPPro = 0;
		String[] tempP = null;
		for(int i=0;i<0.03*matrixNum;i++){
			for(int j=0;j<pageResult.size();j++){
				if(Float.parseFloat(pageResult.get(j)[2]) > maxPPro){
					tempP = pageResult.get(j);
					maxPPro = Float.parseFloat(pageResult.get(j)[2]);
					tempJ = j;
				}
			}
			if(!result.contains(tempP) && maxPPro > 0){
				result.add(tempP);
			}
			pageResult.get(tempJ)[2] = 0+"";
			maxPPro = 0;
			tempP = null;
			tempJ = 0;
		}
		
		//Print result
		for(int i=0;i<result.size();i++){
			System.out.println("["+result.get(i)[0]+","+result.get(i)[1]+"]");
		}
		System.out.println("---------------------------------");
		
		requestNum++;
		userRequestNum++;
		int pageSize = currPageList.size();
		boolean haveRecord;
		for(int i=0;i<result.size();i++){
			//Get history record item
			haveRecord = false;
			HisRecord record = new HisRecord(result.get(i)[0],result.get(i)[1]);
			for(int n=0;n<hisRecords.size();n++){
				if(hisRecords.get(n).equals(record)){
					record = hisRecords.get(n);
					haveRecord = true;
					break;
				}
			}

			//Modify prediction number
			if(result.get(i)[0].equals(currPageList.get(pageSize-1)[0])
					&& result.get(i)[1].equals(currPageList.get(pageSize-1)[1])){
				record.changeStatus(1);
				cacheNum++;
				userCacheNum++;
			}else{
				record.changeStatus(-1);
			}
			
			if(!haveRecord){
				hisRecords.add(record);
			}
		}
		
		float Hitratio = (float)cacheNum / (float)requestNum;
		System.out.println("Hit ratio:"+Hitratio);	
	}
	
	public void predict(){
		init();
		getSimilarMatrix();
		getTransferMatrix();
		getRelevantMatrix();
		getBayesMatrix();
		
//		for(int i=0;i<testUsers.size();i++){
//			popPages = getPopularPage(testUsers.get(i).getPageList());
//			predictCategory(popPages);
//			predictPage(popPages);
//			getResult(testUsers.get(i).getPageList());
//		}
		
		ArrayList<String[]> currPageList = new ArrayList<String[]>();
		for(int i=0;i<testUsers.size();i++){
			userRequestNum = 0;
			userCacheNum = 0;
			
			for(int j=0;j<testUsers.get(i).getPageList().size();j++){
				if(currPageList.size() < 2){
					currPageList.add(testUsers.get(i).getPageList().get(j));
				}else{				
					popPages = getPopularPage(currPageList);
					predictCategory(popPages);
					predictPage(popPages);
					getResult(currPageList);
					currPageList.add(testUsers.get(i).getPageList().get(j));
				}	
			}
			currPageList.clear();
			currPageList = new ArrayList<String[]>();	
			
			float userHitratio = (float)userCacheNum / (float)userRequestNum;
			System.out.println("User Hit ratio:"+userHitratio);
		}
						
	}
	public void setRequestNum(int requestNum) {
		this.requestNum = requestNum;
	}

	public void setCacheNum(int cacheNum) {
		this.cacheNum = cacheNum;
	}

	public int getRequestNum() {
		return requestNum;
	}

	public int getCacheNum() {
		return cacheNum;
	}

	public ArrayList<HisRecord> getHisRecords() {
		return hisRecords;
	}

}
