package org.hhoao.mc.ironelevators;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigParsingTest {
    @Test
    void parseElevatorBlockEntriesSkipsMalformedValues() {
        Map<String, Integer> parsed = ElevatorBlockEntries.parse(List.of(
            "minecraft:iron_block",
            "minecraft:gold_block:12",
            "bad entry",
            "minecraft:emerald_block:not_a_number"
        ));

        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("minecraft:iron_block", -1);
        expected.put("minecraft:gold_block", 12);

        assertEquals(expected, parsed);
    }

    @Test
    void parseElevatorBlockEntriesAcceptsLegacyMinecraftBlockIds() {
        Map<String, Integer> parsed = ElevatorBlockEntries.parse(List.of(
            "iron_block",
            "gold_block:12"
        ));

        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("minecraft:iron_block", -1);
        expected.put("minecraft:gold_block", 12);

        assertEquals(expected, parsed);
    }

    @Test
    void serializeElevatorBlockEntriesUsesFullResourceLocations() {
        Map<String, Integer> entries = new LinkedHashMap<>();
        entries.put("minecraft:iron_block", -1);
        entries.put("minecraft:gold_block", 12);

        assertEquals(
            List.of("minecraft:iron_block", "minecraft:gold_block:12"),
            ElevatorBlockEntries.serialize(entries)
        );
    }
}
