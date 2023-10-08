package ShardDustry;

import java.util.*;

public class Config {
    public boolean activeDiscordBot;
    public String discordToken;
    public String serverID;
    public String channelID;
    public boolean activeEmojiReact;
    public List<EmojiReactor> emojiReact = Arrays.asList(
            new EmojiReactor(1,"egg","U+1F95A"),
            new EmojiReactor(1,"ega","U+1F383"),
            new EmojiReactor(2,"que","so")
    );
}