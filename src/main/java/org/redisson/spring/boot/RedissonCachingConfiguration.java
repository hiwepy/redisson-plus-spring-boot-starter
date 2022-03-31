package org.redisson.spring.boot;

import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@EnableCaching(proxyTargetClass = true)
public class RedissonCachingConfiguration {

	@Bean
	@Order(1)
	public RedissonOperationTemplate redissonOperationTemplate(RedissonClient redissonClient) {
		return new RedissonOperationTemplate(redissonClient);
	}

}
