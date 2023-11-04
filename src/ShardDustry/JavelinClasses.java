package ShardDustry;

import fr.xpdustry.javelin.JavelinEvent;

public class JavelinClasses {

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

        public MindustryCosaEvent(final String serverID, final String message) {
            this.serverID = serverID;
            this.message = message;
        }

        public String getServerID() {
            return serverID;
        }

        public String getMessage() {
            return message;
        }
    }

    public static final class MindustryInfoRequestEvent implements JavelinEvent {

        private final String serverID;
        private final String identifier;

        public MindustryInfoRequestEvent(final String serverID, final String identifier) {
            this.serverID = serverID;
            this.identifier = identifier;
        }

        public String getServerID() {
            return serverID;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    /*public static final class MindustryInfoResponseEvent implements JavelinEvent {

        public MindustryInfoResponseEvent(final String serverID, final String identifier) {
            this.serverID = serverID;
            this.identifier = identifier;
        }

        public String getServerID() {
            return serverID;
        }

        public String getIdentifier() {
            return identifier;
        }
    }*/
    public static final class MindustryExecuteRequestEvent implements JavelinEvent {

        private final String serverID;
        private final String code;

        public MindustryExecuteRequestEvent(final String serverID, final String code) {
            this.serverID = serverID;
            this.code = code;
        }

        public String getServerID() {
            return serverID;
        }

        public String getCode() {
            return code;
        }
    }

    public static final class MindustryExecuteResponseEvent implements JavelinEvent {

        private final String serverID;
        private final String response;

        public MindustryExecuteResponseEvent(final String serverID, final String response) {
            this.serverID = serverID;
            this.response = response;
        }

        public String getServerID() {
            return serverID;
        }

        public String getResponse() {
            return response;
        }
    }

    public static final class MindustryStatusRequestEvent implements JavelinEvent {

        public MindustryStatusRequestEvent() {

        }
    }

    public static final class MindustryStatusResponseEvent implements JavelinEvent {

        private final String serverID;
        private final String mapName;
        private final String wave;
        private final String players;
        private final String playedTime;
        private final String stateID;

        public MindustryStatusResponseEvent(final String serverID, final String mapName, final String wave, final String players, final String playedTime, final String stateID) {
            this.serverID = serverID;
            this.mapName = mapName;
            this.wave = wave;
            this.players = players;
            this.playedTime = playedTime;
            this.stateID = stateID;
        }

        public String getServerID() {
            return serverID;
        }

        public String getMapName() {
            return mapName;
        }

        public String getWave() {
            return wave;
        }

        public String getPlayers() {
            return players;
        }

        public String getPlayedTime() {
            return playedTime;
        }
        
        public String getStateID(){
            return stateID;
        }
    }
    
    public static final class EditPropertyEvent implements JavelinEvent {
        private final String property;
        private final String value;
        
        public EditPropertyEvent(final String property, final String value){
            this.property = property;
            this.value = value;
        }
        
        public String getProperty(){
            return property;
        }
        
        public String getValue(){
            return value;
        }
    }
}
