package com.penkzhou.demo.livemenu;

import java.util.Objects;

public class DishModel {
    String name;
    float price;
    int chooseCount;
    String desc;
    String modelPath;

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    @Override
    public String toString() {
        return "DishModel{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", chooseCount=" + chooseCount +
                ", desc=" + desc +
                '}';
    }


    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DishModel) {
            return ((DishModel) obj).getName().equals(getName());
        }
        return false;
    }
}
