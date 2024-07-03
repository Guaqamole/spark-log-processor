# spark-log-processor
## Overview

- Java 11
- spark-core_2.12:v3.1.1
- spark-sql_2.12:v3.1.1
- spark-hive_2.12:3.1.1
- slack-api-client:1.27.2



## How to run

1. `init.sql` 을 참고하여 hive table 을 생성한다.
2. `build.sh` 을 참고하여 `mvn clean package -P {env}` 환경별로 jar를 build 한다.
3. `submit_job.sh` 을 참고하여 build 한 jar를 spark-cluster에 submit 한다.
