package com.zhang.study.chapter.part2.six.test;

import com.zhang.study.chapter.part2.four.pojo.Dish;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
/**
 * 用流收集数据测试
 */
public class SteamCollectDataTest {
    public static void main(String[] args) {
        List<Dish> menu = Arrays.asList(
                new Dish("pork", false, 800, Dish.Type.MEAT),
                new Dish("beef", false, 700, Dish.Type.MEAT),
                new Dish("chicken", false, 400, Dish.Type.MEAT),
                new Dish("french fries", true, 530, Dish.Type.OTHER),
                new Dish("rice", true, 350, Dish.Type.OTHER),
                new Dish("season fruit", true, 120, Dish.Type.OTHER),
                new Dish("pizza", true, 550, Dish.Type.OTHER),
                new Dish("prawns", false, 300, Dish.Type.FISH),
                new Dish("salmon", false, 450, Dish.Type.FISH));

        // 1. 归约和汇总
        menu.stream().collect(counting());
        // 最大值,最小值
        menu.stream().collect(maxBy(comparing(Dish::getCalories)));
        menu.stream().collect(minBy(comparing(Dish::getCalories)));
        // 汇总
        menu.stream().collect(summingInt(Dish::getCalories));
        // 平均数
        menu.stream().collect(averagingInt(Dish::getCalories));
        // 通过一次summarizing操作你可以就数出菜单中元素的个数，并得到菜肴热量总和、平均值、最大值和最小值
        IntSummaryStatistics summaryStatistics = menu.stream().collect(summarizingInt(Dish::getCalories));
        System.out.println(summaryStatistics);
        // 连接字符串
        String names = menu.stream().map(Dish::getName).collect(joining(", "));
        System.out.println(names);

        // 2. 广义的归约和汇总
        Integer totalCalories = menu.stream().collect(reducing(0, Dish::getCalories, (i, j) -> i + j));
        Optional<Dish> mostCalories = menu.stream().collect(reducing((d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2));

        // counting收集器也是类似地利用三参数reducing工厂方法实现的
        // public static <T> Collector<T, ?, Long> counting() {
        //     return reducing(0L, e -> 1L, Long::sum);
        // }
        // reducing看作本章中讨论的所有其他收集器的概括。然而就
        // 实际应用而言，不管是从可读性还是性能方面考虑，我们始终建议使用joining等收集器

        // 3.分组
        // 菜单中的菜按照类型进行分类
        Map<Dish.Type, List<Dish>> dishesByType = menu.stream().collect(groupingBy(Dish::getType));
        System.out.println(dishesByType);
        // 按卡路里进行分类
        Map<Dish.CaloricLevel, List<Dish>> dishesByCaloric = menu.stream().collect(groupingBy(d -> {
            if (d.getCalories() <= 400) {
                return Dish.CaloricLevel.DIET;
            } else if (d.getCalories() <= 700) {
                return Dish.CaloricLevel.NORMAL;
            } else {
                return Dish.CaloricLevel.FAT;
            }
        }));
        System.out.println(dishesByCaloric);
        // 多级分组, 使用groupingBy的第二个参数 多级分组操作可以扩展至任意层级
        Map<Dish.Type, Map<Dish.CaloricLevel, List<Dish>>> dishesByTypeAndCaloric = menu.stream().collect(groupingBy(Dish::getType, groupingBy(d -> {
            if (d.getCalories() <= 400) {
                return Dish.CaloricLevel.DIET;
            } else if (d.getCalories() <= 700) {
                return Dish.CaloricLevel.NORMAL;
            } else {
                return Dish.CaloricLevel.FAT;
            }
        })));
        System.out.println(dishesByTypeAndCaloric);
        // 第二个收集器可以是任何类型
        Map<Dish.Type, Long> countDishesByType = menu.stream().collect(groupingBy(Dish::getType, counting()));
        System.out.println(countDishesByType);
        // 将收集器转化为另一种类型
        Map<Dish.Type, Dish> dishMap = menu.stream().collect(groupingBy(Dish::getType, collectingAndThen(maxBy(comparing(Dish::getCalories)), Optional::get)));
        System.out.println(dishMap);
        // 与mapping的使用
        // 对于每种类型的Dish,菜单中都有哪些CaloricLevel
        Map<Dish.Type, Set<Dish.CaloricLevel>> caloricLevelsByType = menu.stream().collect(groupingBy(Dish::getType, mapping(d -> {
            if (d.getCalories() <= 400) {
                return Dish.CaloricLevel.DIET;
            } else if (d.getCalories() <= 700) {
                return Dish.CaloricLevel.NORMAL;
            } else {
                return Dish.CaloricLevel.FAT;
            }
        }, toSet())));
        System.out.println(caloricLevelsByType);

        // 4.分区
        // 分区是分组的特殊情况：由一个谓词（返回一个布尔值的函数）作为分类函数
        Map<Boolean, List<Dish>> groupByVegetarian = menu.stream().collect(partitioningBy(Dish::isVegetarian));
        System.out.println(groupByVegetarian);
        // 多级分区参照多级分组

        // 5.收集器接口
        List<Dish> collect = menu.stream().collect(toList());
        List<Dish> collect1 = menu.stream().collect(new ToListCollector<Dish>());
        System.out.println(collect);
        System.out.println(collect1);

        // 开发自己的自定义收集器  质数问题。
        // 505 msecs
        long fastest = Long.MAX_VALUE;
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            //  362 msecs 优化了性能
            //  IntStream.rangeClosed(2,1_000_000).boxed().collect(new PrimeNumbersCollector());
            //  512 msecs
            IntStream.rangeClosed(2,1_000_000).boxed().collect(partitioningBy(n-> isPrime(n)));
            long duration = (System.nanoTime() - start) / 1_000_000;
            if (duration < fastest) fastest = duration;
        }
        System.out.println(
                "Fastest execution done in " + fastest + " msecs");

    }

    /**
     * 判断是否为质数方法
     */
    public static boolean isPrime(int candidate){
        // 取数据的平方根
        int candidateRoot = (int)Math.sqrt((double) candidate);
        // 从2到平方根分别看是否被candidate整除,一旦等于0,代表不是质数
        return IntStream.rangeClosed(2,candidateRoot).noneMatch(i->candidate%i==0);
    }

    /**
     * 将2到n的质数与非质数进行分区
     */
    public static Map<Boolean,List<Integer>> partitionPrimes(int n){
        return IntStream.rangeClosed(2,n)
                        .boxed()
                        .collect(partitioningBy(candidate->isPrime(candidate)));
    }

    /**
     * 给定一个排序列表和一个谓词，它会返回元素满足谓词的最长前缀
     */
    public static <A> List<A> takeWhile(List<A> list, Predicate<A> p){
        int i=0;
        for (A item : list) {
            if(!p.test(item)){
                return list.subList(0,i);
            }
            i++;
        }
        return list;
    }

    /**
     * 利用这个方法，你就可以优化isPrime方法，只用不大于被测数平方根的质数去测试了：
     * primes 所有质数
     */
    public static boolean isPrime(List<Integer> primes,int candidate){
        // 取数据的平方根
        int candidateRoot = (int)Math.sqrt((double) candidate);
        // 从2到平方根分别看是否被candidate整除,一旦等于0,代表不是质数
        return takeWhile(primes,i->i<=candidateRoot).stream().noneMatch(p->candidate % p == 0);
    }
}
