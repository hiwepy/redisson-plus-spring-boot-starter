package org.redisson.mybatis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.apache.ibatis.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

/**
 * Tests for {@link RedissonCache}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class RedissonCacheTest {

    private RedissonCache cache;
    private RMapCache<Object, Object> mockMapCache;

    @BeforeEach
    void setUp() throws Exception {
        cache = new RedissonCache("testId");
        mockMapCache = mock(RMapCache.class);
        Field mapCacheField = RedissonCache.class.getDeclaredField("mapCache");
        mapCacheField.setAccessible(true);
        mapCacheField.set(cache, mockMapCache);
    }

    @Test
    void constructorSetsId() {
        RedissonCache newCache = new RedissonCache("myId");
        assertThat(newCache.getId()).isEqualTo("myId");
    }

    @Test
    void getReadWriteLockReturnsNull() {
        assertThat(cache.getReadWriteLock()).isNull();
    }

    @Test
    void setTimeToLive() {
        cache.setTimeToLive(5000L);
        // Verify via putObject call
        cache.putObject("key", "value");
        verify(mockMapCache).put(eq("key"), eq("value"), eq(5000L), any(), eq(0L), any());
    }

    @Test
    void setMaxIdleTime() {
        cache.setMaxIdleTime(3000L);
        cache.putObject("key", "value");
        verify(mockMapCache).put(eq("key"), eq("value"), eq(0L), any(), eq(3000L), any());
    }

    @Test
    void setMaxSize() {
        cache.setMaxSize(1000);
        // No getter, but should not throw
    }

    @Test
    void putObjectDelegatesToMapCache() {
        cache.setTimeToLive(1000L);
        cache.setMaxIdleTime(500L);
        cache.putObject("key", "value");
        verify(mockMapCache).put(eq("key"), eq("value"), eq(1000L), any(), eq(500L), any());
    }

    @Test
    void getObjectWithTtlOnly() {
        cache.setMaxIdleTime(0);
        cache.setMaxSize(0);
        when(mockMapCache.getWithTTLOnly("key")).thenReturn("value");

        Object result = cache.getObject("key");
        assertThat(result).isEqualTo("value");
        verify(mockMapCache).getWithTTLOnly("key");
    }

    @Test
    void getObjectWithMaxIdleTime() {
        cache.setMaxIdleTime(1000L);
        when(mockMapCache.get("key")).thenReturn("value");

        Object result = cache.getObject("key");
        assertThat(result).isEqualTo("value");
        verify(mockMapCache).get("key");
    }

    @Test
    void getObjectWithMaxSize() {
        cache.setMaxSize(100);
        when(mockMapCache.get("key")).thenReturn("value");

        Object result = cache.getObject("key");
        assertThat(result).isEqualTo("value");
        verify(mockMapCache).get("key");
    }

    @Test
    void removeObjectDelegatesToMapCache() {
        when(mockMapCache.remove("key")).thenReturn("removed");
        Object result = cache.removeObject("key");
        assertThat(result).isEqualTo("removed");
        verify(mockMapCache).remove("key");
    }

    @Test
    void clearDelegatesToMapCache() {
        cache.clear();
        verify(mockMapCache).clear();
    }

    @Test
    void getSizeDelegatesToMapCache() {
        when(mockMapCache.size()).thenReturn(42);
        int size = cache.getSize();
        assertThat(size).isEqualTo(42);
        verify(mockMapCache).size();
    }

    @Test
    void implementsCacheInterface() {
        assertThat(cache).isInstanceOf(Cache.class);
    }

    @Test
    void putObjectThrowsWhenNotConfigured() throws Exception {
        RedissonCache unconfiguredCache = new RedissonCache("testId");
        assertThatThrownBy(() -> unconfiguredCache.putObject("key", "value"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Redisson config is not defined");
    }

    @Test
    void getObjectThrowsWhenNotConfigured() throws Exception {
        RedissonCache unconfiguredCache = new RedissonCache("testId");
        assertThatThrownBy(() -> unconfiguredCache.getObject("key"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Redisson config is not defined");
    }

    @Test
    void removeObjectThrowsWhenNotConfigured() throws Exception {
        RedissonCache unconfiguredCache = new RedissonCache("testId");
        assertThatThrownBy(() -> unconfiguredCache.removeObject("key"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Redisson config is not defined");
    }

    @Test
    void clearThrowsWhenNotConfigured() throws Exception {
        RedissonCache unconfiguredCache = new RedissonCache("testId");
        assertThatThrownBy(() -> unconfiguredCache.clear())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Redisson config is not defined");
    }

    @Test
    void getSizeThrowsWhenNotConfigured() throws Exception {
        RedissonCache unconfiguredCache = new RedissonCache("testId");
        assertThatThrownBy(() -> unconfiguredCache.getSize())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Redisson config is not defined");
    }

    @Test
    void getMapCache() {
        RedissonClient client = mock(RedissonClient.class);
        RMapCache<Object, Object> expectedMap = mock(RMapCache.class);
        when(client.getMapCache("testId")).thenReturn(expectedMap);

        RMapCache<Object, Object> result = cache.getMapCache("testId", client);
        assertThat(result).isEqualTo(expectedMap);
    }

    @Test
    void setRedissonConfigInvalidResource() {
        assertThatThrownBy(() -> cache.setRedissonConfig("classpath:nonexistent.yml"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Can't parse config");
    }
}
