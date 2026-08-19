package org.redisson.spring.boot;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>RedissonManager implementation.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public final class RedissonManager {

    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";

    //必须有volatile修饰（防止指令重排序）
    private volatile static RedissonClient instance;
    //构造函数必须私有（防止外部通过构造方法创建对象）
    private RedissonManager() {
    }

    public static RedissonClient createRedissonClient(MyRedisProperties redisProperties) {
        // 第一个判空（如果是空，就不必再进入同步代码块了，提升效率）
        if (instance == null) {
            // 这里加锁，是为了防止多线程的情况下出现实例化多个对象的情况
            synchronized (RedissonManager.class) {
                // 第二个判空（如果是空，就实例化对象）
                if (instance == null) {
                    Config config = buildConfig(redisProperties);
                    instance = Redisson.create(config);
                }
            }
        }
        return instance;
    }

    /**
     * Builds a Redisson {@link Config} from the given Redis properties.
     * Supports single-server, sentinel and cluster modes.
     *
     * @param redisProperties the Redis connection properties
     * @return the Redisson configuration
     */
    static Config buildConfig(MyRedisProperties redisProperties) {
        Duration timeoutValue = redisProperties.getTimeout();
        int timeout;
        if (null == timeoutValue) {
            timeout = 0;
        } else {
            timeout = Long.valueOf(timeoutValue.toMillis()).intValue();
        }

        Config config = new Config();
        //  不能反序列化 阻塞队列中的string元素
        Codec codec = new JsonJacksonCodec();
        config.setCodec(codec);
        config.setEventLoopGroup(new NioEventLoopGroup());
        if (isLinuxPlatform() && Epoll.isAvailable()) {
            config.setEventLoopGroup(new EpollEventLoopGroup());
            config.setTransportMode(TransportMode.EPOLL);
        }

        if (redisProperties.getSentinel() != null) {
            String[] nodes = convert(redisProperties.getSentinel().getNodes());
            config.useSentinelServers()
                    .setMasterName(redisProperties.getSentinel().getMaster())
                    .addSentinelAddress(nodes)
                    .setDatabase(redisProperties.getDatabase())
                    .setConnectTimeout(timeout)
                    .setPassword(redisProperties.getPassword())
                    // 性能配置
                    .setConnectTimeout(timeout)
                    .setDnsMonitoringInterval(redisProperties.getRedisson().getDnsMonitoringInterval())
                    .setIdleConnectionTimeout(redisProperties.getRedisson().getIdleConnectionTimeout())
                    .setKeepAlive(redisProperties.getRedisson().isKeepAlive())
                    .setPingConnectionInterval(redisProperties.getRedisson().getPingConnectionInterval())
                    //.setPingTimeout(redisProperties.getRedisson().getPingTimeout())
                    .setRetryAttempts(redisProperties.getRedisson().getRetryAttempts())
                    .setRetryInterval(redisProperties.getRedisson().getRetryInterval())
                    .setSubscriptionConnectionMinimumIdleSize(redisProperties.getRedisson().getSubscriptionConnectionMinimumIdleSize())
                    .setSubscriptionConnectionPoolSize(redisProperties.getRedisson().getSubscriptionConnectionPoolSize())
                    .setSubscriptionsPerConnection(redisProperties.getRedisson().getSubscriptionsPerConnection())
                    .setTimeout(redisProperties.getRedisson().getResponseTimeout())
                    .setTcpNoDelay(redisProperties.getRedisson().isTcpNoDelay());
        } else if (redisProperties.getCluster() != null) {

            List<String> nodesObject = redisProperties.getCluster().getNodes();
            String[] nodes = convert(nodesObject);
            config = new Config();
            config.setCodec(codec);
            config.useClusterServers()
                    .addNodeAddress(nodes)
                    .setConnectTimeout(timeout)
                    .setPassword(redisProperties.getPassword())
                    // 性能配置
                    .setConnectTimeout(timeout)
                    .setDnsMonitoringInterval(redisProperties.getRedisson().getDnsMonitoringInterval())
                    .setIdleConnectionTimeout(redisProperties.getRedisson().getIdleConnectionTimeout())
                    .setKeepAlive(redisProperties.getRedisson().isKeepAlive())
                    .setPingConnectionInterval(redisProperties.getRedisson().getPingConnectionInterval())
                    //.setPingTimeout(redisProperties.getRedisson().getPingTimeout())
                    .setRetryAttempts(redisProperties.getRedisson().getRetryAttempts())
                    .setRetryInterval(redisProperties.getRedisson().getRetryInterval())
                    .setSubscriptionConnectionMinimumIdleSize(redisProperties.getRedisson().getSubscriptionConnectionMinimumIdleSize())
                    .setSubscriptionConnectionPoolSize(redisProperties.getRedisson().getSubscriptionConnectionPoolSize())
                    .setSubscriptionsPerConnection(redisProperties.getRedisson().getSubscriptionsPerConnection())
                    .setTimeout(redisProperties.getRedisson().getResponseTimeout())
                    .setTcpNoDelay(redisProperties.getRedisson().isTcpNoDelay());
        } else {

            String prefix = REDIS_PROTOCOL_PREFIX;
            if (redisProperties.isSsl()) {
                prefix = REDISS_PROTOCOL_PREFIX;
            }

            config.useSingleServer()
                    .setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort())
                    .setDatabase(redisProperties.getDatabase())
                    .setPassword(redisProperties.getPassword())
                    //.setClientName(redisProperties.getClientName())
                    // 性能配置
                    .setConnectTimeout(timeout)
                    .setConnectionMinimumIdleSize(redisProperties.getRedisson().getPool().getMinIdle())
                    .setConnectionPoolSize(redisProperties.getRedisson().getPool().getMaxActive())
                    .setDnsMonitoringInterval(redisProperties.getRedisson().getDnsMonitoringInterval())
                    .setIdleConnectionTimeout(redisProperties.getRedisson().getIdleConnectionTimeout())
                    .setKeepAlive(redisProperties.getRedisson().isKeepAlive())
                    .setPingConnectionInterval(redisProperties.getRedisson().getPingConnectionInterval())
                    //.setPingTimeout(redisProperties.getRedisson().getPingTimeout())
                    .setRetryAttempts(redisProperties.getRedisson().getRetryAttempts())
                    .setRetryInterval(redisProperties.getRedisson().getRetryInterval())
                    .setSubscriptionConnectionMinimumIdleSize(redisProperties.getRedisson().getSubscriptionConnectionMinimumIdleSize())
                    .setSubscriptionConnectionPoolSize(redisProperties.getRedisson().getSubscriptionConnectionPoolSize())
                    .setSubscriptionsPerConnection(redisProperties.getRedisson().getSubscriptionsPerConnection())
                    .setTimeout(redisProperties.getRedisson().getResponseTimeout())
                    .setTcpNoDelay(redisProperties.getRedisson().isTcpNoDelay());

        }

        return config;
    }

    private static boolean isLinuxPlatform() {
        String osName = System.getProperty("os.name");
        if (osName == null) {
            return false;
        }
        return osName.toLowerCase().contains("linux");
    }

    private static String[] convert(List<String> nodesObject) {
        List<String> nodes = new ArrayList<String>(nodesObject.size());
        for (String node : nodesObject) {
            if (!node.startsWith(REDIS_PROTOCOL_PREFIX) && !node.startsWith(REDISS_PROTOCOL_PREFIX)) {
                nodes.add(REDIS_PROTOCOL_PREFIX + node);
            } else {
                nodes.add(node);
            }
        }
        return nodes.toArray(new String[0]);
    }

}