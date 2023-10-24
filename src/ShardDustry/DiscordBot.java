package ShardDustry;

import static ShardDustry.ShardDustry.config;
import arc.util.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class DiscordBot {

    public static JDA bot;
    private static Role slashCommandAllowedRole;

    public static void init() {
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
            slashCommandAllowedRole = bot.getRoleById(config.slashCommandAllowedRole);
            OptionData opciones = new OptionData(OptionType.STRING,"servidor","servidor para actuar",true);
            for (String id : config.serverIDList){
                opciones.addChoice(id.toUpperCase(), id);
            }
            opciones.addChoice("GLOBAL", "global");
            bot.getGuildById(config.discordGuild).updateCommands().addCommands(
                    Commands.slash("say", "Ecribe un mensaje como el bot")
                    .addOption(OptionType.STRING, "mensaje", "mensaje a enviar",true),
                    Commands.slash("info", "Solicita informacion a un servidor")
                    .addOptions(opciones)
                    .addOption(OptionType.STRING, "identificador", "Identificador del jugador",true),
                    Commands.slash("excecute", "Ejecuta un comando en algun servidor, no puedes recibir la respuesta del comando")
                    .addOption(OptionType.STRING, "codigo", "comando a ser ejecutado",true)
                    .addOptions(opciones)
            ).queue();

        } catch (Exception e) {
            Log.err(e);
        }
    }

    public static class MessageListener extends ListenerAdapter {

        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;
            if (config.activeEmojiReact) {
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

    public static class CommandListener extends ListenerAdapter {

        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            //comandos de uso general
            switch (event.getName()){
                case "say":
                    event.getChannel().sendMessage(event.getOption("mensaje").getAsString()).queue();
                    event.reply("escribiste " + event.getOption("mensaje").getAsString()).setEphemeral(true).queue();
            }
            
            if (!event.getMember().getRoles().contains(slashCommandAllowedRole)) return;
            // comandos de accion en servidores
            switch (event.getName()){
                case "info":
                    ShardDustry.sendMindustryInfoRequestEvent(event.getOption("servidor").getAsString(),event.getOption("identificador").getAsString());
                    break;
                case "excecute":
                    ShardDustry.sendMindustryExcecuteRequestEvent(event.getOption("servidor").getAsString(),event.getOption("codigo").getAsString());
                    break;
            }
        }
    }
    
    public static void sendMessage(String channel, String message) {
        bot.getTextChannelById(channel).sendMessage(message).queue();
    }
}
