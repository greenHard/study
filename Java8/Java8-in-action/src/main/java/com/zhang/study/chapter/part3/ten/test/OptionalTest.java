package com.zhang.study.chapter.part3.ten.test;

import java.util.Optional;

/**
 * Optional应用
 */
public class OptionalTest {
    public static void main(String[] args) {
        // 1. 创建Optional对象
        // 创建一个空的Optional
        Optional<Car> optCar1 = Optional.empty();
        // 根据一个非空值创建Optional
        // 如果car为null,会直接抛出异常
        Car car = new Car();
        Optional<Car> optCar2 = Optional.of(car);
        // 创建一个允许null的Optional对象
        Optional<Car> optCar3 = Optional.ofNullable(car);
    }

    /**
     * 获取一个实例对象
     */
    public static String getCarInsuranceName(Optional<Person> person) {
        return person.flatMap(Person::getCar)
                .flatMap(Car::getInsurance)
                .map(Insurance::getName)
                .orElse("Unknown");
    }
}

class Person{
    private Optional<Car> car;

    public Optional<Car> getCar() {
        return car;
    }
}

class Car{
    private Optional<Insurance> insurance;

    public Optional<Insurance> getInsurance() {
        return insurance;
    }
}

class Insurance{
    private String name;

    public String getName() {
        return name;
    }
}
