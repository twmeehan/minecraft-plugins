package me.berrycraft.berryeconomy.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.BerryUtility;

public class CommonCrate extends CustomItem implements Listener{

    public CommonCrate() {
        super(Material.PLAYER_HEAD);
        super.setSkull("http://textures.minecraft.net/texture/696b6c77f2c1133f6df736ef2d8c52d47f58487c23e8480ef79e0d60a289f58");

        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Common Crate");

        this.setItemMeta(meta);
        NBT.modify(this, nbt -> {
            nbt.setString("CustomItem","CommonCrate");
        });
        this.setAmount(1);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getPlayer().getItemInHand().getType() != Material.PLAYER_HEAD) return;

        Player p = e.getPlayer();
        NBTItem nbti = new NBTItem(p.getItemInHand());

        if (!nbti.getString("CustomItem").equals("CommonCrate")) return;

        Location loc = e.getClickedBlock().getLocation().add(0.5,0.5,0.5); // in front of player
        ArmorStand stand = p.getWorld().spawn(loc, ArmorStand.class);
        
        Vector direction = p.getLocation().toVector().subtract(loc.toVector());
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ())); // Minecraft yaw is Z-forward

        loc.setYaw(yaw);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.getAttribute(Attribute.SCALE).setBaseValue(1.5);
        stand.setSmall(true);
        stand.setMarker(true); // no hitbox
        stand.setHelmet(new CommonCrate()); // just for visual effect, optional
        float angle = yaw;
        stand.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1f, 1);


        new BukkitRunnable() {
            double time = 0.1;
            

            @Override
            public void run() {
                time += 0.05;
                double base_x = Math.sin(time*10)*0.05;
                double base_y = angle;
                double base_z = Math.cos(time*5)*0.05;
                double shake = Math.max(Math.sin((time+0.5)*4)-0.7,0);
                
                double shake_x = (Math.random() - 0.5) * shake * 0.8;
                double shake_y = (Math.random() - 0.5) * shake * 0.8;
                double shake_z = (Math.random() - 0.5) * shake * 0.8;

                // Apply combined motion to the head pose
                stand.setHeadPose(new EulerAngle(
                    base_x + shake_x,
                    base_y + shake_y,
                    base_z + shake_z
                ));
                stand.getAttribute(Attribute.SCALE).setBaseValue((time/8)*(time/8)+1+Math.sin(time)*0.1);
                if (shake > 0.2) {
                    //stand.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.1f, 1);
                    stand.getWorld().playSound(loc, Sound.BLOCK_CAVE_VINES_BREAK, 1f, 1);

                }
                if (time > 4) {
                    stand.getAttribute(Attribute.SCALE).setBaseValue((time/8)*(time/8)+1+Math.pow((time-4)*2,2)+Math.sin(time)*0.1);
                    stand.teleport(stand.getLocation().subtract(0,0.6*(time-4),0));
                }
                if (time > 4.5) {
                    stand.getWorld().playSound(loc, Sound.ENTITY_LLAMA_SPIT, 1, 1);
                    stand.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1);

                    stand.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1);
                    stand.remove(); // clean up
                    this.cancel();
                }
            }
        }.runTaskTimer(Berry.getInstance(), 0, 1);
    }
    
    
}
