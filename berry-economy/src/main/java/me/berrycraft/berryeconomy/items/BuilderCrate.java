package me.berrycraft.berryeconomy.items;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import me.berrycraft.berryeconomy.Berry;
import me.berrycraft.berryeconomy.custom_loot.CustomLootTable;
import me.berrycraft.berryeconomy.custom_loot.RigLoot;

public class BuilderCrate extends CustomItem  implements Listener{

    public BuilderCrate() {
        super(Material.PLAYER_HEAD);
        super.setSkull("http://textures.minecraft.net/texture/57b8324d1b29efd667bfbd69134e7d8917110791b81baf80fdb6e2c207afe181");

        ItemMeta meta = this.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Builder Crate");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "");

        lore.add( ChatColor.GOLD + "Open " + ChatColor.YELLOW + "" + ChatColor.BOLD + "RIGHT-CLICK");
        lore.add(ChatColor.GRAY + "");
        lore.add(ChatColor.YELLOW + "50%" + ChatColor.GRAY + " chance for" + ChatColor.DARK_GRAY + " Common");
        lore.add(ChatColor.YELLOW + "30%" + ChatColor.GRAY + " chance for" + ChatColor.DARK_AQUA + " Uncommon");
        lore.add(ChatColor.YELLOW + "15%" + ChatColor.GRAY + " chance for" + ChatColor.DARK_PURPLE +  " Rare");
        lore.add(ChatColor.YELLOW + "4%" + ChatColor.GRAY + " chance for" + ChatColor.GOLD +  " Legendary");
        lore.add(ChatColor.YELLOW + "1%" + ChatColor.GRAY + " chance for" + ChatColor.DARK_RED +  " Mythic");

        meta.setLore(lore);
        this.setItemMeta(meta);
        this.setItemMeta(meta);
        NBT.modify(this, nbt -> {
            nbt.setString("CustomItem","BuilderCrate");
        });
        this.setAmount(1);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getPlayer().getItemInHand().getType() != Material.PLAYER_HEAD) return;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        NBTItem nbti = new NBTItem(p.getItemInHand());

        if (!nbti.getString("CustomItem").equals("BuilderCrate")) return;
        if (e.getClickedBlock()==null) return;


