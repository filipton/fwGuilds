package fw.fwguilds;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class PlayerKnockDown implements Listener {
    public static HashMap<Player, Integer> KnockedDownPlayers = new HashMap<>();
    public static HashMap<Player, BukkitTask> KnockedDownPlayersCoutDownTasks = new HashMap<>();
    public static HashMap<Player, Player> RevivingPlayers = new HashMap<>();

    public static int KnockDownTime = 30;

    public static void KnockDownPlayer(Player p){
        startPose(p);

        KnockedDownPlayers.put(p, 0);

        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1200, 255,false, false));

        BukkitTask bt = new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if(!p.isOnline()) this.cancel();

                if(i < KnockDownTime && !RevivingPlayers.containsValue(p)){
                    p.sendTitle("ZOSTALES POWALONY", "ZOSTALO CI " + (KnockDownTime-i) + " SEKUND!", 0, 20, 10);
                }
                else if(i == KnockDownTime){
                    UnKnockDownPlayer(p);
                    p.setHealth(0);
                    this.cancel();
                }

                if(!RevivingPlayers.containsValue(p)){
                    i++;
                }
            }
        }.runTaskTimer(getPlugin(FwGuilds.class), 0, 20);
        KnockedDownPlayersCoutDownTasks.put(p, bt);
    }

    public static void UnKnockDownPlayer(Player p){
        if(KnockedDownPlayers.containsKey(p)){
            p.removePotionEffect(PotionEffectType.SLOW);
            endPose(p);
            if(KnockedDownPlayersCoutDownTasks.containsKey(p)){
                KnockedDownPlayersCoutDownTasks.get(p).cancel();
                KnockedDownPlayersCoutDownTasks.remove(p);
            }
            KnockedDownPlayers.remove(p);
        }
    }

    public static Entity getNearestEntityInSight(Player player, int range) {
        ArrayList<Entity> entities = (ArrayList<Entity>) player.getNearbyEntities(range, range, range);
        ArrayList<Block> sightBlock = (ArrayList<Block>) player.getLineOfSight( (Set<Material>) null, range);
        ArrayList<Location> sight = new ArrayList<Location>();
        for (int i = 0;i<sightBlock.size();i++)
            sight.add(sightBlock.get(i).getLocation());
        for (int i = 0;i<sight.size();i++) {
            for (int k = 0;k<entities.size();k++) {
                if (Math.abs(entities.get(k).getLocation().getX()-sight.get(i).getX())<1.3) {
                    if (Math.abs(entities.get(k).getLocation().getY()-sight.get(i).getY())<1.5) {
                        if (Math.abs(entities.get(k).getLocation().getZ()-sight.get(i).getZ())<1.3) {
                            return entities.get(k);
                        }
                    }
                }
            }
        }
        return null; //Return null/nothing if no entity was found
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        PlayerKnockDown.UnKnockDownPlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        final Player p = e.getPlayer();
        if (KnockedDownPlayers.containsKey(p) && this.isNextBlock(e.getFrom(), e.getTo())) {
            final Block next = e.getTo().getBlock().getRelative(BlockFace.UP);
            final Block old = e.getFrom().getBlock().getRelative(BlockFace.UP);
            if (next.getType().equals((Object)Material.AIR) || !next.getType().isSolid()) {
                if (!next.isLiquid() && !e.getTo().getBlock().isLiquid()) {
                    p.sendBlockChange(next.getLocation(), Material.BARRIER, (byte)0);
                }
                p.sendBlockChange(old.getLocation(), old.getBlockData());
            }
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
                if(event instanceof EntityDamageByEntityEvent){
                    EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;
                    Entity damager = e.getDamager();
                    if(KnockedDownPlayers.containsKey(damager)){
                        event.setCancelled(true);
                        return;
                    }
                }

                if(event.getFinalDamage() >= p.getHealth()){
                    event.setCancelled(true);
                    p.setHealth(10);
                    PlayerKnockDown.KnockDownPlayer(p);
                }
            }
        }
    }

    @EventHandler
    public void onToggleSwimming(EntityToggleSwimEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player p = (Player)e.getEntity();
            if (KnockedDownPlayers.containsKey(p)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent e) {
        final Player p = e.getPlayer();
        if (KnockedDownPlayers.containsKey(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerClickPlayer(PlayerInteractEntityEvent e) {
        if(e.getPlayer().isSneaking()){
            if(e.getRightClicked() instanceof Player && !KnockedDownPlayers.containsKey(e.getPlayer())){
                Player p = (Player) e.getRightClicked();

                if(!RevivingPlayers.containsKey(e.getPlayer())){
                    BukkitTask bt = new BukkitRunnable() {
                        @Override
                        public void run() {
                            if(getNearestEntityInSight(e.getPlayer(), 1) != e.getRightClicked() || !e.getPlayer().isSneaking()) {
                                RevivingPlayers.remove(e.getPlayer());
                                KnockedDownPlayers.replace(p, 0);
                                this.cancel();
                            }


                            if(KnockedDownPlayers.containsKey(p)){
                                KnockedDownPlayers.replace(p, KnockedDownPlayers.get(p)+1);

                                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("PODNOSZENIE: " + Math.round(KnockedDownPlayers.get(p) * 100.0/100.0) + "%"));
                                p.sendTitle("ZOSTALES POWALONY", "JESTES PODNOSZONY: " + Math.round(KnockedDownPlayers.get(p) * 100.0/100.0) + "%", 0, 20, 10);

                                if(KnockedDownPlayers.get(p) >= 100){
                                    RevivingPlayers.remove(e.getPlayer());
                                    UnKnockDownPlayer(p);
                                    this.cancel();
                                }
                            }
                        }
                    }.runTaskTimer(getPlugin(FwGuilds.class), 0, 1);
                    RevivingPlayers.put(e.getPlayer(), p);
                }
            }
        }
    }

    public boolean isNextBlock(Location from, Location to) {
        final int movX = from.getBlockX() - to.getBlockX();
        final int movZ = from.getBlockZ() - to.getBlockZ();
        final int movY = from.getBlockY() - to.getBlockY();
        return Math.abs(movX) > 0 || Math.abs(movZ) > 0 || Math.abs(movY) > 0;
    }

    public static void startPose(final Player p) {
        p.setSwimming(true);
        p.setSprinting(true);
        final Block b = p.getLocation().getBlock().getRelative(BlockFace.UP);
        p.sendBlockChange(b.getLocation(), Material.BARRIER, (byte) 0);
        p.setGameMode(GameMode.ADVENTURE);
        p.setWalkSpeed(0);
        final Entity vehicle = p.getVehicle();
        if (vehicle != null) {
            vehicle.eject();
        }
    }

    public static void endPose(final Player p) {
        float ospeed = 0.19f;
        p.setWalkSpeed(ospeed);
        p.setGameMode(GameMode.SURVIVAL);
        KnockedDownPlayers.remove(p);
        p.setSwimming(false);
        p.setSprinting(false);
        final Block b = p.getLocation().getBlock().getRelative(BlockFace.UP);
        p.sendBlockChange(b.getLocation(), b.getType(), b.getData());
        double health = 10;
        p.setHealth(health);
    }
}