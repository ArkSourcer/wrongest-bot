package ShardDustry;

import fr.xpdustry.javelin.JavelinEvent;

public class JavelinClasses {

    public static final class MindustryChatEvent implements JavelinEvent {

        public final String channelID;
        public final String playerName;
        public final String message;

        public MindustryChatEvent(final String channelID, final String playerName, final String message) {
            this.channelID = channelID;
            this.playerName = playerName;
            this.message = message;
        }
    }

    public static final class MindustryLeaveEvent implements JavelinEvent {

        public final String channelID;
        public final String playerName;
        public final int playerCount;

        public MindustryLeaveEvent(final String channelID, final String playerName, final int playerCount) {
            this.channelID = channelID;
            this.playerName = playerName;
            this.playerCount = playerCount;
        }
    }

    public static final class MindustryJoinEvent implements JavelinEvent {

        public final String channelID;
        public final String playerName;
        public final int playerCount;

        public MindustryJoinEvent(final String channelID, final String playerName, final int playerCount) {
            this.channelID = channelID;
            this.playerName = playerName;
            this.playerCount = playerCount;
        }
    }

    public static final class DiscordMessageEvent implements JavelinEvent {

        public final String channelID;
        public final String message;

        public DiscordMessageEvent(final String channelID, final String message) {
            this.channelID = channelID;
            this.message = message;
        }
    }

    public static final class MindustryInfoRequestEvent implements JavelinEvent {

        public final String serverID;
        public final String identifier;

        public MindustryInfoRequestEvent(final String serverID, final String identifier) {
            this.serverID = serverID;
            this.identifier = identifier;
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

        public final String serverID;
        public final String code;

        public MindustryExecuteRequestEvent(final String serverID, final String code) {
            this.serverID = serverID;
            this.code = code;
        }
    }

    public static final class MindustryExecuteResponseEvent implements JavelinEvent {

        public final String serverID;
        public final String response;

        public MindustryExecuteResponseEvent(final String serverID, final String response) {
            this.serverID = serverID;
            this.response = response;
        }
    }

    public static final class MindustryStatusRequestEvent implements JavelinEvent {

        public MindustryStatusRequestEvent() {

        }
    }

    public static final class MindustryStatusResponseEvent implements JavelinEvent {
        public final String displayName;
        public final String serverID;
        public final String mapName;
        public final String wave;
        public final String players;
        public final String playedTime;
        public final String stateID;

        public MindustryStatusResponseEvent(final String displayName, final String serverID, final String mapName, final String wave, final String players, final String playedTime, final String stateID) {
            this.displayName = displayName;
            this.serverID = serverID;
            this.mapName = mapName;
            this.wave = wave;
            this.players = players;
            this.playedTime = playedTime;
            this.stateID = stateID;
        }
    }
    
    public static final class EditPropertyEvent implements JavelinEvent {
        public final String property;
        public final String value;
        
        public EditPropertyEvent(final String property, final String value){
            this.property = property;
            this.value = value;
        }
    }
    
    public static final class PropertyListRequestEvent implements JavelinEvent {

        public final String serverID;

        public PropertyListRequestEvent(final String serverID) {
            this.serverID = serverID;
        }
    }
    
    public static final class PropertyListResponseEvent implements JavelinEvent {
        public final String serverID;
        public final String params;
        
        public PropertyListResponseEvent(String serverID, String params) {
            this.serverID = serverID;
            this.params = params;
        }
    }
}