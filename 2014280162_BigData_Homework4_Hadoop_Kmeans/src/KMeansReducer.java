import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.mapreduce.Reducer;

public class KMeansReducer extends
		Reducer<IntWritable, Vector, IntWritable, Vector> {

	private final List<ClusterCenter> centers = new ArrayList<>();
	private static final Log LOG = LogFactory.getLog(KMeansReducer.class);

	@Override
	protected void reduce(IntWritable key, Iterable<Vector> values,
			Context context) throws IOException, InterruptedException {
		List<Vector> vectorList = new ArrayList<>();
		Vector newCenter = new Vector();
		int vectorSize = 0;
		for (Vector value : values) {
			vectorSize = value.getVector().length;
			break;
		}
		newCenter.setVector(new float[vectorSize]);
		for (Vector value : values) {
			vectorList.add(new Vector(value));
			for (int i = 0; i < value.getVector().length; i++) {
				newCenter.getVector()[i] += value.getVector()[i];
			}
		}
		for (int i = 0; i < newCenter.getVector().length; i++) {
			newCenter.getVector()[i] = newCenter.getVector()[i]
					/ vectorList.size();
		}

		ClusterCenter center = new ClusterCenter(key, newCenter);
		LOG.info("Key in reducer" + key);
		centers.add(center);
		for (Vector vector : vectorList) {
			context.write(center.getId(), vector);
		}
	}

	@Override
	protected void cleanup(Context context) throws IOException,
			InterruptedException {
		super.cleanup(context);
		Configuration conf = context.getConfiguration();
		Path outPath = new Path(conf.get("centroid.path"));
		FileSystem fs = FileSystem.get(conf);
		fs.delete(outPath, true);
		try (SequenceFile.Writer out = SequenceFile.createWriter(
				context.getConfiguration(), Writer.file(outPath),
				Writer.keyClass(IntWritable.class),
				Writer.valueClass(Vector.class))) {
			for (int i = 0; i < centers.size(); i++) {
				ClusterCenter center = centers.get(i);
				out.append(center.getId(), center.getCenter());
			}
		}
	}
}
