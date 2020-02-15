import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.mapreduce.Mapper;

public class KMeansMapper extends
		Mapper<IntWritable, Vector, IntWritable, Vector> {
	private ArrayList<Vector> centers = new ArrayList<Vector>();

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		Configuration conf = context.getConfiguration();
		Path centroids = new Path(conf.get("centroid.path"));

		try (SequenceFile.Reader reader = new SequenceFile.Reader(conf,
				Reader.file(centroids))) {
			IntWritable key = new IntWritable();
			Vector value = new Vector();
			while (reader.next(key, value)) {
				centers.add(value);
				key = new IntWritable();
				value = new Vector();
			}
		}
	}

	@Override
	protected void map(IntWritable key, Vector value, Context context)
			throws IOException, InterruptedException {

		int nearest = -1;
		double nearestDistance = Double.MAX_VALUE;
		for (int i = 0; i < centers.size(); i++) {
			Vector c = centers.get(i);
			double dist = DistanceMeasurer.measureDistance(c, value);
			if (nearest == -1) {
				nearest = i;
				nearestDistance = dist;
			} else {
				if (nearestDistance > dist) {
					nearest = i;
					nearestDistance = dist;
				}
			}
		}
		context.write(new IntWritable(nearest), value);
	}
}
