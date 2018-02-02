package com.zhang.study.part4.thirdteen.test;

/**
 * 函数式编程
 */
public class FunctionTest {
    public static void main(String[] args) {
        // 需求,计算1到n的阶乘
    }

    /**
     * 递归
     */
    public static long factorialRecursive(long n){
        return n==1?1:n*factorialRecursive(n-1);
    }

    /**
     * 尾递归
     * 如果一个函数中所有递归形式的调用都出现在函数的末尾，我们称这个递归函数是尾递归的。
     * 当递归调用是整个函数体中最后执行的语句且它的返回值不属于表达式的一部分时,这个递归调用就是尾递归
     */
    public static long factorialTailRecursive(long n){
        return factorialHelper(1,n);
    }

    private static long factorialHelper(long i, long n) {
           return n==1?i:factorialHelper(i*n,n-1);
    }



}
