package org.redisson.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.redisson.config.Config;

/**
 * Tests for {@link RedissonManager}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class RedissonManagerTest {

    @Test
    void classExists() {
        assertThat(RedissonManager.class).isNotNull();
    }

    @Test
    void constructorIsPrivate() throws Exception {
        var constructor = RedissonManager.class.getDeclaredConstructor();
        assertThat(constructor.canAccess(null)).isFalse();
    }

    @Test
    void convertAddsRedisPrefix() throws Exception {
        Method convertMethod = RedissonManager.class.getDeclaredMethod("convert", List.class);
        convertMethod.setAccessible(true);

        List<String> nodes = Arrays.asList("host1:6379", "host2:6379");
        String[] result = (String[]) convertMethod.invoke(null, nodes);

        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo("redis://host1:6379");
        assertThat(result[1]).isEqualTo("redis://host2:6379");
    }

    @Test
    void convertPreservesExistingRedisPrefix() throws Exception {
        Method convertMethod = RedissonManager.class.getDeclaredMethod("convert", List.class);
        convertMethod.setAccessible(true);

        List<String> nodes = Arrays.asList("redis://host1:6379", "rediss://host2:6379");
        String[] result = (String[]) convertMethod.invoke(null, nodes);

        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo("redis://host1:6379");
        assertThat(result[1]).isEqualTo("rediss://host2:6379");
    }

    @Test
    void convertMixedPrefixes() throws Exception {
        Method convertMethod = RedissonManager.class.getDeclaredMethod("convert", List.class);
        convertMethod.setAccessible(true);

        List<String> nodes = Arrays.asList("host1:6379", "redis://host2:6379", "rediss://host3:6379");
        String[] result = (String[]) convertMethod.invoke(null, nodes);

        assertThat(result).hasSize(3);
        assertThat(result[0]).isEqualTo("redis://host1:6379");
        assertThat(result[1]).isEqualTo("redis://host2:6379");
        assertThat(result[2]).isEqualTo("rediss://host3:6379");
    }

    @Test
    void isLinuxPlatformReturnsFalseOnNonLinux() throws Exception {
        Method method = RedissonManager.class.getDeclaredMethod("isLinuxPlatform");
        method.setAccessible(true);

        String osName = System.getProperty("os.name");
        boolean expected = osName != null && osName.toLowerCase().contains("linux");
        boolean result = (boolean) method.invoke(null);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void isLinuxPlatformHandlesNullOsName() throws Exception {
        Method method = RedissonManager.class.getDeclaredMethod("isLinuxPlatform");
        method.setAccessible(true);

        String originalOsName = System.getProperty("os.name");
        try {
            System.clearProperty("os.name");
            boolean result = (boolean) method.invoke(null);
            assertThat(result).isFalse();
        } finally {
            if (originalOsName != null) {
                System.setProperty("os.name", originalOsName);
            }
        }
    }

    @Test
    void buildConfigSingleServer() {
        MyRedisProperties props = new MyRedisProperties();
        props.setHost("localhost");
        props.setPort(6379);
        props.setDatabase(0);
        props.setPassword("secret");
        props.setTimeout(Duration.ofMillis(5000));
        props.setSsl(false);

        Config config = RedissonManager.buildConfig(props);
        assertThat(config).isNotNull();
        assertThat(config.useSingleServer()).isNotNull();
        assertThat(config.useSingleServer().getDatabase()).isEqualTo(0);
    }

    @Test
    void buildConfigSingleServerSsl() {
        MyRedisProperties props = new MyRedisProperties();
        props.setHost("localhost");
        props.setPort(6380);
        props.setSsl(true);
        props.setTimeout(Duration.ofMillis(3000));

        Config config = RedissonManager.buildConfig(props);
        assertThat(config).isNotNull();
        assertThat(config.useSingleServer()).isNotNull();
        assertThat(config.useSingleServer().getAddress().toString()).contains("localhost");
    }

    @Test
    void buildConfigSingleServerNullTimeout() {
        MyRedisProperties props = new MyRedisProperties();
        props.setTimeout(null);

        Config config = RedissonManager.buildConfig(props);
        assertThat(config).isNotNull();
        assertThat(config.useSingleServer()).isNotNull();
    }

    @Test
    void buildConfigSentinel() {
        MyRedisProperties props = new MyRedisProperties();
        props.setDatabase(1);
        props.setPassword("sentinelPass");
        props.setTimeout(Duration.ofMillis(2000));

        MyRedisProperties.Sentinel sentinel = new MyRedisProperties.Sentinel();
        sentinel.setMaster("mymaster");
        sentinel.setNodes(Arrays.asList("host1:26379", "host2:26379"));
        sentinel.setPassword("sentinelPass");
        props.setSentinel(sentinel);

        Config config = RedissonManager.buildConfig(props);
        assertThat(config).isNotNull();
        assertThat(config.useSentinelServers()).isNotNull();
        assertThat(config.useSentinelServers().getMasterName()).isEqualTo("mymaster");
        assertThat(config.useSentinelServers().getDatabase()).isEqualTo(1);
    }

    @Test
    void buildConfigCluster() {
        MyRedisProperties props = new MyRedisProperties();
        props.setPassword("clusterPass");
        props.setTimeout(Duration.ofMillis(4000));

        MyRedisProperties.Cluster cluster = new MyRedisProperties.Cluster();
        cluster.setNodes(Arrays.asList("host1:7000", "host2:7001", "host3:7002"));
        cluster.setMaxRedirects(3);
        props.setCluster(cluster);

        Config config = RedissonManager.buildConfig(props);
        assertThat(config).isNotNull();
        assertThat(config.useClusterServers()).isNotNull();
        assertThat(config.useClusterServers().getNodeAddresses()).hasSize(3);
    }

    @Test
    void buildConfigSentinelWithRedisPrefix() {
        MyRedisProperties props = new MyRedisProperties();
        props.setTimeout(Duration.ofMillis(1000));

        MyRedisProperties.Sentinel sentinel = new MyRedisProperties.Sentinel();
        sentinel.setMaster("mymaster");
        sentinel.setNodes(Arrays.asList("redis://host1:26379", "rediss://host2:26379"));
        props.setSentinel(sentinel);
        props.setSentinel(sentinel);

        Config config = RedissonManager.buildConfig(props);
        assertThat(config).isNotNull();
        assertThat(config.useSentinelServers()).isNotNull();
    }
}
