1. Sequential Version
	 - java -jar KMeans.jar <Input file kddcup.data_10_percent> <Number of Processes> <Number of Clusters>  
	 - In the root folder training_attack_types file should be present.
2. Map Reduce version
	 - yarn jar target/2014280162_BigData_Homework4_Hadoop_Kmeans-0.0.1.jar KMeansClusteringJob <Input folder of kddcup.data_10_percent> <Output folder> <Cluster Number>
	 - In the hadoop input folder training_attack_types file should be present.
3. Spark Version
	 - Specify the input folder, the input file and the cluster number after the command
	 - $SPARK_HOME/bin/spark-submit --verbose --class $CLASS --master yarn-cluster --num-executors 16 --driver-memory 8g --executor-memory 4g --executor-cores 16 $APP </user/2014280162/spark> <kddcup.data_10_percent> <5>
