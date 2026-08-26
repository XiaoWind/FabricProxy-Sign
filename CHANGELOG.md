# Changelog

本文件遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/) 规范，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [1.0.0] - 未发布

### 新增
- 在大厅右键告示牌即可通过 Velocity 进入对应服务器（读取告示牌第二行作为服务器名）。
- 支持告示牌**双面**：点击正面读正面、点击背面读背面（调用原版 `SignBlockEntity.isFacingFrontText(Player)`），两面可指向不同服务器。
- 通过 BungeeCord 插件消息通道 `bungeecord:main` 发送 `Connect` 消息。
- 配置文件 `config/fabricproxy-sign.json`（行号、前缀去除、冷却）。
- 潜行 + 右键跳过，方便管理员编辑告示牌。
- 目标环境：Minecraft 26.2（Mojang 映射）、Fabric API 0.158.0+26.2、Java 25。
- GitHub Actions：CI 构建与 tag 触发的发布工作流。
