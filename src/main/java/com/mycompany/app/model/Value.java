package com.mycompany.app.model;

public enum Value {
    A("a"),
    B("b"),
    C("c"),
    D("d"),
    E("e"),
    F("f"),
    G("g"),
    H("h"),
    I("i"),
    J("j"),
    W("w");

    private final String value;

    Value(String value) {
        this.value = value;
    }

    public static Value fromString(String input) {
        for (Value v : Value.values()) {
            if (v.value.equals(input)) {
                return v;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + input);
    }
}
