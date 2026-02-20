package fun.ogtimes.skywars.common;

import fun.ogtimes.skywars.common.keepalive.KeepAliveService;
import fun.ogtimes.skywars.common.nats.NatsHandler;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

import java.io.IOException;
import java.time.Duration;

public class SkyWarsCommon {
    private final NatsHandler natsHandler;
    private final KeepAliveService keepAliveService;

    public SkyWarsCommon(Options natsOptions) throws IOException, InterruptedException {
        Connection connection = Nats.connect(natsOptions);
        this.natsHandler = new NatsHandler(connection);
        this.keepAliveService = new KeepAliveService(this.natsHandler, Duration.ofSeconds(5), Duration.ofSeconds(15));
    }

    public SkyWarsCommon(Connection connection) {
        this.natsHandler = new NatsHandler(connection);
        this.keepAliveService = new KeepAliveService(this.natsHandler, Duration.ofSeconds(5), Duration.ofSeconds(15));
    }

    public NatsHandler nats() {
        return natsHandler;
    }

    public KeepAliveService keepAlive() {
        return keepAliveService;
    }

    public void destroy() {
        if (keepAliveService != null) keepAliveService.stop();
        if (natsHandler != null) natsHandler.close();
    }
}
