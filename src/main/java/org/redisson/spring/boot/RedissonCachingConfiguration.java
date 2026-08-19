package org.redisson.spring.boot;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@AutoConfigureAfter(RedissonAutoConfiguration.class)
@Configuration
@ConditionalOnClass(Redisson.class)
@EnableCaching(proxyTargetClass = true)
@EnableConfigurationProperties(MyRedisProperties.class)
/**
 * <p>Configuration class for RedissonCaching.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class RedissonCachingConfiguration {

	@Bean(name = {"redisson", "redissonClient"}, destroyMethod = "shutdown")
    /**
     * <p>Redisson client.</p>
     * @param redisProperties
     * @return the redisson client
     */
	public RedissonClient redissonClient(MyRedisProperties redisProperties){
		return RedissonManager.createRedissonClient(redisProperties);
	}

	@Bean
	@Order(1)
    /**
     * <p>Redisson operation template.</p>
     * @param redissonClient
     * @return the redisson operation template
     */
	public RedissonOperationTemplate redissonOperationTemplate(RedissonClient redissonClient) {
		return new RedissonOperationTemplate(redissonClient);
	}

}
