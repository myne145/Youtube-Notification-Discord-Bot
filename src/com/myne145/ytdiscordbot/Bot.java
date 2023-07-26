package com.myne145.ytdiscordbot;


import com.myne145.ytdiscordbot.config.BotConfig;
import com.myne145.ytdiscordbot.youtube.YoutubeChannel;
import com.myne145.ytdiscordbot.youtube.Checker;
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
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Bot extends ListenerAdapter {
    private static TextChannel selectedDiscordTextChannel;
    private static final ArrayList<Thread> ytChannelsThreads = new ArrayList<>();
    private static JDA jda;

    private static void checkForNewVideos(YoutubeChannel youtubeChannel) {
        try {
            selectedDiscordTextChannel = jda.getTextChannelById(BotConfig.getNotificationsChannelID());
            boolean hasNewVideos = Checker.hasNewVideos(youtubeChannel);
            System.out.println(hasNewVideos);
            if(selectedDiscordTextChannel != null && hasNewVideos) {
                if(!Checker.isLiveStream())
                    selectedDiscordTextChannel.sendMessage(BotConfig.getNewVideoMessage(youtubeChannel, "https://www.youtube.com/watch?v=" + Checker.getLatestUploadedVideoId())).queue();
                else
                    selectedDiscordTextChannel.sendMessage(BotConfig.getLivestreamMessage(youtubeChannel, "https://www.youtube.com/watch?v=" + Checker.getLatestUploadedVideoId())).queue();
            }

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void newVideoCheckLoop(YoutubeChannel youtubeChannel) {
        while(true) {
            checkForNewVideos(youtubeChannel);
            try {
                Thread.sleep(1000 * 60 * 15); //sleep for 15 minutes
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private static boolean isOwner(String userId) {
        return userId.equals(BotConfig.getOwnerUserId());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "print-debug-info" -> {
                event.deferReply().queue();
                selectedDiscordTextChannel = jda.getTextChannelById(BotConfig.getNotificationsChannelID());
                StringBuilder threadsStates = new StringBuilder();
                for (Thread thread : ytChannelsThreads)
                    threadsStates.append(thread.getName()).append("\t").append(thread.getState()).append("\n");
                event.getHook().sendMessage("```threads:\t" + ytChannelsThreads.size() + "\n"
                        + threadsStates + "\ndiscord notifications channel:\t" + selectedDiscordTextChannel.getId() +
                        ", " + selectedDiscordTextChannel.getName() + ", " + selectedDiscordTextChannel.getGuild() + "\ntracked youtube channels:\t" + BotConfig.getChannels() + ", "
                        + BotConfig.getChannels().size() + "\n\n" +
                        "total RAM:\t" + Runtime.getRuntime().totalMemory() + "b\nfree RAM:\t" + Runtime.getRuntime().freeMemory() +
                        "b\napprox JVM RAM usage:\t" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) +
                        "b\nOS:\t" + System.getProperty("os.name") + "\nJVM uptime:\t" +
                        (ManagementFactory.getRuntimeMXBean().getUptime() / 1000L) + "s" + "```").queue();
            }
            case "set-notification-channel" -> {
                if(isOwner(event.getUser().getId())) {
                    if(event.getOption("channel") == null) {
                        event.reply("Specify the channel first.").queue(); //pointless null check
                        return;
                    }
                    try {
                        BotConfig.updateNotificationChannel(event.getOption("channel").getAsChannel().asTextChannel());
                    } catch (IOException e) {
                        event.reply("Failed to change the notifications channel: " + e.getMessage()).queue();
                    }
                } else {
                    event.reply("You need to be the owner to execute that!").setEphemeral(true).queue();
                }
            }
            case "force-video-check" -> {
                if(isOwner(event.getUser().getId())) {
                    for(YoutubeChannel youtubeChannel : BotConfig.getChannels())
                        checkForNewVideos(youtubeChannel);
                } else {
                    event.reply("You need to be the owner to execute that!").setEphemeral(true).queue();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, LoginException {
        BotConfig.createConfig();
        jda = JDABuilder.createDefault(BotConfig.getToken())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new Bot())
                .build();

        jda.updateCommands().addCommands(
                Commands.slash("print-debug-info", "Prints debug information."),
                Commands.slash("set-notification-channel", "Sets specified channel as the default one for YT notifications.")
                        .addOption(OptionType.CHANNEL, "channel", "Channel you want the notifications to be in.", true),
                Commands.slash("force-video-check", "Forces new videos check.")
        ).queue();

        for(YoutubeChannel youtubeChannel : BotConfig.getChannels()) {
            ytChannelsThreads.add(new Thread(() -> newVideoCheckLoop(youtubeChannel)));
        }
        for(Thread thread : ytChannelsThreads)
            thread.start();
    }
}
