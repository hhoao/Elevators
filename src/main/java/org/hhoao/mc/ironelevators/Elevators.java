package org.hhoao.mc.ironelevators;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Elevators.MODID)
public class Elevators {
    public static final String MODID = "elevators";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ElevatorController ELEVATOR_CONTROLLER = new ElevatorController();

    public static ElevatorController getElevatorController() {
        return ELEVATOR_CONTROLLER;
    }

    public Elevators(IEventBus modBus, ModContainer container) {
        LOGGER.info("Elevators enable");
        Config.initialize(container);
        NeoForge.EVENT_BUS.register(ForgeEventHandler.class);
    }
}
