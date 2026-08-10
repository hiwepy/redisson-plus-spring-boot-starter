package org.redisson.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;

import static org.mockito.Mockito.mock;

/**
 * Tests for {@link RedissonCachingConfiguration}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 */
class RedissonCachingConfigurationTest {

    @Test
    void canInstantiate() {
        RedissonCachingConfiguration config = new RedissonCachingConfiguration();
        assertThat(config).isNotNull();
    }

    @Test
    void redissonOperationTemplate() {
        RedissonCachingConfiguration config = new RedissonCachingConfiguration();
        RedissonClient client = mock(RedissonClient.class);
        RedissonOperationTemplate template = config.redissonOperationTemplate(client);
        assertThat(template).isNotNull();
    }
}
