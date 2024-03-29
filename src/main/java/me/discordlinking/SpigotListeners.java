package me.discordlinking;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import me.discordlinking.utils.ChatUtils;
import me.discordlinking.utils.Formats;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SpigotListeners implements Listener {
    Main plugin;
    static boolean serverChat = false;
    private Message lastMessage = null;
    private UUID lastUUID = null;
    private long lastTime = System.currentTimeMillis();
    private static final long TIME_TO_APPEND = 20000;
    private final Object sync = new Object();

    public SpigotListeners(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent e) {
        if (DiscordBot.channelID == 0 || !DiscordBot.botEnabled) return;
        String message = e.getMessage();
        message = ChatUtils.trimMessage(message);
        if (message.contains("@")) {
            message = ChatColor.stripColor(ChatUtils.atifyMessage(message));
        }
        try {
            WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername(ChatColor.stripColor(e.getPlayer().getDisplayName()));
            builder.setAvatarUrl("https://crafatar.com/avatars/playerUUID?overlay".replace("playerUUID", e.getPlayer().getUniqueId().toString().replaceAll("-", "")));
            builder.setContent(ChatColor.stripColor(message));
            client.send(builder.build());
            client.close();
        } catch (Exception ex) {
            System.out.println("The bot cannot send messages to this channel");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        synchronized (sync) {
            this.lastMessage = null;
            this.lastUUID = null;
        }
        WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername("Login");
        builder.setAvatarUrl(Formats.getAvatarFromUUID(e.getPlayer().getUniqueId()));
        builder.setContent("```fix\n=\n" + ChatColor.stripColor(e.getJoinMessage()) + "\n```");
        client.send(builder.build());
        client.close();
        DiscordBot.client.getPresence().setActivity(Activity.watching(Bukkit.getServer().getOnlinePlayers().size() + "/"
                + Bukkit.getServer().getMaxPlayers() + " Players"));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        synchronized (sync) {
            this.lastMessage = null;
            this.lastUUID = null;
        }
        WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername("Disconnect");
        builder.setAvatarUrl(Formats.getAvatarFromUUID(e.getPlayer().getUniqueId()));
        builder.setContent("```fix\n" + ChatColor.stripColor(e.getQuitMessage()) + "\n=\n```");
        client.send(builder.build());
        client.close();
        DiscordBot.client.getPresence().setActivity(Activity.watching(Bukkit.getServer().getOnlinePlayers().size() - 1 + "/"
                + Bukkit.getServer().getMaxPlayers() + " Players"));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        synchronized (sync) {
            this.lastMessage = null;
            this.lastUUID = null;
        }
        if (DiscordBot.showDeaths && DiscordBot.botEnabled) {
            WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setUsername("Player Death");
            builder.setAvatarUrl(Formats.getAvatarFromUUID(e.getEntity().getUniqueId()));
            builder.setContent("```cs\n# " + ChatColor.stripColor(e.getDeathMessage()) + "\n```");
            client.send(builder.build());
            client.close();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreCommand(PlayerCommandPreprocessEvent e) {
        String[] args = e.getMessage().split(" ");
        if (args[0].equalsIgnoreCase("/reload") || args[0].equalsIgnoreCase("/rl")) {
            synchronized (sync) {
                this.lastMessage = null;
                this.lastUUID = null;
            }
            Main.isReloading("true");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerCommand(ServerCommandEvent e) {
        String[] args = e.getCommand().split(" ");
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (e.getCommand().equalsIgnoreCase(".chat")) {
            if (!serverChat) {
                e.setCancelled(true);
                serverChat = true;
                e.getSender().sendMessage("CONSOLE Chat Enabled");
            } else {
                e.setCancelled(true);
                serverChat = false;
                e.getSender().sendMessage("CONSOLE Chat Disabled");
            }
        } else if (serverChat) {
            e.setCancelled(true);
            if (e.getSender().getName().equalsIgnoreCase("CONSOLE")) {
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&2CONSOLE &7>>&r " + e.getCommand()));
                if (DiscordBot.botEnabled) {
                    WebhookClient client = WebhookClient.withUrl(DiscordBot.webhookURL);
                    WebhookMessageBuilder builder = new WebhookMessageBuilder();
                    builder.setUsername("CONSOLE");
                    builder.setAvatarUrl(DiscordBot.avatarURL);
                    builder.setContent(ChatColor.stripColor(e.getCommand()));
                    client.send(builder.build());
                    client.close();
                }
            }
        } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
            synchronized (sync) {
                this.lastMessage = null;
                this.lastUUID = null;
            }
            Main.isReloading("true");
        }
    }
}
