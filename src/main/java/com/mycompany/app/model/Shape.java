package com.mycompany.app.model;

public enum Shape {
    STAR("st"),
    MOON("m"),
    SUN("su"),
    HEART("h"),
    SKIP("sk"),
    REVERSE("r"),
    DRAW_TWO("p2"),
    WILD("w");

    private final String shapeValue;

    Shape(String h) {
        this.shapeValue = h;
    }
}
