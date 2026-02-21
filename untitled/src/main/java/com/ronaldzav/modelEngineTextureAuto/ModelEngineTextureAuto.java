package com.ronaldzav.modelEngineTextureAuto;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class ModelEngineTextureAuto extends JavaPlugin {

    private ResourcePackServer resourcePackServer;
    private String resourcePackUrl;
    private byte[] resourcePackHash;
    private boolean forceResourcePack;
    private String resourcePackPrompt;
    private boolean sendHash;
    private long delayTicks;
    private boolean debug;

    @Override
    public void onEnable() {
        loadConfigAndServer();
        
        // Register Events
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        
        // Register Commands
        MeatCommand meatCommand = new MeatCommand(this);
        getCommand("meat").setExecutor(meatCommand);
        getCommand("meat").setTabCompleter(meatCommand);

        getLogger().info("Iniciando servicio de texture-pack automatico para ModelEngine. Desarrollado por RonaldZav.");
    }

    public void loadConfigAndServer() {
        saveDefaultConfig();
        reloadConfig();
        FileConfiguration config = getConfig();

        String host = config.getString("server.host", "0.0.0.0");
        int port = config.getInt("server.port", 8080);
        String filePath = config.getString("resource-pack.path", "plugins/ModelEngine/resource pack.zip");
        resourcePackUrl = config.getString("server.external-url", "http://127.0.0.1:8080/resourcepack.zip");
        forceResourcePack = config.getBoolean("resource-pack.force", true);
        resourcePackPrompt = config.getString("resource-pack.prompt", "This resource pack is required for ModelEngine assets.");
        sendHash = config.getBoolean("resource-pack.send-hash", true);
        delayTicks = config.getLong("resource-pack.delay-ticks", 40L);
        debug = config.getBoolean("debug", false);

        // Stop existing server if running
        if (resourcePackServer != null) {
            resourcePackServer.stop();
        }

        // Start HTTP Server
        resourcePackServer = new ResourcePackServer(this, host, port, filePath);
        resourcePackServer.start();

        // Calculate Hash
        File file = new File(getDataFolder().getParentFile().getParentFile(), filePath);
        if (file.exists()) {
            try {
                resourcePackHash = calculateSha1(file);
                if (debug) {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : resourcePackHash) {
                        sb.append(String.format("%02x", b));
                    }
                    getLogger().info("Resource pack SHA-1 hash: " + sb.toString());
                }
            } catch (Exception e) {
                getLogger().warning("Could not calculate SHA-1 hash for resource pack: " + e.getMessage());
                resourcePackHash = null;
            }
        } else {
             getLogger().warning("Resource pack file not found for hash calculation at: " + file.getAbsolutePath());
             resourcePackHash = null;
        }
    }

    @Override
    public void onDisable() {
        if (resourcePackServer != null) {
            resourcePackServer.stop();
        }
        getLogger().info("Deteniendo servicio de texture-pack automatico para ModelEngine. Desarrollado por RonaldZav");
    }

    private byte[] calculateSha1(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[8192];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        return digest.digest();
    }

    // Getters for Listener
    public String getResourcePackUrl() {
        return resourcePackUrl;
    }

    public byte[] getResourcePackHash() {
        return sendHash ? resourcePackHash : null;
    }

    public boolean isForceResourcePack() {
        return forceResourcePack;
    }

    public String getResourcePackPrompt() {
        return resourcePackPrompt;
    }

    public long getDelayTicks() {
        return delayTicks;
    }

    public boolean isDebug() {
        return debug;
    }
}
