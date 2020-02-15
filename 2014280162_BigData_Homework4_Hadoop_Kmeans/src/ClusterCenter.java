import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;

public class ClusterCenter implements WritableComparable<ClusterCenter> {
	private Vector center;
	private IntWritable id;

	public ClusterCenter() {
		super();
		this.center = null;
	}

	public ClusterCenter(IntWritable id, ClusterCenter center) {
		super();
		this.center = new Vector(center.center);
		this.id = id;
		System.out.println("Center is " + id);
	}

	public ClusterCenter(IntWritable id, Vector center) {
		super();
		this.center = center;
		this.id = id;
	}

	public boolean converged(ClusterCenter center) {
		return compareTo(center) == 0 ? false : true;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		center.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.center = new Vector();
		center.readFields(in);
	}

	@Override
	public int compareTo(ClusterCenter o) {
		return center.compareTo(o.getCenter());
	}

	public Vector getCenter() {
		return this.center;
	}

	public IntWritable getId() {
		return this.id;
	}

	@Override
	public String toString() {
		// double sum = 0.0;
		// for (int i = 0; i < center.getVector().length; i++) {
		// sum += center.getVector()[i];
		// }
		return "Clustercenter [center=" + id + "]";
	}
}
