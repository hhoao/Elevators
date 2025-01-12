# Elevators
Elevators 是我的世界Forge模组，用于通过电梯方块(默认为铁块)上下(跳跃蹲下)传送。

## 功能
站在电梯方块上，蹲下将往下方的电梯方块传送，跳跃则往上面的电梯方块传送，传送前电梯方块上面必须有足够的空间容纳角色。

## 指令
* elevators
    * allowElevatingThroughBlocks <value>: 是否允许电梯方块间有其他的方块
    * defaultMaxTeleportHeight <value>: 电梯最大传送高度
    * elevatorBlockWithHeight
      * add <block> [height]: 将方块添加为电梯方块(默认有铁块)
      * remove <block>: 移除指定的电梯方块
      * list: 列出所有的电梯方块
