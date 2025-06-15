package me.berrycraft.dynamicspells.Spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.util.Transformation;
import org.bukkit.util.RayTraceResult;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import me.berrycraft.dynamicspells.DynamicSpells;
import me.berrycraft.dynamicspells.Spell;
import de.tr7zw.nbtapi.NBTItem;

import java.util.*;

public class Copy extends Spell implements Listener {
  public static final String NAME = "copy";
  public static final Material MATERIAL = Material.BOOK;
  public static YamlConfiguration config;
  private static final long PREVIEW_DEBOUNCE_MS = 200; // 200ms debounce time

  private enum SpellState {
    IDLE, // No active operation
    SELECTING, // Selecting positions to copy
    READY_TO_PASTE // Has copied region and ready to paste
  }

  private static class BlockData {
    final Material material;
    final org.bukkit.block.data.BlockData blockData;
    final int relativeX;
    final int relativeY;
    final int relativeZ;

    BlockData(Material material, org.bukkit.block.data.BlockData blockData, int relativeX, int relativeY,
        int relativeZ) {
      this.material = material;
      this.blockData = blockData;
      this.relativeX = relativeX;
      this.relativeY = relativeY;
      this.relativeZ = relativeZ;
    }
  }

  private static class PlayerState {
    SpellState state = SpellState.IDLE;
    Location[] positions = new Location[2];
    int positionIndex = 0;
    List<BlockData> storedBlocks = null;
    List<BlockDisplay> previewDisplays = new ArrayList<>();
    int sizeX = 0;
    int sizeY = 0;
    int sizeZ = 0;
    // Add tracking for last preview target
    Block lastTargetBlock = null;
    org.bukkit.block.BlockFace lastTargetFace = null;
    long lastPreviewUpdate = 0;
  }

  private static final Map<Player, PlayerState> playerStates = new HashMap<>();

  public static void init() {
    config = loadSpellConfig(NAME);
    Copy copySpell = new Copy();
    Bukkit.getPluginManager().registerEvents(copySpell, DynamicSpells.getInstance());

    // Register the rotate command
    DynamicSpells.getInstance().getCommand("rotate").setExecutor((sender, command, label, args) -> {
      if (!(sender instanceof Player)) {
        sender.sendMessage("§cThis command can only be used by players!");
        return true;
      }

      copySpell.handleRotate((Player) sender);
      return true;
    });
  }

  private static PlayerState getPlayerState(Player player) {
    return playerStates.computeIfAbsent(player, p -> new PlayerState());
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    PlayerState state = getPlayerState(player);

    // Only handle preview if we're in paste mode
    if (state.state == SpellState.READY_TO_PASTE) {
      // Check if player is holding the spell book
      if (!isHoldingSpellBook(player)) {
        clearPreview(player);
        return;
      }

      // Get the block and face the player is looking at
      RayTraceResult rayTrace = player.rayTraceBlocks(5);
      if (rayTrace == null || !rayTrace.getHitBlock().getType().isSolid()) {
        clearPreview(player);
        return;
      }

      Block targetBlock = rayTrace.getHitBlock();
      org.bukkit.block.BlockFace face = rayTrace.getHitBlockFace();
      if (face == null) {
        clearPreview(player);
        return;
      }

      // Check if target has changed and debounce time has passed
      long currentTime = System.currentTimeMillis();
      if ((targetBlock.equals(state.lastTargetBlock) && face == state.lastTargetFace) ||
          currentTime - state.lastPreviewUpdate < PREVIEW_DEBOUNCE_MS) {
        return;
      }

      // Update tracking
      state.lastTargetBlock = targetBlock;
      state.lastTargetFace = face;
      state.lastPreviewUpdate = currentTime;

      // Update preview
      updatePreview(player);
    }
  }

  @EventHandler
  public void onPlayerItemHeld(PlayerItemHeldEvent event) {
    Player player = event.getPlayer();
    PlayerState state = getPlayerState(player);

    if (state.state == SpellState.READY_TO_PASTE) {
      clearPreview(player);
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    Player player = event.getPlayer();
    PlayerState state = getPlayerState(player);

    if (state.state == SpellState.READY_TO_PASTE) {
      clearPreview(player);
    }
  }

  @EventHandler
  public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
    Player player = event.getPlayer();
    PlayerState state = getPlayerState(player);

    if (state.state == SpellState.READY_TO_PASTE) {
      clearPreview(player);
    }
  }

