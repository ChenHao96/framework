package com.github.chenhao96.utils.collection;

public class FastNode<D> {

    private FastNode<D>[] array;
    private int level;
    private int index;
    private D data;

    public FastNode<D>[] getArray() {
        return array;
    }

    public void setArray(FastNode<D>[] array) {
        this.array = array;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public D getData() {
        return data;
    }

    public void setData(D data) {
        this.data = data;
    }
}
