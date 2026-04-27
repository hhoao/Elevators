package org.hhoao.mc.ironelevators;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class ElevatorBlockEntries {
    private static final String DEFAULT_NAMESPACE = "minecraft";
    private static final Pattern RESOURCE_LOCATION_PATTERN =
        Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");

    private ElevatorBlockEntries() {
    }

    static Map<String, Integer> parse(List<? extends String> entries) {
        Map<String, Integer> parsed = new LinkedHashMap<>();
        for (String entry : entries) {
            String[] split = entry.split(":");
            if (split.length < 1 || split.length > 3) {
                continue;
            }

            String blockId;
            int height = -1;
            if (split.length == 1) {
                blockId = DEFAULT_NAMESPACE + ":" + split[0];
            } else if (split.length == 2) {
                try {
                    height = Integer.parseInt(split[1]);
                    blockId = DEFAULT_NAMESPACE + ":" + split[0];
                } catch (NumberFormatException ignored) {
                    blockId = split[0] + ":" + split[1];
                }
            } else {
                try {
                    height = Integer.parseInt(split[2]);
                } catch (NumberFormatException ignored) {
                    continue;
                }
                blockId = split[0] + ":" + split[1];
            }

            if (RESOURCE_LOCATION_PATTERN.matcher(blockId).matches()) {
                parsed.put(blockId, height);
            }
        }
        return parsed;
    }

    static List<String> serialize(Map<String, Integer> entries) {
        return entries.entrySet().stream()
            .map(entry -> entry.getKey() + (entry.getValue() == -1 ? "" : ":" + entry.getValue()))
            .toList();
    }
}
