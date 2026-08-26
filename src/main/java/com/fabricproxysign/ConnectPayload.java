package com.fabricproxysign;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 通过 BungeeCord 插件消息通道（bungeecord:main）向 Velocity 发送 "Connect" 指令的负载。
 * 数据格式：writeUTF("Connect") + writeUTF(serverName)。
 */
public record ConnectPayload(byte[] data) implements CustomPacketPayload {
    public static final Type<ConnectPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bungeecord", "main"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConnectPayload> CODEC =
            StreamCodec.of(ConnectPayload::encode, ConnectPayload::decode);

    public static void encode(RegistryFriendlyByteBuf buf, ConnectPayload payload) {
        buf.writeBytes(payload.data);
    }

    public static ConnectPayload decode(RegistryFriendlyByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new ConnectPayload(data);
    }

    /** 在 clientbound play 方向注册该通道，使服务端能够发送此插件消息。 */
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
