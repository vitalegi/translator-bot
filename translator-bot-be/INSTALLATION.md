# Install guide

## Discord

### Obtain server Id

<https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID>

### Onboard server

```
/add-server server_id:111 name:xxx_this is a sample server

/add-channel-group name:xxx_channel1
/add-channel-group name:xxx_channel2

/discord-channel-language-link channel_group:xxx_channel1 server_id:111 channel:offtopic-it language:it
/discord-channel-language-link channel_group:xxx_channel1 server_id:111 channel:offtopic-en language:en
/discord-channel-language-link channel_group:xxx_channel1 server_id:111 channel:offtopic-de language:de

/info
```