package me.coding.panda.learning.v2;

/**
 * 锁异常类
 *
 * @author coding.panda
 * @create 2017-05-02 15:34
 **/
public class LockException extends RuntimeException {

    public LockException(String e){
        super(e);
    }

    public LockException(Exception e){
        super(e);
    }

}
