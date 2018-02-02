package com.zhang.study.chapter.part2.six.test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.zhang.study.chapter.part2.six.test.SteamCollectDataTest.isPrime;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public class PrimeNumbersCollector implements Collector<Integer,Map<Boolean,List<Integer>>,Map<Boolean,List<Integer>>> {
    @Override
    public Supplier<Map<Boolean, List<Integer>>> supplier() {
        // 利用代码块
        return ()->new HashMap<Boolean,List<Integer>>(){{
            put(false,new ArrayList<Integer>());
            put(true,new ArrayList<Integer>());
        }};
    }

    @Override
    public BiConsumer<Map<Boolean, List<Integer>>, Integer> accumulator() {
        // 根据isPrime的结果，获取质数或非质数列表
        return (Map<Boolean,List<Integer>> acc,Integer candidate)->{
            acc.get(isPrime(acc.get(true),candidate)).add(candidate);
        };
    }

    // 把两个部分累加器合并起来，
    @Override
    public BinaryOperator<Map<Boolean, List<Integer>>> combiner() {
        return (Map<Boolean,List<Integer>> map1,Map<Boolean,List<Integer>> map2)->{
            map1.get(false).addAll(map2.get(false));
            map1.get(true).addAll(map2.get(true));
            return map1;
        };
    }

    @Override
    public Function<Map<Boolean, List<Integer>>, Map<Boolean, List<Integer>>> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH));

    }
}
