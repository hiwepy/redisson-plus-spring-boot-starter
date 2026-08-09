package org.redisson.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;

/**
 * Tests for {@link RedissonOperationTemplate}.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 */
class RedissonOperationTemplateTest {

    private RedissonClient redissonClient;
    private RedissonOperationTemplate template;

    @BeforeEach
    void setUp() {
        redissonClient = mock(RedissonClient.class);
        template = new RedissonOperationTemplate(redissonClient);
    }

    @Test
    void constructor() {
        assertThat(template).isNotNull();
    }

    @Test
    void luaIncr() {
        RScript script = mock(RScript.class);
        when(redissonClient.getScript()).thenReturn(script);
        when(script.eval(any(Mode.class), anyString(), any(ReturnType.class), any(java.util.List.class), any())).thenReturn("OK");

        RAtomicLong atomicLong = mock(RAtomicLong.class);
        when(redissonClient.getAtomicLong(anyString())).thenReturn(atomicLong);

        RAtomicLong result = template.luaIncr("testKey", 1);
        assertThat(result).isNotNull();
    }

    @Test
    void loadLuaScript() {
        RScript script = mock(RScript.class);
        when(redissonClient.getScript()).thenReturn(script);
        when(script.scriptLoad(anyString())).thenReturn("sha123");

        String sha = template.loadLuaScript("return 1");
        assertThat(sha).isEqualTo("sha123");
    }

    @Test
    void executeLuaScriptWithKeys() {
        RScript script = mock(RScript.class);
        when(redissonClient.getScript()).thenReturn(script);
        java.util.List<Object> keys = new ArrayList<>();
        when(script.eval(any(Mode.class), anyString(), any(ReturnType.class), any(java.util.List.class), any(Object[].class))).thenReturn("result");

        Object result = template.executeLuaScript("return 1", ReturnType.VALUE, keys);
        assertThat(result).isNotNull();
    }

    @Test
    void executeLuaScriptWithMode() {
        RScript script = mock(RScript.class);
        when(redissonClient.getScript()).thenReturn(script);
        java.util.List<Object> keys = new ArrayList<>();
        when(script.eval(any(Mode.class), anyString(), any(ReturnType.class), any(java.util.List.class), any(Object[].class))).thenReturn("result");

        Object result = template.executeLuaScript(Mode.READ_WRITE, "return 1", ReturnType.VALUE, keys);
        assertThat(result).isNotNull();
    }

    @Test
    void executeLuaScriptSimple() {
        RScript script = mock(RScript.class);
        when(redissonClient.getScript()).thenReturn(script);
        when(script.eval(any(Mode.class), anyString(), any(ReturnType.class))).thenReturn("result");

        Object result = template.executeLuaScript("return 1", ReturnType.VALUE);
        assertThat(result).isEqualTo("result");
    }

    @Test
    void executeLuaScriptWithModeSimple() {
        RScript script = mock(RScript.class);
        when(redissonClient.getScript()).thenReturn(script);
        when(script.eval(any(Mode.class), anyString(), any(ReturnType.class))).thenReturn("result");

        Object result = template.executeLuaScript(Mode.READ_WRITE, "return 1", ReturnType.VALUE);
        assertThat(result).isEqualTo("result");
    }

