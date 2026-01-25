package fun.ogtimes.skywars.common.nats;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fun.ogtimes.skywars.common.nats.annotation.IncomingPacketHandler;
import fun.ogtimes.skywars.common.nats.packet.Packet;
import fun.ogtimes.skywars.common.nats.packet.PacketListener;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class NatsHandler implements Closeable {
    private static final Logger LOGGER = Logger.getLogger(NatsHandler.class.getName());

    private final Connection connection;

    private final Gson gson = new GsonBuilder().create();

    private final Multimap<Object, Method> listeners =
            MultimapBuilder.hashKeys().arrayListValues().build();

    private final Set<String> channels = ConcurrentHashMap.newKeySet();
    private final Multimap<String, Object> channelToListener =
            MultimapBuilder.hashKeys().arrayListValues().build();

    private final Set<String> subscribedChannels = ConcurrentHashMap.newKeySet();

    private volatile Dispatcher dispatcher;

    public synchronized void subscribe() {
        if (channels.isEmpty()) {
            throw new IllegalStateException("No channels to subscribe to. Register listeners first.");
        }
        if (dispatcher != null) return;
        try {
            dispatcher = connection.createDispatcher(this::handleMessage);
            for (String channel : channels) {
                dispatcher.subscribe(channel);
                subscribedChannels.add(channel);
            }
            LOGGER.info("Subscribed to channels: " + channels);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Subscription failed", e);
        }
    }

    public void registerListener(PacketListener listener) {
        Set<Method> packetHandlerMethods =
                Arrays.stream(listener.getClass().getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(IncomingPacketHandler.class))
                        .filter(m -> m.getParameterCount() == 1)
                        .collect(Collectors.toSet());

        if (packetHandlerMethods.isEmpty()) {
            LOGGER.warning("No valid @IncomingPacketHandler in " + listener.getClass().getName());
            return;
        }

        for (Method method : packetHandlerMethods) {
            Class<?> parameterType = method.getParameterTypes()[0];
            String channel = "skywars@" + parameterType.getName();
            channels.add(channel);

            synchronized (this) {
                listeners.put(listener, method);
                channelToListener.put(channel, listener);
            }

            if (dispatcher != null && !subscribedChannels.contains(channel)) {
                try {
                    dispatcher.subscribe(channel);
                    subscribedChannels.add(channel);
                    LOGGER.info("Dynamically subscribed to channel: " + channel);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to dynamically subscribe to channel " + channel, e);
                }
            }

            try {
                method.setAccessible(true);
            } catch (Exception ignored) { }
        }

        LOGGER.info("Registered listener: " + listener.getClass().getName());
    }

    public void unregisterListener(PacketListener listener) {
        Set<Method> methods;
        synchronized (this) {
            methods = new HashSet<>(listeners.get(listener));
        }

        for (Method method : methods) {
            Class<?> parameterType = method.getParameterTypes()[0];
            String channel = "skywars@" + parameterType.getName();

            synchronized (this) {
                listeners.remove(listener, method);
                channelToListener.remove(channel, listener);
                Collection<?> remaining = channelToListener.get(channel);
                if (remaining.isEmpty()) {
                    channels.remove(channel);
                    try {
                        if (dispatcher != null && subscribedChannels.contains(channel)) {
                            dispatcher.unsubscribe(channel);
                            subscribedChannels.remove(channel);
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to unsubscribe channel " + channel, e);
                    }
                }
            }
        }

        LOGGER.info("Unregistered listener: " + listener.getClass().getName());
    }

    public void sendPacket(Packet packet) {
        String channel = "skywars@" + packet.getClass().getName();
        try {
            connection.publish(channel, gson.toJson(packet).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send packet to channel: " + channel, e);
        }
    }

    private void handleMessage(Message message) {
        String channel = message.getSubject();
        String data = new String(message.getData(), StandardCharsets.UTF_8);

        Collection<Object> listenerObjs;
        synchronized (this) {
            listenerObjs = new ArrayList<>(channelToListener.get(channel));
        }

        for (Object listenerObj : listenerObjs) {
            Collection<Method> methods;
            synchronized (this) {
                methods = new ArrayList<>(listeners.get(listenerObj));
            }

            for (Method method : methods) {
                if (!method.isAnnotationPresent(IncomingPacketHandler.class)) continue;

                Class<?> paramType = method.getParameterTypes()[0];
                String expectedChannel = "skywars@" + paramType.getName();
                if (!expectedChannel.equals(channel)) continue;

                try {
                    Object packet = gson.fromJson(data, paramType);
                    method.invoke(listenerObj, packet);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE,
                            "Failed to invoke handler " + method.getName() + " on " +
                                    listenerObj.getClass().getName(), e);
                }
            }
        }
    }

    @Override
    @SneakyThrows
    public synchronized void close() {
        if (dispatcher != null) {
            for (String ch : new ArrayList<>(subscribedChannels)) {
                try {
                    dispatcher.unsubscribe(ch);
                } catch (Exception ignored) {
                }
            }
            subscribedChannels.clear();
        }
        connection.close();
        LOGGER.info("NATS connection closed.");
    }
}