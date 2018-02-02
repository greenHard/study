package com.zhang.study.chapter.part3.eleven.test;

import com.zhang.study.chapter.part3.eleven.pojo.Shop;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * 异步编程
 */
public class CompletableFutureTest {

    public static void main(String[] args) {
        // executeFuture();
        Shop shop = new Shop("BestShop");
        long start = System.nanoTime();
        Future<Double> futurePrice = shop.getPriceAsync("my favorite product");
        // Future<Double> futurePrice = shop.getPriceAsync(null);
        long invocationTime = ((System.nanoTime()-start) / 1_000_000);
        System.out.println("Invocation returned after " + invocationTime
                + " msecs");
        // 执行更多任务，比如查询其他商店
        doSomethingElse();
        // 在计算商品价格的同时
        try {
            // 从流中获取价格,获取不到会阻塞
            double price = futurePrice.get();
            // 将价格进行合并 TODO
            System.out.printf("Price is %.2f%n", price);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long retrievalTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Price returned after " + retrievalTime + " msecs");

        // 商家列表
        List<Shop> shops = Arrays.asList(new Shop("BestPrice"),
                new Shop("LetsSaveBig"),
                new Shop("MyFavoriteShop"),
                new Shop("BuyItAll"));

        start = System.nanoTime();
        // 花了4秒钟左右  普通Stream
        // 优化1,使用并行操作     1017 msecs
        // 异步执行  普通Stream  2010 msecs
        // 异步执行  并行Stream  2008 msecs
        // 优化,加上线程池
        System.out.println(Shop.findPrices("myPhone27S",shops));
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Done in " + duration + " msecs");
    }

    /**
     * Future编程
     */
    public static void  executeFuture(){
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Double> future = executor.submit(() -> {
            return doSomeLongComputation();
        });
        doSomethingElse();
        try{
            // 获得异步操作的结果,如果被阻塞,最多等1秒之后退出
            Double result = future.get(1, TimeUnit.SECONDS);
        }catch (ExecutionException ee) {
            // 计算抛出一个异常
        } catch (InterruptedException ie) {
            // 当前线程在等待过程中被中断
        } catch (TimeoutException te) {
            // 在Future对象完成之前超过已过期
        }
    }

    private static double doSomeLongComputation() throws InterruptedException {
        Thread.sleep(100000);
        double start = System.currentTimeMillis();
        System.out.println("这个任务花费时间比较久");
        return System.currentTimeMillis()-start;
    }

    private static void doSomethingElse() {
        System.out.println("这个任务花费时间比较短");
    }
}

