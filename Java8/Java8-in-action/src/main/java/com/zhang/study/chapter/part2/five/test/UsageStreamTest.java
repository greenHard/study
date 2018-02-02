package com.zhang.study.chapter.part2.five.test;

import com.zhang.study.chapter.part2.four.pojo.Dish;
import com.zhang.study.chapter.part2.four.pojo.Trader;
import com.zhang.study.chapter.part2.four.pojo.Transaction;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * 流的使用介绍
 */
public class UsageStreamTest {
    public static void main(String[] args) throws IOException {
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

        // 1. 筛选和切片
        // 1.1 筛选出所有素菜
        List<Dish> vegetarianCollect = menu.stream().filter(Dish::isVegetarian).collect(toList());
        System.out.println(vegetarianCollect);

        // 1.2 筛选各异的元素
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 2, 7, 8, 10);
        numbers.stream().filter(n -> n % 2 == 0).distinct().forEach(System.out::println);

        // 1.3 截短流
        List<Dish> dishes = menu.stream().filter(d -> d.getCalories() > 300).limit(3).collect(toList());
        System.out.println(dishes);

        // 1.4 跳过元素
        List<Dish> dishList = menu.stream().filter(d -> d.getCalories() > 300).skip(2).collect(toList());
        System.out.println(dishList);

        // 2. 映射
        // 2.1 对流的每一个元素都映射
        List<Integer> dishNameLengths = menu.stream().filter(d -> d.getCalories() > 300).map(Dish::getName).map(String::length).collect(toList());
        System.out.println(dishNameLengths);

        // 2.2 流的扁平化
        // 使用map和Arrays.stream()
        // flatmap方法让你把一个流中的每个值都换成另一个流，然后把所有的流连接起来成为一个流
        List<String> words = Arrays.asList("Goodbye", "World");
        List<String> stringList = words.stream()
                .map(s -> s.split(""))
                // Arrays::stream 让数组变成一个单独的流
                // flatMap 将各个生成流扁平化为单个流
                .flatMap(Arrays::stream)
                .distinct()
                .collect(toList());
        System.out.println(stringList);

        // 给定两个数字列表，如何返回所有的数对呢?
        // 只返回总和能被3整除的数对呢？
        List<Integer> numbers1 = Arrays.asList(1, 2, 3);
        List<Integer> numbers2 = Arrays.asList(3, 4);
        List<int[]> pairs = numbers1.stream()
                .flatMap(i -> numbers2.stream().filter(j -> (i + j) % 3 == 0).map(j -> new int[]{i, j})).collect(toList());
        System.out.println(pairs);

        // 查找和匹配
        // anyMatch、allMatch和noneMatch这三个操作是终止操作
        // llMatch、anyMatch、noneMatch、findFirst和findAny 不用处理整个流就能得到结果 limit也是一个短路操作 它们可以把无限流变成有限流
        if (menu.stream().anyMatch(Dish::isVegetarian)) {  // 终端操作
            System.out.println("The menu is (somewhat) vegetarian friendly!!");
        }
        boolean isHealthy = menu.stream().anyMatch(d -> d.getCalories() < 1000);
        isHealthy = menu.stream().noneMatch(d -> d.getCalories() >= 1000);

        // findAny方法将返回当前流中的任意元素
        // 如果包含一个值就打印,否则什么都不做
        menu.stream().filter(Dish::isVegetarian).findAny().ifPresent(d -> System.out.println(d.getName()));
        menu.stream().filter(Dish::isVegetarian).findFirst().ifPresent(d -> System.out.println(d.getName()));

        // 归约操作
        numbers = Arrays.asList(2, 4, 6, 8);
        // Integer sum = numbers.stream().reduce(0, (a, b) -> a + b);
        Integer sum = numbers.stream().reduce(0, Integer::sum);
        // 最大值,最小值
        Optional<Integer> max = numbers.stream().reduce(Integer::max);
        Optional<Integer> min = numbers.stream().reduce(Integer::min);
        System.out.println(min.get());
        System.out.println(max.get());
        System.out.println(sum);

