package org.hhoao.mc.ironelevators;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.ConfigValue<Boolean> ALLOW_ELEVATING_THROUGH_BLOCKS =
        BUILDER.define("allowElevatingThroughBlocks", true);
    private static final ForgeConfigSpec.ConfigValue<Integer> MAX_TELEPORT_HEIGHT =
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
                if (split.length > 1) {
                    blockMaxHeightMap.put(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0])), Integer.valueOf(split[1]));
                } else {
                    blockMaxHeightMap.put(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(split[0])), -1);
                }
            }
        }
        return blockMaxHeightMap;
    }

    public static Boolean isAllowElevatingThroughBlocks() {
        return ALLOW_ELEVATING_THROUGH_BLOCKS.get();
    }

    public static Integer getMaxTeleportHeight() {
        return MAX_TELEPORT_HEIGHT.get();
    }

    private static List<String> getDefaultElevatorsBlock() {
        ArrayList<String> elevators = new ArrayList<>();
        elevators.add(ForgeRegistries.BLOCKS.getKey(Blocks.IRON_BLOCK).getPath());
        return elevators;
    }


    public static int setAllowElevatingThroughBlocks(CommandContext<CommandSourceStack> context) {
        boolean value = BoolArgumentType.getBool(context, "value");
        ALLOW_ELEVATING_THROUGH_BLOCKS.set(value);
        context.getSource().sendSuccess(new TextComponent("Set allowElevatingThroughBlocks to " + value), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int setMaxTeleportHeight(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "value");
        MAX_TELEPORT_HEIGHT.set(value);
        context.getSource().sendSuccess(new TextComponent("Set maxTeleportHeight to " + value), true);
        return Command.SINGLE_SUCCESS;
    }

    public static int addElevatorBlock(CommandContext<CommandSourceStack> context, int height) {
        BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
        Block block = blockInput.getState().getBlock();
        Map<Block, Integer> elevatorBlockMaxHeightMap = getElevatorBlockMaxHeightMap();
        elevatorBlockMaxHeightMap.put(block, height);
        refreshConfig(elevatorBlockMaxHeightMap);
        context.getSource().sendSuccess(new TextComponent(
            String.format("Add elevatorBlock: %s, height: %s", block.getDescriptionId(), height == -1 ? getMaxTeleportHeight() : height)),
            true);
        return Command.SINGLE_SUCCESS;
    }

    public static int addElevatorBlock(CommandContext<CommandSourceStack> context) {
        return addElevatorBlock(context, -1);
    }

    public static int addElevatorBlockWithHeight(CommandContext<CommandSourceStack> context) {
        int height = IntegerArgumentType.getInteger(context, "height");
        return addElevatorBlock(context, height);
    }

    public static int removeElevatorBlock(CommandContext<CommandSourceStack> context) {
        BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
        Block block= blockInput.getState().getBlock();

        Map<Block, Integer> elevatorBlockMaxHeightMap = getElevatorBlockMaxHeightMap();
        elevatorBlockMaxHeightMap.remove(block);
        refreshConfig(elevatorBlockMaxHeightMap);
        context.getSource().sendSuccess(new TextComponent("Remove elevatorBlock: " + block.getDescriptionId()), true);
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
            .toList());
    }

    public static int listElevatorBlocks(CommandSourceStack source) {
        Map<Block, Integer> elevatorBlockMaxHeightMap = getElevatorBlockMaxHeightMap();
        source.sendSuccess(
            new TextComponent(String.format("Elevator Blocks: %s",
                String.join(", ",
                    elevatorBlockMaxHeightMap.entrySet().stream()
                        .map(block ->
                            ForgeRegistries.BLOCKS.getKey(block.getKey()).toString()+
                                (block.getValue() == -1 ? "" : ":" + block.getValue()))
                        .toArray(String[]::new)))), false);
        return Command.SINGLE_SUCCESS;
    }

    public static void initialize(ModLoadingContext modLoadingContext) {
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
