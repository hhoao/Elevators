package org.hhoao.mc.ironelevators;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hhoao.mc.ironelevators.net.common.DefaultDispatcher;
import org.hhoao.mc.ironelevators.net.common.Dispatcher;
import org.hhoao.mc.ironelevators.registry.MessageHandlerRegistry;

@Mod(Elevators.MODID)
public class Elevators {
    public static final String MODID = "elevators";
    private static final Logger LOGGER = LogManager.getLogger(Elevators.class);
    private static ElevatorController elevatorController;
    private static Dispatcher dispatcher;

    public static ElevatorController getElevatorController() {
        return elevatorController;
    }

    public static Dispatcher getDispatcher() {
        return dispatcher;
    }

    public Elevators() {
        LOGGER.info("Elevators enable");
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        dispatcher = DefaultDispatcher.getInstance();
        Config.initialize(modLoadingContext);
        forgeEventBus.register(new ForgeEventHandler());
        elevatorController = new ElevatorController();
        MessageHandlerRegistry.initialize();
    }

    public static ResourceLocation location(String general) {
        return new ResourceLocation(MODID, general);
    }
}
