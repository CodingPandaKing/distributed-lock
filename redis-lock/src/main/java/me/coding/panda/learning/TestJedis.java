package me.coding.panda.learning;

import redis.clients.jedis.Jedis;

/**
 * 测试操作redis
 *
 * @author shugang@58ganji.com
 * @create 2017-04-24 19:59
 **/
public class TestJedis {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.set("testKey","testValue");
        System.out.println(jedis.get("testKey"));
    }


}
