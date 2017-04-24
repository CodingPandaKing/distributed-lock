package me.coding.panda.learning;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 秒杀服务
 *
 * @author shugang@58ganji.com
 * @create 2017-04-24 19:38
 **/
public class MSService {

    private static JedisPool pool = null;

    static {

        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大连接数
        config.setMaxTotal(200);
        // 设置最大空闲数
        config.setMaxIdle(8);
        // 设置最大等待时间
        config.setMaxWaitMillis(1000 * 100);
        // 在borrow一个jedis实例时，是否需要验证，
        // 若为true，则所有jedis实例均是可用的
        config.setTestOnBorrow(true);

        pool = new JedisPool(config, "127.0.0.1", 6379, 3000);
    }

    DistributedLock distributedLock = new DistributedLock(pool);

    int n = 500;

    public void seckill() {
        // 返回锁的value值，供释放锁时候进行判断
        String indentifier = distributedLock.lockWithTimeout("resource", 5000, 1000);
        System.out.println(Thread.currentThread().getName() + "获得了锁");
        System.out.println(--n);
        distributedLock.releaseLock("resource", indentifier);
    }


}
