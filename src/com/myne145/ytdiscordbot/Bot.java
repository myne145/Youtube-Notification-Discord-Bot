package com.myne145.ytdiscordbot;


import com.myne145.ytdiscordbot.config.BotConfig;
import com.myne145.ytdiscordbot.ytchannel.Checker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;

public class Bot {
    public static void main(String[] args) throws LoginException, URISyntaxException, IOException {
        BotConfig.createConfig();
        JDA jda = JDABuilder.createDefault("MTEzMjY3MTk4NzEzMTYxNzM1MA.GrYNr3.GOxydI5yiFri1gXdQUZQN0mOX6pDzrBerclXyA")
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build();
        Checker.hasNewVideos();


    }
}
