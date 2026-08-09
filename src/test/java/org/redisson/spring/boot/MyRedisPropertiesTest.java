package org.redisson.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MyRedisProperties}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class MyRedisPropertiesTest {

    @Test
    void defaultValues() {
        MyRedisProperties props = new MyRedisProperties();
        assertThat(props.getDatabase()).isEqualTo(0);
        assertThat(props.getHost()).isEqualTo("localhost");
        assertThat(props.getPassword()).isNull();
        assertThat(props.getPort()).isEqualTo(6379);
        assertThat(props.isSsl()).isFalse();
        assertThat(props.getTimeout()).isEqualTo(Duration.ofMillis(10000));
        assertThat(props.getUrl()).isNull();
        assertThat(props.getClientName()).isNull();
        assertThat(props.getSentinel()).isNull();
        assertThat(props.getCluster()).isNull();
        assertThat(props.getRedisson()).isNotNull();
    }

    @Test
    void setAndGetValues() {
        MyRedisProperties props = new MyRedisProperties();
        props.setDatabase(1);
        assertThat(props.getDatabase()).isEqualTo(1);

        props.setHost("192.168.1.100");
        assertThat(props.getHost()).isEqualTo("192.168.1.100");

        props.setPassword("secret");
        assertThat(props.getPassword()).isEqualTo("secret");

        props.setPort(6380);
        assertThat(props.getPort()).isEqualTo(6380);

        props.setSsl(true);
        assertThat(props.isSsl()).isTrue();

        props.setTimeout(Duration.ofMillis(5000));
        assertThat(props.getTimeout()).isEqualTo(Duration.ofMillis(5000));

        props.setUrl("redis://user:pass@host:6379");
        assertThat(props.getUrl()).isEqualTo("redis://user:pass@host:6379");

        props.setClientName("myClient");
        assertThat(props.getClientName()).isEqualTo("myClient");
    }

    @Test
    void sentinelProperties() {
        MyRedisProperties.Sentinel sentinel = new MyRedisProperties.Sentinel();
        sentinel.setMaster("mymaster");
        sentinel.setNodes(Arrays.asList("host1:26379", "host2:26379"));
        sentinel.setPassword("sentinelPass");

        assertThat(sentinel.getMaster()).isEqualTo("mymaster");
        assertThat(sentinel.getNodes()).hasSize(2);
        assertThat(sentinel.getPassword()).isEqualTo("sentinelPass");
    }

    @Test
    void clusterProperties() {
        MyRedisProperties.Cluster cluster = new MyRedisProperties.Cluster();
        cluster.setNodes(Arrays.asList("host1:7000", "host2:7001"));
        cluster.setMaxRedirects(3);

        assertThat(cluster.getNodes()).hasSize(2);
        assertThat(cluster.getMaxRedirects()).isEqualTo(3);
    }

    @Test
    void redissonProperties() {
        MyRedisProperties.Redisson redisson = new MyRedisProperties.Redisson();
        redisson.setThreads(4);
        redisson.setNettyThreads(8);
        redisson.setDnsMonitoringInterval(3000);
        redisson.setSubscriptionConnectionMinimumIdleSize(2);
        redisson.setSubscriptionConnectionPoolSize(100);
        redisson.setSubscriptionsPerConnection(10);
        redisson.setIdleConnectionTimeout(20000);
        redisson.setPingTimeout(2000);
        redisson.setPingConnectionInterval(5000);
        redisson.setResponseTimeout(60000);
        redisson.setRetryAttempts(5);
        redisson.setRetryInterval(3000);
        redisson.setReconnectionTimeout(60000);
        redisson.setFailedAttempts(5);
        redisson.setKeepAlive(false);
        redisson.setTcpNoDelay(true);

        assertThat(redisson.getThreads()).isEqualTo(4);
        assertThat(redisson.getNettyThreads()).isEqualTo(8);
        assertThat(redisson.getDnsMonitoringInterval()).isEqualTo(3000);
        assertThat(redisson.getSubscriptionConnectionMinimumIdleSize()).isEqualTo(2);
        assertThat(redisson.getSubscriptionConnectionPoolSize()).isEqualTo(100);
        assertThat(redisson.getSubscriptionsPerConnection()).isEqualTo(10);
        assertThat(redisson.getIdleConnectionTimeout()).isEqualTo(20000);
        assertThat(redisson.getPingTimeout()).isEqualTo(2000);
        assertThat(redisson.getPingConnectionInterval()).isEqualTo(5000);
        assertThat(redisson.getResponseTimeout()).isEqualTo(60000);
        assertThat(redisson.getRetryAttempts()).isEqualTo(5);
        assertThat(redisson.getRetryInterval()).isEqualTo(3000);
        assertThat(redisson.getReconnectionTimeout()).isEqualTo(60000);
        assertThat(redisson.getFailedAttempts()).isEqualTo(5);
        assertThat(redisson.isKeepAlive()).isFalse();
        assertThat(redisson.isTcpNoDelay()).isTrue();
    }

    @Test
    void poolProperties() {
        MyRedisProperties.Pool pool = new MyRedisProperties.Pool();
        assertThat(pool.getMaxIdle()).isEqualTo(32);
        assertThat(pool.getMinIdle()).isEqualTo(0);
        assertThat(pool.getMaxActive()).isEqualTo(64);

        pool.setMaxIdle(64);
        pool.setMinIdle(10);
        pool.setMaxActive(128);

        assertThat(pool.getMaxIdle()).isEqualTo(64);
        assertThat(pool.getMinIdle()).isEqualTo(10);
        assertThat(pool.getMaxActive()).isEqualTo(128);
    }

    @Test
    void redissonPool() {
        MyRedisProperties props = new MyRedisProperties();
        assertThat(props.getRedisson().getPool()).isNotNull();
        assertThat(props.getRedisson().getPool().getMaxIdle()).isEqualTo(32);
    }
}
