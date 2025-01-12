package org.hhoao.mc.ironelevators.net;

import org.hhoao.mc.ironelevators.net.common.Message;

public class ElevatorTeleportMessage implements Message {
    private final boolean up;

    public ElevatorTeleportMessage(boolean up) {
        this.up = up;
    }

    public boolean isUp() {
        return up;
    }
}