  private void handleRotate(Player player) {
    PlayerState state = getPlayerState(player);

    if (state.state != SpellState.READY_TO_PASTE || state.storedBlocks == null) {
      player.sendMessage("§cYou need to have something copied first!");
      return;
    }

    // Rotate the stored blocks 90 degrees clockwise
    List<BlockData> rotatedBlocks = new ArrayList<>();

    // First find the size of the structure to calculate the new origin point
    int maxX = 0, maxZ = 0;
    for (BlockData block : state.storedBlocks) {
      maxX = Math.max(maxX, block.relativeX);
      maxZ = Math.max(maxZ, block.relativeZ);
    }

    for (BlockData block : state.storedBlocks) {
      // For 90 degrees clockwise:
      // x' = z
      // z' = -(x - maxX) + maxZ
      int newX = block.relativeZ;
      int newZ = -(block.relativeX - maxX);

      // Rotate the block data
      org.bukkit.block.data.BlockData rotatedData = block.blockData.clone();
      if (rotatedData instanceof Directional) {
        Directional dir = (Directional) rotatedData;
        org.bukkit.block.BlockFace face = dir.getFacing();
        // Rotate the facing 90 degrees clockwise
        switch (face) {
          case NORTH:
            dir.setFacing(org.bukkit.block.BlockFace.EAST);
            break;
          case EAST:
            dir.setFacing(org.bukkit.block.BlockFace.SOUTH);
            break;
          case SOUTH:
            dir.setFacing(org.bukkit.block.BlockFace.WEST);
            break;
          case WEST:
            dir.setFacing(org.bukkit.block.BlockFace.NORTH);
            break;
        }
      } else if (rotatedData instanceof Rotatable) {
        Rotatable rot = (Rotatable) rotatedData;
        org.bukkit.block.BlockFace face = rot.getRotation();
        // Rotate the rotation 90 degrees clockwise
        switch (face) {
          case NORTH:
            rot.setRotation(org.bukkit.block.BlockFace.EAST);
            break;
          case EAST:
            rot.setRotation(org.bukkit.block.BlockFace.SOUTH);
            break;
          case SOUTH:
            rot.setRotation(org.bukkit.block.BlockFace.WEST);
            break;
          case WEST:
            rot.setRotation(org.bukkit.block.BlockFace.NORTH);
            break;
        }
      }

      rotatedBlocks.add(new BlockData(
          block.material,
          rotatedData,
          newX,
          block.relativeY,
          newZ));
    }

    // Update the stored blocks
    state.storedBlocks = rotatedBlocks;

    // Swap X and Z dimensions
    int tempX = state.sizeX;
    state.sizeX = state.sizeZ;
    state.sizeZ = tempX;

    // Update the preview if it exists
    if (!state.previewDisplays.isEmpty()) {
      updatePreview(player);
    }

    player.sendMessage("§aRotated the copied blocks 90 degrees clockwise!");
    player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 2.0f);
  }

  private boolean isHoldingSpellBook(Player player) {
    ItemStack mainHand = player.getInventory().getItemInMainHand();
    ItemStack offHand = player.getInventory().getItemInOffHand();
    return isSpellBook(mainHand) || isSpellBook(offHand);
  }

  private static Location calculatePasteLocation(Location targetLoc, org.bukkit.block.BlockFace face,
      PlayerState state) {
    Location pasteOrigin = targetLoc.clone();

    // Adjust paste origin based on the targeted face
    switch (face) {
      case EAST: // Looking at east face, paste should touch it from the west
        pasteOrigin.add(1, 0, 0);
        break;
      case WEST: // Looking at west face, paste should touch it from the east
        pasteOrigin.add(-state.sizeX, 0, 0);
        break;
      case UP: // Looking at top face, paste should touch it from below
        pasteOrigin.add(0, 1, 0);
        break;
      case DOWN: // Looking at bottom face, paste should touch it from above
        pasteOrigin.add(0, -state.sizeY, 0);
        break;
      case SOUTH: // Looking at south face, paste should touch it from the north
        pasteOrigin.add(0, 0, 1);
        break;
      case NORTH: // Looking at north face, paste should touch it from the south
        pasteOrigin.add(0, 0, -state.sizeZ);
        break;
    }
    return pasteOrigin;
  }

  private static void processBlocks(Player player, PlayerState state, Location pasteOrigin, boolean isPreview) {
    if (isPreview) {
      // Clear old preview first
      clearPreview(player);
    }

    List<BlockDisplay> displays = new ArrayList<>();

    for (BlockData blockData : state.storedBlocks) {
      Location newLoc = pasteOrigin.clone().add(
          blockData.relativeX,
          blockData.relativeY,
          blockData.relativeZ);

      if (isPreview) {
        // Create preview block
        BlockDisplay display = player.getWorld().spawn(newLoc, BlockDisplay.class);
        display.setBlock(blockData.blockData);

        // Make it semi-transparent
        display.setTransformation(new Transformation(
            new Vector3f(0, 0, 0),
            new AxisAngle4f(0, 0, 0, 1),
            new Vector3f(1, 1, 1),
            new AxisAngle4f(0, 0, 0, 1)));
        display.setBrightness(new BlockDisplay.Brightness(15, 15));
        display.setGlowing(true);
        display.setViewRange(0.1f);

        displays.add(display);
      } else {
        // Only place block if it's air
        if (newLoc.getBlock().getType().isAir()) {
          // Track the block change
          Undo.trackBlockPlace(player, newLoc.getBlockX(), newLoc.getBlockY(), newLoc.getBlockZ(), 
              Material.AIR, blockData.material);
          
          // Place actual block
          newLoc.getBlock().setBlockData(blockData.blockData);

          // Play block place effect
          player.spawnParticle(Particle.BLOCK, newLoc.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0,
              blockData.blockData);
        }
      }
    }

    if (isPreview) {
      state.previewDisplays = displays;
    } else {
      // Play paste effects
      player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
      player.spawnParticle(Particle.EXPLOSION, pasteOrigin, 20, 0.5, 0.5, 0.5, 0);
    }
  }

  private static void updatePreview(Player player) {
    PlayerState state = getPlayerState(player);

    if (state.storedBlocks == null)
      return;

    // Get the block and face the player is looking at
    RayTraceResult rayTrace = player.rayTraceBlocks(5);
    if (rayTrace == null || !rayTrace.getHitBlock().getType().isSolid())
      return;

    Block targetBlock = rayTrace.getHitBlock();
    org.bukkit.block.BlockFace face = rayTrace.getHitBlockFace();
    if (face == null)
      return;

    Location pasteOrigin = calculatePasteLocation(targetBlock.getLocation(), face, state);
    processBlocks(player, state, pasteOrigin, true);
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    Player player = event.getPlayer();

    // Check main hand
    ItemStack mainHand = player.getInventory().getItemInMainHand();
    if (isSpellBook(mainHand)) {
      event.setCancelled(true);
      return;
    }

    // Check offhand
    ItemStack offHand = player.getInventory().getItemInOffHand();
    if (isSpellBook(offHand)) {
      event.setCancelled(true);
      return;
    }
  }

  private boolean isSpellBook(ItemStack item) {
    if (item == null || item.getType() != MATERIAL) {
      return false;
    }

    try {
      NBTItem nbti = new NBTItem(item);
      return "spell_book".equals(nbti.getString("CustomItem")) &&
          NAME.equals(nbti.getString("Spell"));
    } catch (Exception e) {
      return false;
    }
  }

  private static boolean isUnbreakable(Block block) {
    return block.getType().getHardness() < 0 ||
        block.getType() == Material.BARRIER ||
        block.getType() == Material.BEDROCK ||
        block.getType() == Material.END_PORTAL_FRAME ||
        block.getType() == Material.END_PORTAL ||
        block.getType() == Material.NETHER_PORTAL;
  }

  private static boolean hasContainer(Block block) {
    BlockState state = block.getState();
    return state instanceof Container;
  }

  public static boolean cast(Player caster, int level) {
    PlayerState state = getPlayerState(caster);

    // Handle shift + right click (set positions)
    if (caster.isSneaking()) {
      // If we're in paste mode, cancel it and go back to selection
      if (state.state == SpellState.READY_TO_PASTE) {
        state.state = SpellState.SELECTING;
        state.storedBlocks = null;
        clearPreview(caster);
        caster.sendMessage("§eEntering selection mode. Right-click blocks to set positions.");
        return false;
      }

      // Enter selection mode if we're idle
      if (state.state == SpellState.IDLE) {
        state.state = SpellState.SELECTING;
        state.positions = new Location[2];
        state.positionIndex = 0;
        caster.sendMessage("§eEntering selection mode. Right-click blocks to set positions.");
        return false;
      }

      // Handle position selection
      if (state.state == SpellState.SELECTING) {
        Block targetBlock = caster.getTargetBlock(null, 5);
        if (targetBlock != null && !targetBlock.getType().isAir()) {
          state.positions[state.positionIndex] = targetBlock.getLocation();
          state.positionIndex = (state.positionIndex + 1) % 2;

          // Play sound and particle effects
          caster.playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
          caster.spawnParticle(Particle.END_ROD, targetBlock.getLocation().add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 0);

          caster.sendMessage("§aPosition " + (state.positionIndex == 0 ? 2 : 1) + " set!");

          // If both positions are set, show the region size
          if (state.positions[0] != null && state.positions[1] != null) {
            int minX = Math.min(state.positions[0].getBlockX(), state.positions[1].getBlockX());
            int maxX = Math.max(state.positions[0].getBlockX(), state.positions[1].getBlockX());
            int minY = Math.min(state.positions[0].getBlockY(), state.positions[1].getBlockY());
            int maxY = Math.max(state.positions[0].getBlockY(), state.positions[1].getBlockY());
            int minZ = Math.min(state.positions[0].getBlockZ(), state.positions[1].getBlockZ());
            int maxZ = Math.max(state.positions[0].getBlockZ(), state.positions[1].getBlockZ());

            int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
            caster.sendMessage("§eSelected region size: " + volume + " blocks");
            caster.sendMessage("§eRight-click to copy this region!");
          }
        }
        return false;
      }
    }

    // Handle right click (copy/paste)
    if (state.state == SpellState.SELECTING) {
      if (state.positions[0] == null || state.positions[1] == null) {
        caster.sendMessage("§cPlease select both positions first!");
        return false;
      }

      // Calculate volume and check if it's within limits
      int minX = Math.min(state.positions[0].getBlockX(), state.positions[1].getBlockX());
      int maxX = Math.max(state.positions[0].getBlockX(), state.positions[1].getBlockX());
      int minY = Math.min(state.positions[0].getBlockY(), state.positions[1].getBlockY());
      int maxY = Math.max(state.positions[0].getBlockY(), state.positions[1].getBlockY());
      int minZ = Math.min(state.positions[0].getBlockZ(), state.positions[1].getBlockZ());
      int maxZ = Math.max(state.positions[0].getBlockZ(), state.positions[1].getBlockZ());

      int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
      int maxBlocks = config.getInt(level + ".max_blocks", 32);

      if (volume > maxBlocks) {
        caster.sendMessage("§cSelected area is too large! Maximum size: " + maxBlocks + " blocks");
        return false;
      }

      // Copy the region
      List<BlockData> blocks = new ArrayList<>();
      Map<Material, Integer> materials = new HashMap<>();

      for (int x = minX; x <= maxX; x++) {
        for (int y = minY; y <= maxY; y++) {
          for (int z = minZ; z <= maxZ; z++) {
            Location loc = new Location(state.positions[0].getWorld(), x, y, z);
            Block block = loc.getBlock();

            if (!block.getType().isAir() && !isUnbreakable(block) && !hasContainer(block)) {
              Material mat = block.getType();
              materials.put(mat, materials.getOrDefault(mat, 0) + 1);

              // Store block data with relative coordinates
              blocks.add(new BlockData(
                  mat,
                  block.getBlockData(),
                  x - minX,
                  y - minY,
                  z - minZ));
            }
          }
        }
      }

      state.storedBlocks = blocks;

      // Calculate size of the copied region
      int maxRelX = 0, maxRelY = 0, maxRelZ = 0;
      for (BlockData block : blocks) {
        maxRelX = Math.max(maxRelX, block.relativeX + 1);
        maxRelY = Math.max(maxRelY, block.relativeY + 1);
        maxRelZ = Math.max(maxRelZ, block.relativeZ + 1);
      }
      state.sizeX = maxRelX;
      state.sizeY = maxRelY;
      state.sizeZ = maxRelZ;

      state.state = SpellState.READY_TO_PASTE;

      // Show copied materials
      caster.sendMessage("§aCopied region! Materials required to paste:");
      for (Map.Entry<Material, Integer> entry : materials.entrySet()) {
        caster.sendMessage("§e" + entry.getKey().name() + ": " + entry.getValue());
      }
      caster.sendMessage("§aRight-click on a block to paste! Shift+right-click to cancel.");

      // Start showing preview
      updatePreview(caster);

      return true;
    } else if (state.state == SpellState.READY_TO_PASTE) {
      // Get the block and face the player is looking at
      RayTraceResult rayTrace = caster.rayTraceBlocks(5);
      if (rayTrace == null || !rayTrace.getHitBlock().getType().isSolid()) {
        caster.sendMessage("§cPlease look at a solid block to paste!");
        return false;
      }

      Block targetBlock = rayTrace.getHitBlock();
      org.bukkit.block.BlockFace face = rayTrace.getHitBlockFace();
      if (face == null) {
        caster.sendMessage("§cCouldn't determine which face you're looking at!");
        return false;
      }

      Location pasteOrigin = calculatePasteLocation(targetBlock.getLocation(), face, state);

      // Track the spell cast
      Undo.trackSpellCast(caster, NAME);
      
      // Track the item used (the spell book)
      ItemStack spellBook = caster.getInventory().getItemInMainHand();
      if (spellBook != null && spellBook.getType() == MATERIAL) {
        Undo.trackItemUse(caster, spellBook);
      }

      // Count materials needed
      Map<Material, Integer> materials = new HashMap<>();
      for (BlockData blockData : state.storedBlocks) {
        materials.put(blockData.material, materials.getOrDefault(blockData.material, 0) + 1);
      }

      // Show required materials
      caster.sendMessage("§eRequired materials to paste:");
      for (Map.Entry<Material, Integer> entry : materials.entrySet()) {
        caster.sendMessage("§e" + entry.getKey().name() + ": " + entry.getValue());
      }

      // Check if player has all required materials
      for (Map.Entry<Material, Integer> entry : materials.entrySet()) {
        int count = 0;
        for (ItemStack item : caster.getInventory().getContents()) {
          if (item != null && item.getType() == entry.getKey()) {
            count += item.getAmount();
          }
        }
        if (count < entry.getValue()) {
          caster.sendMessage(
              "§cYou don't have enough " + entry.getKey().name() + "! Need: " + entry.getValue() + ", Have: " + count);
          return false;
        }
      }

      // Remove materials from inventory
      for (Map.Entry<Material, Integer> entry : materials.entrySet()) {
        int remaining = entry.getValue();
        for (ItemStack item : caster.getInventory().getContents()) {
          if (item != null && item.getType() == entry.getKey()) {
            if (item.getAmount() <= remaining) {
              // Track the item before removing it
              Undo.trackItemUse(caster, item.clone());
              remaining -= item.getAmount();
              item.setAmount(0);
            } else {
              // Track the partial item used
              ItemStack partialItem = item.clone();
              partialItem.setAmount(remaining);
              Undo.trackItemUse(caster, partialItem);
              item.setAmount(item.getAmount() - remaining);
              remaining = 0;
            }
            if (remaining <= 0)
              break;
          }
        }
      }

      // Place blocks
      processBlocks(caster, state, pasteOrigin, false);

      // Keep the copied region after pasting
      caster.sendMessage(
          "§aRegion pasted successfully! You can paste it again, or shift+right-click to start a new copy.");
      return true;
    }

    return false;
  }

  private static void clearPreview(Player player) {
    PlayerState state = getPlayerState(player);
    for (BlockDisplay display : state.previewDisplays) {
      display.remove();
    }
    state.previewDisplays.clear();
  }
}