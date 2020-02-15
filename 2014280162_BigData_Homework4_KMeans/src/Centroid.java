import java.util.ArrayList;

public class Centroid {
	private int id = -1;
	private ArrayList<Float> point = new ArrayList<Float>();
	private ArrayList<DataPoint> data = new ArrayList<DataPoint>();

	public Centroid(int id, ArrayList<Float> point) {
		for (int i = 0; i < point.size(); i++) {
			this.point.add(point.get(i));
		}
		this.id = id;
	}

	public ArrayList<Float> getData() {
		return this.point;
	}

	public int getID() {
		return this.id;
	}

	public void addDataPoint(DataPoint p) {
		this.data.add(p);
	}

	public ArrayList<DataPoint> getDataPoints() {
		return this.data;
	}
}
