import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;

public final class KMeansExample {
	private static int CORES;
	private static ArrayList<DataPoint> trainDataPoints;
	private static Reader reader;
	private static NormalizeThread[] normalizeThreadTraining;
	private static HashMap<Integer, String> idMap = new HashMap<Integer, String>();

	private static class ParsePoint implements Function<DataPoint, Vector> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Vector call(DataPoint dp) {
			double[] point = new double[dp.getData().size()];
			for (int i = 0; i < dp.getData().size(); ++i) {
				point[i] = dp.getData().get(i);
			}
			return Vectors.dense(point);
		}
	}

	private static void init(String trainFile) {
		CORES = Runtime.getRuntime().availableProcessors();
		reader = new Reader(trainFile);
		System.out.println("All Training Data Parsed");
		trainDataPoints = reader.getAllTrainingData();
		System.out.println("All Training Data Loaded");
		idMap.put(0, "normal");
		idMap.put(1, "u2r");
		idMap.put(2, "r2l");
		idMap.put(3, "probe");
		idMap.put(4, "dos");
	}

	private static void normalizeAllData() {
		for (int i = 0; i < CORES; i++) {
			normalizeThreadTraining[i].start();
		}
		try {
			for (int i = 0; i < CORES; i++) {
				normalizeThreadTraining[i].join();
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted");
		}
	}

	private static void prepareAllThreads() {
		normalizeThreadTraining = new NormalizeThread[CORES];
		int trainingElems = trainDataPoints.size();
		int trainingElemsPerThread = trainingElems / CORES;
		int countTraining = 0;
		for (int i = 0; i < CORES; i++) {
			if (i == CORES - 1) {
				normalizeThreadTraining[i] = new NormalizeThread(
						i * trainingElemsPerThread,
						(i * trainingElemsPerThread + (trainingElems - countTraining)),
						trainDataPoints, reader.getMaxArray(), reader
								.getMinArray());
			} else {
				normalizeThreadTraining[i] = new NormalizeThread(i
						* trainingElemsPerThread,
						(i * trainingElemsPerThread + trainingElemsPerThread),
						trainDataPoints, reader.getMaxArray(),
						reader.getMinArray());
			}
			countTraining += trainingElemsPerThread;
		}
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		init("/user/2014280162/spark/kddcup.data_10_percent");
		prepareAllThreads();
		normalizeAllData();
		System.out.println("All Data Normalized");
		int k = Integer.parseInt(args[0]);
		int iterations = 5;
		long endTime = System.currentTimeMillis();
		System.out.println("Time Took to process data" + (endTime - startTime));
		startTime = System.currentTimeMillis();
		SparkConf sparkConf = new SparkConf().setAppName("JavaKMeans");
		@SuppressWarnings("resource")
		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		JavaRDD<DataPoint> dataPoints = sc.parallelize(trainDataPoints);
		JavaRDD<Vector> points = dataPoints.map(new ParsePoint());
		endTime = System.currentTimeMillis();
		System.out.println("Time Took to create RDD" + (endTime - startTime));
		System.out.println("Training Started");
		startTime = System.currentTimeMillis();
		KMeansModel model = KMeans.train(points.rdd(), k, iterations);
		System.out.println("Training Finished");
		endTime = System.currentTimeMillis();
		System.out.println("Time Took to train data" + (endTime - startTime));
		startTime = System.currentTimeMillis();
		HashMap<Integer, HashMap<String, Integer>> countMap = new HashMap<Integer, HashMap<String, Integer>>();
		int i = 0;
		for (Vector point : points.collect()) {
			DataPoint p = trainDataPoints.get(i);
			String type = reader.getAttack().get(p.getLabel());
			int result = model.predict(point);
			if (countMap.containsKey(result)) {
				if (countMap.get(result).containsKey(type)) {
					countMap.get(result).put(type,
							countMap.get(result).get(type) + 1);
				} else {
					countMap.get(result).put(type, 1);
				}
			} else {
				countMap.put(result, new HashMap<String, Integer>());
				countMap.get(result).put(type, 1);
			}
			i++;
		}
		for (Map.Entry<Integer, HashMap<String, Integer>> entry : countMap
				.entrySet()) {
			HashMap<String, Integer> value = entry.getValue();
			System.out.print(entry.getKey() + "\t");
			for (Map.Entry<String, Integer> en : value.entrySet()) {
				System.out.print(en.getKey() + " : " + en.getValue() + ", ");
			}
			System.out.println();
		}
		sc.stop();
		endTime = System.currentTimeMillis();
		System.out.println("Time Took to process output"
				+ (endTime - startTime));
	}
}
