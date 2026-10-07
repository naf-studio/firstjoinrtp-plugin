package com.naipret.firstjoinrtp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Event listener managing the first-join random teleportation lifecycle.
 * Coordinates safety periods, teleport execution, and spawnpoint assignment.
 */
public class RTPListener implements Listener {

    private final FirstJoinRTP plugin;

    private final Set<UUID> pendingRtp = new HashSet<>();
    private final Set<UUID> inRtpProcess = new HashSet<>();

    private final String targetWorld;
    private final boolean rtpCommandEnabled;
    private final String rtpCommand;
    private final boolean spawnpointCommandEnabled;
    private final String spawnpointCommand;
    private final boolean saveSpawnpoint;
    private final long delayAfterTp;

    /**
     * Constructs the listener and loads configuration properties.
     *
     * @param plugin The parent plugin instance.
     */
    public RTPListener(FirstJoinRTP plugin) {
        this.plugin = plugin;
        this.targetWorld = plugin.getConfig().getString("target-world", "world");
        this.rtpCommandEnabled = plugin.getConfig().getBoolean("rtp-command-enabled", true);
        this.rtpCommand = plugin.getConfig().getString("rtp-command",
                "spreadplayers 0 0 150 10000 false %player%");
        this.spawnpointCommandEnabled =
                plugin.getConfig().getBoolean("spawnpoint-command-enabled", true);
        this.spawnpointCommand = plugin.getConfig().getString("spawnpoint-command",
                "spawnpoint %player% %x% %y% %z%");
        this.saveSpawnpoint = plugin.getConfig().getBoolean("save-spawnpoint", true);
        this.delayAfterTp = plugin.getConfig().getLong("delay-after-teleport-ticks", 20L);
    }

    /**
     * Handles player join events to trigger RTP if entering target world directly.
     *
     * @param event The player join event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (hasCompletedRTP(player)) {
            return;
        }

        if (player.getWorld().getName().equalsIgnoreCase(targetWorld)) {
            prepareRTP(player);
        }
    }

    /**
     * Handles world transition events to catch players leaving login limbos or lobbies.
     *
     * @param event The world change event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (hasCompletedRTP(player)) {
            return;
        }

        if (player.getWorld().getName().equalsIgnoreCase(targetWorld)) {
            prepareRTP(player);
        }
    }

    /**
     * Evaluates whether a player has completed first-join teleportation.
     *
     * @param player The target player.
     * @return True if already completed, false otherwise.
     */
    private boolean hasCompletedRTP(Player player) {
        Byte val = player.getPersistentDataContainer().get(plugin.getRtpCompletedKey(),
                PersistentDataType.BYTE);
        return val != null && val == (byte) 1;
    }

    /**
     * Persists the RTP completion flag into player data container.
     *
     * @param player The target player.
     */
    private void markCompletedRTP(Player player) {
        player.getPersistentDataContainer().set(plugin.getRtpCompletedKey(),
                PersistentDataType.BYTE, (byte) 1);
    }

