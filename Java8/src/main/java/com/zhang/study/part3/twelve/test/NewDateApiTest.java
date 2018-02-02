package com.zhang.study.part3.twelve.test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

/**
 * 新的时间API
 */
public class NewDateApiTest {
    public static void main(String[] args) {
        // localDateAndLocalTime();
        // instantAndDurationAndPeriod();
        // operateAndParseFormat();
        // createOwnTemporalAdjuster();
        // createOwnDateTimeFormatter();
    }

    /**
     * LocalDate、LocalTime
     */
    public static void localDateAndLocalTime(){
        // LocalDate 2018年2月22日
        LocalDate date = LocalDate.of(2018, 2, 22);
        System.out.println(date.getYear());
        System.out.println(date.getMonth());
        System.out.println(date.getDayOfYear());
        System.out.println(date.isLeapYear());
        // 从系统时钟中获取当前的日期
        // 通过TemporalField获取对应的值
        int year = date.get(ChronoField.YEAR);
        int month = date.get(ChronoField.MONTH_OF_YEAR);
        int day = date.get(ChronoField.DAY_OF_MONTH);

        //LocalTime 13:45:20
        LocalTime time = LocalTime.of(13, 45, 20);
        int hour = time.getHour();
        int minute = time.getMinute();
        int second = time.getSecond();
        // 将字符串解析成对应的date
        date = LocalDate.parse("2014-03-18");
        time = LocalTime.parse("13:45:20");

        // LocalDateTime
        LocalDateTime dt1 = LocalDateTime.of(2014, Month.MARCH, 18, 13, 45, 20);
        LocalDateTime dt2 = LocalDateTime.of(date, time);
        LocalDateTime dt3 = date.atTime(13, 45, 20);
        LocalDateTime dt4 = date.atTime(time);
        LocalDateTime dt5 = time.atDate(date);
        // 将LocalDateTime转换成LocalDate、LocalTime
        date = dt1.toLocalDate();
        time = dt1.toLocalTime();
    }

    /**
     * 机器的时间和格式
     */
    public static void instantAndDurationAndPeriod(){
        // 1秒 = 1_000_000_000纳秒(ns)
        Instant instant1 = Instant.ofEpochSecond(3);
        Instant instant2 = Instant.ofEpochSecond(3,0);
        Instant instant3 = Instant.ofEpochSecond(2,1_000_000_000);
        Instant instant4 = Instant.ofEpochSecond(4, -1_000_000_000);
        System.out.println(instant1);
        System.out.println(instant2);
        System.out.println(instant3);
        System.out.println(instant4);
        // 抛出 Exception in thread "main" java.time.temporal.UnsupportedTemporalTypeException: Unsupported field: DayOfMonth
        int day = Instant.now().get(ChronoField.DAY_OF_MONTH);
        // 由于LocalDateTime和Instant是为不同的目的而设计的，一个是为了便于人阅读使用，另一个是为了便于机器处理，所以你不能将二者混用
        Duration duration = Duration.between(instant1, instant2);
        Duration tenDays = Duration.between(LocalDate.of(2017, 6, 20), LocalDate.of(2018, 2, 2));
        // 创建Duration和Period对象
        Duration d1 = Duration.ofMinutes(3);
        Duration d2 = Duration.of(3, ChronoUnit.MINUTES);
        Period p1 = Period.ofDays(10);
        Period p2 = Period.ofWeeks(3);
        Period p3 = Period.of(2, 6, 1);
    }


    /**
     * 操作解析格式化日期
     */
    public static void  operateAndParseFormat(){
        // get with plus from now of 等都是通用方法
        LocalDate date1 = LocalDate.of(2014, 3, 18);
        System.out.println(date1); // 2014-03-18
        LocalDate date2 = date1.withYear(2011);
        System.out.println(date2); // 2011-03-18
        LocalDate date3 = date2.withMonth(4);
        System.out.println(date3); // 2011-04-18
        LocalDate date4 = date3.with(ChronoField.YEAR, 2018);
        System.out.println(date4); // 2018-04-18
        date4 = date4.plusWeeks(1);
        System.out.println(date4); // 2018-04-25
        date4 = date4.minusYears(3);
        System.out.println(date4); // 2015-04-25
        date4 = date4.plus(2, ChronoUnit.YEARS);
        System.out.println(date4); // 2017-04-25

        String s1 = date4.format(DateTimeFormatter.BASIC_ISO_DATE);
        System.out.println(s1); // 20170425
        String s2 = date4.format(DateTimeFormatter.ISO_LOCAL_DATE);
        System.out.println(s2); // 2017-04-25

        LocalDate date5 = LocalDate.parse("20180202", DateTimeFormatter.BASIC_ISO_DATE);
        System.out.println(date5); // 2018-02-02
        LocalDate date6 = LocalDate.parse("2018-02-02",DateTimeFormatter.ISO_LOCAL_DATE);
        System.out.println(date6); // 2018-02-02
    }


    /**
     * 创建自己的TemporalAdjuster
     */
    public static void createOwnTemporalAdjuster(){
        LocalDate date = LocalDate.now();
        // 下一个工作日
        // 如果是周五,当前时间加三天
        // 如果是周六,当前时间加二天
        // 其他时间当前时间加一天
        date= date.with(temporal -> {
            DayOfWeek dayOfWeek = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK));
            int dayOfAdd = 1;
            if (DayOfWeek.FRIDAY == dayOfWeek) {
                dayOfAdd = 3;
            } else if (DayOfWeek.SATURDAY == dayOfWeek) {
                dayOfAdd = 2;
            }
            return temporal.plus(dayOfAdd, ChronoUnit.DAYS);
        });
        System.out.println(date);

        // 封装成类库,可以通用
        TemporalAdjuster nextWorkingDay = TemporalAdjusters.ofDateAdjuster(temporal -> {
            DayOfWeek dayOfWeek = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK));
            int dayOfAdd = 1;
            if (DayOfWeek.FRIDAY == dayOfWeek) {
                dayOfAdd = 3;
            } else if (DayOfWeek.SATURDAY == dayOfWeek) {
                dayOfAdd = 2;
            }
            return temporal.plus(dayOfAdd, ChronoUnit.DAYS);
        });
    }

    /**
     * 创建自己的DateTimeFormatter
     */
    public static void createOwnDateTimeFormatter(){
        // 按照某个模式创建DateTimeFormatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date1 = LocalDate.of(2014, 3, 18);
        String formattedDate = date1.format(formatter);
        System.out.println(formattedDate);
        LocalDate date2 = LocalDate.parse(formattedDate, formatter);
        // 创建一个本地化的DateTimeFormatter
        DateTimeFormatter localFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.CHINA);
        String localFormattedDate = date1.format(localFormatter);
        System.out.println(localFormattedDate); // 2014/03/18
        // 更加细粒度的创建DateTimeFormatter
        DateTimeFormatter italianFormatter = new DateTimeFormatterBuilder()
                .appendText(ChronoField.DAY_OF_MONTH)
                .appendLiteral(". ")
                .appendText(ChronoField.MONTH_OF_YEAR)
                .appendLiteral(" ")
                .appendText(ChronoField.YEAR)
                .parseCaseInsensitive()
                .toFormatter(Locale.ITALIAN);
    }
}
