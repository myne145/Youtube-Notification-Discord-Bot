# Youtube Notification Bot

Discord bot that notifies members about a new YouTube video being posted.<br>
Coded in Java using the [JDA library](https://github.com/discord-jda/JDA).

## Features
- Support for multiple channels.
- Custom notification messages.
- Custom Discord status.


## TODO:
- Auto updater.
- Support for multiple servers.
- Custom check interval.
- More commands to change config values.

## Setup:
1. Install Java 17 or newer
2. Download the jar file from the [latest release](https://github.com/myne145/Youtube-Notification-Bot-Discord/releases/latest)
3. Create a config file **config.json**
4. Fill it in with: 
```
{
    "owner": "DISCORD_OWNER_USER_ID",
    "notifications_channel_id": "CHANNEL_FOR_NOTIFICATIONS_ID",
    "messages": {
        "new_video": "$CHANNEL (user's channel) posted a new video - $VIDEO_LINK (link to the video)",
        "livestream": "$CHANNEL (user's channel) posted a new video - $VIDEO_LINK (link to the video)"
    },
    "youtube_api_key": "YOUT_YOUTUBE_API_KEY",
    "youtube_channels": [
        {
            "name": "CHANNEL1_NAME",
            "id": "CHANNEL1_ID"
        },
        {
            "name": "CHANNEL2_NAME",
            "id": "CHANNEL2_ID"
        }
    ],
    "status_type": "PLAYING / WATCHING / LISTENING",
    "status_message": "YOUR_STATUS_MESSAGE",
    "token": "YOUR_DISCORD_TOKEN"
}
```
5. Launch the jar file with:
```
java -jar <file_name>.jar &
```
<sup>& detaches the JVM from the terminal window, allowing you to close it.</sup>
