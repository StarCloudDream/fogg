package cn.mcscd.fogg.afk;

import org.bukkit.ChatColor;

public class AFKConfig {

    private final boolean enabled;
    private final boolean kickEnabled;
    private final long afkTimeout;
    private final long afkKickTime;
    private final String afkMessage;
    private final String afkSuffix;
    private final String afkKickMessage;
    private final boolean warnBeforeKick;
    private final long warningTime;
    private final int playerThreshold;

    public AFKConfig(boolean enabled, boolean kickEnabled, long afkTimeout, long afkKickTime,
                     String afkMessage, String afkSuffix, String afkKickMessage,
                     boolean warnBeforeKick, long warningTime, int playerThreshold) {
        this.enabled = enabled;
        this.kickEnabled = kickEnabled;
        this.afkTimeout = afkTimeout;
        this.afkKickTime = afkKickTime;
        this.afkMessage = ChatColor.translateAlternateColorCodes('&', afkMessage);
        this.afkSuffix = ChatColor.translateAlternateColorCodes('&', afkSuffix);
        this.afkKickMessage = ChatColor.translateAlternateColorCodes('&', afkKickMessage);
        this.warnBeforeKick = warnBeforeKick;
        this.warningTime = warningTime;
        this.playerThreshold = playerThreshold;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isKickEnabled() {
        return kickEnabled;
    }

    public long getAfkTimeout() {
        return afkTimeout;
    }

    public long getAfkKickTime() {
        return afkKickTime;
    }

    public String getAfkMessage() {
        return afkMessage;
    }

    public String getAfkSuffix() {
        return afkSuffix;
    }

    public String getAfkKickMessage() {
        return afkKickMessage;
    }

    public boolean isWarnBeforeKick() {
        return warnBeforeKick;
    }

    public long getWarningTime() {
        return warningTime;
    }

    public int getPlayerThreshold() {
        return playerThreshold;
    }
}