        if (p.getGameMode()!=GameMode.CREATIVE) {
            ItemStack stack = p.getItemInHand();
            stack.setAmount(stack.getAmount()-1);

        }
        Location loc = e.getClickedBlock().getLocation().add(0.5,0.5,0.5); // in front of player
        ArmorStand stand = p.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setMarker(true);
            as.setGravity(false);
            as.setInvulnerable(true);
            as.setCustomNameVisible(false);
            as.setSmall(true);
        });
        Vector toPlayer = p.getLocation().toVector().subtract(loc.toVector());
        float angle = (float) Math.atan2(-toPlayer.getX(), toPlayer.getZ());

        stand.getLocation().setYaw(angle);
        //stand.teleport(standLoc);


        stand.getAttribute(Attribute.SCALE).setBaseValue(1.5);
        stand.setHelmet(new BuilderCrate()); // just for visual effect, optional
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
                stand.getWorld().spawnParticle(
                        Particle.DUST,
                        loc.clone().add(0,1,0),
                        1, // count
                        0.2, 0.2, 0.2, // offsetX/Y/Z
                        1.0, // extra (ignored for DUST)
                        new DustOptions(Color.WHITE, 0.5f) // color and size
                    );
                if (shake > 0.2) {
                    //stand.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.1f, 1);
                    stand.getWorld().playSound(loc, Sound.BLOCK_CAVE_VINES_BREAK, 1f, 1);
                    stand.getWorld().spawnParticle(
                        Particle.DUST,
                        loc.clone().add(0,1,0),
                        10, // count
                        0.2, 0.2, 0.2, // offsetX/Y/Z
                        1.0, // extra (ignored for DUST)
                        new DustOptions(Color.WHITE, 1.5f) // color and size
                    );

                }
                if (time > 4) {
                    stand.getAttribute(Attribute.SCALE).setBaseValue((time/8)*(time/8)+1+Math.pow((time-4)*2,2)+Math.sin(time)*0.1);
                    stand.teleport(stand.getLocation().subtract(0,0.6*(time-4),0));
                }
                if (time > 4.3) {
                    stand.getWorld().playSound(loc, Sound.ENTITY_LLAMA_SPIT, 1, 1);
                    stand.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 1);

                    stand.getWorld().spawnParticle(Particle.EXPLOSION, loc, 2);
                    stand.getWorld().spawnParticle(
                        Particle.DUST,
                        loc.clone().add(0,1,0),
                        60, // count
                        0.4, 0.4, 0.4, // offsetX/Y/Z
                        1.0, // extra (ignored for DUST)
                        new DustOptions(Color.WHITE, 2f) // color and size
                    );
                    stand.remove(); // clean up
                    this.cancel();
                    double rand_num = -1.0;
                    rand_num = RigLoot.check(p, "builder_crate");
                    rand_num = rand_num > 0 ? rand_num : Math.random();

                    if (rand_num < 0.5) {
                        dropLoot(loc, "drop_builder_common");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "builder_resource");


                        stand.getWorld().spawnParticle(
                        Particle.DUST,
                        loc.clone().add(0,1,0),
                        30, // count
                        0.4, 0.4, 0.4, // offsetX/Y/Z
                        1.0, // extra (ignored for DUST)
                        new DustOptions(Color.fromRGB(255,255,255), 3.5f)// color and size

                        );

                    } else if (rand_num < 0.8){
                        dropLoot(loc, "drop_builder_uncommon");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "builder_resource");
                        stand.getWorld().spawnParticle(
                        Particle.DUST,
                        loc.clone().add(0,1,0),
                        30, // count
                        0.4, 0.4, 0.4, // offsetX/Y/Z
                        1.0, // extra (ignored for DUST)
                        new DustOptions(Color.fromRGB(51,153,255), 3.5f)// color and size

                        );
                    

                    } else if (rand_num < 0.95){
                        dropLoot(loc, "drop_builder_rare");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "redstone_resource");

                        stand.getWorld().spawnParticle(
                        Particle.DUST,
                        loc.clone().add(0,1,0),
                        30, // count
                        0.4, 0.4, 0.4, // offsetX/Y/Z
                        1.0, // extra (ignored for DUST)
                        new DustOptions(Color.fromRGB(153,51,255), 3.5f)// color and size

                        );
                        
                    } 
                    else if (rand_num < 0.99){
                        dropLoot(loc, "drop_builder_legendary");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "redstone_resource");

                        stand.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1);

                        stand.getWorld().spawnParticle(
                        Particle.DUST,
                        loc.clone().add(0,1,0),
                        30, // count
                        0.4, 0.4, 0.4, // offsetX/Y/Z
                        1.0, // extra (ignored for DUST)
                        new DustOptions(Color.fromRGB(255,204,0), 3.5f)// color and size
                        );
                        
                    } else {
                        dropLoot(loc, "loot_mythic");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "builder_resource");
                        dropLoot(loc, "redstone_resource");

                        stand.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1);

                        stand.getWorld().spawnParticle(
                        Particle.DUST,
                        loc.clone().add(0,1,0),
                        30, // count
                        0.4, 0.4, 0.4, // offsetX/Y/Z
                        1.0, // extra (ignored for DUST)
                        new DustOptions(Color.fromRGB(204,0,0), 3.5f)// color and size
                        );
                    }
                }
            }
        }.runTaskTimer(Berry.getInstance(), 0, 1);
    }

    private void dropLoot(Location loc, String lootTable) {
        LinkedList<ItemStack> loot = CustomLootTable.getTable(lootTable).roll(new Random());
        for (ItemStack item : loot) {
            Item dropped = loc.getWorld().dropItemNaturally(loc.clone().add(0,0.6,0), item);

            // Apply random velocity
            double dx = (Math.random() - 0.5) * 0.4;
            double dy = Math.random() * 0.5 + 0.2;
            double dz = (Math.random() - 0.5) * 0.4;
            dropped.setVelocity(new Vector(dx, dy, dz));
        }
    }
    
}
