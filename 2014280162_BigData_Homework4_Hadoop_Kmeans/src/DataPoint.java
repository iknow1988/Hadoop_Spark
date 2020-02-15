

import java.util.ArrayList;

public class DataPoint {
	private ArrayList<Float> point = new ArrayList<Float>();
	private String label;

	public DataPoint(ArrayList<Float> point, String label) {
		this.point = point;
		this.label = label;
	}

	public DataPoint(ArrayList<Float> point) {
		this.point = point;
	}

	public ArrayList<Float> getData() {
		return this.point;
	}

	public String getLabel() {
		if (this.label != null)
			return this.label;
		else
			return null;
	}
}
