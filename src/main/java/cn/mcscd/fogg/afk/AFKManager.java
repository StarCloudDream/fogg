package cn.mcscd.fogg.afk;

import cn.mcscd.fogg.FoggPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKManager implements Listener, CommandExecutor {

    private final FoggPlugin plugin;
    private final Map<UUID, Long> lastActivityTime = new HashMap<>();
    private final Map<UUID, Boolean> afkStatus = new HashMap<>();
    private final Map<UUID, BukkitRunnable> afkTimers = new HashMap<>();
    private AFKConfig config;
    private boolean isEnabled;

    public AFKManager(FoggPlugin plugin) {
        this.plugin = plugin;
    }

 
    public void initialize() {
        loadConfig();
        checkAndToggleAFK();
    }

    public boolean isEnabled() {
        return isEnabled;
    }
 
    private void loadConfig() {
        config = new AFKConfig(
                plugin.getConfig().getBoolean("afk.enabled", true),
                plugin.getConfig().getBoolean("afk.kick-enabled", true),
                plugin.getConfig().getLong("afk.timeout", 300000),
                plugin.getConfig().getLong("afk.kick-time", 1800000),
                plugin.getConfig().getString("afk.message", ChatColor.GRAY + "你已进入AFK状态"),
                plugin.getConfig().getString("afk.suffix", " " + ChatColor.GRAY + " [AFK]"),
                plugin.getConfig().getString("afk.kick-message", ChatColor.RED + "由于长时间挂机，你已被踢出服务器"),
                plugin.getConfig().getBoolean("afk.warn-before-kick", true),
                plugin.getConfig().getLong("afk.warning-time", 60000),
                plugin.getConfig().getInt("afk.player-threshold", 30)
        );
    }

 
    private void checkAndToggleAFK() {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        isEnabled = onlinePlayers >= config.getPlayerThreshold() && config.isEnabled();

        if (isEnabled) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            startAfkCheckTimer();
            plugin.getLogger().info("AFK功能已启用 (在线人数: " + onlinePlayers + "/" + config.getPlayerThreshold() + ")");
        } else {
            plugin.getLogger().info("AFK功能未启用，在线人数不足 (在线人数: " + onlinePlayers + "/" + config.getPlayerThreshold() + ")");
        }
    }

 
    private void startAfkCheckTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isEnabled) {
                    checkAfkStatus();
                }
            }
        }.runTaskTimer(plugin, 20 * 5, 20 * 5); 
    }

 
    private void checkAfkStatus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            long currentTime = System.currentTimeMillis();

 
            if (!lastActivityTime.containsKey(uuid)) {
                continue;
            }

 
            long inactivityTime = currentTime - lastActivityTime.get(uuid);

 
            if (inactivityTime > config.getAfkTimeout() && !afkStatus.getOrDefault(uuid, false)) {
                setAfkStatus(uuid, true);
                player.sendMessage(config.getAfkMessage());

 
                if (config.isKickEnabled()) {
                    startKickTimer(uuid, player, inactivityTime);
                }
            }
 
            else if (inactivityTime <= config.getAfkTimeout() && afkStatus.getOrDefault(uuid, false)) {
                setAfkStatus(uuid, false);
                cancelKickTimer(uuid);
            }
        }
    }

 
    private void startKickTimer(UUID uuid, Player player, long currentInactivity) {
        cancelKickTimer(uuid); 

        long timeUntilKick = config.getAfkKickTime() - (currentInactivity - config.getAfkTimeout());
        if (timeUntilKick <= 0) {
            kickPlayer(uuid, player);
            return;
        }

 
        if (config.isWarnBeforeKick() && timeUntilKick > config.getWarningTime()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        p.sendMessage(ChatColor.YELLOW + "你将在" +
                                config.getWarningTime() / 1000 + "秒后因AFK被踢出服务器");
                    }
                }
            }.runTaskLater(plugin, (timeUntilKick - config.getWarningTime()) / 50);
        }

        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                kickPlayer(uuid, player);
            }
        };

        afkTimers.put(uuid, timer);
        timer.runTaskLater(plugin, timeUntilKick / 50); 
    }

 
    private void cancelKickTimer(UUID uuid) {
        if (afkTimers.containsKey(uuid)) {
            afkTimers.get(uuid).cancel();
            afkTimers.remove(uuid);
        }
    }

 
    private void kickPlayer(UUID uuid, Player player) {
        cancelKickTimer(uuid);
        player.kickPlayer(config.getAfkKickMessage());
        plugin.getLogger().info("玩家 " + player.getName() + " 因长时间AFK被踢出");
    }

 
    public void setAfkStatus(UUID uuid, boolean isAfk) {
        afkStatus.put(uuid, isAfk);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && isEnabled) {
            updatePlayerDisplayName(player);
        }
    }

 
    private void updatePlayerDisplayName(Player player) {
        UUID uuid = player.getUniqueId();
        String originalName = player.getName();

        if (afkStatus.getOrDefault(uuid, false)) {
            player.setDisplayName(originalName + config.getAfkSuffix());
            player.setPlayerListName(originalName + config.getAfkSuffix());
        } else {
            player.setDisplayName(originalName);
            player.setPlayerListName(originalName);
        }
    }

 
    public void recordActivity(UUID uuid) {
        lastActivityTime.put(uuid, System.currentTimeMillis());
 
        if (afkStatus.getOrDefault(uuid, false) && isEnabled) {
            setAfkStatus(uuid, false);
            cancelKickTimer(uuid);
        }
    }

 
    public boolean isAFK(UUID uuid) {
        return afkStatus.getOrDefault(uuid, false);
    }

 
    public void shutdown() {
 
        for (BukkitRunnable timer : afkTimers.values()) {
            timer.cancel();
        }
        afkTimers.clear();

 
        lastActivityTime.clear();
        afkStatus.clear();
    }

 
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("afk")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行");
                return true;
            }

            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            if (!isEnabled) {
                player.sendMessage(ChatColor.YELLOW + "AFK功能当前未启用（服务器人数不足）");
                return true;
            }

            boolean currentStatus = afkStatus.getOrDefault(uuid, false);

            if (currentStatus) {
                setAfkStatus(uuid, false);
                player.sendMessage(ChatColor.GREEN + "你已取消AFK状态");
            } else {
                setAfkStatus(uuid, true);
                player.sendMessage(config.getAfkMessage());
                recordActivity(uuid); 
            }

            return true;
        } else if (command.getName().equalsIgnoreCase("isafk")) {
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "此命令需要指定玩家");
                    return true;
                }

                Player player = (Player) sender;
                UUID uuid = player.getUniqueId();
                boolean isAfk = afkStatus.getOrDefault(uuid, false);

                sender.sendMessage(ChatColor.GRAY + player.getName() + " 的AFK状态: " +
                        (isAfk ? ChatColor.RED + "是" : ChatColor.GREEN + "否"));
            } else if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "玩家 " + args[0] + " 不在线");
                    return true;
                }

                UUID uuid = target.getUniqueId();
                boolean isAfk = afkStatus.getOrDefault(uuid, false);

                sender.sendMessage(ChatColor.GRAY + target.getName() + " 的AFK状态: " +
                        (isAfk ? ChatColor.RED + "是" : ChatColor.GREEN + "否"));
            } else {
                sender.sendMessage(ChatColor.RED + "用法: /isafk [玩家]");
            }

            return true;
        }

        return false;
    }

 
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isEnabled) {
 
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getY() != event.getTo().getY() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {
                recordActivity(event.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (isEnabled) {
            recordActivity(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isEnabled) {
            recordActivity(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (isEnabled && event.getDamager() instanceof Player) {
            recordActivity(((Player) event.getDamager()).getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (config.isEnabled()) {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            lastActivityTime.put(uuid, System.currentTimeMillis());
            afkStatus.put(uuid, false);

            if (Bukkit.getOnlinePlayers().size() >= config.getPlayerThreshold()) {
                updatePlayerDisplayName(player);
                if (!isEnabled) {
                    checkAndToggleAFK();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (config.isEnabled()) {
            UUID uuid = event.getPlayer().getUniqueId();
            lastActivityTime.remove(uuid);
            afkStatus.remove(uuid);
            cancelKickTimer(uuid);

            if (Bukkit.getOnlinePlayers().size() < config.getPlayerThreshold() && isEnabled) {
                for (BukkitRunnable timer : afkTimers.values()) {
                    timer.cancel();
                }
                afkTimers.clear();
                isEnabled = false;
                plugin.getLogger().info("AFK功能已禁用，在线人数不足 (在线人数: " +
                        (Bukkit.getOnlinePlayers().size()) + "/" + config.getPlayerThreshold() + ")");
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (isEnabled) {
 
            if (!event.getMessage().startsWith("/afk")) {
                recordActivity(event.getPlayer().getUniqueId());
            }
        }
    }

 
    public AFKConfig getConfig() {
        return config;
    }
}