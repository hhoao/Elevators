package org.hhoao.mc.ironelevators;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;


public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.ConfigValue<Boolean> ALLOW_ELEVATING_THROUGH_BLOCKS = BUILDER.define("allowElevatingThroughBlocks", true);
    private static final ForgeConfigSpec.ConfigValue<Integer> MAX_TELEPORT_HEIGHT = BUILDER.define("maxTeleportHeight", 8);
    private static final ForgeConfigSpec.ConfigValue<String> ELEVATOR_BLOCK = BUILDER.define("elevatorBlock", ForgeRegistries.BLOCKS.getKey(Blocks.IRON_BLOCK).getPath());
    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static Block getElevatorBlock() {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(ELEVATOR_BLOCK.get()));
    }

    public static Boolean isAllowElevatingThroughBlocks() {
        return ALLOW_ELEVATING_THROUGH_BLOCKS.get();
    }

    public static Integer getMaxTeleportHeight() {
        return MAX_TELEPORT_HEIGHT.get();
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

    public static int setElevatorBlock(CommandContext<CommandSourceStack> context) {
        String blockName = StringArgumentType.getString(context, "value");
        if (ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(blockName))) {
            ELEVATOR_BLOCK.set(blockName);
            context.getSource().sendSuccess(new TextComponent("Set elevatorBlock to " + blockName), true);
            return Command.SINGLE_SUCCESS;
        } else {
            context.getSource().sendFailure(new TextComponent("Block not found: " + blockName));
            return 0;
        }
    }

    public static void initialize(ModLoadingContext modLoadingContext) {
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
