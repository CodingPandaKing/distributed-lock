package me.coding.panda.learning.v1;

/**
 * @author coding.panda
 * @create 2017-05-02 19:55
 **/
public class DistributedLockTest {

    public static void main(String[] args) {

        for (int i = 0; i < Constants.THREAD_COUNT; i++) {

            final int threadId = i + 1;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DistributedLock dc = new DistributedLock();
                        dc.createConnection(Constants.ZOOKEEPER_URL,Constants.SESSION_TIMEOUT);
                        dc.createNode(Constants.ROOT_PATH, "Root", true);
                        dc.getLock();
                    } catch (Exception e) {
                        System.err.println("【第" + threadId + "个线程】 抛出的异常：" + e);
                    }
                }
            }, String.valueOf(threadId)).start();

        }


        try {
            Constants.THREAD_SEMAPHORE.await();
            System.out.println("所有线程运行结束!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}
