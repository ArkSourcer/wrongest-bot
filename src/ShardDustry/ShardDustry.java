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
import fr.xpdustry.javelin.JavelinPlugin;
import fr.xpdustry.javelin.JavelinSocket;
import java.lang.reflect.Field;
import static mindustry.Vars.dataDirectory;

import ShardDustry.JavelinClasses.*;
import java.awt.Color;
import java.util.Map;
import java.util.Set;
        
public class ShardDustry extends Plugin{

    public static String configPath = "mods/ShardDustryCore.json";
    public static Config config;
    public static Fi cfg = dataDirectory.child(configPath);
    
    public static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
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
        
        boolean newFields = false;
        Field[] fields = config.getClass().getFields();
        for (Field field : fields) {
            if (!json.has(field.getName())) {
                newFields = true;
                try {
                    json.add(field.getName(), gson.toJsonTree(field.get(config),field.getType()));
                    Log.info("Se anadio el field " + field.getName() + " debido a que no se encontraba en el archivo");
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Log.err(ex);
                }
                
            }
        }
        if (newFields) {
            String jsonActualizado = gson.toJson(json);
            if (cfg.exists()) {
                cfg.writeString(jsonActualizado);
                reloadConfig();
            } else {
                Log.info("No existe el documento");
            }
        }
        
        if (config.activeDiscordBot) {
            DiscordBot.init();
        }
        
        JavelinSocket socket = JavelinPlugin.getJavelinSocket();
        
        if (config.activeStatusCheck) {
            Task serverStatus = new Task() {
                @Override
                public void run() {
                    socket.sendEvent(new MindustryStatusRequestEvent());
                    Log.debug("Fue enviado un evento de solicitud de estado");
                }
            };
            Task serverStatusCheck = new Task() {
                @Override
                public void run() {
                    for (Map.Entry<String, String> entry : config.statusIDs.entrySet()) {
                        DiscordBot.setDisabledStatus(entry.getKey(),entry.getValue(),config.statusCooldownMinutes);
                    }
                }
            };
            timer.scheduleTask(serverStatus, 20, config.statusCooldownMinutes*60, -1);
            timer.scheduleTask(serverStatusCheck, 40, config.statusCooldownMinutes*60, -1);
        }
        
        socket.subscribe(MindustryChatEvent.class, e -> {
            if (config.activeDiscordBot){
                String parser = config.chatMessage;
                if (parser.contains("{p}")){
                    parser = parser.replace("{p}", e.playerName);
                }
                if (parser.contains("{m}")){
                    parser = parser.replace("{m}", e.message);
                }
                DiscordBot.sendMessage(e.channelID, parser);
            }
        });
        socket.subscribe(MindustryLeaveEvent.class, e -> {
            if (config.activeDiscordBot) {
                String parser = config.leaveMessage;
                if (parser.contains("{p}")){
                    parser = parser.replace("{p}", e.playerName);
                }
                if (parser.contains("{pc}")){
                    parser = parser.replace("{pc}", e.playerCount+"");
                }
                DiscordBot.sendMessage(e.channelID, parser);
            }
        });
        socket.subscribe(MindustryJoinEvent.class, e -> {
            if (config.activeDiscordBot) {
                String parser = config.joinMessage;
                if (parser.contains("{p}")){
                    parser = parser.replace("{p}", e.playerName);
                }
                if (parser.contains("{pc}")){
                    parser = parser.replace("{pc}", e.playerCount+"");
                }
                DiscordBot.sendMessage(e.channelID, parser);
            }
        });
        
        socket.subscribe(MindustryExecuteResponseEvent.class, e -> {
            if (config.activeDiscordBot) {
                DiscordBot.sendMessage(config.commandChannelID, e.serverID + ": " + e.response);
            }
        });
        
        socket.subscribe(MindustryStatusResponseEvent.class, e -> {
            Log.debug("Se recibio el estado de un servidor: " + e.displayName);
            DiscordBot.sendMessageAsEmbed(config.statusChannelID, e.displayName, Color.green, null,
                    "Mapa: " + e.mapName + "\n" + "Oleada: " + e.wave + "\n" + "Tiempo de Juego: " + e.playedTime + "\n" + "Jugadores: " + e.players, null, null,e.stateID);
        });
        
        socket.subscribe(PropertyListResponseEvent.class, e -> {
            DiscordBot.sendMessageAsEmbedL(config.commandChannelID, e.serverID, Color.CYAN, "ShardDustry", e.params);
        });
    }
    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("reload-config", "Actualiza la configuracion de shardustry", args -> {
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
            Set<Map.Entry<String, JsonElement>> entrySet = json.entrySet();
               for (Map.Entry<String, JsonElement> entry : entrySet) {
                String key = entry.getKey();
                Object value = entry.getValue();

                System.out.println(key + ": " + value);
            }
        });
    }
    
    public static void reloadConfig(){
        try {
            config = gson.fromJson(dataDirectory.child(configPath).readString(), Config.class);
        } catch (Throwable t) {
            Log.err("Hubo un error al cargar el archivo de configuracion");
            Log.err(t);
        }
    }
    public static void writeConfig() {
        try {
            String jsonActualizado = gson.toJson(json);
            if (cfg.exists()) {
                cfg.writeString(jsonActualizado);
                reloadConfig();
            } else {
                Log.info("No existe el documento");
            }
        } catch (Exception e) {
            Log.info(e);
        }
    }
    
    public static void addServerKey(String key, String value){
        config.statusIDs.putIfAbsent(key, value);
        json.add("statusIDs", gson.toJsonTree(config.statusIDs, config.statusIDs.getClass()));
        writeConfig();
    }
    
    public boolean editProperty(String property, String value, String dataType){
        switch(dataType){
            
            case "boolean":
                
                break;
                
            case "integer":
                
                break;
                
            case "string":
                
                break;
                
            case "array":
                
                break;
        }
        
        return true;
    }
    
    public static boolean isJavelinOpen(){
        return JavelinPlugin.getJavelinSocket().getStatus() == JavelinSocket.Status.OPEN;
    }
    
    public static void sendDiscordMessageEvent(String channelID, String message){
        if (isJavelinOpen()){
            JavelinPlugin.getJavelinSocket().sendEvent(new DiscordMessageEvent(channelID,message));
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
    
    public static void sendPropertyListRequestEvent(String serverID){
        if (isJavelinOpen()){
            JavelinPlugin.getJavelinSocket().sendEvent(new PropertyListRequestEvent(serverID));
        }
    }

    private void addProperty(String arg, String arg0) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