        // 交易员和交易
        Trader raoul = new Trader("Raoul", "Cambridge");
        Trader mario = new Trader("Mario", "Milan");
        Trader alan = new Trader("Alan", "Cambridge");
        Trader brian = new Trader("Brian", "Cambridge");
        List<Transaction> transactions = Arrays.asList(
                new Transaction(brian, 2011, 300),
                new Transaction(raoul, 2012, 1000),
                new Transaction(raoul, 2011, 400),
                new Transaction(mario, 2012, 710),
                new Transaction(mario, 2012, 700),
                new Transaction(alan, 2012, 950)
        );

        // 找出2011年发生的所有交易，并按交易额排序（从低到高）。
        List<Transaction> tr2011 =  transactions.stream()
                                                .filter(t -> t.getYear() == 2011)
                                                .sorted(comparing(Transaction::getValue))
                                                .collect(toList());

        // 交易员都在哪些不同的城市工作过
        // 去掉 distinct 用 toSet()
        List<String> cities = transactions.stream()
                                            .map(transaction -> transaction
                                            .getTrader().getCity())
                                            .distinct()
                                            .collect(toList());

        // 查找所有来自于剑桥的交易员，并按姓名排序
        List<Trader> traders = transactions.stream()
                                        .map(Transaction::getTrader)
                                        .filter(t -> t.getCity().equals("Cambridge"))
                                        .distinct()
                                        .sorted(comparing(Trader::getName))
                                        .collect(toList());

        // 返回所有交易员的姓名字符串，按字母顺序排序
        // 这种方式每次迭代的时候都要建立一个新的String对象
        String traderStr = transactions.stream()
                                    .map(transaction -> transaction.getTrader().getName())
                                    .distinct()
                                    .sorted()
                                    .reduce("", (a, b) -> a + b);
        // 内部会用到StringBuilder
        traderStr = transactions.stream()
                                .map(transaction -> transaction.getTrader().getName())
                                .distinct()
                                .sorted()
                                .collect(joining());

        // 有没有交易员是在米兰工作的
        boolean milanBased = transactions.stream()
                                         .anyMatch(transaction -> transaction.getTrader().getCity().equals("Milan"));

        // 打印生活在剑桥的交易员的所有交易额
        transactions.stream().filter(transaction -> transaction.getTrader().getCity().equals("Cambridge"))
                            .map(Transaction::getValue)
                            .forEach(System.out::println);

        // 所有交易中，最高的交易额是多少
        Optional<Integer> highestValue = transactions.stream()
                .map(Transaction::getValue)
                .reduce(Integer::max);

        // 找到交易额最小的交易
        Optional<Integer> smallestTransaction = transactions.stream()
                .map(Transaction::getValue)
                .reduce(Integer::min);

        // 原始类型流特化 IntStream、DoubleStream、LongStream
        // 避免拆装箱 IntStream还支持其他的方便方法，如max、min、average
        int calories = menu.stream().mapToInt(Dish::getCalories).sum();
        // 将数值转化为stream
        IntStream intStream = menu.stream().mapToInt(Dish::getCalories);
        Stream<Integer> stream = intStream.boxed();
        // 数值范围
        IntStream intNumbers = IntStream.rangeClosed(1, 100).filter(i -> i % 2 == 0);
        System.out.println(intNumbers.count());
        intNumbers = IntStream.range(1, 100).filter(i -> i % 2 == 0);
        System.out.println(intNumbers.count());

        // 构建流
        Stream<String> stringStream = Stream.of("Java 8 ", "Lambdas ", "In ", "Action");
        stringStream.map(String::toUpperCase).forEach(System.out::println);
        // 得到一个空流
        Stream<String> emptyStream = Stream.empty();
        // 数组创建流
        Stream<String> arrayStream = Arrays.stream(new String[]{"zhangsan"});
        // 由文件生成流
        // 统计文件中不相同的字有多少个
        long uniqueWords = 0;
        uniqueWords = Files.lines(Paths.get("data.txt"), Charset.defaultCharset())
                        .flatMap(line -> Arrays.stream(line.split("")))
                        .distinct()
                        .count();

        // 创建无限流
        List<Integer> integerList = Stream.iterate(0, n -> n + 2).limit(10).collect(toList());
        // 斐波纳契元组序列
        Stream.iterate(new int[]{0,1},i->new int[]{i[1],i[0]+i[1]})
                .limit(10)
                .map(t->t[0])
                .forEach(System.out::println);
    }
}
