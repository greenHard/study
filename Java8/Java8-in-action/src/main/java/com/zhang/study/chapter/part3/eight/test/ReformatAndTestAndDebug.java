package com.zhang.study.chapter.part3.eight.test;

import com.zhang.study.chapter.part2.four.pojo.Dish;

import java.util.*;
import java.util.function.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * 重构、测试、调试
 */
public class ReformatAndTestAndDebug {
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

        // 1. 用lambda表达式取代匿名类
        // 2. 用方法引用重构Lambda表达式
        // 3. 用Stream API重构命令式的数据处理

        // (1) 从匿名类到lambda表达式的转换
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("匿名内部类的方式运行了");
            }
        }).start();

        new Thread(()-> System.out.println("lambda表达式的方式运行了")).start();

        // (2) 从Lambda 表达式到方法引用的转换
        Map<Dish.CaloricLevel, List<Dish>> dishesByCaloricLevel = menu.stream().collect(groupingBy(dish -> {
            if (dish.getCalories() <= 400) return Dish.CaloricLevel.DIET;
            else if (dish.getCalories() <= 700) return Dish.CaloricLevel.NORMAL;
            else return Dish.CaloricLevel.FAT;
        }));
        // 添加getCaloricLevel()方法,修改成方法引用的方式
        dishesByCaloricLevel = menu.stream().collect(groupingBy(Dish::getCaloricLevel));

        // (3) 从命令式的数据处理切换到Stream
        List<String> dishNames = new ArrayList<>();
        for(Dish dish: menu){
            if(dish.getCalories() > 300){
                dishNames.add(dish.getName());
            }
        }
        // 替代方案使用Stream AP
        dishNames = menu.stream().filter(d->d.getCalories()>300).map(Dish::getName).collect(toList());

        // 2.使用Lambda 重构面向对象的设计模式
        // 2.1 策略模式
        Validator numericValidator = new Validator(new IsNumeric());
        boolean b1 = numericValidator.validate("aaaa");
        Validator lowerCaseValidator = new Validator(new IsAllLowerCase());
        boolean b2 = lowerCaseValidator.validate("bbbb");
        // 使用lambda表达式
        numericValidator = new Validator(s->s.matches("[a-z]+"));
        numericValidator.validate("aaaa");
        lowerCaseValidator = new Validator(s->s.matches("\\d+"));
        lowerCaseValidator.validate("bbbb");

        // 2.2 模板模式
        new CalculateRunningTime().calculate((t)-> System.out.println("111"));

        // 2.3 观察者模式
        Feed f = new Feed();
        f.registerObserver(new NYTimes());
        f.registerObserver(new Guardian());
        f.notifyObservers("The queen said her favourite book is Java 8 in Action!");

        // 使用lambda表达式
        f.registerObserver(tweet->{
            if(tweet != null && tweet.contains("queen")){
                System.out.println("Yet another news in London... " + tweet);
            }
        });
        // 如果逻辑太复杂还是建议使用原来的方式

        // 2.4 责任链模式
        // 先要组装责任链
        Handler h1 = new DeptManager();
        Handler h2 = new ProjectManager();
        h2.setSuccessor(h1);
        String test2 = h2.handleFeeRequest(700);
        System.out.println("test2 = " + test2);

        // 使用lambda表达式
        DoubleConsumer firstHanding = (double fee) -> {
            String user = "张三";
            String str = "";
            //部门经理的权限只能在1000以内
            if (fee < 500) {
                //为了测试，简单点，只同意张三的请求
                if ("张三".equals(user)) {
                    str = "成功：项目经理同意【" + user + "】的聚餐费用，金额为" + fee + "元";
                } else {
                    //其他人一律不同意
                    str = "失败：项目经理不同意【" + user + "】的聚餐费用，金额为" + fee + "元";
                }
            }
            System.out.println(str);
        };


        DoubleConsumer secondHanding = (double fee) -> {
            String user = "张三";
            String str = "";
            //部门经理的权限只能在1000以内
            if (fee>500 && fee < 1000) {
                //为了测试，简单点，只同意张三的请求
                if ("张三".equals(user)) {
                    str = "成功：部门经理同意【" + user + "】的聚餐费用，金额为" + fee + "元";
                } else {
                    //其他人一律不同意
                    str = "失败：部门经理不同意【" + user + "】的聚餐费用，金额为" + fee + "元";
                }
            }
            System.out.println(str);
        };

        DoubleConsumer consumer = firstHanding.andThen(secondHanding);
        consumer.accept(300);

        // 2.5 工厂模式
        Product p = ProductFactory.createProduct("loan");

        // 3. 调试 peek
        List<Integer> numbers = Arrays.asList(2, 3, 4, 5);
        final List<Integer> numberList = numbers.stream().peek(x -> System.out.println("from stream" + x))
                .map(x -> x + 17)
                .peek(x -> System.out.println("after map" + x))
                .filter(x -> x % 2 == 0)
                .peek(x -> System.out.println("after filter" + x))
                .limit(3)
                .peek(x -> System.out.println("after limit" + x))
                .collect(toList());

    }

    /**
     * 策略接口
     */
    public interface ValidationStrategy {
        boolean execute(String s);
    }

    private static class IsAllLowerCase implements  ValidationStrategy{
        @Override
        public boolean execute(String s) {
            return s.matches("[a-z]+");
        }
    }

    private static class IsNumeric implements  ValidationStrategy{
        @Override
        public boolean execute(String s) {
            return s.matches("\\d+");
        }
    }

    private static class Validator{
        private final ValidationStrategy strategy;

        public Validator(ValidationStrategy strategy) {
            this.strategy = strategy;
        }

        /**
         * 验证方法
         */
        public boolean validate(String s){
            return strategy.execute(s);
        }
    }

    /**
     * 模板设计模式:
     * 计算程序运行时间
     */
     static class CalculateRunningTime{
       /* public void calculate(){
            Dish dish = new Dish("pork", false, 800, Dish.Type.MEAT);
            long start = System.currentTimeMillis();
            execute(dish);
            long interval = System.currentTimeMillis()-start;
            System.out.println(interval);
        }*/

        /**
         * lambda表达式的写法
         */
        public  void calculate(Consumer<Dish> consumer){
            Dish dish = new Dish("pork", false, 800, Dish.Type.MEAT);
            long start = System.currentTimeMillis();
            consumer.accept(dish);
            long interval = System.currentTimeMillis()-start;
            System.out.println(interval);
        }

        // abstract void execute(Dish dish);
    }

    /**
     * 观察者模式
     */
    private interface Observer{
        void notifyObserver(String tweet);
    }

    /**
     * 纽约时报
     */
    private static class NYTimes implements  Observer{
        @Override
        public void notifyObserver(String tweet) {
            if(tweet != null && tweet.contains("money")){
                System.out.println("Breaking news in NY! " + tweet);
            }
        }
    }

    /**
     * 英国卫报
     */
    private  static class Guardian implements Observer{
        @Override
        public void notifyObserver(String tweet) {
            if(tweet != null && tweet.contains("queen")){
                System.out.println("Yet another news in London... " + tweet);
            }
        }
    }

    private interface Subject{
        void registerObserver(Observer o);
        void notifyObservers(String tweet);
    }

    private static class Feed implements Subject{
        private final List<Observer> observers = new ArrayList<>();

        @Override
        public void registerObserver(Observer o) {
            observers.add(o);
        }

        @Override
        public void notifyObservers(String tweet) {
            observers.forEach(o->o.notifyObserver(tweet));
        }
    }

    /**
     *  责任链模式
     */

    private abstract static class Handler{
        /**
         * 持有下一个处理请求的对象
         */
        protected  Handler successor  = null;

        /**
         * 取值方法
         */
        public Handler getSuccessor() {
            return successor;
        }

        /**
         * 设置下一个处理请求的对象
         */
        public void setSuccessor(Handler successor) {
            this.successor = successor;
        }

        /**
         * 处理聚餐费用的申请
         */
        protected abstract String handleFeeRequest(double fee);
    }

    private static class ProjectManager extends Handler {

        @Override
        public String handleFeeRequest(double fee) {
            String user = "张三";
            String str = "";
            // 项目经理权限比较小，只能在500以内
            if (fee < 500) {
                // 为了测试，简单点，只同意张三的请求
                if ("张三".equals(user)) {
                    str = "成功：项目经理同意【" + user + "】的聚餐费用，金额为" + fee + "元";
                } else {
                    // 其他人一律不同意
                    str = "失败：项目经理不同意【" + user + "】的聚餐费用，金额为" + fee + "元";
                }
            } else {
                // 超过500，继续传递给级别更高的人处理
                if (getSuccessor() != null) {
                    return getSuccessor().handleFeeRequest(fee);
                }
            }
            return str;
        }
    }

    private static class DeptManager extends Handler {

        @Override
        public String handleFeeRequest( double fee) {
            String user = "张三";
            String str = "";
            //部门经理的权限只能在1000以内
            if(fee < 1000)
            {
                //为了测试，简单点，只同意张三的请求
                if("张三".equals(user))
                {
                    str = "成功：部门经理同意【" + user + "】的聚餐费用，金额为" + fee + "元";
                }else
                {
                    //其他人一律不同意
                    str = "失败：部门经理不同意【" + user + "】的聚餐费用，金额为" + fee + "元";
                }
            }else
            {
                //超过1000，继续传递给级别更高的人处理
                if(getSuccessor() != null)
                {
                    return getSuccessor().handleFeeRequest(fee);
                }
            }
            return str;
        }
    }

    private static class Product{}
    private static class Loan extends Product{}
    private static class Stock extends Product{}
    private static class Bond extends Product{}

    /**
     * 工厂模式
     */
    private static class ProductFactory {
       /* public static Product createProduct(String name){
            switch(name){
                case "loan": return new Loan();
                case "stock": return new Stock();
                case "bond": return new Bond();
                default: throw new RuntimeException("No such product " + name);
            }
        }*/

        /**
         * 使用lambda表达式的方式
         */
        final  static Map<String,Supplier<Product>> map = new HashMap<>();

        static{
            map.put("loan",Loan::new);
            map.put("stock",Stock::new);
            map.put("bond",Bond::new);
        }

        public static Product createProduct(String name){
            Supplier<Product> p = map.get(name);
            if(p != null) return p.get();
            throw new IllegalArgumentException("No such product " + name);
        }
    }



}
