package fw.fwguilds;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeathAndDamageHandler implements Listener {

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity damager = event.getDamager();

        if(victim instanceof Player && damager instanceof Player){
            if(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK){
                if(victim instanceof Player && damager instanceof Player){
                    AntyLogout.AddPlayerToAntyLogout((Player) damager);
                    AntyLogout.AddPlayerToAntyLogout((Player) victim);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event){
        //ala keepinventory
        //event.getEntity().sendMessage(event.getDrops().size() + "");

        event.getEntity().sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "UMARLES NA POZYCJI X: " + event.getEntity().getLocation().getBlockX() + ", Y: " + event.getEntity().getLocation().getBlockY() + ", Z: " + event.getEntity().getLocation().getBlockZ());

        if(FwGuilds.ActiveHalfDrop){
            Player player = event.getEntity();

            //xp
            //((ExperienceOrb)player.getWorld().spawn(player.getLocation(), ExperienceOrb.class)).setExperience(getExpAtLevel(player.getLevel())/3);
            player.setExp(0);
            player.setLevel(0);

            //armors
            List<ItemStack> itemsInArmorPlayer = new ArrayList<>();
            Random rand = new Random();

            for (ItemStack i : player.getInventory().getArmorContents()) {
                if (i != null) {
                    itemsInArmorPlayer.add(i);
                }
            }

            int ArmorToDrop = itemsInArmorPlayer.size()/2;
            if(itemsInArmorPlayer.size() == 1) ArmorToDrop = 1;

            for (int i = 0; i < ArmorToDrop; i++) {
                if(itemsInArmorPlayer.size() >= 1) {
                    int rng = rand.nextInt(itemsInArmorPlayer.size());

                    ItemStack itm = itemsInArmorPlayer.get(rng);

                    if(itm != null) {
                        if(itm.equals(player.getInventory().getHelmet())) {
                            player.getInventory().setHelmet(null);
                            player.getWorld().dropItemNaturally(player.getLocation(), itm);
                        }
                        else if(itm.equals(player.getInventory().getChestplate())) {
                            player.getInventory().setChestplate(null);
                            player.getWorld().dropItemNaturally(player.getLocation(), itm);
                        }
                        else if(itm.equals(player.getInventory().getLeggings())) {
                            player.getInventory().setLeggings(null);
                            player.getWorld().dropItemNaturally(player.getLocation(), itm);
                        }
                        else if(itm.equals(player.getInventory().getBoots())) {
                            player.getInventory().setBoots(null);
                            player.getWorld().dropItemNaturally(player.getLocation(), itm);
                        }
                        itemsInArmorPlayer.remove(rng);
                    }
                }
            }

            //items in eq
            List<ItemStack> itemsInPlayer = new ArrayList<>();

            rand = new Random();

            for (ItemStack i : player.getInventory().getContents()) {
                if (i != null) {
                    itemsInPlayer.add(i);
                }
            }

            itemsInPlayer.removeAll(itemsInArmorPlayer);

            int ItemsToDrop = itemsInPlayer.size()/2;
            if(itemsInPlayer.size() == 1) ItemsToDrop = 1;

            for (int i = 0; i < ItemsToDrop; i++) {
                int randomIndex = rand.nextInt(itemsInPlayer.size());
                ItemStack randomItem = itemsInPlayer.get(randomIndex);

                if(randomItem.equals(player.getInventory().getItemInOffHand())) {
                    player.getInventory().setItemInOffHand(null);
                    player.getWorld().dropItemNaturally(player.getLocation(), randomItem);
                }
                else{
                    player.getWorld().dropItemNaturally(player.getLocation(), randomItem);
                    player.getInventory().removeItem(randomItem);
                }

                itemsInPlayer.remove(randomIndex);
            }
        }

        //head drop
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(event.getEntity());
        meta.setDisplayName(ChatColor.GREEN + "Glowa " + event.getEntity().getName());
        skull.setItemMeta(meta);

        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), skull);
    }
}