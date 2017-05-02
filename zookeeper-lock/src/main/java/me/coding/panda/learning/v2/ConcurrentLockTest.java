package me.coding.panda.learning.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并发测试分布式锁
 *
 * @author coding.panda
 * @create 2017-05-02 17:47
 **/
public class ConcurrentLockTest {

    //开始阀门
    private CountDownLatch startSignal = new CountDownLatch(1);
    //结束阀门
    private CountDownLatch doneSignal = null;
    private CopyOnWriteArrayList<Long> list = new CopyOnWriteArrayList<Long>();
    //原子递增
    private AtomicInteger err = new AtomicInteger();
    private ConcurrentTask[] task = null;

    public ConcurrentLockTest(ConcurrentTask ... task){
        this.task = task;
        if(task == null){
            System.err.println("task can not null");
            System.exit(1);
        }
        doneSignal = new CountDownLatch(task.length);
        start();
    }


    private void start(){
        //创建线程，并将所有线程等待在阀门处
        createThread();
        //打开阀门
        //递减锁存器的计数，如果计数到达零，则释放所有等待的线程
        startSignal.countDown();
        try {
            //等待所有线程都执行完毕
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //计算执行时间
        getExeTime();
    }

    /**
     * 初始化所有线程，并在阀门处等待
     */
    private void createThread() {
        long len = doneSignal.getCount();
        for (int i = 0; i < len; i++) {
            final int j = i;
            new Thread(new Runnable(){
                public void run() {
                    try {
                        //使当前线程在锁存器倒计数至零之前一直等待
                        startSignal.await();
                        long start = System.currentTimeMillis();
                        task[j].run();
                        long end = (System.currentTimeMillis() - start);
                        list.add(end);
                    } catch (Exception e) {
                        err.getAndIncrement();//相当于err++
                    }
                    doneSignal.countDown();
                }
            }).start();
        }
    }

    /**
     * 计算平均响应时间
     */
    private void getExeTime() {
        int size = list.size();
        List<Long> _list = new ArrayList<>(size);
        _list.addAll(list);
        Collections.sort(_list);
        long min = _list.get(0);
        long max = _list.get(size-1);
        long sum = 0L;
        for (Long t : _list) {
            sum += t;
        }
        long avg = sum/size;
        System.out.println("min: " + min);
        System.out.println("max: " + max);
        System.out.println("avg: " + avg);
        System.out.println("err: " + err.get());
    }

}
