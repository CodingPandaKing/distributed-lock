package me.coding.panda.learning.v1;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * @author coding.panda
 * @create 2017-05-02 18:24
 **/
public class DistributedLock implements Watcher {

    private ZooKeeper zooKeeper = null;
    private String currentThreadNode;
    private String waitNode;
    private String THREAD_LOG_PREFIX;


    // zk连接计数器
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);

    public DistributedLock() {
        THREAD_LOG_PREFIX = "【第" + Thread.currentThread().getName() + "线程】";
    }

    /**
     *
     * 获取锁
     * 
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void getLock() throws KeeperException, InterruptedException {
        // 为当前线程创建节点
        // 创建模式为临时自增
        currentThreadNode = zooKeeper.create(Constants.SUB_PATH, new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        //System.out.println(THREAD_LOG_PREFIX + "创建锁路径:" + currentThreadNode);

        if (isMinNode()) {
            getLockSuccess();
        }
    }


    /**
     * 创建节点
     * 
     * @param path 节点path
     * @param data 初始数据内容
     * @return
     */
    public boolean createNode(String path, String data, boolean needWatch)
            throws KeeperException, InterruptedException {

        if (zooKeeper.exists(path, needWatch) == null) {
            this.zooKeeper.create(path, data.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        return true;
    }

    /**
     * 创建ZK连接
     * 
     * @param connectString ZK服务器地址列表
     * @param sessionTimeout Session超时时间
     */
    public void createConnection(String connectString, int sessionTimeout)
            throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(connectString, sessionTimeout, this);
        connectedSemaphore.await();
    }

    /**
     * 获取锁成功
     */
    public void getLockSuccess() throws KeeperException, InterruptedException {

        if (zooKeeper.exists(this.currentThreadNode, false) == null) {
            System.err.println(THREAD_LOG_PREFIX + "创建的节点不存在了");
            return;
        }

        System.out.println(THREAD_LOG_PREFIX+"获得锁");

        // 获取锁成功，执行下单逻辑.
        executeWork();

        zooKeeper.delete(this.currentThreadNode, -1);

        releaseConnection();

        Constants.THREAD_SEMAPHORE.countDown();
    }

    private boolean executeWork() {

        try {
            System.out.println(THREAD_LOG_PREFIX + "购买了商品");
            Thread.sleep(200);
            Commodity.buy();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 关闭ZK连接
     */
    public void releaseConnection() {

        if (this.zooKeeper != null) {
            try {
                this.zooKeeper.close();
                System.out.println(THREAD_LOG_PREFIX+"释放锁");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查自己是不是最小的节点
     * 
     * @return
     */
    public boolean isMinNode() throws KeeperException, InterruptedException {

        List<String> subNodes = zooKeeper.getChildren(Constants.ROOT_PATH, false);
        Collections.sort(subNodes);

        int index = subNodes.indexOf(currentThreadNode.substring(Constants.ROOT_PATH.length() + 1));

        switch (index) {
            case -1: {
                // 不存在
                return false;
            }
            case 0: {
                // 最小ID的Node
                return true;
            }
            default: {
                // 获取比自己次小的节点
                this.waitNode = Constants.ROOT_PATH + "/" + subNodes.get(index - 1);
                try {
                    zooKeeper.getData(waitNode, true, new Stat());
                    return false;
                } catch (KeeperException e) {
                    if (zooKeeper.exists(waitNode, false) == null) {
                        // 比我次小的节点不在 -> 再次检查
                        return isMinNode();
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    @Override
    public void process(WatchedEvent event) {
        Event.KeeperState keeperState = event.getState();
        Event.EventType eventType = event.getType();

        if (Event.KeeperState.SyncConnected == keeperState) {

            if (Event.EventType.None == eventType) {
                //System.out.println(THREAD_LOG_PREFIX + "成功连接ZK服务器");
                connectedSemaphore.countDown();
            } else if (event.getType() == Event.EventType.NodeDeleted
                    && event.getPath().equals(waitNode)) {
                // 比我次小的节点已经被删除
                try {
                    if (isMinNode()) {
                        getLockSuccess();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (Event.KeeperState.Disconnected == keeperState) {
            System.out.println(THREAD_LOG_PREFIX + "与ZK服务器断开连接");
        } else if (Event.KeeperState.AuthFailed == keeperState) {
            System.out.println(THREAD_LOG_PREFIX + "权限检查失败");
        } else if (Event.KeeperState.Expired == keeperState) {
            System.out.println(THREAD_LOG_PREFIX + "会话失效");
        }
    }
}
