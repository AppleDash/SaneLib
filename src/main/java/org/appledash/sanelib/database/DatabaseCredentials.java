package org.appledash.sanelib.database;

/**
 * Created by appledash on 7/2/17.
 * Blackjack is best pony.
 */
public class DatabaseCredentials {
    private final String databaseType;
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;
    private final String databaseName;
    private final String tablePrefix;
    private final int maxRetries;
    private final int queryTimeout;
    private final boolean useSsl;

    public DatabaseCredentials(String databaseType, String hostname, int port, String username, String password, String databaseName, String tablePrefix, boolean useSsl) {
        this.databaseType = databaseType;

        if (!this.databaseType.equalsIgnoreCase("postgresql") && !this.databaseType.equalsIgnoreCase("mysql")) {
            throw new IllegalArgumentException("Database type must be either postgresql or mysql.");
        }

        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.tablePrefix = tablePrefix;
        this.useSsl = useSsl;
        maxRetries = 5;
        queryTimeout = 5000;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getJDBCURL() {
        if (this.databaseType.equalsIgnoreCase("postgresql")) {
            return String.format("jdbc:postgres://%s:%s/%s", hostname, port, databaseName);
        }

        return String.format("jdbc:mysql://%s:%d/%s?useSSL=%s&serverTimezone=UTC", hostname, port, databaseName, useSsl);
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public long getConnectionTimeout() {
        return 1500;
    }

    public long getIdleTimeout() {
        return (60 * 5) * 1000;
    }

    public long getMaxLifetime() {
        return (60 * 30) * 1000;
    }

    public String getDatabaseType() {
        return this.databaseType;
    }

    public int getPoolSize() {
        return 8;
    }
}
