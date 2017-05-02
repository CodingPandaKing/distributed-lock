package me.coding.panda.learning.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * 分布式锁实现类
 *
 * @author coding.panda
 * @create 2017-05-02 15:20
 **/
public class DistributedLock implements Lock, Watcher {


    private ZooKeeper zooKeeper;
    // 分布式锁根目录
    private String root = "/locks";
    // 竞争资源的标志
    private String lockName;
    // 等待前一个锁
    private String waitNode;
    // 当前锁
    private String tempNode;
    // 计数器
    private CountDownLatch latch;
    // 超时时间
    private int sessionTimeout = 30000;
    private List<Exception> exceptionList = new ArrayList<>();



    /**
     * 初始化zk链接
     * 
     * @param config
     * @param lockName
     */
    public DistributedLock(String config, String lockName) {
        this.lockName = lockName;
        // 创建一个与服务器的连接
        try {
            zooKeeper = new ZooKeeper(config, sessionTimeout, this);
            Stat stat = zooKeeper.exists(root, false);
            if (stat == null) {
                // 创建根节点
                zooKeeper.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            exceptionList.add(e);
        }
    }


    @Override
    public void lock() {

        if (exceptionList.size() > 0) {
            throw new LockException(exceptionList.get(0));
        }

        try {
            if (this.tryLock()) {
                System.out.println(
                        "Thread " + Thread.currentThread().getId() + " " + tempNode + " 获得锁");
                return;
            } else {
                // 等待锁
                waitForLock(waitNode, sessionTimeout);
            }
        } catch (Exception e) {
            throw new LockException(e);
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        try {
            String splitStr = "_lock_";

            // 创建临时子节点
            tempNode = zooKeeper.create(root + "/" + lockName + splitStr, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            System.out.println(tempNode + " 创建完成");

            // 取出所有子节点
            List<String> subNodes = zooKeeper.getChildren(root, false);

            // 取出所有lockName的锁
            List<String> lockObjNodes = new ArrayList<String>();
            for (String node : subNodes) {
                String _node = node.split(splitStr)[0];
                if (_node.equals(lockName)) {
                    lockObjNodes.add(node);
                }
            }
            Collections.sort(lockObjNodes);

            System.out.println(tempNode + "==" + lockObjNodes.get(0));

            if (tempNode.equals(root + "/" + lockObjNodes.get(0))) {
                // 如果是最小的节点,则表示取得锁
                return true;
            }
            // 如果不是最小的节点，找到比自己小1的节点
            String subMyZnode = tempNode.substring(tempNode.lastIndexOf("/") + 1);
            waitNode = lockObjNodes.get(Collections.binarySearch(lockObjNodes, subMyZnode) - 1);
        } catch (Exception e) {
            throw new LockException(e);
        }
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            if (this.tryLock()) {
                return true;
            }
            return waitForLock(waitNode, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void unlock() {
        try {
            System.out.println("unlock " + tempNode);
            zooKeeper.delete(tempNode,-1);
            tempNode = null;
            zooKeeper.close();
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    /**
     * zk 节点监视器
     * 
     * @param watchedEvent
     */
    @Override
    public void process(WatchedEvent watchedEvent) {
        if (this.latch != null) {
            this.latch.countDown();
        }
    }

    private boolean waitForLock(String lower, long waitTime) throws Exception {
        Stat stat = zooKeeper.exists(root + "/" + lower, true);
        // 判断比自己小一个数的节点是否存在
        // 如果不存在则无需等待锁,同时注册监听
        if (stat != null) {
            System.out.println(
                    "Thread " + Thread.currentThread().getId() + " 等待 " + root + "/" + lower);
            this.latch = new CountDownLatch(1);
            this.latch.await(waitTime, TimeUnit.MILLISECONDS);
            this.latch = null;
        }
        return true;
    }

}
