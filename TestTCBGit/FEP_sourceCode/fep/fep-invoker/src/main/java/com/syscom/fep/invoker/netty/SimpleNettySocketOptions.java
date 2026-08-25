package com.syscom.fep.invoker.netty;

import com.syscom.fep.common.log.LogHelperFactory;

import java.io.IOException;
import java.net.SocketOption;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class SimpleNettySocketOptions {
    private static final Map<String, SocketOption<?>> socketOptionMap = Collections.synchronizedMap(new HashMap<>());

    static {
        supportedOptions();
    }

    private SimpleNettySocketOptions() {}

    public static <T> SocketOption<T> getSocketOption(SimpleNettySocketOptionName simpleNettySocketOptionName) {
        return (SocketOption<T>) socketOptionMap.get(simpleNettySocketOptionName.name());
    }

    private static Set<SocketOption<?>> supportedOptions() {
        try (AsynchronousSocketChannel socket = AsynchronousSocketChannel.open();) {
            Set<SocketOption<?>> options = socket.supportedOptions();
            LogHelperFactory.getTraceLogger().trace("Supported Socket Options: ", options);
            for (SocketOption<?> option : options) {
                socketOptionMap.put(option.name(), option);
            }
            return options;
        } catch (IOException e) {
            LogHelperFactory.getTraceLogger().warn("Get Supported Socket Options with exception occur, ", e.getMessage());
        }
        return Collections.emptySet();
    }
}
