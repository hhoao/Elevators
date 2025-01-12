package org.hhoao.mc.ironelevators.net.common;

import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.List;

/**
 * The interface Dispatcher.
 */
public interface Dispatcher {
    /**
     * Send.
     *
     * @param <MSG>  the type parameter
     * @param target the target
     * @param message the message
     */
    <MSG> void send(PacketDistributor.PacketTarget target, MSG message);

    /**
     * Send to server.
     *
     * @param <MSG>  the type parameter
     * @param message the message
     */
    <MSG> void sendToServer(MSG message);

    /**
     * Send to.
     *
     * @param <MSG>  the type parameter
     * @param message the message
     * @param manager the manager
     * @param direction the direction
     */
    <MSG> void sendTo(MSG message, NetworkManager manager, NetworkDirection direction);

    /**
     * Register message handler string.
     *
     * @param <T>   the type parameter
     * @param requestHandler the request handler
     * @param messageClass the message class
     * @param networkDirection the network direction
     * @return handlerId, used to reRegistry
     */
    <T extends Message> String registerMessageHandler(MessageHandler<T> requestHandler,
                                                      Class<T> messageClass,
                                                      NetworkDirection networkDirection);

    /**
     * Register message handler string.
     *
     * @param <T>   the type parameter
     * @param requestHandler the request handler
     * @param messageClass the message class
     * @param networkDirectionList the network direction list
     * @return the string
     */
    <T extends Message> String registerMessageHandler(MessageHandler<T> requestHandler,
                                                      Class<T> messageClass,
                                                      List<NetworkDirection> networkDirectionList);

    /**
     * Remove message handler.
     *
     * @param id the id
     */
    void removeMessageHandler(String id);
}
