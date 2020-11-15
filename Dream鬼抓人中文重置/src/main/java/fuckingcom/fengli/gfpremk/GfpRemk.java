package fuckingcom.fengli.gfpremk;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class GfpRemk extends JavaPlugin implements Listener, CommandExecutor {
    //在?为什么看源码?算了 看就看吧.
    //饿 借鉴Dream的思路.
    private static Random suijishu;
    private static GfpRemk Instance;

    public static Random getSuijishu() {
        return GfpRemk.suijishu;
    }
    public static GfpRemk getInstance(){
        return GfpRemk.Instance;
    }
    public void onEnable() {
        GfpRemk.Instance = this;
        GfpRemk.suijishu = new Random();
        this.getServer().getPluginManager().registerEvents((Listener)this,(Plugin)this);
    }
    public void onDisable() {
        GfpRemk.Instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("DUtimatetagRemake")){
            if (args.length == 2){
                if (args[0].equalsIgnoreCase("Start")) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "玩家[" + args[1] + "]不在线!你需要选择一个在线的玩家!");
                        return true;
                    }
                    Pad pad = Pad.getpad(player);
                    if (pad == null) {
                        sender.sendMessage(ChatColor.RED + "" + player.getName() + " 没有一个对手!使用指令为他添加!");
                        return true;
                    }
                    Battle battle = Battle.getbattle(player);
                    if (battle == null) {
                        battle = new Battle(pad);
                    }
                    battle.start(player);
                    sender.sendMessage(ChatColor.BOLD + "" + ChatColor.AQUA + "好兄弟,开打了,冲冲冲!!!");
                    return true;
                } else if (args[0].equalsIgnoreCase("remove")){
                    Player player = Bukkit.getPlayer(args[1]);
                    if (!(player instanceof Player)){
                        sender.sendMessage(ChatColor.RED + "玩家[" + args[1] + "]不在线!你需要选择一个在线的玩家!");
                        return true;
                    }
                    Pad pads = Pad.getpad(player);
                    if (pads == null){
                        sender.sendMessage(ChatColor.RED + "" + player.getName() + " 没有一个对手!使用指令为他添加!");
                        return true;
                    }
                    Battle battle = Battle.getbattle(player);
                    if (battle != null) {
                        battle.remove();
                    }
                    pads.remove();
                    sender.sendMessage(ChatColor.GREEN + "已经移除玩家 " + player.getName());
                    return true;
                } else if (args[0].equalsIgnoreCase("DStart")) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "玩家[" + args[1] + "]不在线!你需要选择一个在线的玩家!");
                        return true;
                    }
                    Pad pad = Pad.getpad(player);
                    if (pad == null) {
                        sender.sendMessage(ChatColor.RED + "" + player.getName() + " 没有一个对手!使用指令为他添加!");
                        return true;
                    }
                    Battle battle = Battle.getbattle(player);
                    if (battle == null) {
                        battle = new Battle(pad);
                    }
                    battle.numstart(player);
                    sender.sendMessage(ChatColor.BOLD + "" + ChatColor.AQUA + "好兄弟,开打了,冲冲冲!!!,别忘了你开的不是新版");
                    return true;
                }
            } else if (args.length == 3 && args[0].equalsIgnoreCase("add")){
                Player player1 = Bukkit.getPlayer(args[1]);
                Player player2 = Bukkit.getPlayer(args[2]);
                if (player1 == null){
                    sender.sendMessage(ChatColor.RED + "玩家" + args[1] + "无法被找到.");
                    return false;
                }
                if (player2 == null){
                    sender.sendMessage(ChatColor.RED + "玩家" + args[2] + "无法被找到.");
                    return false;
                }
                if (player1.equals(player2)){
                    sender.sendMessage(ChatColor.RED + "这两个玩家不能是同一个人!");
                    return false;
                }
                if (Pad.getpad(player1) != null) {
                    sender.sendMessage(ChatColor.RED + player2.getName() + "已经在一个对手组中辣!");
                    return false;
                }
                if (Pad.getpad(player2) != null) {
                    sender.sendMessage(ChatColor.RED + player2.getName() + "已经在一个对手组中辣!");
                    return false;
                }
                new Pad(player1.getUniqueId(), player2.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + player1.getName() + "和" + player2.getName() + "现在是一对辣!(?");
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "就这插件具体咋用呢,宁得这样");
        sender.sendMessage(ChatColor.RED + "/DUtimatetagRemake add <玩家1> <玩家2>");
        sender.sendMessage(ChatColor.RED + "/DUtimatetagRemake Start <抓的玩家> (新装备配置 含有新功能)");
        sender.sendMessage(ChatColor.RED + "/DUtimatetagRemake DStart <抓的玩家> (装备为旧版 含有新功能)");
        sender.sendMessage(ChatColor.RED + "/DUtimatetagRemake remove <被移除的惨b>");
        sender.sendMessage(ChatColor.RED + "每次提示时间双方发光!逃脱者比追杀者多石头和箭矢,但是他没盾.");
        sender.sendMessage(ChatColor.RED + "当被追的人被用指南针探测会有粒子效果. (RM by Fengli) ");
        sender.sendMessage(ChatColor.RED + "oh , by the way...");
        sender.sendMessage(ChatColor.RED +""+ChatColor.BOLD+ "YOU ONLY HAVE ONE CHANCE =) ");
        return false;
    }
    @EventHandler
    public void OnDamage(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player){
            Player damaer = (Player)e.getDamager();
            Battle battle = Battle.getbattle(damaer);
            if (battle != null  && battle.isStarted() && battle.isPlayer((Player)e.getEntity())){
                e.setDamage(0);
                if (battle.isGhoust(damaer)){
                    battle.endRound(damaer,true);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Battle battle = Battle.getbattle(player);
        if (battle != null && battle.isStarted()) {
            battle.endRound(battle.getOther(player), true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        this.onLeave(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKickEvent(PlayerKickEvent event) {
        this.onLeave(event.getPlayer());
    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            Battle battle = Battle.getbattle(player);
            if (battle != null && battle.isStarted()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCLICK(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.hasItem() && event.getItem().getType() == Material.COMPASS  && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            Battle battle = Battle.getbattle(player);
            if (battle != null && battle.isStarted()) {
                Player other = battle.getOther(player);
                player.setCompassTarget(other.getLocation());
                player.sendMessage(ChatColor.GREEN + "指南针现在指着" + other.getName() + "当前他的Y轴为" + ChatColor.AQUA +other.getLocation().getBlockY() + "");
                other.getWorld().spawnParticle(Particle.FIREWORKS_SPARK,other.getLocation().getX(),other.getLocation().getY()+0.4,other.getLocation().getZ(),45,0.1,0.85,0.1,0.09);
            }
        }
    }

    private void onLeave(Player player) {
        Battle battle = Battle.getbattle(player);
        if (battle != null) {
            battle.remove();
        }
        Pad pad = Pad.getpad(player);
        if (pad != null) {
            pad.remove();
        }
        NikoCodeFengliCode.NikoCodeFengliCode(player);
    }
    // iw love Niko
    // I L O V E N I K O ! ! !
    // code by Fengli
    // idea by Dream
    // ... HiPeaksolPlguins!!!!!!!!!!!!!!!!!!!!!!!!
}
