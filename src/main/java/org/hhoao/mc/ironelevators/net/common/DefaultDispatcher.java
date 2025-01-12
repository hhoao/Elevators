package org.hhoao.mc.ironelevators.net.common;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.hhoao.mc.ironelevators.Elevators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DefaultDispatcher implements Dispatcher {
    private static final String PROTOCOL_VERSION = "1.0";
    private final SimpleChannel CHANNEL =
        NetworkRegistry.ChannelBuilder.named(Elevators.location( "general"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    private Set<DirectionalMessageInfo<?>> registeredMessages = new HashSet<>();

    private final Map<DirectionalMessageInfo<?>, List<MessageHandlerWrapper<? extends Message>>> requestHandlerMap = new HashMap<>();

    private int messageNumber = 1;

    private static final class InstanceHolder {
        static final DefaultDispatcher instance = new DefaultDispatcher();
    }

    public static DefaultDispatcher getInstance() {
        return InstanceHolder.instance;
    }

    private DefaultDispatcher() {}

    @Override
    public <MSG> void send(PacketDistributor.PacketTarget target, MSG message) {
        CHANNEL.send(target, message);
    }

    @Override
    public <MSG> void sendToServer(MSG message)
    {
        CHANNEL.sendToServer(message);
    }

    @Override
    public <MSG> void sendTo(MSG message, NetworkManager manager, NetworkDirection direction)
    {
        CHANNEL.sendTo(message, manager, direction);
    }

    @Override
    public <T extends Message> String registerMessageHandler(org.hhoao.mc.ironelevators.net.common.MessageHandler<T> requestHandler,
                                                             Class<T> messageClass,
                                                             NetworkDirection networkDirection) {
        return registerMessageHandler(requestHandler, messageClass, Collections.singletonList(networkDirection));
    }

    @Override
    public <T extends Message> String registerMessageHandler(org.hhoao.mc.ironelevators.net.common.MessageHandler<T> requestHandler,
                                                             Class<T> messageClass,
                                                             List<NetworkDirection> networkDirectionList) {
        String handlerId = UUID.randomUUID().toString();
        for (NetworkDirection networkDirection : networkDirectionList) {
            DirectionalMessageInfo<T> tDirectionMessage = new DirectionalMessageInfo<>(messageClass, networkDirection);
            if (!registeredMessages.contains(tDirectionMessage)) {
                registeredMessages.add(tDirectionMessage);
                CHANNEL.messageBuilder(
                        messageClass, messageNumber++, networkDirection)
                    .encoder(requestHandler::encode)
                    .decoder(requestHandler::decode)
                    .consumer(new InternalMessageHandler<>())
                    .add();
            }
            MessageHandlerWrapper<T> eventHandlerWrapper =
                new MessageHandlerWrapper<>(handlerId, requestHandler);

            List<MessageHandlerWrapper<? extends Message>> messageHandlerWrappers =
                requestHandlerMap.computeIfAbsent(tDirectionMessage, (rt) -> new ArrayList<>());

            messageHandlerWrappers.add(eventHandlerWrapper);
        }


        return handlerId;
    }

    @Override
    public void removeMessageHandler(String id) {
        Collection<List<MessageHandlerWrapper<? extends Message>>> values = requestHandlerMap.values();
        for (List<MessageHandlerWrapper<? extends Message>> value : values) {
            value.removeIf(eventHandlerWrapper -> eventHandlerWrapper.getId().equals(id));
        }
    }

    @SuppressWarnings("unchecked")
    private  <T extends Message> void process(T request, Supplier<NetworkEvent.Context> contextSupplier) {
        MessageType requestType = request.getRequestType();
        if (requestType == null) {
            throw new RuntimeException("Request must not be null");
        }
        NetworkDirection direction = contextSupplier.get().getDirection();

        DirectionalMessageInfo<? extends Message> directionMessageInfo =
            new DirectionalMessageInfo<>(request.getClass(), direction);

        List<MessageHandlerWrapper<? extends Message>> messageHandlerWrappers =
            requestHandlerMap.computeIfAbsent(directionMessageInfo, (rt) -> new ArrayList<>());
        List<MessageHandlerWrapper<? extends Message>> requestHandlers = messageHandlerWrappers.stream()
            .filter(messageHandlerWrapper ->
                messageHandlerWrapper.getMessageTypes().contains(requestType)).collect(Collectors.toList());

        for (org.hhoao.mc.ironelevators.net.common.MessageHandler<? extends Message> requestHandler : requestHandlers) {
            if (requestHandler != null) {
                ((org.hhoao.mc.ironelevators.net.common.MessageHandler<T>)requestHandler).handle(request, contextSupplier);
            }
        }
    }

    private static class MessageHandlerWrapper<T extends Message>
        implements org.hhoao.mc.ironelevators.net.common.MessageHandler<T> {
        private final String id;
        private final org.hhoao.mc.ironelevators.net.common.MessageHandler<T> innerMessageHandler;

        public MessageHandlerWrapper(String id, org.hhoao.mc.ironelevators.net.common.MessageHandler<T> innerMessageHandler) {
            this.id = id;
            this.innerMessageHandler = innerMessageHandler;
        }

        public String getId() {
            return id;
        }

        @Override
        public void handle(T message, Supplier<NetworkEvent.Context> contextSupplier) {
            innerMessageHandler.handle(message, contextSupplier);
        }

        @Override
        public void encode(T message, PacketBuffer buffer) {
            innerMessageHandler.encode(message, buffer);
        }

        @Override
        public T decode(PacketBuffer buffer) {
            return innerMessageHandler.decode(buffer);
        }
    }

    private static class DirectionalMessageInfo<T extends Message> {
        private final Class<T> messageCls;
        private final NetworkDirection networkDirection;

        public DirectionalMessageInfo(Class<T> messageCls, NetworkDirection networkDirection) {
            this.messageCls = messageCls;
            this.networkDirection = networkDirection;
        }

        public Class<T> getMessageCls() {
            return messageCls;
        }

        public NetworkDirection getNetworkDirection() {
            return networkDirection;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            DirectionalMessageInfo<?> that = (DirectionalMessageInfo<?>) o;
            return Objects.equals(messageCls, that.messageCls) && networkDirection == that.networkDirection;
        }

        @Override
        public int hashCode() {
            return Objects.hash(messageCls, networkDirection);
        }
    }

    private static class InternalMessageHandler<MSG extends Message> implements BiConsumer<MSG, Supplier<NetworkEvent.Context>> {
        @Override
        public void accept(MSG msg, Supplier<NetworkEvent.Context> contextSupplier) {
            DefaultDispatcher.getInstance().process(msg, contextSupplier);
        }
    }
}
