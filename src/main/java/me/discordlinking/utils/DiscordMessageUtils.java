package me.discordlinking.utils;

import me.discordlinking.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;


public class DiscordMessageUtils { // Old class that kinda doesn't do anything right now, but keeping it for now. todo delete this
    public static MessageEmbed createNewMessage(String author, String authorAvatar, String extraPrefix, String message) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(author, null, authorAvatar);
        embed.setDescription(extraPrefix + "\n" + message);
        embed.setColor(DiscordBot.chatColor);
        return embed.build();
    }
}
