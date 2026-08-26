package com.fabricproxysign;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricProxySignMod implements ModInitializer {
    public static final String MOD_ID = "fabricproxy-sign";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        FabricProxySignConfig config = FabricProxySignConfig.load();

        // 注册 S2C 插件消息通道（bungeecord:main），用于向 Velocity 发送 Connect
        ConnectPayload.register();

        // 注册右键方块事件
        UseBlockCallback.EVENT.register(new SignClickHandler(config));

        LOGGER.info("[FabricProxy-Sign] 已启用：在大厅右键告示牌即可进入对应服务器（读取第 {} 行作为服务器名）",
                config.serverNameLine + 1);
    }
}
