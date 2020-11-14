package fw.fwguilds;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class AntyLogout implements Listener {
    public static HashMap<Player, BukkitTask> AntyLogouts = new HashMap<Player, BukkitTask>();
    public static HashMap<Player, BossBar> Bossbars = new HashMap<Player, BossBar>();

    public static void AddPlayerToAntyLogout(Player p){
        if(AntyLogouts.containsKey(p)){
            AntyLogouts.get(p).cancel();
            AntyLogouts.remove(p);
        }

        if(Bossbars.containsKey(p)){
            Bossbars.get(p).removeAll();
            Bossbars.remove(p);
        }

        BukkitTask bt = new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if(!p.isOnline()) this.cancel();

                if(i < 15){
                    if(!Bossbars.containsKey(p)){
                        BossBar bossBar = Bukkit.getServer().createBossBar("ANTY LOGOUT (15s)", BarColor.BLUE, BarStyle.SOLID);
                        bossBar.setColor(BarColor.PURPLE);
                        bossBar.addPlayer(p);
                        bossBar.setProgress((15-i)/15D);
                        Bossbars.put(p, bossBar);
                    }
                    else{
                        BossBar bossBar = Bossbars.get(p);
                        bossBar.setTitle("ANTY LOGOUT (" + (15-i) + "s)");
                        bossBar.setProgress((15-i)/15D);
                    }
                }
                else if(i == 15){
                    if(Bossbars.containsKey(p)) {
                        BossBar bossBar = Bossbars.get(p);
                        bossBar.setVisible(false);
                        bossBar.removeAll();
                        Bossbars.remove(p);
                    }
                    AntyLogouts.remove(p);
                }

                i++;
            }
        }.runTaskTimer(getPlugin(FwGuilds.class), 0, 20);

        AntyLogouts.put(p, bt);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if(AntyLogouts.containsKey(event.getPlayer())){
            event.setQuitMessage(ChatColor.YELLOW + "Gracz " + event.getPlayer().getDisplayName() + ChatColor.RESET + ChatColor.YELLOW + " wylogowal sie podczas walki!");
            event.getPlayer().setHealth(0.0);
            AntyLogouts.remove(event.getPlayer());
            Bossbars.remove(event.getPlayer());
        }
    }
}
