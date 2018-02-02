package com.zhang.study.chapter.part2.seven.test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * 并行处理
 */
public class ParallelTest {
    public static void main(String[] args) {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        System.out.println("cpu核数:"+cpuCount);

        //1. 测试性能
        long fastest = Long.MAX_VALUE;
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            long sum = forkJoinSum(10000);
            long duration = (System.nanoTime() - start) / 1_000_000;
            // 多个线程在同时访问累加器
            // forEach中调用的方法有副作用，它会改变多个线程共享的对象的可变状态
            // 结果:37380350
            // 结果:28146740
            // 结果:28381582
            // 结果:43353951
            // 结果:49647598
            // 结果:30368651
            // 结果:35591910
            // 结果:44449062
            // 结果:46878924
            // 结果:50005000
            // System.out.println("结果:"+sum);
            System.out.println("耗时:"+duration);
        }

        // 使用并行流
        // 1. 适当的基准来检查其性能。
        // 2. 留意装箱 自动装箱和拆箱操作会大大降低性能 IntStream、LongStream、DoubleStream来避免这种操作
        // 3. 有些操作本身在并行流上的性能就比顺序流差 limit和findFirst依赖于元素的顺序
        // 4. 还要考虑流的操作流水线的总计算成本。设N是要处理的元素的总数，Q是一个元素通过流水线的大致处理成本，
        // 则N*Q就是这个对成本的一个粗略的定性估计。Q值较高就意味着使用并行流时性能好的可能性比较大。
        // 5. 对于较小的数据量，选择并行流几乎从来都不是一个好的决定
        // 6. 要考虑流背后的数据结构是否易于分解
        // 7. 流自身的特点，以及流水线中的中间操作修改流的方式，都可能会改变分解过程的性能
        // 8. 还要考虑终端操作中合并步骤的代价是大是小（例如Collector中的combiner方法）。
        //  如果这一步代价很大，那么组合每个子流产生的部分结果所付出的代价就可能会超出通过并行流得到的性能提升
        // ArrayList 极佳
        // LinkedList 差
        // IntStream.range 极佳
        // Stream.iterate 差
        // HashSet 好
        // TreeSet 好

        // 2. 分支合并框架
        // 实现了自己的分支合并框架
        // 2.1 使用分支合并框架
        // (1) 对一个任务调用join方法会阻塞调用方，直到该任务做出结果.。因此，有必要在两个子任务的计算都开始之后再调用它
        // (2) 不应该在RecursiveTask内部使用ForkJoinPool的invoke方法。相反，你应该始终直接调用compute或fork方法，只有顺序代码才应该用invoke来启动并行计算。
        // (3) 对子任务调用fork方法可以把它排进ForkJoinPool。
        // (4) 调试使用分支/合并框架的并行计算可能有点棘手
        // (5) 和并行流一样，你不应理所当然地认为在多核处理器上使用分支/合并框架就比顺序计算快
        // (6) 你必须选择一个标准，来决定任务是要进一步拆分还是已小到可以顺序求值

        // 分支/合并框架工程用一种称为工作窃取（work stealing）的技术来解决这个问题
        // 空闲的线程会从队列的尾巴上“偷走”一个任务

        // 3.Spliterator
        // 实现自己的Spliterator
        final String SENTENCE =
                " Nel mezzo del cammin di nostra vita " +
                        "mi ritrovai in una selva oscura" +
                        " ché la dritta via era smarrita ";
        System.out.println("Found " + countWordsIteratively(SENTENCE) + " words");
        // 用函数式风格重写单词计数器
        Stream<Character> characterStream = IntStream.range(0, SENTENCE.length()).mapToObj(SENTENCE::charAt);


    }

    /**
     * 普通迭代的方式  耗时:4
     */
    public static long sum(long n){
        long sum= 0L;
        for(int i=0;i<n;i++){
            sum+=i;
        }
        return sum;
    }
    /**
     * 单行流
     * 耗时:123
     * 耗时:6
     */
    public static long sequentialSum(long n){
        // return Stream.iterate(1L,i->i+1).limit(n).reduce(0L,Long::sum);
        return  LongStream.rangeClosed(1L,n).reduce(0L, Long::sum);
    }

    /**
     * 并行流
     * 耗时:378    这里由于使用iterate导致有一个拆装箱的过程导致速度慢了很多
     * 耗时:1
     * parallel() 将流转化为并行流
     * 默认使用ForkJoinPool 它默认的线程数量就是你的处理器数量  Runtime.getRuntime().availableProcessors();
     * System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism","12") 设置线程池大小 一般不建议修改
     * 最后一次parallel或sequential调用会影响整个流水线
     */
    public static long parallelSum(long n){
        //return Stream.iterate(1L,i->i+1).limit(n).parallel().reduce(0L,Long::sum);
        return  LongStream.rangeClosed(1L,n).parallel().reduce(0L, Long::sum);
    }

    /**
     * 自定义的分支合并框架使用
     * 耗时:0
     */
    public static long forkJoinSum(long n){
        long[] numbers = LongStream.rangeClosed(1,n).toArray();
        ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);
        return new ForkJoinPool().invoke(task);
    }
    /**
     * 累加数据
     */
    public static long sideEffectSum(long n) {
        Accumulator accumulator = new Accumulator();
        LongStream.rangeClosed(1, n).parallel().forEach(accumulator::add);
        return accumulator.total;
    }

    static class Accumulator {
        public long total = 0;
        public void add(long value) { total += value; }
    }

    /**
     * 迭代统计字数
     */
    public static int countWordsIteratively(String s){
        int counter = 0;
        boolean lastSpace = true;
        for (char c : s.toCharArray()){
            if(Character.isWhitespace(c)){
                lastSpace = true;
            } else {
                if(lastSpace){
                    counter++;
                }
                lastSpace = false;
            }
        }
        return counter;
    }
}
