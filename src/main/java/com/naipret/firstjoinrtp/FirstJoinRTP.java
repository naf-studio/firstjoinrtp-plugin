package com.naipret.firstjoinrtp;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for FirstJoinRTP.
 * Manages plugin lifecycle and initializes event listeners for first-join teleportation.
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

    /**
     * Retrieves the NamespacedKey identifying completed RTP status in player persistent storage.
     *
     * @return The NamespacedKey instance.
     */
    public NamespacedKey getRtpCompletedKey() {
        return rtpCompletedKey;
    }
}

