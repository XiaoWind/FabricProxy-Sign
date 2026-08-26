package com.fabricproxysign;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.BlockHitResult;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

public class SignClickHandler implements UseBlockCallback {
    private final FabricProxySignConfig config;
    private final Map<ServerPlayer, Long> lastUse = new WeakHashMap<>();

    public SignClickHandler(FabricProxySignConfig config) {
        this.config = config;
    }

    @Override
    public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        // 只处理主手；潜行右键留给管理员编辑告示牌
        if (hand != InteractionHand.MAIN_HAND || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        if (!(world.getBlockEntity(pos) instanceof SignBlockEntity sign)) {
            return InteractionResult.PASS;
        }

        // 原版方法：判断玩家面对的是告示牌正面还是背面
        boolean front = sign.isFacingFrontText(player);
        String serverName = readServerName(sign, front);
        if (serverName == null || serverName.isBlank()) {
            return InteractionResult.PASS;
        }

        long now = System.currentTimeMillis();
        Long last = lastUse.get(serverPlayer);
        if (last != null && now - last < config.cooldownMillis) {
            // 冷却中：吞掉本次点击，避免连点
            return InteractionResult.SUCCESS;
        }
        lastUse.put(serverPlayer, now);

        sendToServer(serverPlayer, serverName);
        // 返回 SUCCESS 会取消原版处理（避免打开告示牌编辑界面）
        return InteractionResult.SUCCESS;
    }

    private String readServerName(SignBlockEntity sign, boolean front) {
        if (config.serverNameLine < 0 || config.serverNameLine >= 4) {
            return null;
        }
        SignText text = front ? sign.getFrontText() : sign.getBackText();
        return readLine(text, config.serverNameLine);
    }

    private String readLine(SignText text, int index) {
        Component line = text.getMessage(index, false);
        String value = line.getString().trim();
        if (!config.stripPrefix.isEmpty() && value.startsWith(config.stripPrefix)) {
            value = value.substring(config.stripPrefix.length()).trim();
        }
        return value;
    }

    private void sendToServer(ServerPlayer player, String serverName) {
        byte[] payload = buildConnectPayload(serverName);
        boolean canSend = ServerPlayNetworking.canSend(player, ConnectPayload.TYPE);
        ServerPlayNetworking.send(player, new ConnectPayload(payload));

        if (canSend) {
            FabricProxySignMod.LOGGER.info("[FabricProxy-Sign] 玩家 {} 请求进入服务器 {}",
                    player.getName().getString(), serverName);
        } else {
            FabricProxySignMod.LOGGER.warn("[FabricProxy-Sign] 无法向玩家 {} 发送传送请求：通道 {} 未注册。"
                            + " 请确认 velocity.toml 中 bungee-plugin-message-channel = true，且大厅服已安装 FabricProxy-Lite。",
                    player.getName().getString(), ConnectPayload.TYPE.id());
        }
    }

    /** 用 DataOutputStream.writeUTF 生成 BungeeCord 插件消息：writeUTF("Connect") + writeUTF(serverName)。 */
    private static byte[] buildConnectPayload(String serverName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeUTF("Connect");
            out.writeUTF(serverName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to build Connect payload", e);
        }
        return baos.toByteArray();
    }
}
