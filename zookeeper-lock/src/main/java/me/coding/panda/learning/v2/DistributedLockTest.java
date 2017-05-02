package me.coding.panda.learning.v2;

/**
 * 分布式锁测试类
 *
 * @author coding.panda
 * @create 2017-05-02 17:46
 *
 *
 **/
public class DistributedLockTest {

    public static void main(String[] args) {

        // 初始化DistributedLock
        new Thread(new Runnable() {
            public void run() {
                DistributedLock lock = null;
                try {
                    lock = new DistributedLock("121.42.63.39:2181", "test1");
                    lock.lock();
                    Thread.sleep(3000);
                    System.out.println("Thread " + Thread.currentThread().getId() + " Running");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (lock != null)
                        lock.unlock();
                }

            }
        }).start();


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        ConcurrentTask[] tasks = new ConcurrentTask[60];

        for (int i = 0; i < tasks.length; i++) {

            ConcurrentTask task3 = new ConcurrentTask() {
                public void run() {
                    DistributedLock lock = null;
                    try {
                        lock = new DistributedLock("121.42.63.39:2181", "test2");
                        lock.lock();
                        System.out.println("Thread " + Thread.currentThread().getId() + " Running");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }

                }
            };
            tasks[i] = task3;
        }

        new ConcurrentLockTest(tasks);
    }

}
