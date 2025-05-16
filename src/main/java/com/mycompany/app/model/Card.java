package com.mycompany.app.model;

import java.io.Serializable;

import java.io.DataInput;
import java.io.Serializable;

/**
 * Record class for card representation
 *
 * @param shape - one of 9 possible shapes (refer to Shape Class)
 * @param value - one of 11 possible values (refer to Value Class)
 */
public record Card(Shape shape, Value value) implements Serializable {
    @Override
    public String toString() {
        return value + " " + shape;
    }

    public String getFileName() {
        return this.shape.getShape() + "_" + this.value.getValue() + ".png";
    }
}
