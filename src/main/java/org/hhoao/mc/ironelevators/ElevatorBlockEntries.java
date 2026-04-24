package org.hhoao.mc.ironelevators;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ElevatorBlockEntries {
    private ElevatorBlockEntries() {
    }

    static Map<String, Integer> parse(List<? extends String> entries) {
        Map<String, Integer> parsed = new LinkedHashMap<>();
        for (String entry : entries) {
            String[] split = entry.split(":");
            if (split.length < 2 || split.length > 3) {
                continue;
            }

            int height = -1;
            if (split.length == 3) {
                try {
                    height = Integer.parseInt(split[2]);
                } catch (NumberFormatException ignored) {
                    continue;
                }
            }

            parsed.put(split[0] + ":" + split[1], height);
        }
        return parsed;
    }

    static List<String> serialize(Map<String, Integer> entries) {
        return entries.entrySet().stream()
            .map(entry -> entry.getKey() + (entry.getValue() == -1 ? "" : ":" + entry.getValue()))
            .toList();
    }
}
