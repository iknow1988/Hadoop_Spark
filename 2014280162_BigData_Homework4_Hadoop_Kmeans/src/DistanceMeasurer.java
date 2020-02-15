
public class DistanceMeasurer {
	public static final double measureDistance(Vector center, Vector v) {
		double sum = 0;
		int length = v.getVector().length;
		for (int i = 0; i < length; i++) {
			sum += Math.abs(center.getVector()[i] - v.getVector()[i]);
		}

		return sum;
	}
}
