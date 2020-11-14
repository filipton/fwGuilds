package fw.fwguilds.Expansions;

import fw.fwguilds.FwGuilds;
import fw.fwguilds.Structs.Guild;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.Map;

public class PAPIExp extends PlaceholderExpansion {
    private FwGuilds plugin;

    public PAPIExp(FwGuilds plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier(){
        return "fwguilds";
    }

    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        if(player == null){
            return "";
        }

        // %fwguilds_gname%
        if(identifier.equals("gname")){
            for(Map.Entry<String, Guild> guild : plugin.Guilds.entrySet()) {
                for(String mem : guild.getValue().members){
                    if(player.getName().equalsIgnoreCase(mem)){
                        return guild.getKey();
                    }
                }
            }
            return "BRAK";
        }

        if(identifier.equals("gcolor")){
            for(Map.Entry<String, Guild> guild : plugin.Guilds.entrySet()) {
                for(String mem : guild.getValue().members){
                    if(player.getName().equalsIgnoreCase(mem)){
                        return guild.getValue().TeamColor;
                    }
                }
            }
            return ChatColor.AQUA.toString();
        }

        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
        return null;
    }
}
