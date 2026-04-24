package org.hhoao.mc.ironelevators;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.BooleanValue ALLOW_ELEVATING_THROUGH_BLOCKS =
        BUILDER.define("allowElevatingThroughBlocks", true);
    private static final ModConfigSpec.IntValue MAX_TELEPORT_HEIGHT =
        BUILDER.defineInRange("defaultMaxTeleportHeight", 8, 1, 256);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ELEVATOR_BLOCKS_WITH_HEIGHT =
        BUILDER.defineList("elevatorBlocksWithHeight", getDefaultElevatorsBlock(), value -> value instanceof String);
    static final ModConfigSpec SPEC = BUILDER.build();
    private static Map<Block, Integer> blockMaxHeightMap;

    private Config() {
    }

    public static Map<Block, Integer> getElevatorBlockMaxHeightMap() {
        if (blockMaxHeightMap == null) {
            blockMaxHeightMap = resolveElevatorBlockEntries(ElevatorBlockEntries.parse(ELEVATOR_BLOCKS_WITH_HEIGHT.get()));
        }
        return blockMaxHeightMap;
    }

    private static Map<Block, Integer> resolveElevatorBlockEntries(Map<String, Integer> entries) {
        Map<Block, Integer> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries.entrySet()) {
            ResourceLocation blockId;
            try {
                blockId = ResourceLocation.parse(entry.getKey());
            } catch (RuntimeException ignored) {
                continue;
            }
            BuiltInRegistries.BLOCK.getOptional(blockId).ifPresent(block -> resolved.put(block, entry.getValue()));
        }
        return resolved;
    }

    private static List<String> getDefaultElevatorsBlock() {
        return List.of(BuiltInRegistries.BLOCK.getKey(Blocks.IRON_BLOCK).toString());
    }

    public static boolean isAllowElevatingThroughBlocks() {
        return ALLOW_ELEVATING_THROUGH_BLOCKS.get();
    }

    public static int getMaxTeleportHeight() {
        return MAX_TELEPORT_HEIGHT.get();
    }

    public static int setAllowElevatingThroughBlocks(CommandContext<CommandSourceStack> context) {
        boolean value = BoolArgumentType.getBool(context, "value");
        ALLOW_ELEVATING_THROUGH_BLOCKS.set(value);
        context.getSource().sendSuccess(() -> Component.literal("Set allowElevatingThroughBlocks to " + value), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int setMaxTeleportHeight(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "value");
        MAX_TELEPORT_HEIGHT.set(value);
        context.getSource().sendSuccess(() -> Component.literal("Set maxTeleportHeight to " + value), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int addElevatorBlock(CommandContext<CommandSourceStack> context, int height) {
        BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
        Block block = blockInput.getState().getBlock();
        Map<Block, Integer> elevatorBlockMaxHeightMap = new LinkedHashMap<>(getElevatorBlockMaxHeightMap());
        elevatorBlockMaxHeightMap.put(block, height);
        refreshConfig(elevatorBlockMaxHeightMap);
        context.getSource().sendSuccess(() -> Component.literal(
            "Add elevatorBlock: " + block.getDescriptionId() + ", height: " + (height == -1 ? getMaxTeleportHeight() : height)),
            true);
        return Command.SINGLE_SUCCESS;
    }

    public static int addElevatorBlock(CommandContext<CommandSourceStack> context) {
        return addElevatorBlock(context, -1);
    }

    public static int addElevatorBlockWithHeight(CommandContext<CommandSourceStack> context) {
        return addElevatorBlock(context, IntegerArgumentType.getInteger(context, "height"));
    }

    public static int removeElevatorBlock(CommandContext<CommandSourceStack> context) {
        BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
        Block block = blockInput.getState().getBlock();

        Map<Block, Integer> elevatorBlockMaxHeightMap = new LinkedHashMap<>(getElevatorBlockMaxHeightMap());
        elevatorBlockMaxHeightMap.remove(block);
        refreshConfig(elevatorBlockMaxHeightMap);
        context.getSource().sendSuccess(() -> Component.literal("Remove elevatorBlock: " + block.getDescriptionId()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static void refreshConfig(Map<Block, Integer> elevatorBlockMaxHeightMap) {
        blockMaxHeightMap = new LinkedHashMap<>(elevatorBlockMaxHeightMap);
        Map<String, Integer> blockIds = new LinkedHashMap<>();
        for (Map.Entry<Block, Integer> entry : elevatorBlockMaxHeightMap.entrySet()) {
            blockIds.put(BuiltInRegistries.BLOCK.getKey(entry.getKey()).toString(), entry.getValue());
        }
        ELEVATOR_BLOCKS_WITH_HEIGHT.set(ElevatorBlockEntries.serialize(blockIds));
    }

    public static int listElevatorBlocks(CommandSourceStack source) {
        Map<String, Integer> blockIds = new LinkedHashMap<>();
        for (Map.Entry<Block, Integer> entry : getElevatorBlockMaxHeightMap().entrySet()) {
            blockIds.put(BuiltInRegistries.BLOCK.getKey(entry.getKey()).toString(), entry.getValue());
        }

        source.sendSystemMessage(Component.literal(
            "Elevator Blocks: " + String.join(", ", ElevatorBlockEntries.serialize(blockIds))
        ));
        return Command.SINGLE_SUCCESS;
    }

    public static void initialize(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
