package org.hhoao.mc.ironelevators;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.ConfigValue<Boolean> ALLOW_ELEVATING_THROUGH_BLOCKS =
        BUILDER.define("allowElevatingThroughBlocks", true);
    private static final ForgeConfigSpec.ConfigValue<Integer> DEFAULT_MAX_TELEPORT_HEIGHT =
        BUILDER.define("defaultMaxTeleportHeight", 8);
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ELEVATOR_BLOCKS_WITH_HEIGHT =
        BUILDER.defineList("elevatorBlocksWithHeight", getDefaultElevatorsBlock(), (o) -> true);
    static final ForgeConfigSpec SPEC = BUILDER.build();
    private static Map<Block, Integer> blockMaxHeightMap;

    public static Map<Block, Integer> getElevatorBlockMaxHeightMap() {
        if (blockMaxHeightMap == null) {
            blockMaxHeightMap = new HashMap<>();
            for (String blockHeight : ELEVATOR_BLOCKS_WITH_HEIGHT.get()) {
                String[] split = blockHeight.split(":");
                if (split.length > 2) {
                    String substring = blockHeight.substring(0, blockHeight.lastIndexOf(':'));
                        blockMaxHeightMap.put(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(substring)), Integer.valueOf(split[2]));
                    } else {
                        blockMaxHeightMap.put(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockHeight)), -1);
                    }
            }
        }
        return blockMaxHeightMap;
    }

    public static Boolean isAllowElevatingThroughBlocks() {
        return ALLOW_ELEVATING_THROUGH_BLOCKS.get();
    }

    public static Integer getDefaultMaxTeleportHeight() {
        return DEFAULT_MAX_TELEPORT_HEIGHT.get();
    }

    public static int setAllowElevatingThroughBlocks(CommandContext<CommandSource> context) {
        boolean value = BoolArgumentType.getBool(context, "value");
        ALLOW_ELEVATING_THROUGH_BLOCKS.set(value);
        context.getSource().sendFeedback(new StringTextComponent("Set allowElevatingThroughBlocks to " + value), true);
        return Command.SINGLE_SUCCESS;
    }

    private static List<String> getDefaultElevatorsBlock() {
        ArrayList<String> elevators = new ArrayList<>();
        elevators.add(ForgeRegistries.BLOCKS.getKey(Blocks.IRON_BLOCK).toString());
        return elevators;
    }

    public static int setMaxTeleportHeight(CommandContext<CommandSource> context) {
        int value = IntegerArgumentType.getInteger(context, "value");
        DEFAULT_MAX_TELEPORT_HEIGHT.set(value);
        context.getSource().sendFeedback(new StringTextComponent("Set maxTeleportHeight to " + value), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int addElevatorBlock(CommandContext<CommandSource> context, int height) {
        BlockStateInput blockInput = BlockStateArgument.getBlockState(context, "block");
        Block block = blockInput.getState().getBlock();
        Map<Block, Integer> elevatorBlockMaxHeightMap = getElevatorBlockMaxHeightMap();
        elevatorBlockMaxHeightMap.put(block, height);
        refreshConfig(elevatorBlockMaxHeightMap);
        context.getSource().sendFeedback(new StringTextComponent(
            String.format("Add elevatorBlock: %s, height: %s", block.getTranslationKey(), height == -1 ? getDefaultMaxTeleportHeight() : height)),
            true);
        return Command.SINGLE_SUCCESS;
    }

    public static int addElevatorBlock(CommandContext<CommandSource> context) {
        return addElevatorBlock(context, -1);
    }

    public static int addElevatorBlockWithHeight(CommandContext<CommandSource> context) {
        int height = IntegerArgumentType.getInteger(context, "height");
        return addElevatorBlock(context, height);
    }

    public static int removeElevatorBlock(CommandContext<CommandSource> context) {
        BlockStateInput blockInput = BlockStateArgument.getBlockState(context, "block");
        Block block= blockInput.getState().getBlock();

        Map<Block, Integer> elevatorBlockMaxHeightMap = getElevatorBlockMaxHeightMap();
        elevatorBlockMaxHeightMap.remove(block);
        refreshConfig(elevatorBlockMaxHeightMap);
        context.getSource().sendFeedback(new StringTextComponent("Remove elevatorBlock: " + block.getTranslationKey()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static void refreshConfig(Map<Block, Integer> elevatorBlockMaxHeightMap) {
        ELEVATOR_BLOCKS_WITH_HEIGHT.set(elevatorBlockMaxHeightMap
            .entrySet()
            .stream()
            .map(blockHeight ->
                ForgeRegistries.BLOCKS.getKey(blockHeight.getKey()).toString() +
                    (blockHeight.getValue() == -1 ? "" : ":" + blockHeight.getValue())
            )
                .collect(Collectors.toList()));
    }

    public static int listElevatorBlocks(CommandSource source) {
        Map<Block, Integer> elevatorBlockMaxHeightMap = getElevatorBlockMaxHeightMap();
        source.sendFeedback(
            new StringTextComponent(String.format("Elevator Blocks: %s",
                String.join(", ",
                    elevatorBlockMaxHeightMap.entrySet().stream()
                        .map(block ->
                            ForgeRegistries.BLOCKS.getKey(block.getKey()).toString()+
                                (block.getValue() == -1 ? getDefaultMaxTeleportHeight() : ":" + block.getValue()))
                        .toArray(String[]::new)))), false);
        return Command.SINGLE_SUCCESS;
    }

    public static void initialize(ModLoadingContext modLoadingContext) {
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
