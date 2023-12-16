package ShardDustry;

import static ShardDustry.ShardDustry.config;
import arc.util.*;
import static arc.util.Time.time;
import java.awt.Color;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;

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
            updateCommands();
        } catch (Exception e) {
            Log.err(e);
        }
    }
    
    public static void updateCommands() {
        try {
            OptionData servers = new OptionData(OptionType.STRING, "servidor", "servidor para actuar", true);
            for (String id : config.serverIDList) {
                servers.addChoice(id.toUpperCase(), id);
            }
            servers.addChoice("GLOBAL", "global");
            
            bot.getGuildById(config.discordGuild).updateCommands().addCommands(Commands.slash("say", "Escribe un mensaje como el bot")
                    .addOption(OptionType.STRING, "mensaje", "mensaje a enviar", true),
                    Commands.slash("info", "Solicita informacion a un servidor")
                            .addOptions(servers)
                            .addOption(OptionType.STRING, "identificador", "Identificador del jugador", true),
                    Commands.slash("execute", "Ejecuta un comando en algun servidor, no puedes recibir la respuesta del comando")
                            .addOption(OptionType.STRING, "codigo", "comando a ser ejecutado", true)
                            .addOptions(servers)
                    /*Commands.slash("setcomchannel", "Establece un canal para comunicaciones con un servidor")
                            .addOptions(servers),
                    Commands.slash("paramlist", "Obtiene la lista de parametros de la configuracion de un servidor")
                            .addOptions(servers)*/
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
            if (config.activeExternalEmojis){
                Guild guild = bot.getGuildById(config.externalEmojisGuild);
                String content = event.getMessage().getContentRaw();
                if (content.startsWith(config.externalEmojisPrefix+"emojis")){
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Lista de emojis de " + guild.getName());
                    String description = "";
                    for (RichCustomEmoji emoji : guild.getEmojis()){
                        description = description + "`+" + emoji.getName() + "` " + emoji.getFormatted() + " ";
                    }
                    embed.setDescription(description);
                    embed.setTimestamp(Instant.now());
                    embed.setThumbnail(guild.getIconUrl());
                    embed.setAuthor(bot.getSelfUser().getName());
                    embed.setFooter("los emojis pueden o no usar mayusculas");
                    embed.setColor(Color.yellow);
                    event.getChannel().sendMessage("Lista cargada correctamente").queue(e->{
                        e.editMessageEmbeds(embed.build()).queue();
                    });
                }
                if (content.startsWith(config.externalEmojisPrefix)){
                    if (!guild.getEmojisByName(content.replace("+",""),true).isEmpty()){
                        event.getMessage().delete().queue(e->{
                            String name = guild.getEmojisByName(content.replace("+",""),true).get(0).getName();
                            event.getChannel().sendMessage(guild.getEmojisByName(content.replace("+", ""), true).get(0).getFormatted()).queue();
                            bot.getTextChannelById(config.externalEmojisLogChannel).sendMessage(event.getMember().getEffectiveName() + " ha usado el emoji +" + name).queue();
                        });
                    }
                }
                return;
            }
            for (String id : config.connectionList) {
                if (id.equals(event.getChannel().getId())) {
                    
                    if (event.getMessage().getContentDisplay().isEmpty() || event.getMessage().getContentDisplay().startsWith(config.discordMessageSilentPrefix)) {
                        return;
                    }
                    String parser = config.discordMessage;
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
                    event.getChannel().sendMessage(event.getOption("mensaje").getAsString()).queue(e -> {
                        event.getHook().deleteOriginal().queue();
                    });
                    break;
            }
            
            if (!event.getMember().getRoles().contains(slashCommandAllowedRole)) {
                event.reply("No tienes el rol necesario para hacer uso de este comando, revisa que rol es necesario").setEphemeral(true).queue();
                return;
            }
            // comandos de accion en servidores
            switch (event.getName()){
                case "info":
                    ShardDustry.sendMindustryInfoRequestEvent(event.getOption("servidor").getAsString(),event.getOption("identificador").getAsString());
                    break;
                case "execute":
                    ShardDustry.sendMindustryExecuteRequestEvent(event.getOption("servidor").getAsString(),event.getOption("codigo").getAsString());
                    break;
                case "paramlist":
                    ShardDustry.sendPropertyListRequestEvent(event.getOption("servidor").getAsString());
                    break;
                case "setcomchannel":
                    
                    break;
            }
        }
    }
    
    public static void sendMessage(String channel, String message) {
        bot.getTextChannelById(channel).sendMessage(message).queue();
    }
    
    public static void sendMessageAsEmbed(String channel, String title, Color color, String author, String description, String[] fieldName, String[] fieldValue, String stateID){
        EmbedBuilder embed = new EmbedBuilder();
        if (title != null) embed.setTitle(title);
        if (color != null) embed.setColor(color);
        if (author != null) embed.setAuthor(author);
        if (description != null) embed.setDescription(description);
        embed.setTimestamp(Instant.now());
        
        try {
            bot.getTextChannelById(channel).editMessageEmbedsById(stateID, embed.build()).queue(e -> {
                ShardDustry.addServerKey(title, stateID);
            }, new ErrorHandler()
            .handle(ErrorResponse.UNKNOWN_MESSAGE, (error) -> bot.getTextChannelById(channel).sendMessageEmbeds(embed.build()).queue(e -> {
                ShardDustry.sendEditPropertyEvent("stateID", e.getId());
                ShardDustry.addServerKey(title,e.getId());
            })));
        }catch (Exception ex){
            bot.getTextChannelById(channel).sendMessageEmbeds(embed.build()).queue(e -> {
                ShardDustry.sendEditPropertyEvent("stateID", e.getId());    
                ShardDustry.addServerKey(title,e.getId());
            });
        }
    }
    
    public static void sendMessageAsEmbedL(String channel, String title, Color color, String author, String description){
        EmbedBuilder embed = new EmbedBuilder();
        if (title != null) embed.setTitle(title);
        if (color != null) embed.setColor(color);
        if (author != null) embed.setAuthor(author);
        if (description != null) embed.setDescription(description);
        embed.setTimestamp(Instant.now());
        bot.getTextChannelById(channel).sendMessageEmbeds(embed.build()).queue();
    }
    
    public static void setDisabledStatus(String server, String statusID, int statusCooldown){
        try {
            bot.getTextChannelById(config.statusChannelID).retrieveMessageById(statusID).queue(message -> {
                if (message.getEmbeds().isEmpty()) return;
                if (!message.getEmbeds().get(0).getTimestamp().plusMinutes(statusCooldown).isAfter(Instant.now().atOffset(ZoneOffset.UTC))){
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(Color.red);
                    embed.setTitle(server + " (offline)");
                    embed.setTimestamp(message.getEmbeds().get(0).getTimestamp());
                    message.editMessageEmbeds(embed.build()).queue();
                }
            }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, e -> {}));
        }catch (Exception ex){
            
        }
    }
}
