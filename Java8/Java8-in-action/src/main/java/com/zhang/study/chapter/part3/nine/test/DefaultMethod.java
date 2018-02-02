package com.zhang.study.chapter.part3.nine.test;

import java.util.Arrays;
import java.util.List;

import static java.util.Comparator.naturalOrder;

/**
 * 默认方法
 */
public class DefaultMethod {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(3, 5, 1, 2, 6);
        // 自然序列对其中的元素进行排序(即标准的字母数字方式排序)
        numbers.sort(naturalOrder());

        // 二进制级的兼容性、源代码级的兼容(编译不通过)、函数行为的兼容性(程序接受同样的输入能得到同样的结果)
        // 抽象类和抽象接口
        // 1. 一个类只能继承一个抽象类，但是一个类可以实现多个接口
        // 2. 一个抽象类可以通过实例变量（字段）保存一个通用状态，而接口是不能有实例变量的

        // 解决冲突的规则,菱形继承问题
        // (1) 类中的方法优先级最高,类或父类中声明的方法的优先级高于任何声明为默认方法的优先级。
        // (2) 子接口的优先级更高
        // (3) 如果还是无法判断，继承了多个接口的类必须通过显式覆盖和调用期望的方法
        new C().hello();
    }

    public interface A {
        default void hello() {
            System.out.println("Hello from A");
        }
    }
    public interface B extends A {
        default void hello() {
            System.out.println("Hello from B");
        }
    }

    public interface D{
        default void hello() {
            System.out.println("Hello from D");
        }
    }

    // 如果实现D,编译会报错,需要自己实现
    public static class C implements B, A{
    }
}
