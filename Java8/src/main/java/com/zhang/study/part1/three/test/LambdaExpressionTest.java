package com.zhang.study.part1.three.test;

import com.zhang.study.part1.three.pojo.Apple;
import com.zhang.study.part1.three.pojo.Letter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Comparator.comparing;

/**
 * lambda表达式的使用
 */
public class LambdaExpressionTest {

    /**
     * 函数式接口
     */
    @FunctionalInterface
    private interface Adder{
        int add(int a, int b);
    }

    private interface SmartAdder extends Adder{
        int add(double a, double b);
    }

    private interface Nothing{
    }

    // 继续优化
     @FunctionalInterface
     private interface Predicate<T>{
        boolean test(T t);
    }

    public static <T> List<T> filter(List<T> inventory,Predicate<T> predicate) {
        List<T> result = new ArrayList<T>();
        for (T e : inventory) {
            if (predicate.test(e)) {
                result.add(e);
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        List<Apple> inventory = Arrays.asList(new Apple(80L,"green"),
                new Apple(155L, "green"),
                new Apple(120L, "red"));

        System.out.println("排序前:"+inventory);

        // 参数列表——这里它采用了Comparator中compare方法的参数，两个Apple。
        // 箭头——箭头->把参数列表与Lambda主体分隔开。
        // Lambda主体——比较两个Apple的重量。表达式就是Lambda的返回值了。
        inventory.sort((a1,a2)->a1.getWeight().compareTo(a2.getWeight()));
        System.out.println("排序后:"+inventory);

        // Lambda表达式允许你直接以内联的形式为函数式接口的抽象方法提供实现，并把整个表达式作为函数式接口的实例
        List<Apple> appleList = filter(inventory, p -> p.getWeight() > 100);
        System.out.println(appleList);

        // 开启一个线程 Runnable函数式接口
        new Thread(()-> System.out.println(Thread.currentThread())).start();;

        // 读取两行
        // processFile2((brp)->brp.readLine()+brp.readLine());
        // 读取一行
        // processFile2((brp)->brp.readLine());

        // java.util.function
        // 对应的基本类型的Predicate、Consumer、Function可以避免自动装箱,节约性能
        // 例如 IntPredicate、IntConsumer、IntFunction<R>等
        // Predicate  传入一个参数,得到一个布尔值   常用来进行过滤,排除
        // 如果为空,就是false
        List<String> stringList = Arrays.asList("1111","","2222");
        System.out.println("过滤前:"+stringList);
        stringList= filter(stringList, s -> !s.isEmpty());
        System.out.println("过滤后:"+stringList);

        // Consumer 接收一个对象,没有返回值  一般用来在函数里做点什么东西
        forEach(stringList,s-> System.out.println("我要干点什么:"+s));
        forEach(inventory,a-> System.out.println("苹果颜色:"+a.getColor()+",苹果重"+a.getWeight()));

        // Function 接收一个T类型对象,返回一个R类型对象
        // 比如,传入字符串集合,获取字符串的长度集合
        List<Integer> stringLengths = map(Arrays.asList("zhangsan", "lisi", "xiaoming"), r -> r.length());
        System.out.println(stringLengths);

        // 局部变量的问题
        int num = 10;
        new Thread(()-> System.out.println(num));
        // num = 11;  // 这样会报错,因为局部变量必须是final的,后面不能改变

        // closure 闭包
        // Java 8的Lambda和匿名类可以做类似于闭包的事情：
        // 它们可以作为参数传递给方法，并且可以访问其作用域之外的变量。但有一个限制：它们不
        // 能修改定义Lambda的方法的局部变量的内容。这些变量必须是隐式最终
        // 可以认为Lambda是对值封闭，而不是对变量封闭

        // 方法引用
        inventory.sort((a1,a2)->a1.getWeight().compareTo(a2.getWeight()));
        // java.util.Comparator.comparing
        // 将lambda表达式修改之后
        inventory.sort(comparing(Apple::getWeight));
        inventory.sort(comparing(Apple::getColor));
        // 1. 遍历一个集合
        // System.out.println("===================");
        // inventory.forEach((r)-> System.out.println(r));
        System.out.println("===================");
        inventory.forEach(System.out::println);

        // 构造函数引用
        // 1个参数
        // 获取一堆苹果,根据集合
        List<Long> weights = Arrays.asList(1L,3L,4L,7L);
        map(weights,Apple::new);

        // 2个参数的情况,使用BiFunction
        List<String> colors = Arrays.asList("green","yellow","red","white");
        List<Apple> apples = getApples(weights, colors, Apple::new);
        System.out.println(apples);

        // --> 3个或者更多参数，需要自定义函数式接口即可
        // 复合Lambda表达式
        // 如果你想要对苹果按重量递减排序怎么办?
        inventory.sort(comparing(Apple::getWeight).reversed());
        // 但如果发现有两个苹果一样重怎么办？
        inventory.sort(comparing(Apple::getWeight).reversed().thenComparing(Apple::getColor));

        // 谓词复合 感觉不太好用
        // Predicate<Apple> notRedApple = redApple.negate();
        // Predicate<Apple> redAndHeavyApple = redApple.and(a -> a.getWeight() > 150);
        // Predicate<Apple> redAndHeavyAppleOrGreen = redApple.and(a -> a.getWeight() > 150).or(a -> "green".equals(a.getColor()));
        filterApples(inventory,a->"red".equals(a.getColor()));

        // 函数复合
        List<Integer> numberList = Arrays.asList(1,3,5,7);
        Function<Integer, Integer> f = x -> x + 1;
        Function<Integer, Integer> g = x -> x * 2;
        System.out.println("计算前:"+numberList);
        // 先计算f,再计算g
        // 计算前:[1, 3, 5, 7]  --> [2, 4, 6, 8]
        // 计算后:[4, 8, 12, 16]
        // numberList= calculate(numberList, f.andThen(g));

        // 先计算g,再计算f
        // 计算前:[1, 3, 5, 7]  -->[2, 6, 10, 14]
        // 计算后:[3, 7, 11, 15]
        numberList= calculate(numberList, f.compose(g));
        System.out.println("计算后:"+numberList);

        // 用String类型的一封信做文本转换
        // 比如创建一个流水线：先加上抬头，然后进行拼写检查，最后加上一个落款
        Function<String, String> addHeader = Letter::addHeader;
        Function<String, String> transformationPipeline = addHeader.andThen(Letter::checkSpelling).andThen(Letter::addFooter);;
    }

    /**
     * 将集合中的数字按一定规则进行计算并返回
     */
    public static<T,R> List<R> calculate(List<T> numbers, Function<T,R> function){
        List<R> numberList = new ArrayList<>();
        for (T number : numbers) {
            R r = function.apply(number);
            numberList.add(r);
        }
        return numberList;
    }
    /**
     * 自定义函数式接口
     */
    @FunctionalInterface
    private interface TriFunction<T,U,V,R>{
        R apply(T t, U u, V v);
    }

    /**
     * 6. 这就是行为参数化，方法接受不同的行为,在内部使用,来完成不同的功能
     */
    public static<T> List<T> filterApples(List<T> inventory,java.util.function.Predicate<T> predicate) {
        List<T> result = new ArrayList<T>();
        for (T t : inventory) {
            // Predicate封装了具体的过滤实现
            if (predicate.test(t)) {
                result.add(t);
            }
        }
        return result;
    }

    public static<T,U,R> List<R> getApples(List<T> list,List<U> list1,BiFunction<T,U,R> b){
        List<R> result =new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            R r = b.apply(list.get(i),list1.get(i));
            result.add(r);
        }
        return result;
    }
    public static<T> void forEach(List<T> list,Consumer<T> c){
        for (T t : list) {
            c.accept(t);
        }
    }


    public static<T,R> List<R> map(List<T> list, Function<T,R> f){
        List<R> result =new ArrayList<>();
        for (T t : list) {
            R r = f.apply(t);
            result.add(r);
        }
        return result;
    }
    /**
     * 读取一个文件,使用流
     * 方法存在局限,一次只能读一行,如果要读两行、三行、读第一个字节?  --> 行为参数化
     */
    public static String processFile() throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader("Apple.java"))){  // 初始化代码并且自动关闭代码
            return br.readLine();  // 这属于行为把行为抽取出来,变成函数接口,用Lambda表达式进行传递
        }
    }

    /**
     * 改善之后的方法
     */
    public static String processFile2(BufferedReaderProcessor bufferedReaderProcessor) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader("Apple.java"))){  // 初始化代码并且自动关闭代码
            return bufferedReaderProcessor.process(br);
        }
    }

    @FunctionalInterface
    private interface BufferedReaderProcessor{
        String process(BufferedReader br) throws IOException;
    }

}
