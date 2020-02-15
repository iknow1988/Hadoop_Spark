import java.util.ArrayList;

public class Application implements Constants {
	private static int CORES;
	private static ArrayList<DataPoint> dataPoints;
	private static Reader reader;
	private static NormalizeThread[] normalizeThreadTraining;

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		init(args[0], Integer.parseInt(args[1]));
		System.out.println("Normalizing data");
		prepareAllThreads();
		normalizeAllData();
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		System.out.println("Time took to process data " + duration);
		System.out.println("Running my KMeans algorithm");
		startTime = System.currentTimeMillis();
		MyKMeans myKMeans = new MyKMeans(Integer.parseInt(args[2]), dataPoints);
		myKMeans.buildClusters();
		myKMeans.print();
		endTime = System.currentTimeMillis();
		duration = (endTime - startTime);
		System.out.println("Time took " + duration);
		System.out.println("Running Weka KMeans Algorithm");
		startTime = System.currentTimeMillis();
		WekaKMeans wekaKMeans = new WekaKMeans(Integer.parseInt(args[2]),
				dataPoints);
		wekaKMeans.buildClusters();
		wekaKMeans.print();
		endTime = System.currentTimeMillis();
		duration = (endTime - startTime);
		System.out.println("Time took " + duration);
	}

	private static void init(String trainFile, int core) {
		reader = new Reader(trainFile);
		System.out.println("All Testing and Training Data loaded");
		CORES = core;
		dataPoints = reader.getAllTrainingData();
		idMap.put(0, "normal");
		idMap.put(1, "u2r");
		idMap.put(2, "r2l");
		idMap.put(3, "probe");
		idMap.put(4, "dos");
	}

	private static void prepareAllThreads() {
		normalizeThreadTraining = new NormalizeThread[CORES];
		int trainingElems = dataPoints.size();
		int trainingElemsPerThread = trainingElems / CORES;
		int countTraining = 0;
		for (int i = 0; i < CORES; i++) {
			if (i == CORES - 1) {
				normalizeThreadTraining[i] = new NormalizeThread(
						i * trainingElemsPerThread,
						(i * trainingElemsPerThread + (trainingElems - countTraining)),
						dataPoints, reader.getMaxArray(), reader.getMinArray());
			} else {
				normalizeThreadTraining[i] = new NormalizeThread(i
						* trainingElemsPerThread,
						(i * trainingElemsPerThread + trainingElemsPerThread),
						dataPoints, reader.getMaxArray(), reader.getMinArray());
			}
			countTraining += trainingElemsPerThread;
		}
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
}
