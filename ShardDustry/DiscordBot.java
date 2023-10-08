package ShardDustry;

import static ShardDustry.ShardDustry.config;
import arc.util.*;

import mindustry.gen.Call;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DiscordBot {
    public JDA bot;
    
    public void init(){
        try {
            
            bot = JDABuilder.createDefault(config.discordToken)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .build()
                    .awaitReady();
            bot.addEventListener(new MessageListener());
            bot.addEventListener(new CommandListener());
            
        }catch (Exception e){
            Log.err(e);
        }
    }
    public static class MessageListener extends ListenerAdapter {
        public void onMessageReceived(MessageReceivedEvent event){
            if (!event.getAuthor().isBot() && config.activeEmojiReact){
                String content = event.getMessage().getContentRaw();
                for (EmojiReactor response : config.emojiReact){
                    if (content.contains(response.trigger)){
                        switch(response.triggerType){
                            case 1: 
                                event.getMessage().addReaction(Emoji.fromUnicode(response.args)).queue();
                                break;
                            case 2:
                                event.getMessage().reply(response.args).queue();
                                break;
                            default:
                                break;
                        } 
                    }
                }
            }
            if (!event.getAuthor().isBot() && event.getMessage().getChannel().getId().equals("1124859863483306087")){
                Call.sendMessage(event.getAuthor().getName()+ ": " + event.getMessage().getContentRaw());
            }
        }
    }
    public static class CommandListener extends ListenerAdapter {
        
    }
}
