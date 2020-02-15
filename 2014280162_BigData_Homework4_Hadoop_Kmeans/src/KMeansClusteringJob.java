import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

public class KMeansClusteringJob {
	private static int CORES;
	private static int CLUSTERS = 5;
	private static final int ITERATION = 5;
	private static final Log LOG = LogFactory.getLog(KMeansClusteringJob.class);
	private static ArrayList<DataPoint> trainDataPoints;
	private static MyReader myReader;
	private static NormalizeThread[] normalizeThreadTraining;
	private static HashMap<Integer, String> idMap = new HashMap<Integer, String>();

	private static void init(String folder, String trainFile) {
		CORES = Runtime.getRuntime().availableProcessors();
		myReader = new MyReader(folder, trainFile);
		System.out.println("All Testing and Training Data loaded");
		trainDataPoints = myReader.getAllTrainingData();
		idMap.put(0, "normal");
		idMap.put(1, "u2r");
		idMap.put(2, "r2l");
		idMap.put(3, "probe");
		idMap.put(4, "dos");
	}

	private static void normalizeAllData() {
		for (int i = 0; i < CORES; i++) {
			normalizeThreadTraining[i].start();
		}
		try {
			for (int i = 0; i < CORES; i++) {
				normalizeThreadTraining[i].join();
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted");
		}
	}

	private static void prepareAllThreads() {
		normalizeThreadTraining = new NormalizeThread[CORES];
		int trainingElems = trainDataPoints.size();
		int trainingElemsPerThread = trainingElems / CORES;
		int countTraining = 0;
		for (int i = 0; i < CORES; i++) {
			if (i == CORES - 1) {
				normalizeThreadTraining[i] = new NormalizeThread(
						i * trainingElemsPerThread,
						(i * trainingElemsPerThread + (trainingElems - countTraining)),
						trainDataPoints, myReader.getMaxArray(), myReader
								.getMinArray());
			} else {
				normalizeThreadTraining[i] = new NormalizeThread(i
						* trainingElemsPerThread,
						(i * trainingElemsPerThread + trainingElemsPerThread),
						trainDataPoints, myReader.getMaxArray(),
						myReader.getMinArray());
			}
			countTraining += trainingElemsPerThread;
		}
	}

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {
		long start = System.currentTimeMillis();
		String inPath = args[0];
		String outPath = args[1];
		CLUSTERS = Integer.parseInt(args[2]);
		init(inPath + "/", "kddcup.data_10_percent");
		prepareAllThreads();
		normalizeAllData();
		long end = System.currentTimeMillis();

		int iteration = 1;
		Configuration conf = new Configuration();
		conf.set("num.iteration", iteration + "");
		Path in = new Path(inPath + "/input");
		Path center = new Path(inPath + "/center");
		conf.set("centroid.path", center.toString());
		Path out = new Path(outPath + "/depth_1");
		Job job = Job.getInstance(conf);
		job.setJobName("KMeans Clustering");
		job.setMapperClass(KMeansMapper.class);
		job.setReducerClass(KMeansReducer.class);
		job.setJarByClass(KMeansMapper.class);
		SequenceFileInputFormat.addInputPath(job, in);
		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(out)) {
			fs.delete(out, true);
		}
		if (fs.exists(center)) {
			fs.delete(out, true);
		}
		if (fs.exists(in)) {
			fs.delete(in, true);
		}
		writeInitialCenters(conf, center, fs);
		writeInputVectors(conf, in, fs);
		
		LOG.info("Time took to process data " + (end - start));
		start = System.currentTimeMillis();
		
		FileOutputFormat.setOutputPath(job, out);
		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Vector.class);
		job.waitForCompletion(true);

