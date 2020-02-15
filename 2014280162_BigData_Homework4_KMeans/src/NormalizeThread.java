import java.util.ArrayList;

public class NormalizeThread extends Thread {

	private int from, to;
	private ArrayList<DataPoint> data;
	private float[] max;
	private float[] min;

	public NormalizeThread(int from, int to, ArrayList<DataPoint> data,
			float[] max, float[] min) {
		this.data = data;
		this.from = from;
		this.to = to;
		this.max = max;
		this.min = min;
	}

	public void run() {
		for (int i = from; i < to; i++) {
			DataPoint dataPoint = data.get(i);
			for (int j = 0; j < dataPoint.getData().size() - 1; j++) {
				float span = max[j] - min[j];
				if (span != 0) {
					dataPoint.getData().set(j,
							(dataPoint.getData().get(j) - min[j]) / span);
				}
			}
		}
	}
}