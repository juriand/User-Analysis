package clustering;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import bean.Cluster;

public class KmeansTest {

	public static void main(String[] args) {
		Kmeans kmeans = new Kmeans(1,19000,1000);		
		kmeans.kmeans();
		
		ArrayList<Cluster> cluster = kmeans.getCluster();
		//Print result
		System.out.println("write result");
		File file = new File("kmeans_result.txt");
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            for(int i=0;i<cluster.size();i++)
    		{
    			writer.write(kmeans.printCluster(cluster.get(i)));
                writer.newLine();
    		}
            writer.flush();
            writer.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("write result end");
		
	}

}
