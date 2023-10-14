package ShardDustry;

import static ShardDustry.ShardDustry.config;
import arc.util.*;

import mindustry.gen.Call;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class DiscordBot {

    public JDA bot;

    public void init() {
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
            OptionData opciones = new OptionData(OptionType.STRING,"servidor","servidor para actuar",true,true);
            for (String id : config.serverIDList){
                opciones.addChoice(id.toUpperCase(), id);
            }
            opciones.addChoice("GLOBAL", "global");
            bot.getGuildById(config.discordGuild).updateCommands().addCommands(
                    Commands.slash("say", "manda un mensaje al servidor")
                            .addOptions(opciones)
                    .addOption(OptionType.STRING, "mensaje", "mensaje a enviar", true, true)
            ).queue();

        } catch (Exception e) {
            Log.err(e);
        }
    }

    public static class MessageListener extends ListenerAdapter {

        public void onMessageReceived(MessageReceivedEvent event) {
            if (!event.getAuthor().isBot() && config.activeEmojiReact) {
                String content = event.getMessage().getContentRaw();
                for (EmojiReactor response : config.emojiReact) {
                    if (content.contains(response.trigger)) {
                        switch (response.triggerType) {
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
            if (!event.getAuthor().isBot()) {
                for (String id : config.connectionList) {
                    if (id.equals(event.getChannel().getId())) {
                        String parser = config.discordMessage;
                        if (event.getMessage().getContentDisplay().isEmpty()) {
                            return;
                        }
                        if (parser.contains("{a}")) {
                            parser = parser.replace("{a}", event.getAuthor().getName());
                        }
                        if (parser.contains("{m}")) {
                            parser = parser.replace("{m}", event.getMessage().getContentDisplay());
                        }
                        ShardDustry.sendDiscordMessageEvent(id, parser);
                    }
                }
            }
        }
    }

    public static class CommandListener extends ListenerAdapter {

        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            switch (event.getName()){
                case "say":
                    ShardDustry.sendMindustryCosaEvent(event.getOption("servidor").getAsString(),event.getOption("mensaje").getAsString());
                    event.reply("mensaje enviado").setEphemeral(true).queue();
                    break;
            }
        }
    }
}
