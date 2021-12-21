package fw.fwguilds;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DeathAndDamageHandler implements Listener {

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity damager = event.getDamager();

        if(victim instanceof Player && damager instanceof Player){
            if(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK){
                AntyLogout.AddPlayerToAntyLogout((Player) damager);
                AntyLogout.AddPlayerToAntyLogout((Player) victim);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event){
        event.setKeepInventory(true);
        event.getDrops().clear();

        event.getEntity().sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "UMARLES NA POZYCJI X: " + event.getEntity().getLocation().getBlockX() + ", Y: " + event.getEntity().getLocation().getBlockY() + ", Z: " + event.getEntity().getLocation().getBlockZ());

        if(FwGuilds.ActiveHalfDrop){
            Player player = event.getEntity();

            player.setExp(0);
            player.setLevel(0);

            //HALF DROP BETTER WAY
            List<Integer> itemsIds = new ArrayList<>();

            for(int i = 0; i < player.getInventory().getContents().length; i++){
                ItemStack is = player.getInventory().getContents()[i];

                if(is != null){
                    itemsIds.add(i);
                }
            }

            Collections.shuffle(itemsIds);

            for(int iId = 0; iId < itemsIds.size()/2; iId++){
                ItemStack item = player.getInventory().getContents()[itemsIds.get(iId)];

                player.getInventory().setItem(itemsIds.get(iId), null);
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }

        //head drop
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(event.getEntity());
        meta.setDisplayName(ChatColor.GREEN + "Glowa " + event.getEntity().getName());
        skull.setItemMeta(meta);

        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), skull);
        return;
    }
}