    /**
     * Hides the player, assigns invulnerability, and schedules teleport command dispatch.
     *
     * @param player The player entering first-join sequence.
     */
    private void prepareRTP(Player player) {
        if (!rtpCommandEnabled) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if (pendingRtp.contains(uuid) || inRtpProcess.contains(uuid)) {
            return;
        }

        plugin.getLogger().info("Initiating first-join RTP sequence for " + player.getName()
                + " in " + targetWorld + ".");
        pendingRtp.add(uuid);

        for (Player other : Bukkit.getOnlinePlayers()) {
            other.hidePlayer(plugin, player);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                pendingRtp.remove(uuid);

                if (player.isOnline()) {
                    inRtpProcess.add(uuid);
                    dispatchCustomCommand(player, rtpCommand, null);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (inRtpProcess.contains(uuid)) {
                                inRtpProcess.remove(uuid);
                                plugin.getLogger().warning("RTP timeout reached for " + player.getName()
                                        + ". Restoring visibility.");

                                if (player.isOnline()) {
                                    for (Player other : Bukkit.getOnlinePlayers()) {
                                        other.showPlayer(plugin, player);
                                    }
                                }
                            }
                        }
                    }.runTaskLater(plugin, 300L);
                }
            }
        }.runTaskLater(plugin, 10L);
    }

    /**
     * Intercepts teleport completions to record landing locations and set spawnpoints.
     *
     * @param event The player teleport event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (inRtpProcess.contains(uuid)) {
            if (event.getFrom().getWorld() != null
                    && event.getFrom().getWorld().equals(event.getTo().getWorld())
                    && event.getFrom().distanceSquared(event.getTo()) < 100) {
                return;
            }

            if (event.getTo() != null
                    && event.getTo().getWorld() != null
                    && event.getTo().getWorld().getName().equalsIgnoreCase(targetWorld)
                    && (event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND
                            || event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN
                            || event.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN)) {

                Location newLoc = event.getTo();

                boolean hasNewConfig = plugin.getConfig().contains("spawnpoint-command-enabled")
                        || plugin.getConfig().contains("spawnpoint-command");

                if (hasNewConfig) {
                    if (spawnpointCommandEnabled && spawnpointCommand != null
                            && !spawnpointCommand.trim().isEmpty()) {
                        dispatchCustomCommand(player, spawnpointCommand, newLoc);
                        plugin.getLogger().info("RTP completed for " + player.getName()
                                + ". Executed spawnpoint command: " + spawnpointCommand);
                    } else {
                        plugin.getLogger().info("RTP completed for " + player.getName()
                                + " (spawnpoint command disabled).");
                    }
                } else {
                    if (saveSpawnpoint) {
                        player.setRespawnLocation(newLoc, true);
                        plugin.getLogger().info("RTP completed for " + player.getName()
                                + ". Native spawnpoint registered.");
                    } else {
                        plugin.getLogger().info("RTP completed for " + player.getName()
                                + " (native spawnpoint disabled).");
                    }
                }

                markCompletedRTP(player);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline()) {
                            inRtpProcess.remove(uuid);
                            return;
                        }

                        for (Player other : Bukkit.getOnlinePlayers()) {
                            other.showPlayer(plugin, player);
                        }

                        inRtpProcess.remove(uuid);
                        plugin.getLogger().info("Safety period concluded for " + player.getName() + ".");
                    }
                }.runTaskLater(plugin, delayAfterTp);
            }
        }
    }

    /**
     * Prevents damage to players undergoing teleportation or safety periods.
     *
     * @param event The entity damage event.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (inRtpProcess.contains(player.getUniqueId()) || pendingRtp.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Prevents entities from targeting players undergoing teleportation or safety periods.
     *
     * @param event The entity target event.
     */
    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (inRtpProcess.contains(player.getUniqueId()) || pendingRtp.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Cleans up tracking collections when players disconnect.
     *
     * @param event The player quit event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        inRtpProcess.remove(uuid);
        pendingRtp.remove(uuid);
    }

    /**
     * Interpolates placeholder values and dispatches command via console sender.
     *
     * @param player The subject player.
     * @param rawCommand Raw command template.
     * @param loc Teleport destination location.
     */
    private void dispatchCustomCommand(Player player, String rawCommand, Location loc) {
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            return;
        }

        String command = rawCommand.trim();
        if (command.startsWith("/")) {
            command = command.substring(1).trim();
        }

        String worldName = (loc != null && loc.getWorld() != null)
                ? loc.getWorld().getName()
                : targetWorld;

        command = command.replace("%player%", player.getName())
                .replace("%world%", worldName);

        if (loc != null) {
            command = command.replace("%x%", String.valueOf(loc.getBlockX()))
                    .replace("%y%", String.valueOf(loc.getBlockY()))
                    .replace("%z%", String.valueOf(loc.getBlockZ()))
                    .replace("%yaw%", String.valueOf(Math.round(loc.getYaw())))
                    .replace("%pitch%", String.valueOf(Math.round(loc.getPitch())));
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    /**
     * Clears tracking sets on plugin shutdown.
     */
    public void cleanup() {
        inRtpProcess.clear();
        pendingRtp.clear();
    }
}
