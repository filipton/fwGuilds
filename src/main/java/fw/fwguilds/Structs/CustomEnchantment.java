package fw.fwguilds.Structs;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomEnchantment extends Enchantment {
    public String Name;
    public Integer MaxLvl;
    public CustomEnchantment(String Namespace, String name, int maxLvl) {
        super(NamespacedKey.minecraft(Namespace));
        Name = name;
        MaxLvl = maxLvl;
    }

    @Override
    public boolean canEnchantItem(ItemStack item){
        return true;
    }

    @Override
    public boolean conflictsWith(Enchantment other){
        return false;
    }

    @Override
    public int getMaxLevel(){
        return MaxLvl;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public String getName(){
        return Name;
    }
}
