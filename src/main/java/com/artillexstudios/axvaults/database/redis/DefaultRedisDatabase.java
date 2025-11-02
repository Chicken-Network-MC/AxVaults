package com.artillexstudios.axvaults.database.redis;

import com.artillexstudios.axvaults.AxVaults;
import com.chickennw.utils.database.redis.RedisDatabase;
import com.chickennw.utils.models.config.redis.RedisConfiguration;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DefaultRedisDatabase extends RedisDatabase {

    private static final Logger log = LoggerFactory.getLogger(DefaultRedisDatabase.class);
    private static DefaultRedisDatabase instance;
    private final String format = "%s:%s:lock"; // prefix:uuid
    private final String prefix;
    private final RedisCommands<String, String> syncCommands;
    private final Executor executor;

    public static DefaultRedisDatabase getInstance() {
        if (instance == null) {
            RedisConfiguration configuration = new RedisConfiguration();
            configuration.setHost(AxVaults.CONFIG.get("redis.host"));
            configuration.setPort(AxVaults.CONFIG.getInt("redis.port"));
            configuration.setPassword(AxVaults.CONFIG.get("redis.password"));
            configuration.setUser(AxVaults.CONFIG.get("redis.user"));
            instance = new DefaultRedisDatabase(configuration);
        }

        return instance;
    }

    private DefaultRedisDatabase(RedisConfiguration redisConfiguration) {
        super(redisConfiguration);

        prefix = AxVaults.CONFIG.get("redis.prefix");
        syncCommands = redisConnection.sync();
        ThreadFactory factory = Thread.ofVirtual()
                .name("axvaults-redis-worker-", 0)
                .uncaughtExceptionHandler((thread, throwable) -> throwable.printStackTrace())
                .factory();
        executor = Executors.newFixedThreadPool(10, factory);
    }

    @Override
    public void onMessage(String s, String s1) {

    }

    public CompletableFuture<Void> lock(UUID uuid) {
        log.info("Locking user with uuid {}", uuid);
        return CompletableFuture.runAsync(() ->
                syncCommands.setex(String.format(format, prefix, uuid.toString()), 60, "true"), executor);
    }

    public CompletableFuture<Void> unlock(UUID uuid) {
        log.info("Unlocking user with uuid {}", uuid);
        return CompletableFuture.runAsync(() -> syncCommands.del(String.format(format, prefix, uuid.toString())));
    }

    public CompletableFuture<Boolean> isLocked(UUID uuid) {
        log.info("Checking if user with uuid is locked {}", uuid);
        return CompletableFuture.supplyAsync(() -> {
            String raw = syncCommands.get(String.format(format, prefix, uuid.toString()));

            log.info("Is locked raw value: {}", (raw != null));
            return raw != null;
        }, executor);
    }

    // totally not ai generated shutdown logic
    public void shutdown() {
        log.info("Shutting down redis database");

        if (executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdownNow();
            log.info("Executor shutdown completed");
        }

        String pattern = prefix + ":*:lock";

        try {
            ScanCursor cursor = ScanCursor.INITIAL;
            ScanArgs scanArgs = ScanArgs.Builder.limit(100).match(pattern);
            int deletedCount = 0;
            int maxIterations = 1000;
            int iteration = 0;
            KeyScanCursor<String> scanResult = null;

            do {
                scanResult = syncCommands.scan(cursor, scanArgs);

                List<String> keys = scanResult.getKeys();
                if (!keys.isEmpty()) {
                    long deleted = syncCommands.del(keys.toArray(new String[0]));
                    deletedCount += (int) deleted;
                    log.info("Deleted {} lock keys (iteration {})", deleted, iteration);
                }

                cursor = ScanCursor.of(scanResult.getCursor());
                iteration++;

                if (iteration >= maxIterations) {
                    log.warn("Reached max iterations ({}), stopping scan", maxIterations);
                    break;
                }

                if ("0".equals(scanResult.getCursor())) {
                    break;
                }
            } while (!scanResult.isFinished());

            log.info("Total deleted lock keys: {} in {} iterations", deletedCount, iteration);
        } catch (Exception e) {
            log.error("Error while cleaning up locks", e);
        }

        if (redisConnection.isOpen()) {
            redisConnection.close();
            log.info("Redis connection closed");
        }
        log.info("Redis database shutdown");
    }
}
