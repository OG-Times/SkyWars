package fun.ogtimes.skywars.common.nats.packet.impl;

import fun.ogtimes.skywars.common.instance.InstanceProperties;
import fun.ogtimes.skywars.common.nats.packet.Packet;

public record InstanceStartPacket(InstanceProperties properties) implements Packet { }