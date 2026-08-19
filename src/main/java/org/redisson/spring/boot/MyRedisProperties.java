package org.redisson.spring.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "spring.redis")
@Data
/**
 * <p>Configuration properties for MyRedis.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class MyRedisProperties {


    /**
     * Database index used by the connection factory.
     */
    private int database = 0;

    /**
     * Connection URL. Overrides host, port, and password. User is ignored. Example:
     * redis://user:password@example.com:6379
     */
    private String url;

    /**
     * Redis server host.
     */
    private String host = "localhost";

    /**
     * Login password of the redis server.
     */
    private String password;

    /**
     * Redis server port.
     */
    private int port = 6379;

    /**
     * Whether to enable SSL support.
     */
    private boolean ssl;

    /**
     * Timeout during connecting to any Redis server.
     */
    private Duration timeout = Duration.ofMillis(10000);

    /**
     * Client name to be set on connections with CLIENT SETNAME.
     */
    private String clientName;

    private Sentinel sentinel;

    private Cluster cluster;

    private final Redisson redisson = new Redisson();

    /**
     * Redisson client properties.
     */
    @Data
    public static class Redisson {

        private String config;

        private String file;

        /**
         * Threads amount shared between all redis node clients
         */
        private int threads = 0; // 0 = current_processors_amount * 2

        private int nettyThreads = 0; // 0 = current_processors_amount * 2

        /**
         * Interval in milliseconds to check DNS
         */
        private long dnsMonitoringInterval = 5000;

        /**
         * Minimum idle subscription connection amount
         */
        private int subscriptionConnectionMinimumIdleSize = 1;

        /**
         * Redis subscription connection maximum pool size
         *
         */
        private int subscriptionConnectionPoolSize = 50;

        /**
         * Subscriptions per Redis connection limit
         */
        private int subscriptionsPerConnection = 5;

        /**
         * If pooled connection not used for a <code>timeout</code> time
         * and current connections amount bigger than minimum idle connections pool size,
         * then it will closed and removed from pool.
         * Value in milliseconds.
         *
         */
        private int idleConnectionTimeout = 10000;

        /**
         * Ping timeout used in <code>Node.ping</code> and <code>Node.pingAll<code> operation.
         * Value in milliseconds.
         *
         */
        private int pingTimeout = 1000;

        private int pingConnectionInterval;

        /**
         * Redis server response timeout. Starts to countdown when Redis command has been successfully sent.
         * 等待节点回复命令的时间。该时间从命令发送成功时开始计时。
         * <p>
         * Default is <code>3000</code> milliseconds
         */
        private int responseTimeout = 30000;

        private int retryAttempts = 3;

        private int retryInterval = 1500;

        private  int reconnectionTimeout = 30000;
        private  int failedAttempts = 3;

        private boolean keepAlive = true;

        private boolean tcpNoDelay;

        /**
         * Redisson pool configuration.
         */
        private Pool pool = new Pool();

    }

    /**
     * Pool properties.
     */
    @Data
    public static class Pool {

        /**
         * Maximum number of "idle" connections in the pool. Use a negative value to
         * indicate an unlimited number of idle connections.
         */
        private int maxIdle = 32;

        /**
         * Target for the minimum number of idle connections to maintain in the pool. This
         * setting only has an effect if both it and time between eviction runs are
         * positive.
         */
        private int minIdle = 0;

        /**
         * Maximum number of connections that can be allocated by the pool at a given
         * time. Use a negative value for no limit.
         */
        private int maxActive = 64;

    }


    /**
     * Cluster properties.
     */
    @Data
    public static class Cluster {

        /**
         * Comma-separated list of "host:port" pairs to bootstrap from. This represents an
         * "initial" list of cluster nodes and is required to have at least one entry.
         */
        private List<String> nodes;

        /**
         * Maximum number of redirects to follow when executing commands across the
         * cluster.
         */
        private Integer maxRedirects;

    }

    /**
     * Redis sentinel properties.
     */
    @Data
    public static class Sentinel {

        /**
         * Name of the Redis server.
         */
        private String master;

        /**
         * Comma-separated list of "host:port" pairs.
         */
        private List<String> nodes;

        /**
         * Password for authenticating with sentinel(s).
         */
        private String password;

    }

}
