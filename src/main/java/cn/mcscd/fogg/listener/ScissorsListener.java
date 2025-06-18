package cn.mcscd.fogg.listener;

import cn.mcscd.fogg.FoggPlugin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class ScissorsListener implements Listener {

    private final FoggPlugin plugin;
    private final Random random = new Random();

    public ScissorsListener(FoggPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 
        if (!plugin.getConfig().getBoolean("scissors.enabled", true)) {
            return;
        }

 
        if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof Player victim)) {
            return;
        }

 
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon.getType() != Material.SHEARS) {
            return;
        }

 
        if (plugin.getConfig().getBoolean("scissors.require-permission", true) &&
                !attacker.hasPermission("fogg.scissors.use")) {
            sendMessage(attacker, "no-permission");
            return;
        }

 
        if (victim.hasPermission("fogg.scissors.protect")) {
            return;
        }

 
        if (plugin.getAFKManager().isEnabled() && plugin.getAFKManager().isAFK(victim.getUniqueId())) {
            sendMessage(attacker, "victim-is-afk");
            return;
        }

 
        boolean enableBotCheck = plugin.getConfig().getBoolean("scissors.check-bot-names", true);
        if (enableBotCheck && isBotName(victim.getName())) {
            sendMessage(attacker, "bot-head-not-allowed");
            return;
        }

 
        if (isPlayerWearingArmor(victim)) {
            sendMessage(attacker, "victim-has-armor");
            return;
        }

 
        ItemStack offHand = victim.getInventory().getItemInOffHand();
        if (offHand == null || offHand.getType() != Material.TOTEM_OF_UNDYING) {
 
            sendMessage(attacker, "no-totem");
            return;
        }

 
        double dropChance = plugin.getConfig().getDouble("head.drop-chance", 100);
        if (dropChance < 100 && random.nextDouble() * 100 > dropChance) {
            return;
        }

 
        event.setDamage(60);

 
        dropHead(victim, attacker, weapon);

 
        sendMessage(attacker, "head-dropped", victim.getName());

 
        attacker.getWorld().playSound(attacker.getLocation(),
                Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);

 
        if (plugin.getConfig().getBoolean("scissors.consume-on-success", true)) {
            attacker.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            plugin.getLogger().info("玩家 " + attacker.getName() + " 的剪刀已消耗");
        }
    }

 
    private boolean isPlayerWearingArmor(Player player) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) {
                return true; 
            }
        }
        return false; 
    }

 
    private boolean isBotName(String name) {
        return name.toUpperCase().startsWith("BOT_");
    }

 
    private void dropHead(Player victim, Player attacker, ItemStack weapon) {
 
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(victim);

 
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = now.format(formatter);

 
        String headName = "§f" + victim.getName() + " §7" + formattedDate;
        meta.setDisplayName(headName);

        head.setItemMeta(meta);

 
        if (plugin.getConfig().getBoolean("head.drop-naturally", true)) {
            int lootingLevel = weapon.getEnchantmentLevel(Enchantment.LOOTING);
            int amount = 1 + random.nextInt(lootingLevel + 1);
            head.setAmount(amount);
            victim.getWorld().dropItemNaturally(victim.getLocation(), head);
        } else {
            victim.getWorld().dropItemNaturally(victim.getLocation(), head);
        }
    }

 
    private void sendMessage(Player player, String messageKey, String... replacements) {
        if (!plugin.getConfig().getBoolean("messages.enabled", true)) {
            return;
        }

        String message = plugin.getConfig().getString("messages." + messageKey);
        if (message == null || message.isEmpty()) {
            return;
        }

 
        if (replacements.length > 0 && message.contains("%player%")) {
            message = message.replace("%player%", replacements[0]);
        }

 
        message = message.replace('&', '§');

        player.sendMessage(message);
    }
}