		iteration++;
		while (iteration < ITERATION) {
			conf = new Configuration();
			conf.set("centroid.path", center.toString());
			conf.set("num.iteration", iteration + "");
			job = Job.getInstance(conf);
			job.setJobName("KMeans Clustering " + iteration);
			job.setMapperClass(KMeansMapper.class);
			job.setReducerClass(KMeansReducer.class);
			job.setJarByClass(KMeansMapper.class);
			in = new Path(outPath + "/depth_" + (iteration - 1) + "/");
			out = new Path(outPath + "/depth_" + iteration);
			SequenceFileInputFormat.addInputPath(job, in);
			if (fs.exists(out))
				fs.delete(out, true);
			SequenceFileOutputFormat.setOutputPath(job, out);
			job.setInputFormatClass(SequenceFileInputFormat.class);
			job.setOutputFormatClass(SequenceFileOutputFormat.class);
			job.setOutputKeyClass(IntWritable.class);
			job.setOutputValueClass(Vector.class);
			job.waitForCompletion(true);
			iteration++;
		}
		end = System.currentTimeMillis();
		LOG.info("Time took to train " + (end - start));
		
		start = System.currentTimeMillis();
		Path result = new Path(outPath + "/depth_" + (iteration - 1) + "/");
		FileStatus[] stati = fs.listStatus(result);
		int count = 0;
		HashMap<String, HashMap<String, Integer>> countMap = new HashMap<String, HashMap<String, Integer>>();
		for (FileStatus status : stati) {
			if (!status.isDirectory()) {
				Path path = status.getPath();
				if (!path.getName().equals("_SUCCESS")) {
					LOG.info("FOUND " + path.toString());
					try (SequenceFile.Reader reader = new SequenceFile.Reader(
							conf, Reader.file(path))) {
						IntWritable key = new IntWritable();
						Vector v = new Vector();
						while (reader.next(key, v)) {
							DataPoint p = trainDataPoints.get(count);
							String type = myReader.getAttack()
									.get(p.getLabel());
							if (countMap.containsKey(key.toString())) {
								if (countMap.get(key.toString()).containsKey(
										type)) {
									countMap.get(key.toString()).put(
											type,
											countMap.get(key.toString()).get(
													type) + 1);
								} else {
									countMap.get(key.toString()).put(type, 1);
								}
							} else {
								countMap.put(key.toString(),
										new HashMap<String, Integer>());
								countMap.get(key.toString()).put(type, 1);
							}
							count++;
						}
					}
				}
			}
		}
		for (Map.Entry<String, HashMap<String, Integer>> entry : countMap
				.entrySet()) {
			HashMap<String, Integer> value = entry.getValue();
			System.out.print(entry.getKey() + "\t");
			for (Map.Entry<String, Integer> en : value.entrySet()) {
				System.out.print(en.getKey() + " : " + en.getValue() + ", ");
			}
			System.out.println();
		}
		end = System.currentTimeMillis();

		LOG.info("Time took to process output " + (end - start));
	}

	public static void writeInputVectors(Configuration conf, Path in,
			FileSystem fs) throws IOException {
		ArrayList<Double> center = new ArrayList<Double>();
		for (int i = 0; i < trainDataPoints.get(0).getData().size(); i++) {
			center.add(0.0);
		}
		try (SequenceFile.Writer dataWriter = SequenceFile.createWriter(conf,
				Writer.file(in), Writer.keyClass(IntWritable.class),
				Writer.valueClass(Vector.class))) {
			for (int i = 0; i < trainDataPoints.size(); i++) {
				DataPoint dp = trainDataPoints.get(i);
				ArrayList<Float> data = dp.getData();
				dataWriter.append(new IntWritable(-1), new Vector(data));
			}
		}
	}

	public static void writeInitialCenters(Configuration conf, Path center,
			FileSystem fs) throws IOException {
		int totalData = trainDataPoints.size();
		int step = totalData / CLUSTERS;
		try (SequenceFile.Writer centerWriter = SequenceFile.createWriter(conf,
				Writer.file(center), Writer.keyClass(IntWritable.class),
				Writer.valueClass(Vector.class))) {
			for (int i = 0; i < CLUSTERS; i++) {
				DataPoint dp = trainDataPoints.get(i * step);
				final IntWritable value = new IntWritable(i);
				centerWriter.append(value, new Vector(dp.getData()));
			}
		}
	}
}
