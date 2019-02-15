package clustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import bean.Cluster;

public class ClusterTest {

	public static void main(String[] args) {
		Hierclustering c = new Hierclustering(5,1,1500,100);
		c.clustering();
		
		ArrayList<Cluster> cluster = c.getClusters();
		//Print result
		System.out.println("write result");
		File file = new File("hier_result.txt");
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            for(int i=0;i<cluster.size();i++)
    		{
    			writer.write(c.printCluster(cluster.get(i)));
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
