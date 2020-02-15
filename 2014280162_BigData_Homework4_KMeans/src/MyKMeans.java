import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyKMeans implements Constants {
	private ArrayList<DataPoint> dataPoints;
	private ArrayList<Centroid> centroids = new ArrayList<Centroid>();
	private int CLUSTERS;

	public MyKMeans(int CLUSTERS, ArrayList<DataPoint> dataPoints) {
		this.dataPoints = dataPoints;
		this.CLUSTERS = CLUSTERS;
	}

	private Double distance(ArrayList<Float> data, ArrayList<Float> centroidData) {
		double distance = 0.0;
		for (int i = 0; i < data.size() - 1; i++) {
			float testPoint = data.get(i);
			float trainPoint = centroidData.get(i);
			double dist = Math.abs(testPoint - trainPoint);
			distance += dist * dist;
		}
		distance = Math.sqrt(distance);

		return distance;
	}

	public void buildClusters() {
		int totalData = dataPoints.size();
		int step = totalData / CLUSTERS;
		for (int i = 0; i < CLUSTERS; i++) {
			DataPoint dp = dataPoints.get(i + step);
			centroids.add(new Centroid(i, dp.getData()));
		}
		for (int count = 0; count < ITERATION; count++) {
			for (int i = 0; i < dataPoints.size(); i++) {
				DataPoint dataPoint = dataPoints.get(i);
				double best_distance = INF;
				for (Centroid centroid : centroids) {
					ArrayList<Float> data = dataPoint.getData();
					ArrayList<Float> centroidData = centroid.getData();
					double distance = 0.0;
					distance = distance(data, centroidData);
					if (distance < best_distance) {
						best_distance = distance;
						dataPoints.get(i).cluster(centroid.getID());
					}
				}
			}
			HashMap<Integer, Integer> check = new HashMap<Integer, Integer>();
			for (int k = 0; k < dataPoints.size(); k++) {
				DataPoint trainData = dataPoints.get(k);
				ArrayList<Float> trainDataPoints = trainData.getData();
				int cluster = trainData.cluster();
				if (check.containsKey(cluster)) {
					check.put(cluster, check.get(cluster) + 1);
					ArrayList<Float> centroidPoints = centroids.get(cluster)
							.getData();
					centroids.get(cluster).addDataPoint(trainData);
					for (int l = 0; l < centroidPoints.size(); l++) {
						centroidPoints.set(l, centroidPoints.get(l)
								+ trainDataPoints.get(l));
					}
				} else {
					check.put(cluster, 1);
					centroids.get(cluster).getDataPoints().clear();
					for (int l = 0; l < trainDataPoints.size(); l++) {
						centroids.get(cluster).getData()
								.set(l, trainDataPoints.get(l));
					}
				}
			}
			for (int j = 0; j < CLUSTERS; j++) {
				if (check.containsKey(j)) {
					ArrayList<Float> centroidPoints = centroids.get(j)
							.getData();
					for (int l = 0; l < centroidPoints.size(); l++) {
						centroidPoints.set(l,
								centroidPoints.get(l) / check.get(j));
					}
				}
			}
		}
	}

	public void print() {
		// printNumOfPointsInCluster();
		// printNumOfRawTypeOfPointsInCluster();
		// System.out.println("---------------------------");
		printNumOfAttackTypesInCluster();
	}

	private void printNumOfAttackTypesInCluster() {
		for (Centroid c : centroids) {
			HashMap<String, Integer> map2 = new HashMap<String, Integer>();
			for (DataPoint p : c.getDataPoints()) {
				String type = attackType.get(p.getLabel());
				if (map2.containsKey(type)) {
					map2.put(type, map2.get(type) + 1);
				} else {
					map2.put(type, 1);
				}
			}
			System.out.print(c.getID() + "\t");
			for (Map.Entry<String, Integer> entry : map2.entrySet()) {
				System.out.print(entry.getKey() + " : " + entry.getValue()
						+ ", ");
			}
			System.out.println();
		}

	}

	private void printNumOfRawTypeOfPointsInCluster() {
		for (Centroid c : centroids) {
			System.out.print(c.getID() + "\t");
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			for (DataPoint p : c.getDataPoints()) {
				if (map.containsKey(p.getLabel())) {
					map.put(p.getLabel(), map.get(p.getLabel()) + 1);
				} else {
					map.put(p.getLabel(), 1);
				}
			}
			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				System.out.print(entry.getKey() + " : " + entry.getValue()
						+ ", ");
			}
			System.out.println();
		}

	}

	@SuppressWarnings("unused")
	private void printNumOfPointsInCluster() {
		for (int i = 0; i < CLUSTERS; i++) {
			int c = 0;
			System.out.print("Cluster " + i + " includes : ");
			for (int j = 0; j < dataPoints.size(); j++) {
				if (i == dataPoints.get(j).cluster())
					c++;
			}
			System.out.println(c);
		}
	}
}
