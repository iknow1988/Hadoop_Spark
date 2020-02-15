import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class MyReader {
	public static final Charset ENCODING = StandardCharsets.UTF_8;
	private static final int FEATURE_SIZE = 41;
	private ArrayList<DataPoint> trainDataPoint = new ArrayList<DataPoint>();
	private ArrayList<DataPoint> testDataPoint = new ArrayList<DataPoint>();
	private float[] max = new float[FEATURE_SIZE];
	private float[] min = new float[FEATURE_SIZE];
	private HashMap<String, String> attackType = new HashMap<String, String>();
	private static final String TRAINING = "training";

	public MyReader(String folder, String trainingFile) {
		attackType.put("normal", "normal");
		getDataPoints(folder + trainingFile, TRAINING);
		getAttackTypes();
	}

	private void getDataPoints(String fileName, String type) {
		int id = 0;
		HashMap<String, Float> map = new HashMap<String, Float>();
		try {
			Path pt = new Path(fileName);
			FileSystem fs = FileSystem.get(new Configuration());
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fs.open(pt)));
			String line;
			line = br.readLine();
			while (line != null) {
				ArrayList<Float> point = new ArrayList<Float>();
				String[] inputs = line.split(",");
				float val = 0.0f;
				int i = 0;
				String label = null;
				for (String input : inputs) {
					try {
						val = Float.parseFloat(input);
					} catch (NumberFormatException ex) {
						input = input.trim().replace(".", "");
						if (map.containsKey(input)) {
							val = map.get(input);
						} else {
							val = (float) id;
							map.put(input, val);
							id++;
						}
					}
					if (i < FEATURE_SIZE) {
						if (val > max[i]) {
							max[i] = val;
						}
						if (val < min[i]) {
							min[i] = val;
						}
						point.add(val);
					} else {
						label = input;
					}
					i++;
				}
				if (type.equals("training")) {
					trainDataPoint.add(new DataPoint(point, label));
				} else {
					testDataPoint.add(new DataPoint(point, label));
				}
				line = br.readLine();
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Input File not found " + e.getMessage());
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Input File not found " + e.getMessage());
			System.exit(0);
		}
	}

	private void getAttackTypes() {
		try {
			Path pt = new Path("/user/2014280162/kmeans/training_attack_types");
			FileSystem fs = FileSystem.get(new Configuration());
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fs.open(pt)));
			String line;
			line = br.readLine();
			while (line != null) {
				String[] inputs = line.split(" ");
				attackType.put(inputs[0].trim(), inputs[1].trim());
				line = br.readLine();
			}
		} catch (Exception e) {
			System.out.println("Attack File not found");
		}
	}

	public ArrayList<DataPoint> getAllTrainingData() {
		return this.trainDataPoint;
	}

	public ArrayList<DataPoint> getAllTestingData() {
		return this.testDataPoint;
	}

	public float[] getMaxArray() {
		return this.max;
	}

	public float[] getMinArray() {
		return this.min;
	}

	public HashMap<String, String> getAttack() {
		return this.attackType;
	}
}
