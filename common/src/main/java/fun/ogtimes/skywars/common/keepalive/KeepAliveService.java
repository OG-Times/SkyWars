package fun.ogtimes.skywars.common.keepalive;

import fun.ogtimes.skywars.common.instance.InstanceProperties;
import fun.ogtimes.skywars.common.nats.NatsHandler;
import fun.ogtimes.skywars.common.nats.annotation.IncomingPacketHandler;
import fun.ogtimes.skywars.common.nats.packet.PacketListener;
import fun.ogtimes.skywars.common.nats.packet.impl.KeepAlivePacket;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeepAliveService {
    private static final Logger LOGGER = Logger.getLogger(KeepAliveService.class.getName());

    private final NatsHandler nats;
    private final ScheduledExecutorService scheduler;
    private final Map<String, Instant> lastSeen = new ConcurrentHashMap<>();
    private final CopyOnWriteArraySet<KeepAliveListener> listeners = new CopyOnWriteArraySet<>();
    private final Duration interval;
    private final Duration timeout;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledFuture<?> future;
    private final PacketListener packetListener;

    public KeepAliveService(NatsHandler natsHandler, Duration interval, Duration timeout) {
        this.nats = natsHandler;
        this.interval = interval;
        this.timeout = timeout;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "skywars-keepalive"));

        this.packetListener = new PacketListener() {
            @IncomingPacketHandler
            public void onKeepAlive(KeepAlivePacket packet) {
                if (packet != null && packet.properties() != null) {
                    lastSeen.put(packet.properties().getId(), Instant.now());
                    listeners.forEach(l -> l.onInstanceAlive(packet.properties()));
                }
            }
        };
    }

    public void start(InstanceProperties properties) {
        if (running.compareAndSet(false, true)) {
            try {
                nats.registerListener(packetListener);
                try {
                    nats.subscribe();
                    LOGGER.info("KeepAliveService: subscribed to NATS channels");
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "KeepAliveService: failed to subscribe to NATS channels", ex);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to register keepalive listener", ex);
            }

            LOGGER.info("KeepAliveService: starting scheduler (interval=" + interval.toMillis() + "ms, timeout=" + timeout.toMillis() + "ms)");
            future = scheduler.scheduleAtFixedRate(() -> {
                try {
                    nats.sendPacket(new KeepAlivePacket(properties));
                    LOGGER.fine("KeepAliveService: sent keepalive for " + properties.getId());
                    lastSeen.put(properties.getId(), Instant.now());

                    Instant cutoff = Instant.now().minus(timeout);
                    for (Map.Entry<String, Instant> e : lastSeen.entrySet()) {
                        if (e.getValue().isBefore(cutoff)) {
                            listeners.forEach(l -> l.onInstanceOffline(new InstanceProperties(e.getKey(), "", 0)));
                            lastSeen.remove(e.getKey());
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "KeepAlive send failed", ex);
                }
            }, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            if (future != null) future.cancel(true);
            scheduler.shutdownNow();

            try {
                nats.unregisterListener(packetListener);
                LOGGER.info("KeepAliveService: unregistered packet listener from NATS");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "KeepAliveService: failed to unregister packet listener", ex);
            }

            lastSeen.clear();
            listeners.clear();

            LOGGER.info("KeepAliveService: stopped");
        }
    }

    public void sendNow(InstanceProperties properties) {
        nats.sendPacket(new KeepAlivePacket(properties));
        lastSeen.put(properties.getId(), Instant.now());
    }

    public void registerListener(KeepAliveListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(KeepAliveListener listener) {
        listeners.remove(listener);
    }

    public boolean isAlive(String instanceId) {
        Instant i = lastSeen.get(instanceId);
        return i != null && i.isAfter(Instant.now().minus(timeout));
    }

    public Set<String> getAliveInstances() {
        return lastSeen.keySet();
    }

    /**
     * Start only listening for incoming KeepAlivePacket without sending periodic keepalives.
     * Useful for proxies that only need to observe instance keepalives.
     */
    public void startListening() {
        try {
            nats.registerListener(packetListener);
            try {
                nats.subscribe();
                LOGGER.info("KeepAliveService: subscribed to NATS channels (listen-only)");
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "KeepAliveService: failed to subscribe to NATS channels (listen-only)", ex);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to register keepalive listener (listen-only)", ex);
        }
    }

    /**
     * Stop listening for keepalive packets without affecting the send scheduler.
     */
    public void stopListening() {
        try {
            nats.unregisterListener(packetListener);
            LOGGER.info("KeepAliveService: unregistered keepalive listener (listen-only)");
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "KeepAliveService: failed to unregister keepalive listener (listen-only)", ex);
        }
    }
}
