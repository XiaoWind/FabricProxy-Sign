# FabricProxy-Sign

一个 **Fabric 服务端模组**：装在大厅服务器上，玩家**右键告示牌**即可通过 Velocity 进入对应服务器，无需手动输入 `/server 服务器名`。

## 工作原理

1. 玩家在大厅右键一块告示牌；
2. 模组监听到该交互，读取告示牌**第 2 行**的文字作为服务器名（**点击正面读正面、点击背面读背面**）；
3. 模组通过 BungeeCord 插件消息通道 `bungeecord:main` 向 Velocity 发送一条 `Connect` 消息；
4. Velocity 收到后把玩家切换到目标服务器（等价于执行 `/server 服务器名`）。

> 本模组**不是**修改 [FabricProxy-Lite](https://modrinth.com/mod/fabricproxy-lite)，而是与它并存。FabricProxy-Lite 负责打通「Velocity ↔ Fabric 后端」的转发通道，本模组负责「右键告示牌 → 发送 Connect」。

## 前置条件

- **大厅服务器**是 Fabric 服务端，且已安装 [FabricProxy-Lite](https://modrinth.com/mod/fabricproxy-lite)。
- 大厅服务器已安装 **Fabric API**（本模组依赖它）。
- Velocity 的 `velocity.toml` 中启用了 BungeeCord 插件消息通道：

  ```toml
  [advanced]
  bungee-plugin-message-channel = true
  ```

  （大多数情况下默认就是 true，改完记得重启 Velocity。）

## 构建

本模组目标版本为 **Minecraft 26.2**（26.2 已无 Yarn，使用 **Mojang 官方映射/Mojmap**），构建需要 **JDK 25** 与 **Gradle 9.5.1**。构建前请核对 `gradle.properties` 里的 `loader_version`、`fabric_version`，可到 <https://fabricmc.net/develop/> 查看当前推荐值。

```powershell
# 在项目根目录（FabricProxy-Sign/）执行
gradle wrapper          # 首次生成 gradlew（并请提交到 git，方便 CI）
./gradlew build
```

生成的模组在：`build/libs/fabricproxy-sign-1.0.0.jar`。

> 提示：如果本机没有 Gradle，最简单的方式是用 **IntelliJ IDEA**（配合 Minecraft Development 插件）直接打开本目录导入，它会自动处理 Gradle 和 wrapper。

## 安装

1. 把 `build/libs/fabricproxy-sign-1.0.0.jar` 放进**大厅服务器**的 `mods/` 目录（和 FabricProxy-Lite、Fabric API 放在一起）。
2. 重启大厅服务器。日志出现 `[FabricProxy-Sign] 已启用…` 即成功。

> 只需要装在大厅服，其它游戏子服不用装。

## 配置

首次运行后会在 `config/fabricproxy-sign.json` 生成配置（修改后重启大厅服生效）：

```json
{
  "serverNameLine": 1,
  "stripPrefix": "",
  "cooldownMillis": 1000
}
```

| 字段 | 说明 |
| --- | --- |
| `serverNameLine` | 服务器名所在的告示牌行号，**从 0 开始**。默认 `1` = 第二行。 |
| `stripPrefix` | 可选：从该行开头去掉的前缀。例如告示牌写 `服务器: survival`，设成 `"服务器: "` 后取到 `survival`。留空 `""` 表示不去掉。 |
| `cooldownMillis` | 两次点击之间的冷却（毫秒），防止连点重复发送。 |

## 使用

在大厅的告示牌上，把**第二行**写成服务器名（即 `/server` 后面要填的参数），例如：

```
[生存服]
survival
----------
空岛生存
```

玩家右键这块告示牌，就会被送到 `survival` 服。

- 告示牌是双面的：**点击哪一面就读哪一面**。所以正面第二行写 `survival`、背面第二行写 `skyblock`，玩家点正面进 `survival`、点背面进 `skyblock`，两面可以不同。
- **潜行 + 右键**会跳过本模组，方便管理员编辑告示牌。
- 被点击那一面的第二行为空时不触发。

## 常见问题

- **右键没反应 / 日志提示「通道未注册」**：检查 `velocity.toml` 的 `bungee-plugin-message-channel = true`，并确认大厅服已装 FabricProxy-Lite、Fabric API，然后重启 Velocity 和大厅服。
- **传送目标服务器名不匹配**：确认告示牌第二行写的就是 Velocity 里配置的服务器名（`velocity.toml` 中 `[servers]` 段的名字），大小写敏感。
- **正反面反了**：说明服务器名写在了告示牌的另一面，换一面写即可。
- **构建报 loader/fabric 版本找不到**：到 <https://fabricmc.net/develop/> 核对 `gradle.properties` 中的版本号，改成当前可用版本。
- **构建报 Java 版本错误**：本模组需要 **JDK 25**，请确认 `JAVA_HOME` 指向 JDK 25（`build.gradle` 里 `options.release = 25`）。

## 发布

### GitHub

1. 在 GitHub 新建仓库，名称填 **FabricProxy-Sign**（要同步到 Modrinth 的话请设为公开）。
2. 本地初始化并推送：

   ```bash
   cd FabricProxy-Sign
   git init
   gradle wrapper                 # 生成 gradlew、gradlew.bat、gradle/wrapper/gradle-wrapper.jar
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<你的用户名>/FabricProxy-Sign.git
   git push -u origin main
   ```

3. 推送后，`.github/workflows/build.yml` 会自动跑 CI 构建。

### Modrinth

- 推荐用 Modrinth 官方 **GitHub App** 连接该仓库：之后在 GitHub 打 tag 发 release，Modrinth 会自动同步版本（无需 token）。

  ```bash
  git tag v1.0.0
  git push origin v1.0.0
  ```

- 也可以手动上传：把 `build/libs/fabricproxy-sign-1.0.0.jar` 上传到 Modrinth 项目对应版本页。

## 二次开发说明

- 核心逻辑在 `src/main/java/com/fabricproxysign/SignClickHandler.java`（右键事件 + 读告示牌 + 发送）。正反面判定直接调用原版 `SignBlockEntity.isFacingFrontText(Player)`。
- `ConnectPayload.java` 定义了 `bungeecord:main` 通道的负载与编解码。
- `FabricProxySignConfig.java` 负责配置文件读写。
- 如果未来的 Fabric API 移除了当前网络 API，只需改 `ConnectPayload` 与 `SignClickHandler#sendToServer` 这一处发送逻辑，其余不变。
