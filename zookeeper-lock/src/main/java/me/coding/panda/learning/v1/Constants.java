package me.coding.panda.learning.v1;

import java.util.concurrent.CountDownLatch;

/**
 * @author coding.panda
 * @create 2017-05-02 20:15
 **/
public class Constants {

    public static final int THREAD_COUNT =  10 ;
    public static final int SESSION_TIMEOUT = 10000;
    public static final String ZOOKEEPER_URL = "127.0.0.1:2181";


    public static final String ROOT_PATH = "/disLocks";
    public static final String SUB_PATH = "/disLocks/sub";


    // 模拟的线程计数器
    public static final CountDownLatch THREAD_SEMAPHORE = new CountDownLatch(THREAD_COUNT);

}
