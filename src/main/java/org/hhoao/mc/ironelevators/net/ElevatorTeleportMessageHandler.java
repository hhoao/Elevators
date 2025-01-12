package org.hhoao.mc.ironelevators.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.hhoao.mc.ironelevators.ElevatorController;
import org.hhoao.mc.ironelevators.Elevators;
import org.hhoao.mc.ironelevators.net.common.MessageHandler;

import java.util.function.Supplier;

public class ElevatorTeleportMessageHandler implements MessageHandler<ElevatorTeleportMessage> {
    @Override
    public void handle(ElevatorTeleportMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        ServerPlayer player = contextSupplier.get().getSender();
        if (player != null) {
            boolean up = message.isUp();
            ElevatorController elevatorController =
                Elevators.getElevatorController();
            if (up) {
                if (elevatorController.tryTeleport(player, true)) {
                    player.setOnGround(false);
                }
            } else {
                elevatorController.tryTeleport(player, false);
            }
        }
    }

    @Override
    public void encode(ElevatorTeleportMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isUp());
    }

    @Override
    public ElevatorTeleportMessage decode(FriendlyByteBuf buffer) {
        return new ElevatorTeleportMessage(buffer.readBoolean());
    }
}
