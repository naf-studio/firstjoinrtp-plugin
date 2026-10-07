package com.naipret.firstjoinrtp;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

/**
 * Main plugin class for FirstJoinRTP.
 * Manages plugin lifecycle, event listeners, and administrative commands.
 */
public class FirstJoinRTP extends JavaPlugin {

    private RTPListener rtpListener;
    private NamespacedKey rtpCompletedKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        rtpCompletedKey = new NamespacedKey(this, "rtp_completed");

        rtpListener = new RTPListener(this);
        getServer().getPluginManager().registerEvents(rtpListener, this);

        getLogger().info("FirstJoinRTP has been enabled.");
    }

    @Override
    public void onDisable() {
        if (rtpListener != null) {
            rtpListener.cleanup();
        }
        getLogger().info("FirstJoinRTP has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("firstjoinrtp")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("firstjoinrtp.admin")) {
                    sender.sendMessage(ChatColor.RED + "[FirstJoinRTP] You do not have permission to execute this command.");
                    return true;
                }

                reloadConfig();
                if (rtpListener != null) {
                    rtpListener.reloadConfig();
                }
                sender.sendMessage(ChatColor.GREEN + "[FirstJoinRTP] Configuration reloaded successfully.");
                getLogger().info("Configuration reloaded by " + sender.getName() + ".");
                return true;
            }

            sender.sendMessage(ChatColor.YELLOW + "[FirstJoinRTP] Usage: /" + label + " reload");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("firstjoinrtp") && args.length == 1) {
            if (sender.hasPermission("firstjoinrtp.admin") && "reload".startsWith(args[0].toLowerCase())) {
                return Collections.singletonList("reload");
            }
        }
        return Collections.emptyList();
    }

    /**
     * Retrieves the NamespacedKey identifying completed RTP status in player persistent storage.
     *
     * @return The NamespacedKey instance.
     */
    public NamespacedKey getRtpCompletedKey() {
        return rtpCompletedKey;
    }
}
