package com.zhang.study.part1.two;

import com.zhang.study.chapter.part1.three.pojo.Apple;
import com.zhang.study.part1.three.pojo.Apple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterApple {
    /**
     * 1. 需要筛选绿苹果
     */
    public static List<Apple> findGreenApple(List<Apple> apples) {
        List<Apple> list = new ArrayList<Apple>();
        for (Apple apple : apples) {
            if ("green".equals(apple.getColor())) {
                list.add(apple);
            }
        }
        return list;
    }
    /**
     * 2. 需要筛选红苹果
     */
    public static List<Apple> findRedApple(List<Apple> apples) {
        List<Apple> list = new ArrayList<Apple>();
        for (Apple apple : apples) {
            if ("green".equals(apple.getColor())) {
                list.add(apple);
            }
        }
        return list;
    }

    /**
     * 3. 浅绿色、暗红色、黄色等，这种方法就应付不了了。一个良好的原则是在编写类似的代码之后，尝试将其抽象化
     */
    public static List<Apple> findAppleByColor(List<Apple> apples,String color) {
        List<Apple> list = new ArrayList<Apple>();
        for (Apple apple : apples) {
            if (color.equals(apple.getColor())) {
                list.add(apple);
            }
        }
        return list;
    }

    // 4. 如果要筛选苹果每个属性，各种搭配?

    /**
     * 5.通过策略模式
     * ApplePredicate封装了筛选苹果的策略
     */
    private interface ApplePredicate{
        boolean filterApple(Apple apple);
    }

    /**
     * 具体苹果的不同策略,容易扩展
     */
    private static class AppleColorPredicate implements ApplePredicate{
        @Override
        public boolean filterApple(Apple apple) {
            return "green".equals(apple.getColor());
        }
    }

    private static class AppleWeightPredicate implements ApplePredicate{
        @Override
        public boolean filterApple(Apple apple) {
            return apple.getWeight()>150L;
        }
    }

    private static class AppleRedAndWeightLess150Predicate implements ApplePredicate{
        @Override
        public boolean filterApple(Apple apple) {
            return "red".equals(apple.getColor())&&apple.getWeight()<150L;
        }
    }


    /**
     * 6. 这就是行为参数化，方法接受不同的行为,在内部使用,来完成不同的功能
     */
    public static List<Apple> filterApples(List<Apple> inventory,ApplePredicate applePredicate) {
        List<Apple> result = new ArrayList<Apple>();
        for (Apple apple : inventory) {
            // Predicate封装了具体的过滤实现
            if (applePredicate.filterApple(apple)) {
                result.add(apple);
            }
        }
        return result;
    }

    // 继续优化
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
    public static void main(String[] args) throws InterruptedException {
        List<Apple> inventory = Arrays.asList(new Apple(80L,"green"),
                new Apple(155L, "green"),
                new Apple(120L, "red"));
     /*   List<Apple> heavyApples =
                filterApples(inventory, new AppleColorPredicate());
        List<Apple> greenApples =
                filterApples(inventory, new AppleWeightPredicate());*/

        // 7.使用匿名内部类的方式
   /*     List<Apple> greenAndWeightApples = filterApples(inventory, new ApplePredicate() {
            @Override
            public boolean filterApple(Apple apple) {
                return "yellow".equals(apple.getColor())&&apple.getWeight()>200L;
            }
        });*/

        // 8.使用Lambda表达式
        // List<Apple> greenAndWeightApples = filterApples(inventory,apple -> "yellow".equals(apple.getColor())&&apple.getWeight()>200L);

        // 9. 更加进一步的优化,目前filterApples方法还只适用于Apple。你还可以将List类型抽象化,引入泛型T
        List<Integer> numbers = Arrays.asList(1,3,4,6,7,8);
        List<Apple> greenAndWeightApples = filter(inventory,apple -> "yellow".equals(apple.getColor())&&apple.getWeight()>200L);
        List<Integer> integerList = filter(numbers, i -> i % 2 == 0);
    }

}