import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Reader implements Constants {
	private ArrayList<DataPoint> trainDataPoint = new ArrayList<DataPoint>();
	private ArrayList<DataPoint> testDataPoint = new ArrayList<DataPoint>();
	private float[] max = new float[FEATURE_SIZE];
	private float[] min = new float[FEATURE_SIZE];

	public Reader(String trainingFile, String testingFile) {
		attackType.put("normal", "normal");
		getDataPoints(trainingFile, TRAINING);
		getDataPoints(testingFile, TESTING);
		getAttackTypes();
	}

	public Reader(String trainingFile) {
		attackType.put("normal", "normal");
		getDataPoints(trainingFile, TRAINING);
		getAttackTypes();
	}

	private void getDataPoints(String fileName, String type) {
		File file = new File(fileName);
		int id = 0;
		HashMap<String, Float> map = new HashMap<String, Float>();
		try (Scanner scanner = new Scanner(file, ENCODING.name())) {
			while (scanner.hasNextLine()) {
				ArrayList<Float> point = new ArrayList<Float>();
				String aLine = scanner.nextLine();
				String[] inputs = aLine.split(",");
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
			}
		} catch (FileNotFoundException e) {
			System.out.println("Input File not found");
		}
	}

	private void getAttackTypes() {
		File file = new File("training_attack_types");
		try (Scanner scanner = new Scanner(file, ENCODING.name())) {
			while (scanner.hasNextLine()) {
				String aLine = scanner.nextLine();
				String[] inputs = aLine.split(" ");
				attackType.put(inputs[0].trim(), inputs[1].trim());
			}
		} catch (FileNotFoundException e) {
			System.out.println("Input File not found");
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
}
