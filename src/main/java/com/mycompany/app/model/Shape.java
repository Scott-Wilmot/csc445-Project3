package com.mycompany.app.model;

public enum Shape {
    STAR("st"),
    MOON("m"),
    SUN("su"),
    HEART("h"),
    SKIP("sk"),
    REVERSE("r"),
    DRAW_TWO("p2"),
    DRAW_FOUR("p4"),
    WILD("w");

    private final String shapeValue;

    Shape(String h) {
        this.shapeValue = h;
    }

    public String getShapeValue() {
        return shapeValue;
    }

    public static Shape fromString(String value) {
        for (Shape s : Shape.values()) {
            if (s.shapeValue.equals(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
