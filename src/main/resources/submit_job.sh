$SPARK_HOME/bin/spark-submit --name "UserActivityLogProcessor" \
--class org.guaqamole.java.UserActivityLogProcessor \
--master spark://192.168.51.190:7077 \
$SPARK_JARS/UserActivityLogProcessor-0.1.jar \
hdfs:///data/2019-Oct.csv hdfs:///data/2019-Nov.csv