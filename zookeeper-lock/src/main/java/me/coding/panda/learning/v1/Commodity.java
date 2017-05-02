package me.coding.panda.learning.v1;

/**
 * @author coding.panda
 * @create 2017-05-02 22:34
 **/
public class Commodity {

    static int count  = 50 ;

    public static void buy(){
        count -- ;
        System.out.println("当前商品剩余 ：" + count);
    }

}
