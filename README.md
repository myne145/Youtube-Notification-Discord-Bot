# Youtube-Notification-Bot-Discord
## Features
- Check for multiple YouTube channel at once.
- Customizable notification messages.
- Customizable notification messages.
- Custom Discord satus.


## TODO:
- Auto updater.
- Support for multiple servers.
- Customizable delay between checks.

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

