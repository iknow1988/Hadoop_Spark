import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.io.WritableComparable;

public class Vector implements WritableComparable<Vector> {

	private float[] vector;

	public Vector() {
		super();
	}

	public Vector(Vector v) {
		super();
		int l = v.vector.length;
		this.vector = new float[l];
		System.arraycopy(v.vector, 0, this.vector, 0, l);
	}

	public Vector(ArrayList<Float> vector) {
		super();
		int l = vector.size();
		this.vector = new float[l];
		for (int i = 0; i < l; i++) {
			this.vector[i] = vector.get(i);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(vector.length);
		for (int i = 0; i < vector.length; i++)
			out.writeFloat(vector[i]);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int size = in.readInt();
		vector = new float[size];
		for (int i = 0; i < size; i++)
			vector[i] = in.readFloat();
	}

	@Override
	public int compareTo(Vector o) {

		boolean equals = true;
		for (int i = 0; i < vector.length; i++) {
			if (vector[i] != o.vector[i]) {
				equals = false;
				break;
			}
		}
		if (equals)
			return 0;
		else
			return 1;
	}

	public float[] getVector() {
		return vector;
	}

	public void setVector(float[] vector) {
		this.vector = vector;
	}

	@Override
	public String toString() {
		return "Vector [vector=]" + Arrays.toString(vector) + "]";
	}

}
