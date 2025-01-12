package org.hhoao.mc.ironelevators.net.common;

/**
 * The interface Message.
 */
public interface Message {
    /**
     * Gets request type.
     *
     * @return the request type
     */
    default MessageType getRequestType() { return MessageType.GET;}
}
