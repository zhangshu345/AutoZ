package com.stardust.concurrent;

/**

 */

public class Value<T> {

    private T mValue;

    public Value(T value) {
        mValue = value;
    }

    public Value() {
    }

    public T get() {
        return mValue;
    }

    public void set(T value) {
        mValue = value;
    }


}
