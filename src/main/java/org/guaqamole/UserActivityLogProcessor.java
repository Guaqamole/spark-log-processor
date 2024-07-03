package org.guaqamole;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.avoholo.java.alert.SlackAlertManager;

import java.io.IOException;

public class UserActivityLogProcessor {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: UserActivityLogProcessor <input-csv-file1> <input-csv-file2> ...");
            System.exit(1);
        }

        SparkConf conf = new SparkConf()
                .setAppName("UserActivityLogProcessor")
                .set("hive.exec.dynamic.partition.mode", "nonstrict");

        JavaSparkContext sc = new JavaSparkContext(conf);
        String checkpointDir = System.getProperty("spark.checkpoint.dir");
        sc.setCheckpointDir(checkpointDir);

        SparkSession spark = SparkSession.builder()
                .appName("UserActivityLogProcessor")
                .enableHiveSupport()
                .config("hive.exec.dynamic.partition.mode", "nonstrict")
                .getOrCreate();

        for (String filePath : args) {
            try {
                processFile(spark, filePath, checkpointDir);
            } catch (Exception e) {
                if (!webhookUrl.isEmpty()) {
                    slackAlertManager.sendSlackAlert("Error processing file " + filePath + ": " + e.getMessage());
                } else {
                    System.err.println("Error processing file " + filePath + ": " + e.getMessage());
                }
            }
        }
        sc.stop();
    }

    private static void processFile(SparkSession spark, String filePath, String checkpointDir) throws IOException {
        String checkpointPath = checkpointDir + "/" + filePath.replaceAll("[^a-zA-Z0-9]", "_");

        Dataset<Row> data;
        FileSystem fs = FileSystem.get(new Configuration());

        if (fs.exists(new Path(checkpointPath))) {
            System.out.println("Resuming from checkpoint: " + checkpointPath);
            data = spark.read().parquet(checkpointPath);
        } else {
            data = spark.read()
                    .option("header", "true")
                    .option("inferSchema", "true")
                    .csv(filePath);

            Dataset<Row> dataWithKST = data.withColumn("event_time", functions.expr("from_unixtime(unix_timestamp(event_time) + 9 * 3600)"));

            Dataset<Row> dataWithPartition = dataWithKST.withColumn("event_date", dataWithKST.col("event_time").substr(0, 10));

            dataWithPartition.checkpoint();
            dataWithPartition.write().mode(SaveMode.Overwrite).parquet(checkpointPath);

            dataWithPartition.write()
                    .mode(SaveMode.Append)
                    .format("parquet")
                    .option("compression", "snappy")
                    .partitionBy("event_date")
                    .saveAsTable("user_activity_log");
        }
    }
}
