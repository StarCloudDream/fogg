package cn.mcscd.fogg.afk;

import cn.mcscd.fogg.FoggPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AfkCommand implements CommandExecutor {

    private final FoggPlugin plugin;

    public AfkCommand(FoggPlugin plugin) {
        this.plugin = plugin;
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
            boolean currentStatus = plugin.getAFKManager().isAFK(uuid);

            if (currentStatus) {
                plugin.getAFKManager().setAfkStatus(uuid, false);
                player.sendMessage(ChatColor.GREEN + "你已取消AFK状态");
            } else {
                plugin.getAFKManager().setAfkStatus(uuid, true);
                player.sendMessage(plugin.getAFKManager().getConfig().getAfkMessage());
                plugin.getAFKManager().recordActivity(uuid);
            }

            return true;
        }
        return false;
    }
}