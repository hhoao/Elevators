# Elevators
Elevators 是我的世界 NeoForge 1.21 模组，用于通过电梯方块（默认为铁块）上下（跳跃和蹲下）传送。

## 开发环境
- Java 21
- NeoForge 21.0.167
- Minecraft 1.21

## 构建与运行
```bash
bash ./gradlew build
bash ./gradlew runClient
bash ./gradlew runServer
```

## 功能
站在电梯方块上，蹲下会向下传送到下方的电梯方块，跳跃会向上传送到上方的电梯方块。目标电梯方块上方必须有足够空间容纳玩家。

## 指令
- `/elevators allowElevatingThroughBlocks <true|false>`：是否允许电梯方块之间存在其他方块
- `/elevators defaultMaxTeleportHeight <value>`：设置默认最大传送高度
- `/elevators elevatorBlockWithHeight add <block> [height]`：添加电梯方块并可选覆盖最大高度
- `/elevators elevatorBlockWithHeight remove <block>`：移除电梯方块
- `/elevators elevatorBlockWithHeight list`：列出当前电梯方块配置
