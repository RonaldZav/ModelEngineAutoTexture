package com.ronaldzav.modelEngineTextureAuto;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ResourcePackServer {

    private final JavaPlugin plugin;
    private HttpServer server;
    private final String host;
    private final int port;
    private final String filePath;

    public ResourcePackServer(JavaPlugin plugin, String host, int port, String filePath) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.filePath = filePath;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.createContext("/resourcepack.zip", new FileHandler());
            // Use a thread pool to handle requests asynchronously
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            plugin.getLogger().info("Resource pack server started at http://" + host + ":" + port + "/resourcepack.zip");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not start resource pack server: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Resource pack server stopped.");
        }
    }

    private class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String remoteAddress = t.getRemoteAddress().toString();
            plugin.getLogger().info("Received request from: " + remoteAddress);

            File file = new File(filePath);
            
            // If absolute path is not provided, assume relative to server root (where plugins folder is)
            if (!file.isAbsolute()) {
                file = new File(plugin.getDataFolder().getParentFile().getParentFile(), filePath);
            }

            if (!file.exists()) {
                String response = "File not found";
                t.sendResponseHeaders(404, response.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }
                plugin.getLogger().warning("Resource pack file not found at: " + file.getAbsolutePath());
                return;
            }

            // Set headers
            t.getResponseHeaders().add("Content-Type", "application/zip");
            t.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"resourcepack.zip\"");
            
            // Send response headers with file length
            t.sendResponseHeaders(200, file.length());
            
            // Write file content
            try (OutputStream os = t.getResponseBody(); FileInputStream fs = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = fs.read(buffer)) != -1) {
                    os.write(buffer, 0, count);
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Error sending resource pack to " + remoteAddress + ": " + e.getMessage());
                // Don't re-throw, as headers are already sent
            }

            plugin.getLogger().info("Resource pack sent successfully to " + remoteAddress);
        }
    }
}
