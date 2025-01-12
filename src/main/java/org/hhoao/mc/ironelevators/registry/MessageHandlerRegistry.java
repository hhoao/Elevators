package org.hhoao.mc.ironelevators.registry;

import net.minecraftforge.network.NetworkDirection;
import org.hhoao.mc.ironelevators.net.common.DefaultDispatcher;
import org.hhoao.mc.ironelevators.net.ElevatorTeleportMessage;
import org.hhoao.mc.ironelevators.net.ElevatorTeleportMessageHandler;

public class MessageHandlerRegistry {
    public static void initialize() {
        DefaultDispatcher instance = DefaultDispatcher.getInstance();
        instance.registerMessageHandler(
            new ElevatorTeleportMessageHandler(),
            ElevatorTeleportMessage.class,
            NetworkDirection.PLAY_TO_SERVER);
    }
}
