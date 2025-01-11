package org.hhoao.mc.ironelevators;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Elevators.MODID)
public class Elevators {
    public static final String MODID = "elevators";
    private static final Logger LOGGER = LogManager.getLogger(Elevators.class);
    private static ElevatorController elevatorController;

    public static ElevatorController getElevatorController() {
        return elevatorController;
    }

    public Elevators() {
        LOGGER.info("Elevators enable");
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        Config.initialize(modLoadingContext);
        forgeEventBus.register(new ForgeEventHandler());
        elevatorController = new ElevatorController();
    }
}
