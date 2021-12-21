package fw.fwguilds;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class PlayerKnockDown implements Listener {
    public static HashMap<Player, Integer> KnockedDownPlayers = new HashMap<>();
    public static HashMap<Player, BukkitTask> KnockedDownPlayersCountDownTasks = new HashMap<>();
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
        KnockedDownPlayersCountDownTasks.put(p, bt);
    }

    public static void UnKnockDownPlayer(Player p){
        if(KnockedDownPlayers.containsKey(p)){
            p.removePotionEffect(PotionEffectType.SLOW);
            endPose(p);
            if(KnockedDownPlayersCountDownTasks.containsKey(p)){
                KnockedDownPlayersCountDownTasks.get(p).cancel();
                KnockedDownPlayersCountDownTasks.remove(p);
            }
            KnockedDownPlayers.remove(p);
        }
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
                    p.sendBlockChange(next.getLocation(), Bukkit.createBlockData(Material.BARRIER));
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
                    if(e.getPlayer().getLevel() < 5){
                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "NIE MASZ WYSTARCZAJACEGO LEWELU ABY PODNIESC GRACZA!"));
                        return;
                    }

                    BukkitTask bt = new BukkitRunnable() {
                        @Override
                        public void run() {
                            if(!(getLookingAt(e.getPlayer(), p) && e.getPlayer().getLocation().distance(p.getLocation()) <= 2) || !e.getPlayer().isSneaking()) {
                                RevivingPlayers.remove(e.getPlayer());
                                KnockedDownPlayers.replace(p, 0);
                                this.cancel();
                                return;
                            }


                            if(KnockedDownPlayers.containsKey(p)){
                                KnockedDownPlayers.replace(p, KnockedDownPlayers.get(p)+1);

                                if(KnockedDownPlayers.get(p) % 20 == 0){
                                    e.getPlayer().setLevel(e.getPlayer().getLevel()-1);
                                }

                                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("PODNOSZENIE: " + KnockedDownPlayers.get(p) + "%"));
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

    private boolean getLookingAt(Player player, Player player1) {
        Location eye = player.getEyeLocation();
        Vector toEntity = player1.getEyeLocation().toVector().subtract(eye.toVector());
        double dot = toEntity.normalize().dot(eye.getDirection());

        return dot > 0.94D;
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
        p.sendBlockChange(b.getLocation(), Bukkit.createBlockData(Material.BARRIER));
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
        p.sendBlockChange(b.getLocation(), b.getBlockData());
        double health = 10;
        p.setHealth(health);
    }
}