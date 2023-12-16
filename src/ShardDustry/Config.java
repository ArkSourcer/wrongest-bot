package ShardDustry;

import java.util.*;

public class Config {

    public boolean activeDiscordBot = false;
    public String discordToken = "";
    public String discordGuild = "";
    public String joinMessage = "";
    public String chatMessage = "";
    public String leaveMessage = "";
    public String discordMessage = "";
    public String discordMessageSilentPrefix = "-";
    public String slashCommandAllowedRole = "";
    public String commandChannelID = "";
    
    public List<String> connectionList = Arrays.asList("1137044691100123136", "1151227393395142656");
    public List<String> serverIDList = Arrays.asList("pvp","ataque","sandbox");
    
    public boolean activeStatusCheck = false;
    public String statusChannelID = "";
    public HashMap<String, String> statusIDs = new HashMap<String, String>(){{
        put("server1","valor1");
        put("server2","valor2");
    }};
    
    public boolean activeEmojiReact;
    public List<EmojiReactor> emojiReact = Arrays.asList(
            new EmojiReactor(1, "egg", "U+1F95A"),
            new EmojiReactor(1, "ega", "U+1F383"),
            new EmojiReactor(2, "que", "so")
    );
    
    public boolean activeExternalEmojis = false;
    public String externalEmojisGuild = "";
    public String externalEmojisPrefix = "";
    public String externalEmojisLogChannel = "";
}
