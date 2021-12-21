package fw.fwguilds;

import fw.fwguilds.Structs.CustomEnchantment;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomEnchants {
    public static final CustomEnchantment DROPPROTECTION = new CustomEnchantment("drop-prot", "Drop Protection", 1);

    public static List<CustomEnchantment> Enchantments = new ArrayList<CustomEnchantment>(
            Arrays.asList(DROPPROTECTION)
    );

    public static void register(){
        for(Enchantment e : Enchantments){
            boolean registered = Arrays.stream(Enchantment.values()).collect(Collectors.toList()).contains(e);

            if(!registered){
                registerEnchantments(e);
            }
        }
    }

    public static void registerEnchantments(Enchantment enchantment){
        boolean registered = true;
        try{
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            Enchantment.registerEnchantment(enchantment);
        } catch (Exception e) {
            registered = false;
            e.printStackTrace();
        }

        if(registered){

        }
    }

    public static void AddEnchantment(ItemStack item, CustomEnchantment e, int lvl){
        item.addUnsafeEnchantment(e, lvl);
        item.addEnchantment(e, lvl);

        ItemMeta m = item.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GREEN + e.getName());
        if(Objects.requireNonNull(m).hasLore()){
            lore.addAll(Objects.requireNonNull(m.getLore()));
        }

        m.setLore(lore);
        item.setItemMeta(m);
    }

    public static void RemoveEnchantment(ItemStack item, CustomEnchantment e){
        item.removeEnchantment(e);

        ItemMeta m = item.getItemMeta();
        List<String> lore = new ArrayList<String>();
        if(Objects.requireNonNull(m).hasLore()){
            for(String l: Objects.requireNonNull(m.getLore())){
                if(!l.contains(e.getName())) lore.add(l);
            }
        }

        m.setLore(lore);
        item.setItemMeta(m);
    }
}
