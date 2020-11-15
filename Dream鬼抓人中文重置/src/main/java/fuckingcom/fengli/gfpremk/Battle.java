package fuckingcom.fengli.gfpremk;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Battle {
    private static int battle_le = 130;
    private static int battleAr_wi = 110;
    private static List<Integer> Cou;
    private static Set<Battle> battles;
    private Pad pads;
    private World Fengliworld;
    private UUID ghoust;
    private BukkitTask ksat;
    private Location location;
    private boolean sted;

    public Battle(Pad pads){
        this.pads = pads;
        this.Fengliworld = pads.getPlayer1().getWorld();
        this.sted = false;
        Battle.battles.add(this);
    }
    public static Battle getbattle(Player player){
        for (Battle battle : Battle.battles){
            if (battle.pads.getPlayer1().equals(player) || battle.pads.getPlayer2().equals(player)){
                return battle;
            }
        }
        return null;
    }
    public static Set<Battle> getBattles() {
        return Battle.battles;
    }
    private void chooseLoca() {
        this.location = this.Fengliworld.getHighestBlockAt(GfpRemk.getSuijishu().nextInt(5000),GfpRemk.getSuijishu().nextInt(5000)).getLocation();
    }
    public void start(Player ghoust){
        this.sted = true;
        this.ghoust = ghoust.getUniqueId();
        this.chooseLoca();
        WorldBorder worldBorder = this.Fengliworld.getWorldBorder();
        worldBorder.setCenter(this.location);
        worldBorder.setSize(100,0);
        Location p1sp = this.location.clone().add(48.5,0.0,48.5);
        Location p2sp = this.location.clone().add(-48.5,0.0,-48.5);
        p1sp.setY((double)(this.Fengliworld.getHighestBlockYAt(p1sp)+2));
        p1sp.setYaw(135f);
        p2sp.setY((double)(this.Fengliworld.getHighestBlockYAt(p2sp)+2));
        p2sp.setYaw(-45f);
        this.pads.getPlayer1().teleport(p1sp);
        this.pads.getPlayer2().teleport(p2sp);
        this.gk(this.pads.getPlayer1());
        this.gk(this.pads.getPlayer2());
        if (this.ksat != null && !this.ksat.isCancelled()){
            this.ksat.cancel();
        }
        this.ksat = new BukkitRunnable(){
            int time = 120;
            public void run(){
                if (Battle.Cou.contains(this.time)){
                    Battle.this.pads.getPlayer1().sendMessage(ChatColor.RED + "" + this.time + ((this.time == 1) ? " 秒 " : " 秒 ") + "剩余!");
                    Battle.this.pads.getPlayer2().sendMessage(ChatColor.RED + "" + this.time + ((this.time == 1) ? " 秒 " : " 秒 ") + "剩余!");
                    Battle.this.pads.getPlayer1().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,Integer.valueOf(80),1));
                    Battle.this.pads.getPlayer2().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,Integer.valueOf(80),1));
                }
                if (this.time == 0){
                    Battle.this.endRound(Battle.this.pads.getPlayer1().equals(ghoust) ? Battle.this.pads.getPlayer2() : Battle.this.pads.getPlayer1(),true);
                    this.cancel();
                    return;
                }
                --this.time;
            }
        }.runTaskTimer((Plugin)GfpRemk.getInstance(),20L,20L);
    }
    public void numstart(Player ghoust){
        this.sted = true;
        this.ghoust = ghoust.getUniqueId();
        this.chooseLoca();
        WorldBorder worldBorder = this.Fengliworld.getWorldBorder();
        worldBorder.setCenter(this.location);
        worldBorder.setSize(100,0);
        Location p1sp = this.location.clone().add(48.5,0.0,48.5);
        Location p2sp = this.location.clone().add(-48.5,0.0,-48.5);
        p1sp.setY((double)(this.Fengliworld.getHighestBlockYAt(p1sp)+2));
        p1sp.setYaw(135f);
        p2sp.setY((double)(this.Fengliworld.getHighestBlockYAt(p2sp)+2));
        p2sp.setYaw(-45f);
        this.pads.getPlayer1().teleport(p1sp);
        this.pads.getPlayer2().teleport(p2sp);
        this.givekit(this.pads.getPlayer1());
        this.givekit(this.pads.getPlayer2());
        if (this.ksat != null && !this.ksat.isCancelled()){
            this.ksat.cancel();
        }
        this.ksat = new BukkitRunnable(){
            int time = 120;
            public void run(){
                if (Battle.Cou.contains(this.time)){
                    Battle.this.pads.getPlayer1().sendMessage(ChatColor.RED + "" + this.time + ((this.time == 1) ? " 秒 " : " 秒 ") + "剩余!");
                    Battle.this.pads.getPlayer2().sendMessage(ChatColor.RED + "" + this.time + ((this.time == 1) ? " 秒 " : " 秒 ") + "剩余!");
                    Battle.this.pads.getPlayer1().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,Integer.valueOf(80),1));
                    Battle.this.pads.getPlayer2().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,Integer.valueOf(80),1));
                }
                if (this.time == 0){
                    Battle.this.endRound(Battle.this.pads.getPlayer1().equals(ghoust) ? Battle.this.pads.getPlayer2() : Battle.this.pads.getPlayer1(),true);
                    this.cancel();
                    return;
                }
                --this.time;
            }
        }.runTaskTimer((Plugin)GfpRemk.getInstance(),20L,20L);
    }

    public void endRound(Player winner,boolean announce) {
        this.sted = false;
        if (this.ksat != null && !this.ksat.isCancelled()) {
            this.ksat.cancel();
        }
        this.Fengliworld.getWorldBorder().reset();
        if (announce) {
            this.pads.getPlayer1().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + winner.getName() + " 获取了该回合的游戏胜利!OHHHHH!");
            this.pads.getPlayer2().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + winner.getName() + " 获取了该回合的游戏胜利!OHHHHH!");
        }
    }

    public void remove() {
        this.endRound(null, false);
        Battle.battles.remove(this);
    }

    public boolean isPlayer(Player player) {
        return player.equals(this.pads.getPlayer1()) || player.equals(this.pads.getPlayer2());
    }

    public boolean isGhoust(Player player) {
        return player.getUniqueId().equals(this.ghoust);
    }

    public Player getOther(Player player) {
        return this.pads.getPlayer1().equals(player) ? this.pads.getPlayer2() : this.pads.getPlayer1();
    }

    public boolean isStarted() {
        return this.sted;
    }
    private void gk(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(12.0f);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        player.getInventory().clear();
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.DIAMOND_PICKAXE) });
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.DIAMOND_SHOVEL) });
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.DIAMOND_AXE) });
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.BOW, 1) });
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.ARROW, 24) });
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.COBBLESTONE, 32) });
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.WATER_BUCKET) });
        if (player.getUniqueId().equals(this.ghoust)) {
            player.getInventory().setItem(8, new ItemStack(Material.COMPASS));
            player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.SHIELD, 1) });
        }else {
            player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.COBBLESTONE, 8) });
            player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.ARROW, 8) });
        }
    }
    public boolean isNomStarted() {
        return this.sted;
    }
    private void givekit(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(12.0f);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        player.getInventory().clear();
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.DIAMOND_PICKAXE) });
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.DIAMOND_SHOVEL) });
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.DIAMOND_AXE) });
        player.getInventory().addItem(new ItemStack[] { new ItemStack(Material.COBBLESTONE, 16) });
        if (player.getUniqueId().equals(this.ghoust)) {
            player.getInventory().setItem(8, new ItemStack(Material.COMPASS));
        }
    }
    static {
        Cou = Arrays.asList(60, 30, 15, 10, 5, 4, 3, 2, 1);
        Battle.battles = new HashSet<Battle>();
    }
}
