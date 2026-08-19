/**
 * Copyright (C) 2018 Jeebiz (http://jeebiz.net).
 * All Rights Reserved.
 */
package org.redisson.mybatis;

import org.apache.ibatis.cache.Cache;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.biz.utils.SpringResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * MyBatis cache implementation
 * @author Nikita Koksharov
 */
/**
 * <p>RedissonCache implementation.</p>
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class RedissonCache implements Cache {

    private String id;
    private RMapCache<Object, Object> mapCache;
    private long timeToLive;
    private long maxIdleTime;
    private int maxSize;

    public RedissonCache(String id) {
        this.id = id;
    }

    @Override
    /**
     * <p>Returns the id.</p>
     * @return the get id
     */
    public String getId() {
        return id;
    }

    @Override
    /**
     * <p>Put object.</p>
     * @param o
     * @param o1
     */
    public void putObject(Object o, Object o1) {
        check();
        mapCache.put(o, o1, timeToLive, TimeUnit.MILLISECONDS, maxIdleTime, TimeUnit.MILLISECONDS);
    }

    @Override
    /**
     * <p>Returns the object.</p>
     * @param o
     * @return the get object
     */
    public Object getObject(Object o) {
        check();
        if (maxIdleTime == 0 && maxSize == 0) {
            return mapCache.getWithTTLOnly(o);
        }

        return mapCache.get(o);
    }

    @Override
    /**
     * <p>Remove object.</p>
     * @param o
     * @return the remove object
     */
    public Object removeObject(Object o) {
        check();
        return mapCache.remove(o);
    }

    @Override
    /**
     * <p>Clear.</p>
     */
    public void clear() {
        check();
        mapCache.clear();
    }

    @Override
    /**
     * <p>Returns the size.</p>
     * @return the get size
     */
    public int getSize() {
        check();
        return mapCache.size();
    }

    /**
     * <p>Sets the time to live.</p>
     * @param timeToLive
     */
    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * <p>Sets the max idle time.</p>
     * @param maxIdleTime
     */
    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    /**
     * <p>Sets the max size.</p>
     * @param maxSize
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    /**
     * <p>Returns the read write lock.</p>
     * @return the get read write lock
     */
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    /**
     * <p>Sets the redisson config.</p>
     * @param config
     */
    public void setRedissonConfig(String config) {
        Config cfg;
        try {
            InputStream is = SpringResourceUtils.getResource(config).getInputStream();
            cfg = Config.fromYAML(is);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't parse config", e);
        }
        RedissonClient redisson = Redisson.create(cfg);
        mapCache = getMapCache(id, redisson);
        if (maxSize > 0) {
            mapCache.setMaxSize(maxSize);
        }
    }

    /**
     * <p>Returns the map cache.</p>
     * @param id
     * @param redisson
     * @return the get map cache
     */
    protected RMapCache<Object, Object> getMapCache(String id, RedissonClient redisson) {
        return redisson.getMapCache(id);
    }

    private void check() {
        if (mapCache == null) {
            throw new IllegalStateException("Redisson config is not defined");
        }
    }

}

