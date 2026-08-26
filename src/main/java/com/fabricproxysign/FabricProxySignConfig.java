package com.fabricproxysign;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricProxySignConfig {
    /** 0 开始的告示牌行号：1 表示第二行。 */
    public int serverNameLine = 1;
    /** 可选：从该行开头去掉的前缀，例如 "服务器: "。留空表示不去掉。 */
    public String stripPrefix = "";
    /** 两次点击之间的冷却时间（毫秒），防止连点重复发送。 */
    public long cooldownMillis = 1000;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fabricproxy-sign.json");

    public static FabricProxySignConfig load() {
        if (Files.exists(FILE)) {
            try {
                FabricProxySignConfig cfg = GSON.fromJson(Files.readString(FILE), FabricProxySignConfig.class);
                if (cfg != null) {
                    return cfg;
                }
            } catch (Exception e) {
                FabricProxySignMod.LOGGER.warn("[FabricProxy-Sign] 配置文件解析失败，使用默认配置", e);
            }
        }
        FabricProxySignConfig cfg = new FabricProxySignConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this));
        } catch (IOException e) {
            FabricProxySignMod.LOGGER.warn("[FabricProxy-Sign] 无法写入配置文件", e);
        }
    }
}
