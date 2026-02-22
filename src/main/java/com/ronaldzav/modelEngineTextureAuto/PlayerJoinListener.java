package com.ronaldzav.modelEngineTextureAuto;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final ModelEngineTextureAuto plugin;

    public PlayerJoinListener(ModelEngineTextureAuto plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // En modo ItemsAdder, el propio ItemsAdder gestiona el envio del resource pack.
        if (plugin.isItemsAdderIntegration()) return;

        // Delay sending the resource pack slightly to avoid conflicts with other plugins or login processes
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            String resourcePackUrl = plugin.getResourcePackUrl();
            byte[] resourcePackHash = plugin.getResourcePackHash();
            String resourcePackPrompt = plugin.getResourcePackPrompt();
            boolean forceResourcePack = plugin.isForceResourcePack();

            if (resourcePackUrl != null && !resourcePackUrl.isEmpty()) {
                try {
                    if (plugin.isDebug()) {
                        plugin.getLogger().info("Sending resource pack to " + event.getPlayer().getName());
                        plugin.getLogger().info("URL: " + resourcePackUrl);
                        plugin.getLogger().info("Hash: " + (resourcePackHash != null ? "Present" : "Null"));
                    }
                    event.getPlayer().setResourcePack(resourcePackUrl, resourcePackHash, resourcePackPrompt, forceResourcePack);
                } catch (Exception e) {
                     plugin.getLogger().warning("Failed to send resource pack to " + event.getPlayer().getName() + ": " + e.getMessage());
                }
            }
        }, plugin.getDelayTicks());
    }
}
