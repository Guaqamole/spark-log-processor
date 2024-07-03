CREATE EXTERNAL TABLE IF NOT EXISTS user_activity_log (
    event_time STRING,
    event_type STRING,
    product_id BIGINT,
    category_id BIGINT,
    category_code STRING,
    brand STRING,
    price DOUBLE,
    user_id BIGINT,
    user_session STRING
)
PARTITIONED BY (event_date STRING)
STORED AS PARQUET
LOCATION 'hdfs:///user/hive/external/user_activity_log'
TBLPROPERTIES ('parquet.compression'='SNAPPY');