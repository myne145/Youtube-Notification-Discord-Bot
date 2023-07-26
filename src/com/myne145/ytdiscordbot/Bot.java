package com.myne145.ytdiscordbot;


import com.myne145.ytdiscordbot.config.BotConfig;
import com.myne145.ytdiscordbot.youtube.YoutubeChannel;
import com.myne145.ytdiscordbot.youtube.Checker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Bot extends ListenerAdapter {
    private static TextChannel selectedDiscordTextChannel;
    private static final ArrayList<Thread> ytChannelsThreads = new ArrayList<>();
    private static JDA jda;

    private static void newVideoCheckLoop(YoutubeChannel youtubeChannel) {
        while(true) {
            try {
                selectedDiscordTextChannel = jda.getTextChannelById(BotConfig.getNotificationsChannelID());
                boolean hasNewVideos = Checker.hasNewVideos(youtubeChannel);
                System.out.println(hasNewVideos);
                if(selectedDiscordTextChannel != null && !hasNewVideos) {
                    selectedDiscordTextChannel.sendMessage(youtubeChannel.getName() + "has just uploaded a new video!\n" + "https://www.youtube.com/watch?v=" + Checker.getLatestUploadedVideoId()).queue();
                }
                Thread.sleep(1000);
            } catch (URISyntaxException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };



    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "print-debug-info" -> {
                event.deferReply().queue();
                StringBuilder threadsStates = new StringBuilder();
                for (Thread thread : ytChannelsThreads)
                    threadsStates.append(thread.getName()).append("\t").append(thread.getState()).append("\n");
                event.getHook().sendMessage("Amount Of Threads = " + ytChannelsThreads.size() + "\n"
                        + threadsStates).queue();
            }
            case "set-notification-channel" -> {
                try {
                    BotConfig.updateNotificationChannel(event.getOption("channel").getAsChannel().asTextChannel());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, LoginException {
        BotConfig.createConfig();
        jda = JDABuilder.createDefault("MTEzMjY3MTk4NzEzMTYxNzM1MA.GrYNr3.GOxydI5yiFri1gXdQUZQN0mOX6pDzrBerclXyA")
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new Bot())
                .build();

        jda.updateCommands().addCommands(
                Commands.slash("print-debug-info", "Prints debug information."),
                Commands.slash("set-notification-channel", "Sets specified channel as the default one for YT notifications.")
                        .addOption(OptionType.CHANNEL, "channel", "Channel you want the notifications to be in.")
        ).queue();
//        System.out.println(BotConfig.getNotificationsChannelID());
//
//        System.out.println(selectedChannel);

        for(YoutubeChannel youtubeChannel : BotConfig.getChannels()) {
            ytChannelsThreads.add(new Thread(() -> newVideoCheckLoop(youtubeChannel)));
        }
        for(Thread thread : ytChannelsThreads)
            thread.start();
    }
}
