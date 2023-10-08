package ShardDustry;

public class EmojiReactor {
    public int triggerType;
    public String trigger;
    public String args;
    
    public EmojiReactor(int triggerType, String trigger, String args){
        this.triggerType = triggerType;
        this.trigger = trigger;
        this.args = args;
    }
}