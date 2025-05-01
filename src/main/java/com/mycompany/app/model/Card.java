package com.mycompany.app.model;

/**
 * The playing card used for the game, 445.
 *
 * @param shape - one of eight possible shapes (STAR, MOON, SUN, HEART, SKIP, REVERSE, DRAW_TWO, WILD)
 * @param value - one of ten possible value (    PAPRIKA, VALENCIA, JAFFA, KOROMIKO, SALOMIE, BUTTERFLY_BUSH, PARADISO, TRADEWIND, MOSS_GREEN, SANDWISP)
 */
public record Card(Shape shape, Value value) {
    @Override
    public String toString() {
        return value + " " + shape;
    }
}
