package com.farfarcoder.db;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;  // jakarta -> javax로 변경
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.zaxxer.hikari.HikariDataSource;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class DbApplication implements CommandLineRunner {

	@Autowired
	private DataSource dataSource;

	private final List<ConnectionWrapper> connections = new ArrayList<>();
	private volatile boolean isRunning = true;

	@Getter
	@Setter
	private static class ConnectionWrapper {
		private Connection connection;
		private int connectionId;
		private AtomicLong queryCount;

		public ConnectionWrapper(Connection connection, int connectionId, AtomicLong queryCount) {
			this.connection = connection;
			this.connectionId = connectionId;
			this.queryCount = queryCount;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(DbApplication.class, args);
	}

	@Override
	public void run(String... args) {
		createConnections(100);
		startContinuousQueries();
	}

	private void createConnections(int connectionCount) {
		try {
			log.info("Starting to create {} connections", connectionCount);

			for (int i = 0; i < connectionCount; i++) {
				Connection conn = dataSource.getConnection();
				connections.add(new ConnectionWrapper(conn, i + 1, new AtomicLong(0)));
				log.info("Connection {} created", i + 1);
			}

			log.info("Successfully created {} connections", connections.size());

		} catch (Exception e) {
			log.error("Error during connection creation", e);
		}
	}

	private void startContinuousQueries() {
		for (ConnectionWrapper wrapper : connections) {
			Thread queryThread = new Thread(() -> {
				while (isRunning) {
					try {
						if (!wrapper.getConnection().isClosed()) {
							try (Statement stmt = wrapper.getConnection().createStatement()) {
								ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL");
								if (rs.next()) {
									wrapper.getQueryCount().incrementAndGet();
									log.debug("Connection {} - Query {} successful",
											wrapper.getConnectionId(),
											wrapper.getQueryCount().get());
								}
							}
							Thread.sleep(1000);
						} else {
							log.warn("Connection {} is closed, attempting to reconnect",
									wrapper.getConnectionId());
							wrapper.setConnection(dataSource.getConnection());
						}
					} catch (Exception e) {
						log.error("Error in connection {}: {}",
								wrapper.getConnectionId(), e.getMessage());
						try {
							Thread.sleep(5000);
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
						}
					}
				}
			});
			queryThread.setName("Query-Thread-" + wrapper.getConnectionId());
			queryThread.start();
		}
	}

	@PreDestroy
	public void cleanup() {
		isRunning = false;
		log.info("Shutting down connection test...");
		connections.forEach(wrapper -> {
			try {
				if (wrapper.getConnection() != null && !wrapper.getConnection().isClosed()) {
					wrapper.getConnection().close();
					log.info("Connection {} closed. Total queries executed: {}",
							wrapper.getConnectionId(),
							wrapper.getQueryCount().get());
				}
			} catch (SQLException e) {
				log.error("Error closing connection {}", wrapper.getConnectionId(), e);
			}
		});
	}

	@Component
	@Slf4j
	public static class ConnectionMonitor {

		@Autowired
		private DataSource dataSource;

		@Scheduled(fixedRate = 10000)
		public void monitorConnections() {
			if (dataSource instanceof HikariDataSource) {
				HikariDataSource hikariDS = (HikariDataSource) dataSource;
				log.info("=== Connection Pool Status ===");
				log.info("Active connections: {}", hikariDS.getHikariPoolMXBean().getActiveConnections());
				log.info("Idle connections: {}", hikariDS.getHikariPoolMXBean().getIdleConnections());
				log.info("Total connections: {}", hikariDS.getHikariPoolMXBean().getTotalConnections());
			}
		}
	}
}