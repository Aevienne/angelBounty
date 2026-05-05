package me.angelique.angelBounty.config;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

public final class PluginConfig {

    private final int killsPerStar;
    private final int maxStars;
    private final boolean decayEnabled;
    private final long decayIntervalTicks;
    private final int starsPerDecay;
    private final boolean loseAllOnDeath;
    private final boolean resetKillsOnDeath;
    private final double rewardBase;
    private final double rewardPerStar;
    private final long saveIntervalTicks;
    private final boolean sidebarEnabled;
    private final boolean tabPrefixEnabled;
    private final boolean nametagPrefixEnabled;
    private final boolean belowNameEnabled;
    private final String filledStar;
    private final String emptyStar;
    private final String primaryColor;
    private final String secondaryColor;
    private final String labelColor;
    private final String bountyColor;
    private final String sidebarTitle;
    private final boolean economyEnabled;
    private final boolean vaultRequired;
    private final boolean fallbackMessageReward;
    private final String messageWantedIncrease;
    private final String messageWantedCleared;
    private final String messageBountyClaim;
    private final String messageTargetStatus;
    private final String messageNoEconomy;
    private final String messageTopHeader;
    private final boolean useAllWorldsWhenEmpty;
    private final Set<String> enabledWorlds;

    public PluginConfig(FileConfiguration config) {
        this.killsPerStar = Math.max(1, config.getInt("wanted.kills-per-star", 2));
        this.maxStars = Math.max(1, Math.min(6, config.getInt("wanted.max-stars", 6)));
        this.decayEnabled = config.getBoolean("wanted.decay.enabled", true);
        this.decayIntervalTicks = Math.max(20L, config.getLong("wanted.decay.interval-seconds", 300L) * 20L);
        this.starsPerDecay = Math.max(1, config.getInt("wanted.decay.stars-per-interval", 1));
        this.loseAllOnDeath = config.getBoolean("wanted.lose-all-on-death", true);
        this.resetKillsOnDeath = config.getBoolean("wanted.reset-kills-on-death", true);
        this.rewardBase = Math.max(0.0D, config.getDouble("wanted.reward-base", 100.0D));
        this.rewardPerStar = Math.max(0.0D, config.getDouble("wanted.reward-per-star", 75.0D));
        this.saveIntervalTicks = Math.max(20L, config.getLong("wanted.save-interval-seconds", 60L) * 20L);
        this.sidebarEnabled = config.getBoolean("display.scoreboard-sidebar", true);
        this.tabPrefixEnabled = config.getBoolean("display.tab-prefix", true);
        this.nametagPrefixEnabled = config.getBoolean("display.nametag-prefix", true);
        this.belowNameEnabled = config.getBoolean("display.below-name", false);
        this.filledStar = config.getString("display.stars-symbol-filled", "★");
        this.emptyStar = config.getString("display.stars-symbol-empty", "☆");
        this.primaryColor = color(config.getString("display.primary-color", "&c"));
        this.secondaryColor = color(config.getString("display.secondary-color", "&7"));
        this.labelColor = color(config.getString("display.label-color", "&6"));
        this.bountyColor = color(config.getString("display.bounty-color", "&a"));
        this.sidebarTitle = color(config.getString("display.safe-sidebar-title", "&6Wanted Status"));
        this.economyEnabled = config.getBoolean("economy.enabled", false);
        this.vaultRequired = config.getBoolean("economy.vault-required", false);
        this.fallbackMessageReward = config.getBoolean("economy.fallback-message-reward", true);
        this.messageWantedIncrease = color(config.getString("messages.wanted-increase", "&cYou are now wanted: {stars} star(s)."));
        this.messageWantedCleared = color(config.getString("messages.wanted-cleared", "&aYour wanted level has been cleared."));
        this.messageBountyClaim = color(config.getString("messages.bounty-claim", "&6{killer} claimed a bounty of &a${reward}&6 for taking down {victim}&6."));
        this.messageTargetStatus = color(config.getString("messages.target-status", "&6{player} &7- Wanted: {stars} star(s), Bounty: &a${reward}"));
        this.messageNoEconomy = color(config.getString("messages.no-economy", "&eVault economy not found. Reward noted but not paid."));
        this.messageTopHeader = color(config.getString("messages.top-header", "&6Top Wanted Players:"));
        this.useAllWorldsWhenEmpty = config.getBoolean("wanted.use-all-worlds-when-empty", true);
        this.enabledWorlds = new HashSet<>(config.getStringList("wanted.worlds-enabled"));
    }

    private String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value == null ? "" : value);
    }

    public boolean isWorldEnabled(World world) {
        if (useAllWorldsWhenEmpty && enabledWorlds.isEmpty()) {
            return true;
        }
        return enabledWorlds.contains(world.getName());
    }

    public int getKillsPerStar() { return killsPerStar; }
    public int getMaxStars() { return maxStars; }
    public boolean isDecayEnabled() { return decayEnabled; }
    public long getDecayIntervalTicks() { return decayIntervalTicks; }
    public int getStarsPerDecay() { return starsPerDecay; }
    public boolean isLoseAllOnDeath() { return loseAllOnDeath; }
    public boolean isResetKillsOnDeath() { return resetKillsOnDeath; }
    public double getRewardBase() { return rewardBase; }
    public double getRewardPerStar() { return rewardPerStar; }
    public long getSaveIntervalTicks() { return saveIntervalTicks; }
    public boolean isSidebarEnabled() { return sidebarEnabled; }
    public boolean isTabPrefixEnabled() { return tabPrefixEnabled; }
    public boolean isNametagPrefixEnabled() { return nametagPrefixEnabled; }
    public boolean isBelowNameEnabled() { return belowNameEnabled; }
    public String getFilledStar() { return filledStar; }
    public String getEmptyStar() { return emptyStar; }
    public String getPrimaryColor() { return primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public String getLabelColor() { return labelColor; }
    public String getBountyColor() { return bountyColor; }
    public String getSidebarTitle() { return sidebarTitle; }
    public boolean isEconomyEnabled() { return economyEnabled; }
    public boolean isVaultRequired() { return vaultRequired; }
    public boolean isFallbackMessageReward() { return fallbackMessageReward; }
    public String getMessageWantedIncrease() { return messageWantedIncrease; }
    public String getMessageWantedCleared() { return messageWantedCleared; }
    public String getMessageBountyClaim() { return messageBountyClaim; }
    public String getMessageTargetStatus() { return messageTargetStatus; }
    public String getMessageNoEconomy() { return messageNoEconomy; }
    public String getMessageTopHeader() { return messageTopHeader; }
}
