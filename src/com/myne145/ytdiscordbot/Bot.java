package com.myne145.ytdiscordbot;


import com.myne145.ytdiscordbot.config.BotConfig;
import com.myne145.ytdiscordbot.youtube.YoutubeChannel;
import com.myne145.ytdiscordbot.youtube.YoutubeChannelChecker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

public class Bot extends ListenerAdapter {
    private static TextChannel selectedDiscordTextChannel;
    private static final ArrayList<Thread> ytChannelsThreads = new ArrayList<>();
    private static final ArrayList<YoutubeChannelChecker> youtubeChannelsCheckers = new ArrayList<>();
    private static JDA jda;


    /**
     * Determines whether the Discord user is the owner of the bot.
     * @param userId Discord user's ID
     * @return is user the owner
     */
    private static boolean isOwner(String userId) {
        return userId.equals(BotConfig.getOwnerUserId());
    }

    public static void broadcastNewVideoMessage(boolean isLiveStream, YoutubeChannelChecker youtubeChannelChecker) {
        selectedDiscordTextChannel = jda.getTextChannelById(BotConfig.getNotificationsChannelID());
        if(selectedDiscordTextChannel == null)
            return;

        if(!isLiveStream) {
            selectedDiscordTextChannel.sendMessage(BotConfig.getNewVideoMessage(youtubeChannelChecker.getYoutubeChannel(),
                    youtubeChannelChecker.getLatestUploadedVideoId())).queue();
        } else {
            selectedDiscordTextChannel.sendMessage(BotConfig.getLivestreamMessage(youtubeChannelChecker.getYoutubeChannel(),
                    youtubeChannelChecker.getLatestUploadedVideoId())).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "help" -> event.reply(MarkdownUtil.bold(event.getJDA().getSelfUser().getName()) + " commands:\n\n" +
                    MarkdownUtil.bold("About:\n") +
                    "`/print-debug-info` - prints debug information\n" +
                    "`/help` - shows this message\n\n"+
                    MarkdownUtil.bold("Admin:\n") +
                    "`/set-notification-channel` - sets the channel for YouTube notifications.\n" +
                    "`/force-video-check` - forces check for new videos on every tracked channels.").queue();

            case "print-debug-info" -> {
                event.deferReply().queue();
                selectedDiscordTextChannel = jda.getTextChannelById(BotConfig.getNotificationsChannelID());
                StringBuilder threadsStates = new StringBuilder();
                for (Thread thread : ytChannelsThreads) {
                    threadsStates.append(thread.getName()).append("\t").append(thread.getState()).append("\n");
                }
                event.getHook().sendMessage("```System:\n" +
                        "OS:\t" + System.getProperty("os.name") + "\n" +
                        "total RAM:\t" + Runtime.getRuntime().totalMemory() +
                        "b\nfree RAM:\t" + Runtime.getRuntime().freeMemory() +
                        "b\napprox JVM RAM usage:\t" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) +"b\n\n" +
                "Bot:\n" +
                       "last_youtube_videos file list:\t" + Arrays.asList(new File("last_youtube_videos").list()) +
                        "\nchannel scan threads:\t" + ytChannelsThreads.size() + "\n" +
                        threadsStates + "\ndiscord notifications channel:\t" + selectedDiscordTextChannel.getId() +
                        ", " + selectedDiscordTextChannel.getName() + ", " + selectedDiscordTextChannel.getGuild() + "\ntracked youtube channels:\t" + BotConfig.getChannels() + ", "
                        + BotConfig.getChannels().size() + "\n" +
                        "JVM uptime:\t" + (ManagementFactory.getRuntimeMXBean().getUptime() / 1000L) + "s\n" + "```").queue();
            }
            case "set-notification-channel" -> {
                Channel channel = event.getOption("channel").getAsChannel();

                if(!isOwner(event.getUser().getId())) {
                    event.reply("You need to be the owner to execute that!").setEphemeral(true).queue();
                    return;
                }
                if(event.getOption("channel") == null) {
                    event.reply("Specify the channel first.").queue(); //pointless null check
                    return;
                }
                if(channel.getType() == ChannelType.VOICE) {
                    event.reply("Incorrect channel type! Please select a text channel.").setEphemeral(true).queue();
                    return;
                }

                try {
                    BotConfig.updateNotificationChannel((TextChannel) channel);
                } catch (IOException e) {
                    event.reply("Failed to change the notifications channel: " + e.getMessage()).queue();
                }
                event.reply("Successfully changed the notifications channel!").queue();
            }
            case "force-video-check" -> {
                if(!isOwner(event.getUser().getId())) {
                    event.reply("You need to be the owner to execute that!").setEphemeral(true).queue();
                    return;
                }
                boolean wereAnyVideosFound = false;
                for(YoutubeChannelChecker checker : youtubeChannelsCheckers) {
                    boolean hasNewVideos;
                    try {
                        hasNewVideos = checker.hasNewVideos();
                    } catch (URISyntaxException | IOException e) {
                        throw new RuntimeException(e);
                    }
                    if(hasNewVideos) {
                        wereAnyVideosFound = true;
                        broadcastNewVideoMessage(checker.isLiveStream(), checker);
                    }
                }
                if(!wereAnyVideosFound) //TODO: update this message
                    event.reply(MarkdownUtil.quoteBlock("No new videos found.\nNote: Spamming this command will deplete your API tokens, scheduled checks occur every 15 minutes.")).setEphemeral(true).queue();
                else
                    event.reply(MarkdownUtil.quoteBlock("New videos were found.\nNote: Spamming this command will deplete your API tokens, scheduled checks occur every 15 minutes.")).setEphemeral(true).queue();

            }
            case "set-check-interval" -> {
                if(!isOwner(event.getUser().getId())) {
                    event.reply("You need to be the owner to execute that!").setEphemeral(true).queue();
                    return;
                }

                if(event.getOption("time_seconds") == null ||
                        event.getOption("time_seconds").getAsDouble() <= 0 ||
                        event.getOption("time_seconds").getAsDouble() % 1 != 0) { //only works with double
                    event.reply("Invalid value!").setEphemeral(true).queue();
                    return;
                }
                try {
                    BotConfig.updateCheckInterval((int)event.getOption("time_seconds").getAsDouble());
                } catch (IOException e) {
                    event.reply("Cannot update the value in the config file." + e.getMessage()).setEphemeral(true).queue();
                    return;
                }
                event.reply("Done! The changes will take place after the next check.").setEphemeral(true).queue();
            }

        }
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            BotConfig.createConfig();
        } catch (Exception e) {
            throw new JSONException("Cannot parse config file - Follow the instructions at https://github.com/myne145/Youtube-Notification-Discord-Bot#local to set it up properly.");
        }

        jda = JDABuilder.createDefault(BotConfig.getToken())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new Bot())
                .build();
        jda.getPresence().setPresence(Activity.playing("Loading..."), true);
        jda.updateCommands().addCommands(
                Commands.slash("print-debug-info", "Prints debug information."),
                Commands.slash("force-video-check", "Forces new videos check."),
                Commands.slash("help", "Lists all the commands and their descriptions."),

                Commands.slash("set-notification-channel", "Sets specified channel as the default one for YT notifications.")
                        .addOption(OptionType.CHANNEL, "channel", "Channel you want the notifications to be in.", true),

                Commands.slash("set-check-interval", "Sets the channel check interval.")
                        .addOption(OptionType.NUMBER, "time_seconds", "The check interval in seconds.", true)
        ).queue();

        System.out.println("Waiting 5 seconds for the loading process to finish...");
        Thread.sleep(5000);

        String activityType = BotConfig.getActivityType();
        switch (activityType) {
            case "WATCHING" -> jda.getPresence().setPresence(Activity.watching(BotConfig.getActivityText()), true);
            case "LISTENING" -> jda.getPresence().setPresence(Activity.listening(BotConfig.getActivityText()), true);
            default -> jda.getPresence().setPresence(Activity.playing(BotConfig.getActivityText()), true);
        }

        for(YoutubeChannel youtubeChannel : BotConfig.getChannels()) {
            YoutubeChannelChecker youtubeChannelChecker = new YoutubeChannelChecker(youtubeChannel);
            ytChannelsThreads.add(new Thread(() -> {
                try {
                    youtubeChannelChecker.checkForNewVideosInLoop();
                } catch (InterruptedException ignored) {

                }
            }
            ));
            youtubeChannelsCheckers.add(youtubeChannelChecker);
        }
        for(Thread thread : ytChannelsThreads)
            thread.start();
    }
}
