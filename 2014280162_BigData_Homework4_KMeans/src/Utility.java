import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class Utility {
	public static void writeOutputFile(ArrayList<DataPoint> trainDataPoints) {
		String outputFileName = "results.txt";
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(outputFileName)))) {
			for (DataPoint result : trainDataPoints) {
				for (int i = 0; i < result.getData().size(); i++) {
					float val = result.getData().get(i);
					if (i < result.getData().size() - 1)
						out.print(val + " ");
					else
						out.print(val);
				}
				out.println();
			}
		} catch (IOException e) {
			System.out.println("Can not write to a file");
		}
	}

	public static Instances getInstances(ArrayList<DataPoint> dataPoints)
			throws Exception {
		ArrayList<Attribute> atts;
		Instances data = null;
		atts = new ArrayList<Attribute>();
		for (int i = 0; i < dataPoints.get(0).getData().size(); i++) {
			atts.add(new Attribute("att" + i));
		}
		data = new Instances("KMeans", atts, 0);
		for (DataPoint dp : dataPoints) {
			double[] vals = null;
			vals = new double[data.numAttributes()];
			for (int i = 0; i < dp.getData().size(); i++) {
				vals[i] = dp.getData().get(i);
			}
			data.add(new DenseInstance(1.0, vals));
		}
		return data;
	}
}
