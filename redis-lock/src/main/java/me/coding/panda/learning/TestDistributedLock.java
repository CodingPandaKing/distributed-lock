package me.coding.panda.learning;

/**
 * 测试分布式锁
 *
 * @author shugang@58ganji.com
 * @create 2017-04-24 19:47
 *
 **/

class ThreadA extends Thread {

    private MSService service;

    public ThreadA(MSService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.seckill();
    }
}



public class TestDistributedLock {


    public static void main(String[] args) {

        MSService service = new MSService();

        for (int i = 0; i < 50; i++) {
            ThreadA threadA = new ThreadA(service);
            threadA.start();
        }
    }

}
