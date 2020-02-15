import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

public class WekaKMeans implements Constants {
	private ArrayList<DataPoint> trainDataPoints;
	private int[] assignments = null;
	private int CLUSTERS;

	public WekaKMeans(int CLUSTERS, ArrayList<DataPoint> trainDataPoints) {
		this.trainDataPoints = trainDataPoints;
		this.CLUSTERS = CLUSTERS;
	}

	public void buildClusters() {
		SimpleKMeans kmeans = new SimpleKMeans();
		kmeans.setSeed(10);
		kmeans.setPreserveInstancesOrder(true);
		Instances data = null;
		try {
			kmeans.setNumClusters(CLUSTERS);
			data = Utility.getInstances(trainDataPoints);
			kmeans.buildClusterer(data);
			assignments = kmeans.getAssignments();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void print() {
		int i = 0;
		HashMap<Integer, HashMap<String, Integer>> count = new HashMap<Integer, HashMap<String, Integer>>();
		HashMap<Integer, HashMap<String, Integer>> count2 = new HashMap<Integer, HashMap<String, Integer>>();
		for (int clusterNum : assignments) {
			DataPoint p = trainDataPoints.get(i);
			if (count.containsKey(clusterNum)) {
				if (count.get(clusterNum).containsKey(p.getLabel())) {
					count.get(clusterNum).put(p.getLabel(),
							count.get(clusterNum).get(p.getLabel()) + 1);
				} else {
					count.get(clusterNum).put(p.getLabel(), 1);
				}
			} else {
				count.put(clusterNum, new HashMap<String, Integer>());
				count.get(clusterNum).put(p.getLabel(), 1);
			}
			String type = attackType.get(p.getLabel());
			if (count2.containsKey(clusterNum)) {
				if (count2.get(clusterNum).containsKey(type)) {
					count2.get(clusterNum).put(type,
							count2.get(clusterNum).get(type) + 1);
				} else {
					count2.get(clusterNum).put(type, 1);
				}
			} else {
				count2.put(clusterNum, new HashMap<String, Integer>());
				count2.get(clusterNum).put(type, 1);
			}
			i++;
		}
		// for (Map.Entry<Integer, HashMap<String, Integer>> entry : count
		// .entrySet()) {
		// HashMap<String, Integer> value = entry.getValue();
		// System.out.print(entry.getKey() + "\t");
		// for (Map.Entry<String, Integer> en : value.entrySet()) {
		// System.out.print(en.getKey() + " : " + en.getValue() + ", ");
		// }
		// System.out.println();
		// }
		// System.out.println("------------------------------------");
		for (Map.Entry<Integer, HashMap<String, Integer>> entry : count2
				.entrySet()) {
			HashMap<String, Integer> value = entry.getValue();
			System.out.print(entry.getKey() + "\t");
			for (Map.Entry<String, Integer> en : value.entrySet()) {
				System.out.print(en.getKey() + " : " + en.getValue() + ", ");
			}
			System.out.println();
		}
	}
}