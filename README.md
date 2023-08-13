# Youtube-Notification-Bot-Discord

Discord bot that notifies members about a new YouTube video being posted.<br>
Coded in Java using the [JDA library](https://github.com/discord-jda/JDA)

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
1. Install java 17 or newer.
2. Download the jar file from the [latest release](https://github.com/myne145/Youtube-Notification-Bot-Discord/releases/latest)
3. Create a config file **config.json**.
4. Fill it in with: 
```
{
    "owner": "DISCORD_OWNER_USER_ID",
    "notifications_channel_id": "CHANNEL_FOR_NOTIFICATIONS_ID",
    "messages": {
        "new_video": "NEW_VIDEO_MESSAGE",
        "livestream": "STREAM_MESSAGE"
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
    "token": "YOUR_DISCORD_TOKEN"
}
```
> Config messages variables:<br>
> **$CHANNEL** - YouTube channel link<br>
> **$VIDEO_LINK** - link to a YouTube video or a livestream<br>
5. Launch the jar file with:
```
java -jar <file_name>.jar &
```
> & detaches the JVM from the terminal window, allowing you to close it.
