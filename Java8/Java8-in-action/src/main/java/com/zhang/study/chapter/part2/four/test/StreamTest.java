package com.zhang.study.chapter.part2.four.test;

import com.zhang.study.chapter.part2.four.pojo.Dish;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Stream流的测试
 */
public class StreamTest {
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
                new Dish("salmon", false, 450, Dish.Type.FISH) );

        // 从支持数据处理操作的源生成的元素序列
        // 获取菜肴清单中卡路里大于300的前三个菜肴
        List<String> threeHighCaloricDishNames = menu.stream().filter(dish -> dish.getCalories() > 300).map(Dish::getName).limit(3).collect(toList());
        // System.out.println(threeHighCaloricDishNames);

        // 流只能消费一次
        // stream has already been operated upon or closed
        List<String> title = Arrays.asList("Java8","in","action");
        Stream<String> s = title.stream();
        s.forEach(System.out::println);
        // s.forEach(System.out::println);

        // 循环合并
        threeHighCaloricDishNames = menu.stream().filter(dish ->
            {
                System.out.println("filtering"+dish.getName());
                return dish.getCalories() > 300;
            }
        ).map(d->{
            System.out.println("mapping" + d.getName());
            return d.getName();
        }).limit(3).collect(toList());
        System.out.println(threeHighCaloricDishNames);

        // 流操作:
        // 一个数据源（如集合）来执行一个查询
        // 一个中间操作链，形成一条流的流水线  filter,sorted,map,limit,distinct
        // 一个终端操作，执行流水线，并能生成结果。 count,forEach,collect
    }



}
