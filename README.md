Fogg 插件 - 服务器管理插件
功能特点
AFK 管理系统
自动检测玩家无活动状态，超过设定时间标记为 AFK
AFK 玩家名称显示后缀标识，支持自定义格式和颜色
可配置 AFK 超时踢人功能，支持踢前警告
支持手动切换 AFK 状态命令 /afk
支持查看玩家 AFK 状态命令 /isafk [玩家]
可配置 AFK 功能仅在服务器人数达到指定阈值时启用
剪刀特殊功能
攻击持有不死图腾的玩家时，消耗图腾并掉落玩家头颅
自动检测玩家是否穿戴盔甲，穿戴则不执行特殊效果
支持 BOT 名称检测，防止获取假人头颅
可配置头颅掉落几率和是否自然掉落
支持权限控制，可保护特定玩家不被剪刀功能影响

命令列表
命令	描述	权限
/afk	切换自己的 AFK 状态	无 (玩家可用)
/isafk [玩家]	查看自己或其他玩家的 AFK 状态	无 (玩家可用)
权限列表
权限	描述	默认权限
fogg.scissors.use	使用剪刀特殊功能的权限	OP
fogg.scissors.protect	保护自己不被剪刀功能影响的权限	OP
配置说明
AFK 配置
yaml
afk:
  enabled: true # 是否启用AFK功能
  kick-enabled: true # 是否启用AFK踢人功能
  player-threshold: 30 # 服务器人数阈值，达到此人数时启用AFK功能
  timeout: 300000 # AFK检测时间，5分钟，单位毫秒
  kick-time: 1800000 # AFK踢人时间，30分钟，单位毫秒
  message: "&7你已进入AFK状态" # AFK提示消息
  suffix: " &7[AFK]" # AFK后缀，灰色
  kick-message: "&c由于长时间AFK，你已被踢出服务器" # AFK踢人消息
  warn-before-kick: true # 是否在踢人前警告
  warning-time: 60000 # 踢人前警告时间，1分钟，单位毫秒
剪刀功能配置
yaml
scissors:
  enabled: true # 是否启用剪刀功能
  require-permission: true # 是否需要权限才能使用
  check-bot-names: true # 是否检查BOT名称
  head:
    drop-chance: 100.0 # 头颅掉落几率
    drop-naturally: true # 是否自然掉落
  messages:
    enabled: true # 是否启用消息提示
    no-permission: "&c你没有使用此功能的权限"
    no-totem: "&c目标没有持有不死图腾"
    victim-has-armor: "&c目标穿戴了盔甲，无法使用剪刀特殊效果"
    bot-head-not-allowed: "&c无法获取假人头颅"
    head-dropped: "&a成功获取 %player% 的头颅"
    totem-consumed: "&a你的不死图腾已被消耗"
    victim-is-afk: "&c目标处于AFK状态，无法执行此操作"
实体优化配置
yaml
entity-optimization:
  enabled: false # 是否启用实体优化功能
  allowed-entities: # 允许处理的实体类型列表
    - ZOMBIE
    - SKELETON
    - CREEPER
    - ENDERMAN
  drop-equipment: true # 是否掉落装备物品
  drop-armor: true # 是否掉落盔甲物品
  despawn-distance: 128.0 # 实体清除的距离阈值
  protect-named-entities: true # 是否保护有命名的实体
  logging:
    enabled: false # 是否记录实体清除日志
