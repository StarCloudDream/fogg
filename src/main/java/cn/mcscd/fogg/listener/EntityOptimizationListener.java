package cn.mcscd.fogg.listener;

import cn.mcscd.fogg.FoggPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class EntityOptimizationListener implements Listener {

    private final FoggPlugin plugin;
    private final boolean enabled;
    private final List<String> allowedEntities;
    private final boolean dropEquipment;
    private final boolean dropArmor;
    private final double despawnDistance;
    private final boolean protectNamedEntities;

    public EntityOptimizationListener(FoggPlugin plugin) {
        this.plugin = plugin;

 
        this.enabled = plugin.getConfig().getBoolean("entity-optimization.enabled", false);
        this.allowedEntities = plugin.getConfig().getStringList("entity-optimization.allowed-entities");
        this.dropEquipment = plugin.getConfig().getBoolean("entity-optimization.drop-equipment", true);
        this.dropArmor = plugin.getConfig().getBoolean("entity-optimization.drop-armor", true);
        this.despawnDistance = plugin.getConfig().getDouble("entity-optimization.despawn-distance", 128.0);
        this.protectNamedEntities = plugin.getConfig().getBoolean("entity-optimization.protect-named-entities", true);
    }

    @EventHandler
    public void onEntityRemove(EntityRemoveEvent event) {
 
        if (!enabled) {
            return;
        }

 
        Entity entity = event.getEntity();
        if (!isAllowedEntity(entity)) {
            return;
        }

 
        if (protectNamedEntities && entity.getCustomName() != null) {
            return;
        }

 
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            EntityEquipment equipment = livingEntity.getEquipment();

            if (equipment != null && hasEquipment(equipment)) {
 
                if (isTooFarFromPlayers(entity.getLocation())) {
 
                    if (dropEquipment) {
                        dropEquipmentItems(livingEntity, equipment);
                    }

                    if (dropArmor) {
                        dropArmorItems(livingEntity, equipment);
                    }

 
                    if (plugin.getConfig().getBoolean("entity-optimization.logging.enabled", false)) {
                        plugin.getLogger().info("优化移除装备实体: " + entity.getType().name() +
                                " 在位置: " + entity.getLocation().toString());
                    }
                }
            }
        }
    }

 
    private boolean isAllowedEntity(Entity entity) {
        String entityType = entity.getType().name();
        return allowedEntities.contains(entityType) || allowedEntities.contains("ALL");
    }

 
    private boolean hasEquipment(EntityEquipment equipment) {
 
        ItemStack itemInHand = equipment.getItemInMainHand();
        if (itemInHand != null && itemInHand.getType() != Material.AIR) {
            return true;
        }

 
        ItemStack itemInOffHand = equipment.getItemInOffHand();
        if (itemInOffHand != null && itemInOffHand.getType() != Material.AIR) {
            return true;
        }

 
        if (dropArmor) {
            for (ItemStack armor : equipment.getArmorContents()) {
                if (armor != null && armor.getType() != Material.AIR) {
                    return true;
                }
            }
        }

        return false;
    }

 
    private void dropEquipmentItems(LivingEntity entity, EntityEquipment equipment) {
 
        ItemStack itemInHand = equipment.getItemInMainHand();
        if (itemInHand != null && itemInHand.getType() != Material.AIR) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), itemInHand);
        }

 
        ItemStack itemInOffHand = equipment.getItemInOffHand();
        if (itemInOffHand != null && itemInOffHand.getType() != Material.AIR) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), itemInOffHand);
        }
    }

 
    private void dropArmorItems(LivingEntity entity, EntityEquipment equipment) {
        for (ItemStack armor : equipment.getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) {
                entity.getWorld().dropItemNaturally(entity.getLocation(), armor);
            }
        }
    }

 
    private boolean isTooFarFromPlayers(Location location) {
        double minDistance = Double.MAX_VALUE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() == location.getWorld()) {
                double distance = player.getLocation().distance(location);
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
        }

        return minDistance > despawnDistance;
    }
}