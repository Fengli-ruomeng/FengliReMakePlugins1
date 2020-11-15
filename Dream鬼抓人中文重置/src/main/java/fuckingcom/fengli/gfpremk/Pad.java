package fuckingcom.fengli.gfpremk;

import org.bukkit.entity.*;
import org.bukkit.*;
import java.util.*;

public class Pad {
    private static Set<Pad> pads;
    private UUID Player1;
    private UUID Player2;

    public Pad(UUID p1,UUID p2){
        this.Player1 = p1;
        this.Player2 = p2;
        Pad.pads.add(this);
    }

    public static Set<Pad> getPads() {
        return Pad.pads;
    }

    public static Pad getpad(Player player){
        for(Pad pad : Pad.pads){
            if(pad.getid1().equals(player.getUniqueId()) || pad.getid2().equals(player.getUniqueId())){
                return pad;
            }
        }
        return null;
    }


    public UUID getid1() {
        return this.Player1;
    }

    public UUID getid2() {
        return this.Player2;
    }

    public Player getPlayer1() {
        return Bukkit.getPlayer(this.Player1);
    }

    public Player getPlayer2() {
        return Bukkit.getPlayer(this.Player2);
    }

    public void remove() {
        Pad.pads.remove(this);
    }
    static {
        Pad.pads = new HashSet<Pad>();
    }
}