    @Test
    void tryLockSuccess() throws InterruptedException {
        RLock fairLock = mock(RLock.class);
        when(redissonClient.getFairLock(anyString())).thenReturn(fairLock);
        when(fairLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        RLock result = template.tryLock("lockKey", 10000, 3, 100);
        assertThat(result).isNotNull();
    }

    @Test
    void tryLockRetryThenSuccess() throws InterruptedException {
        RLock fairLock = mock(RLock.class);
        when(redissonClient.getFairLock(anyString())).thenReturn(fairLock);
        when(fairLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false)
                .thenReturn(true);

        RLock result = template.tryLock("lockKey", 10000, 3, 10);
        assertThat(result).isNotNull();
    }

    @Test
    void tryLockException() throws InterruptedException {
        RLock fairLock = mock(RLock.class);
        when(redissonClient.getFairLock(anyString())).thenReturn(fairLock);
        when(fairLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                .thenThrow(new RuntimeException("test exception"));

        RLock result = template.tryLock("lockKey", 10000, 3, 100);
        assertThat(result).isNotNull();
    }

    @Test
    void tryLockFailure() throws InterruptedException {
        RLock fairLock = mock(RLock.class);
        when(redissonClient.getFairLock(anyString())).thenReturn(fairLock);
        when(fairLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        RLock result = template.tryLock("lockKey", 10000, 1, 10);
        assertThat(result).isNotNull();
    }

    @Test
    void unlockSuccess() {
        RLock fairLock = mock(RLock.class);
        when(fairLock.isLocked()).thenReturn(true);
        when(fairLock.isHeldByCurrentThread()).thenReturn(true);

        boolean result = template.unlock(fairLock);
        assertThat(result).isTrue();
    }

    @Test
    void unlockNotLocked() {
        RLock fairLock = mock(RLock.class);
        when(fairLock.isLocked()).thenReturn(false);

        boolean result = template.unlock(fairLock);
        assertThat(result).isFalse();
    }

    @Test
    void unlockNotHeldByCurrentThread() {
        RLock fairLock = mock(RLock.class);
        when(fairLock.isLocked()).thenReturn(true);
        when(fairLock.isHeldByCurrentThread()).thenReturn(false);

        boolean result = template.unlock(fairLock);
        assertThat(result).isFalse();
    }

    @Test
    void unlockException() {
        RLock fairLock = mock(RLock.class);
        when(fairLock.isLocked()).thenThrow(new RuntimeException("test"));

        boolean result = template.unlock(fairLock);
        assertThat(result).isFalse();
    }

    @Test
    void deductCashAccountSuccess() throws InterruptedException {
        RLock lock = mock(RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        RBucket<Integer> bucket = mock(RBucket.class);
        when(redissonClient.<Integer>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(100);

        boolean result = template.deductCashAccount("acc1", 10);
        assertThat(result).isTrue();
    }

    @Test
    void deductCashAccountLockFailed() throws InterruptedException {
        RLock lock = mock(RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        boolean result = template.deductCashAccount("acc1", 10);
        assertThat(result).isFalse();
    }

    @Test
    void deductCashAccountZeroBalance() throws InterruptedException {
        RLock lock = mock(RLock.class);
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        RBucket<Integer> bucket = mock(RBucket.class);
        when(redissonClient.<Integer>getBucket(anyString())).thenReturn(bucket);
        when(bucket.get()).thenReturn(0);

        boolean result = template.deductCashAccount("acc1", 10);
        assertThat(result).isFalse();
    }

    @Test
    void tryLockWithRetrySuccess() throws InterruptedException {
        RLock fairLock = mock(RLock.class);
        when(redissonClient.getFairLock(anyString())).thenReturn(fairLock);
        when(fairLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false)
                .thenReturn(true);

        RLock result = template.tryLock("lockKey", 10000, 3, 1);
        assertThat(result).isNotNull();
    }

    @Test
    void tryLockWithRetryAllFail() throws InterruptedException {
        RLock fairLock = mock(RLock.class);
        when(redissonClient.getFairLock(anyString())).thenReturn(fairLock);
        when(fairLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        RLock result = template.tryLock("lockKey", 10000, 2, 1);
        assertThat(result).isNotNull();
    }

    @Test
    void tryLockWithException() throws InterruptedException {
        RLock fairLock = mock(RLock.class);
        when(redissonClient.getFairLock(anyString())).thenReturn(fairLock);
        when(fairLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                .thenThrow(new RuntimeException("connection lost"));

        RLock result = template.tryLock("lockKey", 10000, 3, 100);
        assertThat(result).isNotNull();
    }

    @Test
    void tryLockWithRetryException() throws InterruptedException {
        RLock fairLock = mock(RLock.class);
        when(redissonClient.getFairLock(anyString())).thenReturn(fairLock);
        when(fairLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false)
                .thenThrow(new RuntimeException("retry error"));

        RLock result = template.tryLock("lockKey", 10000, 3, 1);
        assertThat(result).isNotNull();
    }
}
