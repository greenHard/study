package com.zhang.study.chapter.part3.eleven.pojo;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static java.util.stream.Collectors.toList;

/**
 * 商店
 */
public class Shop{

    private final static Executor executor =
            Executors.newFixedThreadPool(Math.min(4, 100),
                    new ThreadFactory() {
                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r);
                            t.setDaemon(true);
                            return t;
                        }
                    });

    private String name;

    public Shop(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice(String product) {
        return calculatePrice(product);
    }

    public static List<String> findPrices(String product, List<Shop> shops){
        // return shops.stream().map(shop -> String.format("%s price is %.2f",shop.getName(),shop.getPrice(product))).collect(toList());
        // 使用并行操作
        // return shops.parallelStream().map(shop -> String.format("%s price is %.2f",shop.getName(),shop.getPrice(product))).collect(toList());
        // 使用异步方式计算每种商品的价格
        // 优化加上线程池
        List<CompletableFuture<String>> priceFutures = shops.parallelStream()
                .map(shop -> CompletableFuture.supplyAsync(
                        () -> String.format("%s price is %.2f", shop.getName(), shop.getPrice(product)),executor)).collect(toList());
        // 等待所有异步操作结束
        return priceFutures.parallelStream().map(CompletableFuture::join).collect(toList());
    }
    /**
     * CompletableFuture 异步方法
     */
    public Future<Double> getPriceAsync(String product){
        // CompletableFuture<Double> futurePrice = new CompletableFuture<>();
        /*new Thread(()->{
            try{
                double price = calculatePrice(product);
                // 需长时间计算得到结果,设置Future返回值
                futurePrice.complete(price);
            }catch (Exception ex){
                // 否则抛出异常完成这次操作
                futurePrice.completeExceptionally(ex);
            }

        }).start();*/
        // 无需等待直接返回Future对象
        // return futurePrice;
        // 使用supplyAsync()方法创建CompletableFuture,提供了同样的错误管理机制
        return CompletableFuture.supplyAsync(()->calculatePrice(product));
    }

    private double calculatePrice(String product) {
        delay();
        Random random = new Random();
        return random.nextDouble() * product.charAt(0) + product.charAt(1);
    }

    /**
     * 用来进行延迟操作
     * 延迟1s
     */
    public static void delay() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
