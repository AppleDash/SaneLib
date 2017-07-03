package org.appledash.sanelib.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by appledash on 7/23/16.
 * Blackjack is still best pony.
 */
public class SaneDatabase {
    public static final Logger LOGGER = Logger.getLogger("SaneDatabase");
    private HikariDataSource hikariDataSource;
    private ThreadCheckDatabaseConnection threadCheckDatabaseConnection;
    private List<ThreadRunDatabaseOperation> dbOpThreads = new ArrayList<>();
    private Queue<Runnable> databaseOperations = new ConcurrentLinkedQueue<>();
    public AtomicInteger openTransactions = new AtomicInteger(0);

    public SaneDatabase(DatabaseCredentials credentials) {
        initDataSource(credentials);
        threadCheckDatabaseConnection = new ThreadCheckDatabaseConnection(hikariDataSource);
        threadCheckDatabaseConnection.start();

        for (int i = 0; i < 4; i++) {
            ThreadRunDatabaseOperation threadRunDatabaseOperation = new ThreadRunDatabaseOperation(this, databaseOperations);
            threadRunDatabaseOperation.start();
            this.dbOpThreads.add(threadRunDatabaseOperation);
        }

        LOGGER.info("Initialized Hikari data source.");
    }

    public void cleanup() {
        if (hikariDataSource != null) {
            threadCheckDatabaseConnection.abort();
            for (ThreadRunDatabaseOperation threadRunDatabaseOperation : this.dbOpThreads) {
                threadRunDatabaseOperation.interrupt();
                threadRunDatabaseOperation.abort();
            }
            LOGGER.info("Closing Hikari data source.");
            hikariDataSource.close();
            hikariDataSource = null;
        }
    }

    public boolean areAllTransactionsDone() {
        return this.openTransactions.get() == 0;
    }

    public boolean isFinished() {
        return this.dbOpThreads.stream().allMatch(t -> t.getState() == Thread.State.TERMINATED);
    }

    private void initDataSource(DatabaseCredentials configuration) {
        if (hikariDataSource != null) {
            throw new IllegalStateException("Cannot re-initialize data source!");
        }

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(configuration.getPoolSize());
        // config.addDataSourceProperty("serverName", configuration.getHostname());
        // config.addDataSourceProperty("databaseName", configuration.getDatabaseName());
        // config.addDataSourceProperty("portNumber", configuration.getPort());
        config.addDataSourceProperty("user", configuration.getUsername());
        config.addDataSourceProperty("password", configuration.getPassword());
        config.setConnectionTimeout(configuration.getConnectionTimeout());
        config.setIdleTimeout(configuration.getIdleTimeout());
        config.setMaxLifetime(configuration.getMaxLifetime());

        config.setJdbcUrl(configuration.getJDBCURL());

        if (configuration.getDatabaseType().equalsIgnoreCase("postgres")) {
            // config.setDataSourceClassName(org.postgresql.ds.PGSimpleDataSource.class.getName());
            try {
                Class.forName(org.postgresql.ds.PGSimpleDataSource.class.getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            // config.setDataSourceClassName(com.mysql.cj.jdbc.MysqlDataSource.class.getName());
            try {
                Class.forName(com.mysql.cj.jdbc.MysqlDataSource.class.getName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

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
