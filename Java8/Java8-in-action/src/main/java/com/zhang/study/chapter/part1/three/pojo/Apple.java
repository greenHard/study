package com.zhang.study.chapter.part1.three.pojo;

/**
 *  苹果实体类
 */
public class Apple {

    private String color;

    private Long weight;

    public Apple(Long weight, String color) {
        this.color = color;
        this.weight = weight;
    }

    public Apple(Long weight) {
        this.weight = weight;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Apple{" +
                "color='" + color + '\'' +
                ", weight=" + weight +
                '}';
    }
}
