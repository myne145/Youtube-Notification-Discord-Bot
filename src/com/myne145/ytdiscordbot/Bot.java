package com.myne145.ytdiscordbot;


import com.myne145.ytdiscordbot.config.BotConfig;
import com.myne145.ytdiscordbot.youtube.YoutubeChannel;
import com.myne145.ytdiscordbot.youtube.Checker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Bot extends ListenerAdapter {
    private static TextChannel selectedDiscordTextChannel;
    private static final ArrayList<Thread> ytChannelsThreads = new ArrayList<>();
    private static JDA jda;

    /**
     * Checks for new videos on specified YouTube channel, and if there are sends the message to a Discord channel.
     * @param youtubeChannel Youtube channel you want to check
     * @return if the operation was successful
     */
    private static boolean checkForNewVideos(YoutubeChannel youtubeChannel) {
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
            return false;
        }
        return true;
    }

    /**
     * Calls {@link #checkForNewVideos(YoutubeChannel)} in a loop, every 15 minutes.
     * @param youtubeChannel Youtube channel you want to check
     */
    private static void newVideoCheckLoop(YoutubeChannel youtubeChannel) {
        while(true) {
            checkForNewVideos(youtubeChannel);
            try {
                Thread.sleep(1000 * 60 * 15); //sleep for 15 minutes
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Determines whether the Discord user is the owner of the bot.
     * @param userId Discord user's ID
     * @return is user the owner
     */
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
                if(!isOwner(event.getUser().getId())) {
                    event.reply("You need to be the owner to execute that!").setEphemeral(true).queue();
                    return;
                }
                if(event.getOption("channel") == null) {
                    event.reply("Specify the channel first.").queue(); //pointless null check
                    return;
                }
                try {
                    BotConfig.updateNotificationChannel(event.getOption("channel").getAsChannel().asTextChannel());
                } catch (IOException e) {
                    event.reply("Failed to change the notifications channel: " + e.getMessage()).queue();
                }

            }
            case "force-video-check" -> {
                event.deferReply().queue();
                if(isOwner(event.getUser().getId())) {
                    event.reply("You need to be the owner to execute that!").setEphemeral(true).queue();
                    return;
                }
                for(YoutubeChannel youtubeChannel : BotConfig.getChannels())
                    checkForNewVideos(youtubeChannel);

            }
            case "help" -> {
                event.reply(MarkdownUtil.bold(event.getJDA().getSelfUser().getName()) + " commands:\n\n" +
                        MarkdownUtil.bold("About:\n") +
                        "`/print-debug-info` - prints debug information\n" +
                        "`/help` - shows this message\n\n"+
                        MarkdownUtil.bold("Admin:\n") +
                        "`/set-notification-channel` - sets the channel for YouTube notifications.\n" +
                        "`/force-video-check` - forces check for new videos on every tracked channels.").queue();
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
                Commands.slash("force-video-check", "Forces new videos check."),
                Commands.slash("help", "Lists all the commands and their descriptions.")
        ).queue();

        for(YoutubeChannel youtubeChannel : BotConfig.getChannels()) {
            ytChannelsThreads.add(new Thread(() -> newVideoCheckLoop(youtubeChannel)));
        }
        for(Thread thread : ytChannelsThreads)
            thread.start();
    }
}
