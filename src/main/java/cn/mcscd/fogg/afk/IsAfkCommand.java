package cn.mcscd.fogg.afk;

import cn.mcscd.fogg.FoggPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class IsAfkCommand implements CommandExecutor {

    private final FoggPlugin plugin;

    public IsAfkCommand(FoggPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("isafk")) {
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "此命令需要指定玩家");
                    return true;
                }

                Player player = (Player) sender;
                UUID uuid = player.getUniqueId();
                boolean isAfk = plugin.getAFKManager().isAFK(uuid);

                sender.sendMessage(ChatColor.GRAY + player.getName() + " 的AFK状态: " +
                        (isAfk ? ChatColor.RED + "是" : ChatColor.GREEN + "否"));
            } else if (args.length == 1) {
                Player target = plugin.getServer().getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "玩家 " + args[0] + " 不在线");
                    return true;
                }

                UUID uuid = target.getUniqueId();
                boolean isAfk = plugin.getAFKManager().isAFK(uuid);

                sender.sendMessage(ChatColor.GRAY + target.getName() + " 的AFK状态: " +
                        (isAfk ? ChatColor.RED + "是" : ChatColor.GREEN + "否"));
            } else {
                sender.sendMessage(ChatColor.RED + "用法: /isafk [玩家]");
            }

            return true;
        }
        return false;
    }
}