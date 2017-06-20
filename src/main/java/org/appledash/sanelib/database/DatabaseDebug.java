package org.appledash.sanelib.database;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by appledash on 7/24/16.
 * Blackjack is still best pony.
 */
public class DatabaseDebug {
    private static final Logger LOGGER = LogManager.getLogManager().getLogger("DatabaseDebug");
    private static final Map<String, Long> debugs = new ConcurrentHashMap<>();

    public synchronized static void startDebug(String tag) {
        if (debugs.containsKey(tag.toLowerCase())) {
            throw new IllegalStateException("Cannot start debugging when we're already debugging this tag!");
        }

        debugs.put(tag.toLowerCase(), System.currentTimeMillis());
    }

    public synchronized static void finishDebug(String tag) {
        if (!debugs.containsKey(tag.toLowerCase())) {
            throw new IllegalStateException("Cannot finish debugging when we never started debugging this tag!");
        }

        long startTime = debugs.remove(tag.toLowerCase());
        long delta = System.currentTimeMillis() - startTime;

        LOGGER.info("Database call " + tag + " finished in " + delta + "ms.");
    }

    public static void printStatement(PreparedStatement ps) {
        LOGGER.info("Executing query: " + ps.toString());
    }
}
