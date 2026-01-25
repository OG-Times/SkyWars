package fun.ogtimes.skywars.common.keepalive;

import fun.ogtimes.skywars.common.instance.InstanceProperties;

public interface KeepAliveListener {
    void onInstanceAlive(InstanceProperties properties);
    void onInstanceOffline(InstanceProperties properties);
}

