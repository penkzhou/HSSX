package com.penkzhou.demo.livemenu;

public class DishModel {
    String name;
    float price;
    int chooseCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getChooseCount() {
        return chooseCount;
    }

    public void setChooseCount(int chooseCount) {
        this.chooseCount = chooseCount;
    }

    @Override
    public String toString() {
        return "DishModel{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", chooseCount=" + chooseCount +
                '}';
    }
}
