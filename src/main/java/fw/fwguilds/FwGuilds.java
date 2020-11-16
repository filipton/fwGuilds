package fw.fwguilds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.io.File;

import fw.fwguilds.Expansions.PAPIExp;
import fw.fwguilds.Structs.Guild;
import fw.fwguilds.Structs.GuildWaiter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

public final class FwGuilds extends JavaPlugin implements Listener {
    FileConfiguration config = getConfig();
    static File cfile;

    static Scoreboard scoreboard;

    static HashMap<String, Double> Drops = new HashMap<>();
    public static HashMap<String, Guild> Guilds = new HashMap<>();

    static List<GuildWaiter> guildWaiters = new ArrayList<>();
    static List<GuildWaiter> addToGuildWaiters = new ArrayList<>();
    static List<GuildWaiter> deleteGuildWaiters = new ArrayList<>();

    static double Fortune2DropRate = 1.20;
    static double Fortune3DropRate = 1.35;
    static int DropExpCount = 1;
    static int CuboidSize = 100;

    static boolean OpBypass = false;
    static boolean ActiveHalfDrop = true;
    static boolean ActiveGuildsSystem = true;

    public boolean glide = false;

    @Override
    public void onEnable() {
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new PAPIExp(this).register();
        }

        //put default values into hashmap Drops
        Drops.put("EXP", 50.0);
        Drops.put("IRON_ORE", 10.0);
        Drops.put("DIAMOND", 0.5);
        Drops.put("GOLD_ORE", 5.0);

        Drops = sortHashMapByValues(Drops);

