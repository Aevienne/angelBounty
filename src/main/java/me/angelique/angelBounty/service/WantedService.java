package me.angelique.angelBounty.service;

import me.angelique.angelBounty.AngelBounty;
import me.angelique.angelBounty.config.PluginConfig;
import me.angelique.angelBounty.model.WantedData;
import me.angelique.angelBounty.storage.WantedStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WantedService {

    private static final String SIDEBAR_OBJECTIVE = "wsidebar";
    private static final String BELOW_NAME_OBJECTIVE = "wbelow";
    private static final String TEAM_PREFIX = "wteam_";

    private final AngelBounty plugin;
    private final PluginConfig config;
    private final EconomyService economyService;
    private final WantedStorage wantedStorage;
    private final Map<UUID, WantedData> wantedMap = new ConcurrentHashMap<>();
    private int autosaveTaskId = -1;
    private int decayTaskId = -1;
    private boolean dirty;

    public WantedService(AngelBounty plugin, PluginConfig config, EconomyService economyService, WantedStorage wantedStorage) {
        this.plugin = plugin;
        this.config = config;
        this.economyService = economyService;
        this.wantedStorage = wantedStorage;
    }

    public void initialize() {
        wantedMap.clear();
        wantedMap.putAll(wantedStorage.load());
        for (Player player : Bukkit.getOnlinePlayers()) {
            wantedMap.computeIfAbsent(player.getUniqueId(), uuid -> new WantedData());
        }
        startAutosaveTask();
        startDecayTask();
        refreshDisplays();
    }

    public void shutdown() {
        if (autosaveTaskId != -1) {
            Bukkit.getScheduler().cancelTask(autosaveTaskId);
            autosaveTaskId = -1;
        }
        if (decayTaskId != -1) {
            Bukkit.getScheduler().cancelTask(decayTaskId);
            decayTaskId = -1;
        }
        flush();
    }

    public void handleJoin(Player player) {
        wantedMap.computeIfAbsent(player.getUniqueId(), uuid -> new WantedData());
        refreshDisplays();
    }

    public void handleQuit(Player player) {
        refreshDisplays();
    }

    public void recordPlayerKill(Player killer, Player victim) {
        if (killer == null || victim == null || killer.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }
        if (!config.isWorldEnabled(killer.getWorld()) || killer.hasPermission("angelbounty.bypass")) {
            return;
        }

        WantedData data = wantedMap.computeIfAbsent(killer.getUniqueId(), uuid -> new WantedData());
        int oldStars = data.getStars();
        data.addPlayerKill();
        int newStars = Math.min(config.getMaxStars(), Math.max(0, data.getPlayerKills() / config.getKillsPerStar()));
        data.setStars(newStars);
        markDirty();
        refreshDisplays();

        if (newStars > oldStars) {
            killer.sendMessage(config.getMessageWantedIncrease().replace("{stars}", String.valueOf(newStars)));
        }
    }

    public void handleWantedDeath(Player killer, Player victim) {
        if (victim == null) {
            return;
        }

        WantedData victimData = wantedMap.computeIfAbsent(victim.getUniqueId(), uuid -> new WantedData());
        int victimStars = victimData.getStars();
        if (victimStars <= 0) {
            return;
        }

        double reward = calculateReward(victimStars);
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            boolean paid = economyService.payout(killer, reward);
            String rewardFormatted = economyService.format(reward);
            String message = config.getMessageBountyClaim()
                    .replace("{killer}", killer.getName())
                    .replace("{victim}", victim.getName())
                    .replace("{reward}", rewardFormatted);
            Bukkit.broadcastMessage(message);
            if (!paid && config.isFallbackMessageReward()) {
                killer.sendMessage(config.getMessageNoEconomy());
            }
        }

        if (config.isLoseAllOnDeath()) {
            victimData.setStars(0);
        }
        if (config.isResetKillsOnDeath()) {
            victimData.setPlayerKills(0);
        }
        markDirty();
        refreshDisplays();
        victim.sendMessage(config.getMessageWantedCleared());
    }

    public int getStars(OfflinePlayer player) {
        if (player == null) {
            return 0;
        }
        return wantedMap.computeIfAbsent(player.getUniqueId(), uuid -> new WantedData()).getStars();
    }

    public double calculateReward(int stars) {
        if (stars <= 0) {
            return 0.0D;
        }
        return config.getRewardBase() + (config.getRewardPerStar() * stars);
    }

    public void setStars(OfflinePlayer player, int stars) {
        if (player == null) {
            return;
        }
        WantedData data = wantedMap.computeIfAbsent(player.getUniqueId(), uuid -> new WantedData());
        int clampedStars = Math.max(0, Math.min(config.getMaxStars(), stars));
        data.setStars(clampedStars);
        data.setPlayerKills(clampedStars * config.getKillsPerStar());
        markDirty();
        refreshDisplays();
    }

    public void addStars(OfflinePlayer player, int starsToAdd) {
        setStars(player, getStars(player) + starsToAdd);
    }

    public void clear(OfflinePlayer player) {
        if (player == null) {
            return;
        }
        WantedData data = wantedMap.computeIfAbsent(player.getUniqueId(), uuid -> new WantedData());
        data.setStars(0);
        data.setPlayerKills(0);
        markDirty();
        refreshDisplays();
    }

    public List<OfflinePlayer> getTopWanted(int limit) {
        return wantedMap.entrySet().stream()
                .filter(entry -> entry.getValue().getStars() > 0)
                .sorted(Comparator.<Map.Entry<UUID, WantedData>>comparingInt(entry -> entry.getValue().getStars()).reversed()
                        .thenComparing(entry -> Bukkit.getOfflinePlayer(entry.getKey()).getName(), Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .limit(limit)
                .map(entry -> Bukkit.getOfflinePlayer(entry.getKey()))
                .toList();
    }

    public String getStarsBar(int stars) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < config.getMaxStars(); i++) {
            if (i < stars) {
                builder.append(config.getPrimaryColor()).append(config.getFilledStar());
            } else {
                builder.append(config.getSecondaryColor()).append(config.getEmptyStar());
            }
        }
        return builder.toString();
    }

    public void refreshDisplays() {
        updateTabNames();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            if (config.isSidebarEnabled()) {
                applySidebar(scoreboard, viewer);
            }
            if (config.isBelowNameEnabled()) {
                applyBelowName(scoreboard);
            }
            applyTeams(scoreboard);
            viewer.setScoreboard(scoreboard);
        }
    }

    private void applySidebar(Scoreboard scoreboard, Player viewer) {
        Objective objective = scoreboard.registerNewObjective(SIDEBAR_OBJECTIVE, Criteria.DUMMY, config.getSidebarTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.getScore(config.getLabelColor() + "You").setScore(7);
        objective.getScore(getSafeLine(6, getStarsBar(getStars(viewer)))).setScore(6);
        objective.getScore(getSafeLine(5, ChatColor.GRAY + "Bounty: " + config.getBountyColor() + economyService.format(calculateReward(getStars(viewer))))).setScore(5);
        objective.getScore(getSafeLine(4, ChatColor.DARK_GRAY + " ")).setScore(4);
        objective.getScore(config.getLabelColor() + "Top Wanted").setScore(3);

        int score = 2;
        for (OfflinePlayer target : getTopWanted(3)) {
            int stars = getStars(target);
            String name = target.getName() == null ? target.getUniqueId().toString().substring(0, 8) : target.getName();
            objective.getScore(getSafeLine(score, ChatColor.RED + trim(name, 10) + ChatColor.GRAY + " " + stars + "★")).setScore(score);
            score--;
        }
    }

    private void applyBelowName(Scoreboard scoreboard) {
        Objective objective = scoreboard.registerNewObjective(BELOW_NAME_OBJECTIVE, Criteria.DUMMY, ChatColor.RED + "Wanted");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        for (Player player : Bukkit.getOnlinePlayers()) {
            objective.getScore(player.getName()).setScore(getStars(player));
        }
    }

    private void applyTeams(Scoreboard scoreboard) {
        for (int stars = 0; stars <= config.getMaxStars(); stars++) {
            Team team = scoreboard.getTeam(TEAM_PREFIX + stars);
            if (team == null) {
                team = scoreboard.registerNewTeam(TEAM_PREFIX + stars);
            }
            if (config.isNametagPrefixEnabled()) {
                team.setPrefix(stars > 0 ? getStarsBar(stars) + ChatColor.RESET + " " : "");
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            Team team = scoreboard.getTeam(TEAM_PREFIX + getStars(player));
            if (team != null) {
                team.addEntry(player.getName());
            }
        }
    }

    private void updateTabNames() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!config.isTabPrefixEnabled()) {
                player.setPlayerListName(player.getName());
                continue;
            }
            String tabName = getStarsBar(getStars(player)) + ChatColor.RESET + " " + ChatColor.WHITE + player.getName();
            player.setPlayerListName(trim(tabName, 80));
        }
    }

    private String getSafeLine(int score, String line) {
        return trim(line + ChatColor.values()[Math.min(score, ChatColor.values().length - 1)], 40);
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private void startAutosaveTask() {
        autosaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::flush, config.getSaveIntervalTicks(), config.getSaveIntervalTicks());
    }

    private void startDecayTask() {
        if (!config.isDecayEnabled()) {
            return;
        }
        decayTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            boolean changed = false;
            for (WantedData data : wantedMap.values()) {
                if (data.getStars() <= 0) {
                    continue;
                }
                data.setStars(Math.max(0, data.getStars() - config.getStarsPerDecay()));
                data.setPlayerKills(Math.min(data.getPlayerKills(), data.getStars() * config.getKillsPerStar()));
                changed = true;
            }
            if (changed) {
                markDirty();
                refreshDisplays();
            }
        }, config.getDecayIntervalTicks(), config.getDecayIntervalTicks());
    }

    private void markDirty() {
        this.dirty = true;
    }

    private void flush() {
        if (!dirty) {
            return;
        }
        wantedStorage.save(wantedMap);
        dirty = false;
    }
}
