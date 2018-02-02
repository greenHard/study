package com.zhang.study.part2.seven.test;

import java.util.concurrent.RecursiveTask;

/**
 * 自定义用分支/合并框架
 */
public class ForkJoinSumCalculator extends RecursiveTask<Long> {
    // 要求和的数组
    private final long[] numbers;
    // 子任务处理的数组的起始和终止位置
    private final int start;
    private final int end;
    // 不再将任务分解为子任务的数组大小
    public static final long THRESHOLD = 10_000;

    public ForkJoinSumCalculator(long[] numbers) {
        this(numbers, 0, numbers.length);
    }

    public ForkJoinSumCalculator(long[] numbers, int start, int end) {
        this.start = start;
        this.end = end;
        this.numbers = numbers;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length < THRESHOLD) {
            return computeSequentially();
        }
        ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length / 2);
        // 利用另一个ForkJoinPool线程异步执行新创建的子任务
        leftTask.fork();
        ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length / 2, end);
        // 同步执行第二个子任务，有可能允许进一步递归划分
        Long rightResult = rightTask.compute();
        Long leftResult = leftTask.join();
        return leftResult + rightResult;
    }

    private long computeSequentially() {
        long sum = 0;
        for (int i = start; i < end; i++) {
             sum += numbers[i];
        }
        return sum;
    }
}
