package cn.mcscd.fogg;

import cn.mcscd.fogg.afk.AFKManager;
import cn.mcscd.fogg.listener.ScissorsListener;
import org.bukkit.plugin.java.JavaPlugin;

public class FoggPlugin extends JavaPlugin {

    private AFKManager afkManager;

    @Override
    public void onEnable() {
 
        saveDefaultConfig();

 
        afkManager = new AFKManager(this);
        afkManager.initialize();

 
        getServer().getPluginManager().registerEvents(new ScissorsListener(this), this);

        getLogger().info("Fogg插件已启用，AFK和剪刀功能已激活");
    }

    @Override
    public void onDisable() {
 
        if (afkManager != null) {
            afkManager.shutdown();
        }

        getLogger().info("Fogg插件已禁用");
    }

 
    public AFKManager getAFKManager() {
        return afkManager;
    }
}
