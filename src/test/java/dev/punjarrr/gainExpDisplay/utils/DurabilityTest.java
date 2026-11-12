package dev.punjarrr.gainExpDisplay.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DurabilityTest {

    @Test
    void addsTheRepairToTheRemainingDurability() {
        // Diamond pickaxe, 100 damage, Mending repairs 10
        assertEquals(1471, Durability.afterRepair(1561, 100, 10));
    }

    @Test
    void neverShowsMoreThanTheMaximum() {
        // Only 3 damage left, but the event reports a 10 point repair
        assertEquals(250, Durability.afterRepair(250, 3, 10));
    }

    @Test
    void neverShowsANegativeDurability() {
        assertEquals(0, Durability.afterRepair(10, 50, 1));
    }
}
