package ShardDustry;

import arc.*;
import arc.files.Fi;
import arc.util.*;
import arc.util.Timer.Task;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.net.Administration.*;
import mindustry.world.blocks.storage.*;

import com.google.gson.*;
import com.google.gson.JsonObject;
import fr.xpdustry.javelin.JavelinEvent;
import fr.xpdustry.javelin.JavelinPlugin;
import fr.xpdustry.javelin.JavelinSocket;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mindustry.Vars.dataDirectory;
import mindustry.game.EventType;

import ShardDustry.JavelinClasses.*;
import java.awt.Color;
import java.util.Map;
import java.util.Set;
        
public class ShardDustry extends Plugin{

    public String configPath = "mods/ShardDustryCore.json";
    public static Config config;
    public Fi cfg = dataDirectory.child(configPath);
    
    public final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();
    
    public static JsonObject json;
    
    //called when game initializes
    @Override
    public void init(){
        if (!cfg.exists()) {
            cfg.writeString(gson.toJson(config = new Config()));
            Log.info("Configuracion no encontrada, fue creado un archivo de configuracion en: (@)", cfg.absolutePath());
            config = gson.fromJson(cfg.reader(), Config.class);
            json = gson.fromJson(cfg.reader(), JsonObject.class);
        } else {
            try {
                config = gson.fromJson(cfg.reader(), Config.class);
                json = gson.fromJson(cfg.reader(), JsonObject.class);
                cfg.reader().close();
            } catch (Throwable t) {
                Log.err("Fallo al cargar el archivo de configuracion");
                Log.err(t);
            }
        }

        Timer timer = new Timer();
        
        if (config.activeDiscordBot) {
            DiscordBot.init();   
        }
        
        JavelinSocket socket = JavelinPlugin.getJavelinSocket();
        
        Task serverStatus = new Task() {
            @Override
            public void run() {
                socket.sendEvent(new MindustryStatusRequestEvent());
                Log.info("Fue enviado un evento de solicitud de estado");
                
            }
        };
        Task serverStatusCheck = new Task() {
            @Override
            public void run() {
                for (Object key : config.statusIDs.keySet()){
                    
                }
                Log.info("Se reviso si se actualizo el mensaje");
            }
        };
        
        timer.scheduleTask(serverStatus, 60,60,-1);
        timer.scheduleTask(serverStatusCheck, 80,60,-1);
        
        socket.subscribe(MindustryChatEvent.class, e -> {
            if (config.activeDiscordBot){
                String parser = config.chatMessage;
                if (parser.contains("{p}")){
                    parser = parser.replace("{p}", e.getPlayerName());
                }
                if (parser.contains("{m}")){
                    parser = parser.replace("{m}", e.getMessage());
                }
                DiscordBot.sendMessage(e.getChannelID(), parser);
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
                DiscordBot.sendMessage(e.getChannelID(), parser);
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
                DiscordBot.sendMessage(e.getChannelID(), parser);
            }
        });
        
        socket.subscribe(MindustryExecuteResponseEvent.class, e -> {
            if (config.activeDiscordBot) {
                DiscordBot.sendMessage(config.commandChannelID, e.getServerID() + ": " + e.getResponse());
            }
        });
        
        socket.subscribe(MindustryStatusResponseEvent.class, e -> {
            DiscordBot.sendMessageAsEmbed(config.commandChannelID, "Estado del servidor: " + e.getServerID(), Color.green, null,
                    "Mapa: " + e.getMapName() + "\n" + "Oleada: " + e.getWave() + "\n" + "Tiempo de Juego: " + e.getPlayedTime() + "\n" + "Jugadores: " + e.getPlayers(), null, null,e.getStateID());
            Log.info("La informacion fue mostrada");
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
            reloadConfig();
        });
        
        handler.register("modify", "<parametro> <valor...>", "modifica un parametro del archivo de configuracion", args -> {
            if (!json.has(args[0])) {
                Log.info("El archivo no cuenta con esa propiedad!");
                return;
            }
            
            if (!cfg.exists()){
                Log.info("No existe el documento");
                return;
            }
            Field[] fields = config.getClass().getDeclaredFields();
            for (Field field : fields){
                
            }
            if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false")){
                json.addProperty(args[0], Boolean.parseBoolean(args[1]));
            }else {
                json.addProperty(args[0], args[1]);
            }
            char[] chA = args[1].toCharArray();
            boolean valid = true;
            for (char c : chA){
                if (!Character.isDigit(c)){
                    if (c != '.'){
                        valid = false;
                    }
                }
            }
            Number num = null;
            try {
                num = Float.parseFloat(args[1]);
            } catch (NumberFormatException e) {
                try {
                    num = Double.parseDouble(args[1]);
                } catch (NumberFormatException e1) {
                    try {
                        num = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e2) {
                        try {
                            num = Float.parseFloat(args[1]);
                        } catch (NumberFormatException e3) {
                            Log.info("La entrada no es valida!");
                        }
                    }
                }
            }
            if (num != null){
                json.addProperty(args[0], num);
            }
            
            String jsonActualizado = gson.toJson(json);
            if (cfg.exists()){
                cfg.writeString(jsonActualizado);
            }else {
                Log.info("No existe el documento");
            }
        });
        
        handler.register("configlist","muestra una lista de parametros", args -> {
            String txt = "";
            Field[] fields = config.getClass().getDeclaredFields();
            for (Field field : fields) {
                txt += field.getName() + " " + field.getType().getName() + "\n";
            }
            Log.info("mostrando valores usables: \n" + txt);
        });
        
        handler.register("configvalues", "muestra la lista de parametros con sus valores", args -> {
            Field[] fields = config.getClass().getDeclaredFields();
            Set<Map.Entry<String, JsonElement>> entrySet = json.entrySet();
               for (Map.Entry<String, JsonElement> entry : entrySet) {
                String key = entry.getKey();
                Object value = entry.getValue();

                System.out.println(key + ": " + value);
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
    
    public void reloadConfig(){
        try {
            config = gson.fromJson(dataDirectory.child(configPath).readString(), Config.class);
        } catch (Throwable t) {
            Log.err("Hubo un error al cargar el archivo de configuracion");
            Log.err(t);
        }
    }
    
    public static boolean isJavelinOpen(){
        return JavelinPlugin.getJavelinSocket().getStatus() == JavelinSocket.Status.OPEN;
    }
    
    public static void sendDiscordMessageEvent(String channelID, String message){
        if (isJavelinOpen()){
            JavelinPlugin.getJavelinSocket().sendEvent(new DiscordMessageEvent(channelID,message));
        }
    }
    
    public static void sendMindustryCosaEvent(String serverID, String message){
        if (isJavelinOpen()){
            JavelinPlugin.getJavelinSocket().sendEvent(new MindustryCosaEvent(serverID,message));
        }
    }
    
    public static void sendMindustryInfoRequestEvent(String serverID, String identificador){
        if (isJavelinOpen()){
            JavelinPlugin.getJavelinSocket().sendEvent(new MindustryInfoRequestEvent(serverID,identificador));
        }
    }
    
    public static void sendMindustryExecuteRequestEvent(String serverID, String code){
        if (isJavelinOpen()){
            JavelinPlugin.getJavelinSocket().sendEvent(new MindustryExecuteRequestEvent(serverID,code));
        }
    }
    
    public static void sendEditPropertyEvent(String property, String value){
        if (isJavelinOpen()){
            JavelinPlugin.getJavelinSocket().sendEvent(new EditPropertyEvent(property, value));
        }
    }

    private void addProperty(String arg, String arg0) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
