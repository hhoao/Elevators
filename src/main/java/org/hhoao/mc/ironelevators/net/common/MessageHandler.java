package org.hhoao.mc.ironelevators.net.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The interface Message handler.
 *
 * @param <T>  the type parameter
 */
public interface MessageHandler<T extends Message> {
    /**
     * Handle.
     *
     * @param message the message
     * @param contextSupplier the context supplier
     */
    void handle(T message, Supplier<NetworkEvent.Context> contextSupplier);

    /**
     * Encode.
     *
     * @param message the message
     * @param buffer the buffer
     */
    void encode(T message, FriendlyByteBuf buffer);

    /**
     * Decode t.
     *
     * @param buffer the buffer
     * @return the t
     */
    T decode(FriendlyByteBuf buffer);

    /**
     * Gets message types.
     *
     * @return the message types
     */
    default Set<MessageType> getMessageTypes() {
        HashSet<MessageType> messageTypes = new HashSet<>();
        messageTypes.add(MessageType.GET);
        return messageTypes;
    }
}
