package com.ronaldzav.modelEngineTextureAuto;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ModelEngineTextureAuto extends JavaPlugin {

    private ResourcePackServer resourcePackServer;
    private String resourcePackUrl;
    private byte[] resourcePackHash;
    private boolean forceResourcePack;
    private String resourcePackPrompt;
    private boolean sendHash;
    private long delayTicks;
    private boolean debug;
    private boolean itemsAdderIntegration;
    private String itemsAdderTargetPath;

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
        itemsAdderIntegration = config.getBoolean("integrations.itemsadder.enabled", false);
        itemsAdderTargetPath = config.getString("integrations.itemsadder.target-path", "plugins/ItemsAdder/contents/modelengine/resourcepack");

        // Stop existing server if running
        if (resourcePackServer != null) {
            resourcePackServer.stop();
            resourcePackServer = null;
        }

        if (itemsAdderIntegration) {
            getLogger().info("Integracion con ItemsAdder activada. El servidor HTTP interno esta desactivado.");
        } else {
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
    }

    /**
     * Extrae el zip de ModelEngine en la carpeta de namespace de ItemsAdder.
     * Limpia el directorio destino antes de extraer para garantizar un estado limpio.
     * @return true si la extraccion fue exitosa, false en caso de error.
     */
    public boolean extractModelEnginePackToItemsAdder() {
        String zipPath = getConfig().getString("resource-pack.path", "plugins/ModelEngine/resource pack.zip");
        File zipFile = new File(zipPath);
        if (!zipFile.isAbsolute()) {
            zipFile = new File(getDataFolder().getParentFile().getParentFile(), zipPath);
        }

        if (!zipFile.exists()) {
            getLogger().warning("No se encontro el resource pack de ModelEngine en: " + zipFile.getAbsolutePath());
            return false;
        }

        File targetDir = new File(itemsAdderTargetPath);
        if (!targetDir.isAbsolute()) {
            targetDir = new File(getDataFolder().getParentFile().getParentFile(), itemsAdderTargetPath);
        }

        // Limpiar el directorio destino para un estado limpio
        if (targetDir.exists()) {
            deleteDirectory(targetDir);
        }
        targetDir.mkdirs();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());

                // Seguridad: evitar path traversal
                if (!outFile.getCanonicalPath().startsWith(targetDir.getCanonicalPath() + File.separator)) {
                    getLogger().warning("Entrada de zip omitida por seguridad (path traversal): " + entry.getName());
                    zis.closeEntry();
                    continue;
                }

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();

                if (debug) {
                    getLogger().info("Extraido: " + entry.getName());
                }
            }
            getLogger().info("Resource pack de ModelEngine extraido correctamente en: " + targetDir.getAbsolutePath());
            return true;
        } catch (IOException e) {
            getLogger().severe("Error al extraer el resource pack de ModelEngine: " + e.getMessage());
            return false;
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
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

    // Getters
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

    public boolean isItemsAdderIntegration() {
        return itemsAdderIntegration;
    }
}
