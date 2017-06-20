package org.appledash.sanelib.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by appledash on 7/23/16.
 * Blackjack is still best pony.
 */
public class SaneDatabase {
    public static final Logger LOGGER = LogManager.getLogManager().getLogger("SaneDatabase");
    private HikariDataSource hikariDataSource;
    private ThreadCheckDatabaseConnection threadCheckDatabaseConnection;
    private List<ThreadRunDatabaseOperation> dbOpThreads = new ArrayList<>();
    private Queue<Runnable> databaseOperations = new ConcurrentLinkedQueue<>();

    public SaneDatabase(ConfigurationSection config) {
        initDataSource(config);
        threadCheckDatabaseConnection = new ThreadCheckDatabaseConnection(hikariDataSource);
        threadCheckDatabaseConnection.start();

        for (int i = 0; i < 4; i++) {
            ThreadRunDatabaseOperation threadRunDatabaseOperation = new ThreadRunDatabaseOperation(databaseOperations);
            threadRunDatabaseOperation.start();
            this.dbOpThreads.add(threadRunDatabaseOperation);
        }

        LOGGER.info("Initialized Hikari data source.");
    }

    public void cleanup() {
        if (hikariDataSource != null) {
            threadCheckDatabaseConnection.abort();
            for (ThreadRunDatabaseOperation threadRunDatabaseOperation : this.dbOpThreads) {
                threadRunDatabaseOperation.abort();
            }
            LOGGER.info("Closing Hikari data source.");
            hikariDataSource.close();
            hikariDataSource = null;
        }
    }

    private void initDataSource(ConfigurationSection configuration) {
        if (hikariDataSource != null) {
            throw new IllegalStateException("Cannot re-initialize data source!");
        }

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(configuration.getInt("database.pool_size", 8));

        config.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource");
        config.addDataSourceProperty("serverName", configuration.get("host"));
        config.addDataSourceProperty("databaseName", configuration.get("database"));
        config.addDataSourceProperty("portNumber", configuration.get("port", 0));
        config.addDataSourceProperty("user", configuration.get("username"));
        config.addDataSourceProperty("password", configuration.get("password"));
        config.setConnectionTimeout(configuration.getInt("connection_timeout", 1500));
        config.setIdleTimeout(configuration.getInt("idle_timeout", (60 * 5) * 1000));
        config.setIdleTimeout(configuration.getInt("max_lifetime", (60 * 30) * 1000));
        hikariDataSource = new HikariDataSource(config);
    }

    /**
     * Check whether we currently have a connection to the database.
     * @return True if we can reasonably assume the database is connected, false otherwise
     */
    public boolean isDatabaseConnected() {
        return threadCheckDatabaseConnection != null && threadCheckDatabaseConnection.isConnected();
    }

    /**
     * Get a handle to a database connection from the connection pool.
     * @return Database connection
     * @throws SQLException If the connection can't be opened for some reason
     * @throws IllegalStateException If the plugin or database have not been initialized yet
     */
    public Connection getConnection() throws SQLException {
        if (hikariDataSource == null) {
            throw new IllegalStateException("Cannot get a database connection before Hikari is initialized!");
        }

        return hikariDataSource.getConnection();
    }

    public void runDatabaseOperationAsync(String tag, Runnable callback) {
        databaseOperations.add(() -> {
            DatabaseDebug.startDebug(tag);
            try {
                callback.run();
            } finally {
                DatabaseDebug.finishDebug(tag);
            }
        });
    }
}
