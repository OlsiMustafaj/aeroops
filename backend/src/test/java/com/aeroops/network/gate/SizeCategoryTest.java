package com.aeroops.network.gate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SizeCategoryTest {

    @Test
    void gateAcceptsSameOrSmallerAircraft() {
        assertThat(SizeCategory.E.accommodates(SizeCategory.C)).isTrue();
        assertThat(SizeCategory.C.accommodates(SizeCategory.C)).isTrue();
    }

    @Test
    void gateRejectsLargerAircraft() {
        assertThat(SizeCategory.C.accommodates(SizeCategory.E)).isFalse();
    }
}
