package com.zhang.study.chapter.part2.six.test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.*;

/**
 * 自定义收集器
 * @param <T>
 */
public class ToListCollector<T> implements Collector<T,List<T>,List<T>> {

    // 在调用时它会创建一个空的累加器实例，供数据收集过程使用
    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    // 将元素添加到结果容器
    // accumulator方法会返回执行归约操作的函数
    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return List::add;
    }

    // 对结果容器应用最终转换
    // finisher方法必须返回在累积过程的最后要调用的一个函数，以便将累加器对象转换为整个集合操作的最终结果
    @Override
    public Function<List<T>, List<T>> finisher() {
        return Function.identity();
    }

    // 合并两个结果容器
    // combiner方法会返回一个供归约操作使用的函数
    @Override
    public BinaryOperator<List<T>> combiner() {
        return (list1,list2)->{
            list1.addAll(list2);
            return list1;
        };
    }

    // characteristics会返回一个不可变的Characteristics集合，它定义了收集器的行为
    // UNORDERED 归约结果不受流中项目的遍历和累积顺序的影响。
    // CONCURRENT accumulator函数可以从多个线程同时调用，且该收集器可以并行归约流。如果收集器没有标为UNORDERED，那它仅在用于无序数据源时才可以并行归约
    // IDENTITY_FINISH 这表明完成器方法返回的函数是一个恒等函数，可以跳过。这种情况下，累加器对象将会直接用作归约过程的最终结果。这也意味着，将累加器A不加检查地转换为结果R是安全的
    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH,CONCURRENT));
    }
}
