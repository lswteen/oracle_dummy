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
# 기본 메모리 DB 모드
java -jar target/oracle-connection-test-1.0.0.jar \
--spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1 \
--spring.datasource.username=sa \
--spring.datasource.password= \
--spring.datasource.driver-class-name=org.h2.Driver