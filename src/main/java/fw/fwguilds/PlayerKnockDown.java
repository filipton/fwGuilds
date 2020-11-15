package fw.fwguilds;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class PlayerKnockDown implements Listener {
    public static HashMap<Player, Integer> KnockedDownPlayers = new HashMap<>();
    public static HashMap<Player, BukkitTask> KnockedDownPlayersCoutDownTasks = new HashMap<>();

    @EventHandler
    public void onPlayerGlide(EntityToggleGlideEvent event) {
        if(KnockedDownPlayers.containsKey(event.getEntity()) && !event.isGliding()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if(KnockedDownPlayers.containsKey(event.getPlayer())){
            event.getPlayer().setVelocity(new Vector(0, 0, 0));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player){
            Player p = (Player)event.getEntity();

            if(PlayerKnockDown.KnockedDownPlayers.containsKey(p)){
                if(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK){
                    event.setCancelled(true);
                }
            }
            else{
                if(p.getHealth() - event.getFinalDamage() <= 0){
                    event.setCancelled(true);
                    p.setHealth(20);
                    PlayerKnockDown.KnockDownPlayer(p);
                }
            }
        }
    }

    public static void KnockDownPlayer(Player p){
        KnockedDownPlayers.put(p, 60);

        Location l = p.getLocation();
        l.setY(l.getBlockY());
        l.setPitch(0);
        p.teleport(l);
        p.setGliding(true);
        p.setVelocity(new Vector(0.0, 0.0, 0.0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1200, 255,false, false));

        BukkitTask bt = new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if(!p.isOnline()) this.cancel();

                if(i < 60){
                    p.sendTitle("ZOSTALES POWALONY", "ZOSTALO CI " + (60-i) + " SEKUND!", 0, 20, 10);
                }
                else if(i == 60){
                    KnockedDownPlayers.remove(p);
                    p.setHealth(0);
                }

                i++;
            }
        }.runTaskTimer(getPlugin(FwGuilds.class), 0, 20);
        KnockedDownPlayersCoutDownTasks.put(p, bt);
    }

    public static void UnKnockDownPlayer(Player p){
        if(KnockedDownPlayers.containsKey(p)){
            p.removePotionEffect(PotionEffectType.SLOW);
            if(KnockedDownPlayersCoutDownTasks.containsKey(p)){
                KnockedDownPlayersCoutDownTasks.get(p).cancel();
                KnockedDownPlayersCoutDownTasks.remove(p);
            }
            KnockedDownPlayers.remove(p);
        }
    }
}