        //save default config
        config.addDefault("fortune2DropRate", 1.20);
        config.addDefault("fortune3DropRate", 1.35);
        config.addDefault("DropExpCount", 1);
        config.addDefault("CuboidSize", 100);
        config.addDefault("dropsRate", Drops);
        config.addDefault("guilds", Guilds);
        config.addDefault("opBypass", false);
        config.addDefault("halfDrop", true);
        config.addDefault("guildsSystem", true);
        config.options().copyDefaults(true);
        saveConfig();
        cfile = new File(getDataFolder(), "config.yml");

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new DeathAndDamageHandler(), this);
        getServer().getPluginManager().registerEvents(new AntyLogout(), this);
        getServer().getPluginManager().registerEvents(new PlayerKnockDown(), this);

        ReloadConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {
        if (command.getName().equalsIgnoreCase("fwguilds") || command.getAliases().contains("fg") || command.getAliases().contains("g")) {
            if(args.length > 0){
                switch (args[0]){
                    case "reload":
                        if(sender.isOp()){
                            sender.sendMessage("Reloading config...");
                            ReloadConfig();
                        }
                        else{
                            sender.sendMessage(ChatColor.RED + "Nie masz wystarczających permisji!");
                        }
                        break;
                    case "guilds":
                        if(sender.isOp()){
                            Guilds.forEach((k, v) -> {
                                sender.sendMessage(ChatColor.RED + k);
                                sender.sendMessage(ChatColor.YELLOW + v.Owner);
                                sender.sendMessage(ChatColor.AQUA + "" + v.CuboidBlockPosition.getX() + " : " + v.CuboidBlockPosition.getZ());
                                for (String member : v.members) {
                                    sender.sendMessage(ChatColor.BLUE + member);
                                }
                            });
                        }
                        else{
                            sender.sendMessage(ChatColor.RED + "Nie masz wystarczających permisji!");
                        }
                        break;
                    case "utworz":
                        if(args.length == 2){
                            if(((Player)sender).getInventory().containsAtLeast(new ItemStack(Material.GOLD_BLOCK), 3) &&
                                    ((Player)sender).getInventory().containsAtLeast(new ItemStack(Material.DIAMOND_BLOCK), 5) &&
                                    ((Player)sender).getInventory().containsAtLeast(new ItemStack(Material.OBSIDIAN), 1))
                            {
                                if(args[1].length() != 4) {
                                    sender.sendMessage(ChatColor.RED + "NAZWA GILDII MUSI MIEC 4 ZNAKI!");
                                    return true;
                                }

                                for(Map.Entry<String, Guild> guild : Guilds.entrySet()) {
                                    if(guild.getValue().Owner.equalsIgnoreCase(sender.getName()) || guild.getValue().members.contains(sender.getName())){
                                        sender.sendMessage(ChatColor.RED + "JESTES AKTUALNIE W GILDII, NIE MOZESZ UTWORZYC KOLEJNEJ!");
                                        return true;
                                    }
                                }

                                removeInventoryItems(((Player)sender).getInventory(), Material.GOLD_BLOCK, 3);
                                removeInventoryItems(((Player)sender).getInventory(), Material.DIAMOND_BLOCK, 5);
                                removeInventoryItems(((Player)sender).getInventory(), Material.OBSIDIAN, 1);

                                ItemStack cuboidIS = new ItemStack(Material.CRYING_OBSIDIAN);
                                ItemMeta cuboid = cuboidIS.getItemMeta();
                                cuboid.setDisplayName(ChatColor.AQUA + "CUBOID");
                                List<String> lore = new ArrayList<>();
                                lore.add("CUBOID");
                                cuboid.setLore(lore);
                                cuboidIS.setItemMeta(cuboid);
                                sender.getServer().getPlayer(sender.getName()).getInventory().addItem(cuboidIS);
                                sender.sendMessage(ChatColor.YELLOW + "POSTAW BLOK CUBOIDU ZEBY UTWORZYC GILDIE!");
                                GuildWaiter gw = new GuildWaiter();
                                gw.ownerPlayer = (Player) sender;
                                gw.GuildName = args[1];
                                guildWaiters.add(gw);

                                SaveConfigRemastered();
                            }
                            else{
                                sender.sendMessage(ChatColor.RED + "ZEBY UTWORZYC GILDIE POTRZEBUJESZ: 5 BLOKOW DIAMENTOW, 3 BLOKOW ZOTA, 1 OBSYDIAN!");
                            }
                        }
                        else {
                            sender.sendMessage(ChatColor.RED + "MUSISZ PODAC NAZWE GILDII! /fg utworz <nazwa>");
                        }
                        break;
                    case "dodaj-gracza":
                        if(args.length == 2) {
                            GuildWaiter gw = new GuildWaiter();
                            Player p = Bukkit.getPlayer(args[1]);

                            if(p != null){
                                gw.ownerPlayer = p;

                                if(p == sender){
                                    sender.sendMessage(ChatColor.RED + "NIE MOZESZ DODAC SAMEGO SIEBIE DO GILDII!");
                                    return true;
                                }

                                for(Map.Entry<String, Guild> guild : Guilds.entrySet()) {
                                    if(guild.getValue().Owner.equalsIgnoreCase(sender.getName())){
                                        gw.GuildName = guild.getKey();
                                        addToGuildWaiters.add(gw);

                                        p.sendMessage(ChatColor.AQUA + "ZOSTALES ZAPROSZONY DO GILDII " + ChatColor.YELLOW + gw.GuildName + ChatColor.AQUA + ", WPISZ /fg akceptuj ZEBY DO NIEJ DOLACZYC! (BEDZIESZ POTRZEBOWAL 3 BLOKOW ZLOTA)");
                                        return true;
                                    }
                                }

                                sender.sendMessage(ChatColor.RED + "NIE JESTES OWNEREM TEJ GILDII, NIE MOZESZ WYSLAC ZAPROSZENIA!");
                                return true;
                            }
                            else{
                                sender.sendMessage(ChatColor.RED + "TEN GRACZ MUSI BYC ONLINE!");
                            }
                        }
                        else {
                            sender.sendMessage(ChatColor.RED + "MUSISZ PODAC GRACZA ZEBY GO DODAC DO GILDII!! /fg dodaj-gracza <gracz>");
                        }
                        break;
                    case "akceptuj":
                        Player p = (Player) sender;
                        if(p.getInventory().containsAtLeast(new ItemStack(Material.GOLD_BLOCK), 3)) {
                            for(GuildWaiter gw : addToGuildWaiters){
                                if(gw.ownerPlayer == p) {
                                    for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
                                        if(guild.getKey().equals(gw.GuildName)){
                                            removeInventoryItems(p.getInventory(), Material.GOLD_BLOCK, 3);

                                            guild.getValue().members.add(p.getName());
                                            SendMessageToallPlayers(ChatColor.GREEN + "GRACZ " + p.getName() + " DOLACZYL DO GILDII: " + ChatColor.YELLOW + gw.GuildName + ChatColor.GREEN + "!");
                                            SaveConfigRemastered();

                                            addToGuildWaiters.remove(gw);
                                            ReloadTeams();

                                            return true;
                                        }
                                    }
                                }
                            }
                            sender.sendMessage(ChatColor.RED + "NIE POSIADASZ ZADNEGO ZAPROSZENIA DO GILDII!");
                        }
                        else {
                            sender.sendMessage(ChatColor.RED + "MUSISZ POSIADAC CONAJMNIEJ 3 BLOKI ZLOTA ZEBY DOLACZYC DO JAKIEJS GILDII!");
                        }
                        break;
                    case "usun-gracza":
                        for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
                            if(guild.getValue().Owner.equalsIgnoreCase(sender.getName())){
                                if(args.length == 2){
                                    for(String mem : guild.getValue().members){
                                        if(mem.equalsIgnoreCase(args[1])){
                                            if(guild.getValue().Owner.equalsIgnoreCase(mem)){
                                                sender.sendMessage(ChatColor.RED + "NIE MOZESZ USUNAC WLASCICIELA GILDII, MOZESZ USUNAC CALKOWICIE GILDIE!");

                                                return true;
                                            }

                                            guild.getValue().members.remove(mem);

                                            SendMessageToallPlayers(ChatColor.GREEN + "GRACZ " + mem + " ZOSTAL USUNIETY Z GILDII: " + ChatColor.YELLOW + guild.getKey() + ChatColor.GREEN + "!");
                                            SaveConfigRemastered();

                                            ReloadTeams();
                                            return true;
                                        }
                                    }

                                    sender.sendMessage(ChatColor.RED + "TEN GRACZ NIE JEST AKTUALNIE W TWOJEJ GILDII!");
                                    return true;
                                }
                                else{
                                    sender.sendMessage(ChatColor.RED + "MUSISZ PODAC JAKIEGO GRACZA CHCESZ USUNAC! /fg usun-gracza <gracz>");

                                    return true;
                                }
                            }
                        }
                        sender.sendMessage(ChatColor.RED + "MUSISZ BYC WLASCICIELEM TEJ GILDII ABY USUNAC Z NIEJ GRACZA!");
                        break;
                    case "usun":
                        for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
                            if(guild.getValue().Owner.equalsIgnoreCase(sender.getName())){
                                GuildWaiter gw = new GuildWaiter();
                                gw.GuildName = guild.getKey();
                                gw.ownerPlayer = (Player) sender;

                                deleteGuildWaiters.add(gw);

                                sender.sendMessage(ChatColor.YELLOW + "CZY NAPEWNO CHCESZ USUNAC GILDIE " + gw.GuildName + "? WPISZ /fg potwierdz ZEBY JA USUNAC!");

                                ReloadTeams();
                                SaveConfigRemastered();
                                return true;
                            }
                        }

                        sender.sendMessage(ChatColor.RED + "MUSISZ BYC WLASCICIELEM JAKIEJSC GILDII ZEBY JA USUNAC!");
                        break;
                    case "potwierdz":
                        for(GuildWaiter dwg : deleteGuildWaiters){
                            if(dwg.ownerPlayer.equals(sender)){
                                for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
                                    if(guild.getKey().equalsIgnoreCase(dwg.GuildName)){
                                        getServer().getWorld("world").getBlockAt(guild.getValue().CuboidBlockPosition.getBlockX(), guild.getValue().CuboidBlockPosition.getBlockY(), guild.getValue().CuboidBlockPosition.getBlockZ()).setType(Material.AIR);

                                        Guilds.remove(guild.getKey());
                                        deleteGuildWaiters.remove(dwg);

                                        SendMessageToallPlayers(ChatColor.AQUA + "GILDIA " + dwg.GuildName + " ZOSTALA USUNIETA!");

                                        org.bukkit.scoreboard.Team guildTeam = scoreboard.getTeam(guild.getKey());
                                        if(guildTeam != null){
                                            guildTeam.unregister();
                                        }

                                        ReloadTeams();
                                        SaveConfigRemastered();
                                        return true;
                                    }
                                }
                                return true;
                            }
                        }

                        sender.sendMessage(ChatColor.RED + "MUSISZ WYSLAC USUNIECIE GILDII /fg usun ZEBY JA USUNAC!");
                        break;
                    case "opusc":
                        for(Map.Entry<String, Guild> guild : Guilds.entrySet()) {
                            if(guild.getValue().Owner.equalsIgnoreCase(sender.getName())){
                                sender.sendMessage(ChatColor.RED + "NIE MOZESZ UPUSCIC GILDIE BEDAC JEJ OWNEREM!");
                                return true;
                            }

                            for(String mem : guild.getValue().members){
                                if(mem.equalsIgnoreCase(sender.getName())){
                                    guild.getValue().members.remove(mem);
                                    SendMessageToallPlayers(ChatColor.AQUA + sender.getName() + " OPUSCIL GILDIE " + guild.getKey() + "!");

                                    ReloadTeams();
                                    SaveConfigRemastered();

                                    return true;
                                }
                            }
                        }
                        break;
                    case "color":
                        if(args.length == 2){
                            ChatColor cc = ChatColor.getByChar(args[1]);

                            if(cc != null){
                                for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
                                    if(guild.getValue().Owner.equalsIgnoreCase(sender.getName())){
                                        sender.sendMessage("ZMIENILES KOLOR GILDII NA: " + cc + "KOLOR");
                                        guild.getValue().TeamColor = cc.toString();

                                        ReloadTeams();
                                        SaveConfigRemastered();
                                        return true;
                                    }
                                }

                                sender.sendMessage(ChatColor.RED + "NIE MOZESZ ZMIENIC KOLORU GILDII NIE BEDAC OWNEREM JAKIEJKOLWIEK GILDII!");
                                return true;
                            }
                            else{
                                sender.sendMessage(ChatColor.RED + "MUSISZ PODAC POPRAWNY KOLOR! WPIKSZ /fg color ZEBY WYSWIETLIC LISTE KOLOROW!");
                            }
                        }
                        else{
                            sender.sendMessage(ChatColor.GREEN + "=================== COLORS ====================");
                            sender.sendMessage(ChatColor.BLACK + "0 - Black");
                            sender.sendMessage(ChatColor.DARK_BLUE + "1 - Dark Blue");
                            sender.sendMessage(ChatColor.DARK_GREEN + "2 - Dark Green");
                            sender.sendMessage(ChatColor.DARK_AQUA + "3 - Dark Aqua");
                            sender.sendMessage(ChatColor.DARK_RED + "4 - Dark Red");
                            sender.sendMessage(ChatColor.DARK_PURPLE + "5 - Dark Purple");
                            sender.sendMessage(ChatColor.GOLD + "6 - Gold");
                            sender.sendMessage(ChatColor.GRAY + "7 - Gray");
                            sender.sendMessage(ChatColor.DARK_GRAY + "8 - Dark Gray");
                            sender.sendMessage(ChatColor.BLUE + "9 - Blue");
                            sender.sendMessage(ChatColor.GREEN + "a - Green");
                            sender.sendMessage(ChatColor.AQUA + "b - Aqua");
                            sender.sendMessage(ChatColor.RED + "c - Red");
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + "d - Light Purple");
                            sender.sendMessage(ChatColor.YELLOW + "e - Yellow");
                            sender.sendMessage(ChatColor.WHITE + "f - White");
                            sender.sendMessage(ChatColor.GREEN + "===============================================");
                        }
                        break;
                    case "ozyw":
                        if(sender.isOp()){
                            Player player;
                            if(args.length == 2){
                                player = Bukkit.getPlayer(args[1]);
                            }
                            else {
                                player = (Player) sender;
                            }

                            PlayerKnockDown.UnKnockDownPlayer(player);
                        }

                        break;
                    case "test2":
                        //XDDDDDDDDDDDDDDDDDDDDDDDDDDDD DONT TRY THIS XDDDD
                        /*for(Player player : Bukkit.getOnlinePlayers()){
                            if((Player)sender != player){
                                ((Player)sender).addPassenger(player);
                            }
                        }*/
                        //PlayerKnockDown.UnKnockDownPlayer((Player)sender);
                        break;
                    default:
                    case "help":
                        sender.sendMessage(ChatColor.GREEN + "=================== FWGUILDS HELP ====================");
                        sender.sendMessage(ChatColor.AQUA + "/fg utworz <nazwa/4znaki> - tworzy gildie o danej nazwie");
                        sender.sendMessage(ChatColor.AQUA + "/fg dodaj-gracza <gracz> - wysyla zaproszenie do gildii danemu graczu");
                        sender.sendMessage(ChatColor.AQUA + "/fg akceptuj - przyjmuje zaproszenie do gildii (oplata 3 bloki zlota)");
                        sender.sendMessage(ChatColor.AQUA + "/fg usun-gracza <gracz> - usuwa gracza z gildii");
                        sender.sendMessage(ChatColor.AQUA + "/fg usun - usuwa gildie (wymaga potwierdzenia)");
                        sender.sendMessage(ChatColor.AQUA + "/fg potwierdz - potwierdza usuniecie gildii");
                        sender.sendMessage(ChatColor.AQUA + "/fg opusc - opuszcza gildie");
                        sender.sendMessage(ChatColor.AQUA + "/fg color - ustawia color gildii");
                        sender.sendMessage(ChatColor.GREEN + "=====================================================");
                        break;
                }
            }
            else{
                sender.sendMessage(ChatColor.RED + "Wpisz /fg help żeby wyświetlić listę komend!");
            }
            return true;
        }
        else if(command.getName().equalsIgnoreCase("baza")){
            boolean memberHasGuild = false;
            Guild g = new Guild();
            double closestDistance = 10000;

            Player p = (Player)sender;
            Vector pos = new Vector().setX(p.getLocation().getBlockX()).setY(p.getLocation().getBlockY()).setZ(p.getLocation().getBlockZ());
            double hp = p.getHealth();

            for(Map.Entry<String, Guild> guild : Guilds.entrySet()) {
                double dist = pos.distance(guild.getValue().CuboidBlockPosition);
                if(dist < closestDistance){
                    closestDistance = dist;
                }

                for(String member : guild.getValue().members){
                    if(p.getName().equalsIgnoreCase(member)){
                        memberHasGuild = true;
                        g = guild.getValue();
                    }
                }
            }

            if(closestDistance <= 300){
                ActionBarMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "NIE MOZESZ SIE TEPNAC DO BAZY BEDAC 300 KRATEK OD JAKIEJKOLWIEK GILDII!");
                return true;
            }

            boolean finalMemberHasGuild = memberHasGuild;
            Guild finalG = g;
            new BukkitRunnable() {
                int i = 0;

                @Override
                public void run() {
                    ActionBarMessage(p, ChatColor.YELLOW + "" + ChatColor.BOLD + "TELEPORTUJESZ SIE DO BAZY ZA: " + ChatColor.GREEN + "" + ChatColor.BOLD + (15-i) + " sekund!");

                    if (i < 14) {
                        if (!pos.equals(new Vector().setX(p.getLocation().getBlockX()).setY(p.getLocation().getBlockY()).setZ(p.getLocation().getBlockZ()))) {
                            ActionBarMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "RUSZYLES SIE Z MIEJSCA WIEC NIE PRZETELEPORTOWALO CIE!");
                            this.cancel();
                        }
                        else if(hp > p.getHealth()){
                            ActionBarMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "DOSTALES JAKIS DAMAGE WIEC NIE PRZETELEPORTOWALO CIE!");
                            this.cancel();
                        }
                    } else if (i == 15) {
                        ActionBarMessage(p, ChatColor.GREEN + "" + ChatColor.BOLD + "ZOSTALES PRZETELEPORTOWANY DO BAZY!");
                        p.teleport(new Location(getServer().getWorld("world"), finalG.CuboidBlockPosition.getBlockX() + 0.5, finalG.CuboidBlockPosition.getBlockY()+1, finalG.CuboidBlockPosition.getBlockZ() + 0.5, p.getLocation().getYaw(), p.getLocation().getPitch()));
                        this.cancel();
                    }
                    i++;

                    if(!finalMemberHasGuild){
                        ActionBarMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "NIE JESTES AKTUALNIE W GILDII!");
                        this.cancel();
                    }
                }
            }.runTaskTimer(getPlugin(this.getClass()), 0, 20);
            return true;
        }
        else if(command.getName().equalsIgnoreCase("ucieczka")){
            boolean memberHasGuild = false;
            Guild g = new Guild();
            double closestDistance = 10000;

            Player p = (Player)sender;
            Vector pos = new Vector().setX(p.getLocation().getBlockX()).setY(p.getLocation().getBlockY()).setZ(p.getLocation().getBlockZ());
            double hp = p.getHealth();

            for(Map.Entry<String, Guild> guild : Guilds.entrySet()) {
                double dist = pos.distance(guild.getValue().CuboidBlockPosition);
                if(dist < closestDistance){
                    closestDistance = dist;
                }

                for(String member : guild.getValue().members){
                    if(p.getName().equalsIgnoreCase(member)){
                        memberHasGuild = true;
                        g = guild.getValue();
                    }
                }
            }

            if(closestDistance <= 100){
                boolean finalMemberHasGuild = memberHasGuild;
                Guild finalG = g;
                new BukkitRunnable() {
                    int i = 0;

                    @Override
                    public void run() {
                        ActionBarMessage(p, ChatColor.YELLOW + "" + ChatColor.BOLD + "UCIEKASZ DO BAZY ZA: " + ChatColor.GREEN + "" + ChatColor.BOLD + (120-i) + " sekund!");

                        if (i < 119) {
                            if (!pos.equals(new Vector().setX(p.getLocation().getBlockX()).setY(p.getLocation().getBlockY()).setZ(p.getLocation().getBlockZ()))) {
                                ActionBarMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "RUSZYLES SIE Z MIEJSCA WIEC NIE PRZETELEPORTOWALO CIE!");
                                this.cancel();
                            }
                            else if(hp > p.getHealth()){
                                ActionBarMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "DOSTALES JAKIS DAMAGE WIEC NIE PRZETELEPORTOWALO CIE!");
                                this.cancel();
                            }
                        } else if (i == 120) {
                            ActionBarMessage(p, ChatColor.GREEN + "" + ChatColor.BOLD + "UCIEKLES DO BAZY!");
                            p.teleport(new Location(getServer().getWorld("world"), finalG.CuboidBlockPosition.getBlockX() + 0.5, finalG.CuboidBlockPosition.getBlockY()+1, finalG.CuboidBlockPosition.getBlockZ() + 0.5, p.getLocation().getYaw(), p.getLocation().getPitch()));
                            this.cancel();
                        }
                        i++;

                        if(!finalMemberHasGuild){
                            ActionBarMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "NIE JESTES AKTUALNIE W GILDII!");
                            this.cancel();
                        }
                    }
                }.runTaskTimer(getPlugin(this.getClass()), 0, 20);
                return true;
            }
            ActionBarMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "NIE MOZESZ ZACZAC UCIECZKI DO BAZY NIE BEDAC NA TERENIE JAKIEJKOLWIEK GILDII!");

            return true;
        }
        else if(command.getName().equalsIgnoreCase("samobojstwo")){
            PlayerKnockDown.KnockDownPlayer((Player) sender);
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();

        if(ActiveGuildsSystem && (OpBypass || !p.isOp())){
            Block b = event.getBlock();
            if(b.getType().equals(Material.CRYING_OBSIDIAN)){
                for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
                    Vector cpos = guild.getValue().CuboidBlockPosition;
                    if(cpos.getBlockX() == b.getX() && cpos.getBlockY() == b.getY() && cpos.getBlockZ() == b.getZ()){
                        ActionBarMessage(p, ChatColor.RED + "AKTUALNIE PSUCIE SERC GILDII JEST WYLACZONE!");
                        event.setCancelled(true);
                    }
                }
            }

            for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
                if(guild != null){
                    if(!CanInteract(event.getBlock(), guild.getValue().CuboidBlockPosition.getBlockX(), guild.getValue().CuboidBlockPosition.getBlockZ())) {
                        if(!(guild.getValue().Owner.equalsIgnoreCase(p.getName()) || guild.getValue().members.contains(p.getName()))){
                            event.setCancelled(true);
                            ActionBarMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "Nie możesz tutaj niszczyć bo to teren gildii " + ChatColor.YELLOW + "" + ChatColor.BOLD  + guild.getKey() + ChatColor.RED + "" + ChatColor.BOLD  + "!");
                            return;
                        }
                    }
                }
            }
        }

        if (event.getBlock().getType() == Material.STONE) {
            DropFromStone(event);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();

        if(ActiveGuildsSystem && (OpBypass || !p.isOp())){
            for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
                if(guild != null){
                    if(!CanInteract(event.getBlock(), guild.getValue().CuboidBlockPosition.getBlockX(), guild.getValue().CuboidBlockPosition.getBlockZ())) {
                        if(!(guild.getValue().Owner.equalsIgnoreCase(p.getName()) || guild.getValue().members.contains(p.getName()))){
                            event.setCancelled(true);
                            ActionBarMessage(p, ChatColor.RED + "" + ChatColor.BOLD + "Nie możesz tutaj budować bo to teren gildii " + ChatColor.YELLOW + "" + ChatColor.BOLD  + guild.getKey() + ChatColor.RED + "" + ChatColor.BOLD  + "!");
                            return;
                        }
                    }
                }
            }
        }

        if(event.getItemInHand().hasItemMeta()){
            if(!event.getPlayer().getWorld().getEnvironment().equals(World.Environment.NORMAL)){
                ActionBarMessage(p, ChatColor.RED + "GILDIE MOZESZ WYLACZNIE POSTAWIC W NORMALNYM SWIECIE!");
                return;
            }

            List<String> lore = event.getItemInHand().getItemMeta().getLore();
            Block b = event.getBlock();
            if(lore != null && lore.size() > 0){
                if(lore.get(0).contains("CUBOID")){
                    for(GuildWaiter guildW : guildWaiters){
                        if(guildW.ownerPlayer == p){
                            SendMessageToallPlayers(ChatColor.AQUA + "POWSTALA NOWA GILDIA: " + ChatColor.YELLOW + ChatColor.BOLD + guildW.GuildName + "!");

                            Guild g = new Guild();
                            g.Owner = guildW.ownerPlayer.getName();
                            g.CuboidBlockPosition = new Vector().setX(b.getX()).setY(b.getY()).setZ(b.getZ());
                            g.members = new ArrayList<>();
                            g.members.add(guildW.ownerPlayer.getName());
                            g.TeamColor = ChatColor.GOLD.toString();

                            Guilds.put(guildW.GuildName, g);

                            guildWaiters.remove(guildW);

                            SaveConfigRemastered();
                            ReloadTeams();
                            return;
                        }
                    }
                }
            }
        }
    }

    public void SendMessageToallPlayers(String msg){
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg);
        }
    }
    public void ActionBarMessage(Player p, String msg){
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }
    
    public void DropFromStone(BlockBreakEvent event) {
        Player p = event.getPlayer();
        ItemStack itemInMainHand = p.getInventory().getItemInMainHand();

        if(itemInMainHand != null &&
                (itemInMainHand.getType().equals(Material.IRON_PICKAXE) ||
                        itemInMainHand.getType().equals(Material.DIAMOND_PICKAXE) ||
                        itemInMainHand.getType().equals(Material.NETHERITE_PICKAXE)))
        {
            double fortMultiplier = 1;

            int fortuneLevel = itemInMainHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
            if(fortuneLevel == 2)
            {
                fortMultiplier = Fortune2DropRate;
            }
            else if(fortuneLevel == 3)
            {
                fortMultiplier = Fortune3DropRate;
            }

            Random r = new Random();
            double rngi = 0 + (100) * r.nextDouble();

            for (String i : Drops.keySet())
            {
                if(rngi <= Drops.get(i)*fortMultiplier)
                {
                    if(i.equalsIgnoreCase("EXP"))
                    {
                        p.giveExp(DropExpCount);
                    }
                    else
                    {
                        p.sendMessage(ChatColor.RED + "[" + ChatColor.YELLOW + "DROP" + ChatColor.RED + "]" + ChatColor.RESET + ChatColor.GREEN + " Wydropiles " + ChatColor.AQUA + ChatColor.BOLD + i + ChatColor.RESET + ChatColor.GREEN + "!");
                        p.getInventory().addItem(new ItemStack(Material.valueOf(i)));
                    }

                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerBucketFillEvent(PlayerBucketFillEvent event){
        if(ActiveGuildsSystem && (OpBypass || !event.getPlayer().isOp())){
            for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
                if(guild != null){
                    if(!CanInteract(event.getBlock(), guild.getValue().CuboidBlockPosition.getBlockX(), guild.getValue().CuboidBlockPosition.getBlockZ())) {
                        if(!(guild.getValue().Owner.equalsIgnoreCase(event.getPlayer().getName()) || guild.getValue().members.contains(event.getPlayer().getName()))){
                            event.setCancelled(true);
                            ActionBarMessage(event.getPlayer(), ChatColor.RED + "" + ChatColor.BOLD + "Nie możesz stąd wziaść cieczy bo to teren gildii " + ChatColor.YELLOW + "" + ChatColor.BOLD  + guild.getKey() + ChatColor.RED + "" + ChatColor.BOLD  + "!");
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event){
        if(ActiveGuildsSystem && (OpBypass || !event.getPlayer().isOp())){
            for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
                if(guild != null){
                    if(!CanInteract(event.getBlock(), guild.getValue().CuboidBlockPosition.getBlockX(), guild.getValue().CuboidBlockPosition.getBlockZ())) {
                        if(!(guild.getValue().Owner.equalsIgnoreCase(event.getPlayer().getName()) || guild.getValue().members.contains(event.getPlayer().getName()))){
                            event.setCancelled(true);
                            ActionBarMessage(event.getPlayer(), ChatColor.RED + "" + ChatColor.BOLD + "Nie możesz tutaj wylać cieczy bo to teren gildii " + ChatColor.YELLOW + "" + ChatColor.BOLD  + guild.getKey() + ChatColor.RED + "" + ChatColor.BOLD  + "!");
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(scoreboard == null) scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        ReloadTeams();
    }

    public void ManageTeam(String gName, ChatColor gColor){
        org.bukkit.scoreboard.Team guildTeam;

        if(scoreboard != null){
            if(scoreboard.getTeam(gName) != null){
                guildTeam = scoreboard.getTeam(gName);
            }
            else{
                guildTeam = scoreboard.registerNewTeam(gName);
            }

            guildTeam.setColor(gColor);
            guildTeam.setPrefix(ChatColor.GRAY + "" + ChatColor.BOLD + "[" + gColor + gName + ChatColor.GRAY + "" + ChatColor.BOLD + "] " + ChatColor.RESET);
            guildTeam.setCanSeeFriendlyInvisibles(true);
            guildTeam.setAllowFriendlyFire(true);
        }
        else{
            scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            if(scoreboard != null) ManageTeam(gName, gColor);
        }
    }

    public void ReloadTeams() {
        for(Player pl : Bukkit.getOnlinePlayers()){
            pl.setDisplayName(pl.getName());
            pl.setPlayerListName(pl.getName());

            outerloop:
            for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
                ManageTeam(guild.getKey(), ChatColor.getByChar(guild.getValue().TeamColor.replace("§", "")));
                org.bukkit.scoreboard.Team guildTeam = scoreboard.getTeam(guild.getKey());

                for(String gtn : guildTeam.getEntries()){
                    guildTeam.removeEntry(gtn);
                }

                for(String pName : guild.getValue().members){
                    if(pl.getName().equalsIgnoreCase(pName)){
                        pl.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "[" + guild.getValue().TeamColor + guild.getKey() + ChatColor.GRAY + "" + ChatColor.BOLD + "] " + ChatColor.RESET + pName);
                        //pl.setPlayerListName(ChatColor.GRAY + "" + ChatColor.BOLD + "[" + guild.getValue().TeamColor + guild.getKey() + ChatColor.GRAY + "" + ChatColor.BOLD + "] " + ChatColor.RESET + pName);

                        break outerloop;
                    }
                }
            }
        }

        for(Map.Entry<String, Guild> guild : Guilds.entrySet()){
            org.bukkit.scoreboard.Team guildTeam = scoreboard.getTeam(guild.getKey());

            if(guildTeam != null){
                for(String pName : guild.getValue().members) {
                    if (!guildTeam.hasEntry(pName)) {
                        guildTeam.addEntry(pName);
                    }
                }
            }
        }
    }

    public boolean CanInteract(Block b, int cubX, int cubZ) {
        return Math.abs(cubX - b.getX()) > CuboidSize / 2 || Math.abs(cubZ - b.getZ()) > CuboidSize / 2;
    }

    public static void removeInventoryItems(Inventory inv, Material type, int amount) {
        ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack is = items[i];
            if (is != null && is.getType() == type) {
                int newamount = is.getAmount() - amount;
                if (newamount > 0) {
                    is.setAmount(newamount);
                    break;
                } else {
                    items[i] = new ItemStack(Material.AIR);
                    amount = -newamount;
                    if (amount == 0) break;
                }
            }
        }
        inv.setContents(items);
    }

    public void ReloadConfig() {
        config = YamlConfiguration.loadConfiguration(cfile);

        HashMap<String, Double> DropsTmp = new HashMap<>();
        HashMap<String, Guild> GuildsTmp = new HashMap<>();

        for(String inv : Objects.requireNonNull(config.getConfigurationSection("dropsRate")).getKeys(false)) {
            DropsTmp.put(inv, config.getDouble("dropsRate." + inv));
        }

        for(String guild : Objects.requireNonNull(config.getConfigurationSection("guilds")).getKeys(false)) {
            Guild g = new Guild();
            g.Owner = config.getString("guilds." + guild + ".Owner");
            g.CuboidBlockPosition = config.getVector("guilds." + guild + ".CuboidBlockPosition");
            g.members = (List<String>) config.getList("guilds." + guild + ".members");
            g.TeamColor = config.getString("guilds." + guild + ".TeamColor");

            GuildsTmp.put(guild, g);
        }

        Drops = sortHashMapByValues(DropsTmp);
        Guilds = GuildsTmp;

        Fortune2DropRate = config.getDouble("fortune2DropRate");
        Fortune3DropRate = config.getDouble("fortune3DropRate");
        DropExpCount = config.getInt("DropExpCount");
        CuboidSize = config.getInt("CuboidSize");
        OpBypass = config.getBoolean("opBypass");
        ActiveHalfDrop = config.getBoolean("halfDrop");
        ActiveGuildsSystem = config.getBoolean("guildsSystem");

        if(scoreboard != null) ReloadTeams();
    }

    public void SaveConfigRemastered() {
        config.set("guilds", Guilds);
        String cnf = null;

        BufferedReader bufReader = new BufferedReader(new StringReader(config.saveToString()));

        String line=null;
        try
        {
            while( (line=bufReader.readLine()) != null )
            {
                int indexOf = line.indexOf("!!");
                if(indexOf >= 0){
                    line = line.substring(0, indexOf);
                }

                cnf += line;
                cnf += "\n";
            }
        } catch(IOException ignored) {}

        try {
            config.loadFromString(cnf);
        } catch (InvalidConfigurationException ignored) {}

        try {
            config.save(cfile);
        } catch (IOException e) {
            System.out.println("Error saving config file!");
        }
    }

    public LinkedHashMap<String, Double> sortHashMapByValues(HashMap<String, Double> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());

        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, Double> sortedMap =
                new LinkedHashMap<>();

        for (Double val : mapValues) {
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Double comp1 = passedMap.get(key);

                if (comp1.equals(val)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}