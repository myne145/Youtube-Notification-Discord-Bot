package com.myne145.ytdiscordbot;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class Bot {

    public static void main(String[] args) throws LoginException {
        JDA jda = JDABuilder.createDefault("MTEzMjY3MTk4NzEzMTYxNzM1MA.GrYNr3.GOxydI5yiFri1gXdQUZQN0mOX6pDzrBerclXyA")
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build();


    }
}
