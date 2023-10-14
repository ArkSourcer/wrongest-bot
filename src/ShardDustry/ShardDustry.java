package ShardDustry;

import arc.*;
import arc.files.Fi;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.world.blocks.storage.*;

import com.google.gson.*;
import fr.xpdustry.javelin.JavelinEvent;
import fr.xpdustry.javelin.JavelinPlugin;
import fr.xpdustry.javelin.JavelinSocket;
import static mindustry.Vars.dataDirectory;
import mindustry.game.EventType;
        
public class ShardDustry extends Plugin{

    
    public static Config config;
    
    public final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();
    
    //called when game initializes
    @Override
    public void init(){
        Fi cfg = dataDirectory.child("mods/ShardDustryCore.json");
        if (!cfg.exists()) {
            cfg.writeString(gson.toJson(config = new Config()));
            Log.info("Configuracion no encontrada, fue creado un archivo de configuracion en: (@)", cfg.absolutePath());
        } else {
            try {
                config = gson.fromJson(cfg.reader(), Config.class);
            } catch (Throwable t) {
                Log.err("Fallo al cargar el archivo de configuracion");
                Log.err(t);
            }
        }
        
        DiscordBot bot = new DiscordBot();
        if (config.activeDiscordBot) {
            bot.init();
        }
        
        JavelinSocket socket = JavelinPlugin.getJavelinSocket();

        socket.subscribe(MindustryChatEvent.class, e -> {
            if (config.activeDiscordBot){
                String parser = config.chatMessage;
                if (parser.contains("{p}")){
                    parser = parser.replace("{p}", e.getPlayerName());
                }
                if (parser.contains("{m}")){
                    parser = parser.replace("{m}", e.getMessage());
                }
                bot.bot.getTextChannelById(e.getChannelID()).sendMessage(parser).queue();
            }
        });
        socket.subscribe(MindustryLeaveEvent.class, e -> {
            if (config.activeDiscordBot) {
                String parser = config.leaveMessage;
                if (parser.contains("{p}")){
                    parser = parser.replace("{p}", e.getPlayerName());
                }
                if (parser.contains("{pc}")){
                    parser = parser.replace("{pc}", e.getPlayerCount()+"");
                }
                bot.bot.getTextChannelById(e.getChannelID()).sendMessage(parser).queue();
            }
        });
        socket.subscribe(MindustryJoinEvent.class, e -> {
            if (config.activeDiscordBot) {
                String parser = config.joinMessage;
                if (parser.contains("{p}")){
                    parser = parser.replace("{p}", e.getPlayerName());
                }
                if (parser.contains("{pc}")){
                    parser = parser.replace("{pc}", e.getPlayerCount()+"");
                }
                bot.bot.getTextChannelById(e.getChannelID()).sendMessage(parser).queue();
            }
        });
        
        //listen for a block selection event
        Events.on(BuildSelectEvent.class, event -> {
            if(!event.breaking && event.builder != null && event.builder.buildPlan() != null && event.builder.buildPlan().block == Blocks.thoriumReactor && event.builder.isPlayer()){
                //player is the unit controller
                Player player = event.builder.getPlayer();

                //send a message to everyone saying that this player has begun building a reactor
                Call.sendMessage("[scarlet]ALERT![] " + player.name + " has begun building a reactor at " + event.tile.x + ", " + event.tile.y);
            }
        });
        //add a chat filter that changes the contents of all messages
        //in this case, all instances of "heck" are censored
        Vars.netServer.admins.addChatFilter((player, text) -> text.replace("heck", "h*ck"));

        //add an action filter for preventing players from doing certain things
        Vars.netServer.admins.addActionFilter(action -> {
            //random example: prevent blast compound depositing
            if(action.type == ActionType.depositItem && action.item == Items.blastCompound && action.tile.block() instanceof CoreBlock){
                action.player.sendMessage("Example action filter: Prevents players from depositing blast compound into the core.");
                return false;
            }
            return true;
        });
    }
    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("reactors", "List all thorium reactors in the map.", args -> {
            for(int x = 0; x < Vars.world.width(); x++){
                for(int y = 0; y < Vars.world.height(); y++){
                    //loop through and log all found reactors
                    //make sure to only log reactor centers
                    if(Vars.world.tile(x, y).block() == Blocks.thoriumReactor && Vars.world.tile(x, y).isCenter()){
                        Log.info("Reactor at @, @", x, y);
                    }
                }
            }
        });
        handler.register("reload-shardustry-config", "Actualiza la configuracion de shardustry", args -> {
           try {
               config = gson.fromJson(dataDirectory.child("mods/ShardDustryCore.json").readString(), Config.class);
           } catch (Throwable t){
               Log.err("Hubo un error al cargar el archivo de configuracion");
               Log.err(t);
           }
        });
    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){

        //register a simple reply command
        handler.<Player>register("reply", "<text...>", "A simple ping command that echoes a player's text.", (args, player) -> {
            player.sendMessage("You said: [accent] " + args[0]);
        });
        
        //register a whisper command which can be used to send other players messages
        handler.<Player>register("whisper", "<player> <text...>", "Whisper text to another player.", (args, player) -> {
            //find player by name
            Player other = Groups.player.find(p -> p.name.equalsIgnoreCase(args[0]));

            //give error message with scarlet-colored text if player isn't found
            if(other == null){
                player.sendMessage("[scarlet]No player by that name found!");
                return;
            }

            //send the other player a message, using [lightgray] for gray text color and [] to reset color
            other.sendMessage("[lightgray](whisper) " + player.name + ":[] " + args[1]);
        });
    }
    
    public static void sendDiscordMessageEvent(String channelID, String message){
        if (JavelinPlugin.getJavelinSocket().getStatus() == JavelinSocket.Status.OPEN){
            JavelinPlugin.getJavelinSocket().sendEvent(new DiscordMessageEvent(channelID,message));
        }
    }
    
    public static void sendMindustryCosaEvent(String serverID, String message){
        if (JavelinPlugin.getJavelinSocket().getStatus() == JavelinSocket.Status.OPEN){
            JavelinPlugin.getJavelinSocket().sendEvent(new MindustryCosaEvent(serverID,message));
        }
    }
    
    public static final class MindustryChatEvent implements JavelinEvent {

        private final String channelID;
        private final String playerName;
        private final String message;

        public MindustryChatEvent(final String channelID, final String playerName, final String message) {
            this.channelID = channelID;
            this.playerName = playerName;
            this.message = message;
        }

        public String getChannelID() {
            return channelID;
        }

        public String getMessage() {
            return message;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    public static final class MindustryLeaveEvent implements JavelinEvent {

        private final String channelID;
        private final String playerName;
        private final int playerCount;

        public MindustryLeaveEvent(final String channelID, final String playerName, final int playerCount) {
            this.channelID = channelID;
            this.playerName = playerName;
            this.playerCount = playerCount;
        }

        public String getChannelID() {
            return channelID;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getPlayerCount() {
            return playerCount;
        }
    }

    public static final class MindustryJoinEvent implements JavelinEvent {

        private final String channelID;
        private final String playerName;
        private final int playerCount;

        public MindustryJoinEvent(final String channelID, final String playerName, final int playerCount) {
            this.channelID = channelID;
            this.playerName = playerName;
            this.playerCount = playerCount;
        }

        public String getChannelID() {
            return channelID;
        }

        public String getPlayerName() {
            return playerName;
        }

        public int getPlayerCount() {
            return playerCount;
        }
    }
    
    public static final class DiscordMessageEvent implements JavelinEvent {
        
        private final String channelID;
        private final String message;
        
        public DiscordMessageEvent(final String channelID, final String message) {
            this.channelID = channelID;
            this.message = message;
        }
        
        public String getChannelID() {
            return channelID;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    public static final class MindustryCosaEvent implements JavelinEvent {
        
        private final String serverID;
        private final String message;
        
        public MindustryCosaEvent(final String serverID, final String message){
            this.serverID = serverID;
            this.message = message;
        }
        
        public String getServerID(){
            return serverID;
        }
        
        public String getMessage(){
            return message;
        }
    }
}
