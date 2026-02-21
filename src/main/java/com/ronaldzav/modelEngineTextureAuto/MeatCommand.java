package com.ronaldzav.modelEngineTextureAuto;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MeatCommand implements CommandExecutor, TabCompleter {

    private final ModelEngineTextureAuto plugin;

    public MeatCommand(ModelEngineTextureAuto plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN + "--- ModelEngineAutoTexture Help ---");
            sender.sendMessage(ChatColor.GOLD + "/meat reload" + ChatColor.WHITE + " - Recarga ModelEngine, la configuración y reinicia el servidor HTTP.");
            sender.sendMessage(ChatColor.GOLD + "/meat zip" + ChatColor.WHITE + " - Recarga todo y reenvía el resource pack a todos los jugadores.");
            sender.sendMessage(ChatColor.GOLD + "/meat about" + ChatColor.WHITE + " - Información sobre el desarrollador.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("meat.admin")) {
                sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
                return true;
            }
            performReload(sender, false);
            return true;
        }

        if (args[0].equalsIgnoreCase("zip")) {
            if (!sender.hasPermission("meat.admin")) {
                sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
                return true;
            }
            performReload(sender, true);
            return true;
        }

        if (args[0].equalsIgnoreCase("about")) {
            sender.sendMessage(ChatColor.GREEN + "ModelEngineAutoTexture v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GOLD + "Desarrollado por: " + ChatColor.AQUA + "RonaldZav");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Comando desconocido. Usa /meat para ver la ayuda.");
        return true;
    }

    private void performReload(CommandSender sender, boolean sendToPlayers) {
        sender.sendMessage(ChatColor.YELLOW + "Recargando ModelEngine...");
        // Execute ModelEngine reload command
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "modelengine:meg reload");

        // Schedule our reload to happen slightly after, to ensure ModelEngine has finished generating the pack
        // 40 ticks = 2 seconds. Adjust if ModelEngine takes longer.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.loadConfigAndServer();
            sender.sendMessage(ChatColor.GREEN + "ModelEngine recargado, configuración actualizada y servidor HTTP reiniciado.");
            
            if (sendToPlayers) {
                sender.sendMessage(ChatColor.YELLOW + "Enviando resource pack a todos los jugadores...");
                String url = plugin.getResourcePackUrl();
                byte[] hash = plugin.getResourcePackHash();
                String prompt = plugin.getResourcePackPrompt();
                boolean force = plugin.isForceResourcePack();

                if (url != null && !url.isEmpty()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        try {
                            player.setResourcePack(url, hash, prompt, force);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to send resource pack to " + player.getName() + ": " + e.getMessage());
                        }
                    }
                    sender.sendMessage(ChatColor.GREEN + "Resource pack enviado a " + Bukkit.getOnlinePlayers().size() + " jugadores.");
                } else {
                    sender.sendMessage(ChatColor.RED + "No se pudo enviar el resource pack: URL no configurada.");
                }
            }
        }, 40L);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase()) && sender.hasPermission("meat.admin")) {
                completions.add("reload");
            }
            if ("zip".startsWith(args[0].toLowerCase()) && sender.hasPermission("meat.admin")) {
                completions.add("zip");
            }
            if ("about".startsWith(args[0].toLowerCase())) {
                completions.add("about");
            }
            return completions;
        }
        return completions;
    }
}
