package com.aeroops.network.gate;

/**
 * ICAO aerodrome reference code letter, by wingspan. A gate accepts aircraft up to its letter
 * (e.g. A320/B737 are code C; A330/B787 are code E; A380 is code F).
 */
public enum SizeCategory {
    A, B, C, D, E, F;

    /** True if an aircraft of {@code aircraftCategory} fits a gate of this category. */
    public boolean accommodates(SizeCategory aircraftCategory) {
        return aircraftCategory.ordinal() <= this.ordinal();
    }
}
