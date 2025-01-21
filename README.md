## oracle connection pool
```text
oracle-connection-test/
├── .gitignore
├── README.md
├── pom.xml
└── src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── oracletest/
│   │               ├── OracleConnectionTest.java
│   │               └── config/
│   │                   └── DatabaseConfig.java
│   └── resources/
│       ├── application.yml
│       └── logback-spring.xml
└── test/
└── java/
└── com/
└── example/
└── oracletest/
└── OracleConnectionTestApplicationTests.java
```
##
```text
# jar 생성
mvn clean package

# jar 실행 (기본)
java -jar target/oracle-connection-test-1.0.0.jar

# 또는 외부 설정 파일 사용
java -jar target/oracle-connection-test-1.0.0.jar --spring.config.location=file:/path/to/application.yml

# 또는 명령줄에서 직접 DB 설정
java -jar target/oracle-connection-test-1.0.0.jar \
--spring.datasource.url=jdbc:oracle:thin:@//your_host:1521/your_service_name \
--spring.datasource.username=your_username \
--spring.datasource.password=your_password
```