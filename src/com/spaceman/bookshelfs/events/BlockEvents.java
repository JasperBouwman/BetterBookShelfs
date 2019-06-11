package com.spaceman.bookshelfs.events;

import com.spaceman.bookshelfs.Main;
import com.spaceman.bookshelfs.Pair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.spaceman.bookshelfs.Main.SHELF_SIZE;

public class BlockEvents implements Listener {
    
    @EventHandler
    @SuppressWarnings("unused")
    public void Event(PlayerInteractEvent e) {
        
        //todo add lock/remove lock
        
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && Material.BOOKSHELF.equals(e.getClickedBlock().getType()) && !e.getPlayer().isSneaking() && EquipmentSlot.HAND.equals(e.getHand())) {
            e.setCancelled(true);
            
            if (Main.viewers.values().stream().noneMatch(p -> p.getLeft().equals(e.getClickedBlock().getLocation()))) {
                
                if (Main.inventories.get(e.getClickedBlock().getLocation()).getRight() == null) {
                    e.getPlayer().sendMessage(ChatColor.RED + "This bookshelf has a lock");
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_LOCKED, 1, 1);
                    return;
                }
                
                Pair<Location, String> pair = new Pair<>(
                        e.getClickedBlock().getLocation(),
                        Main.inventories.getOrDefault(
                            e.getClickedBlock().getLocation(),
                            new Pair<>(null, null)
                ).getRight());
                
                Main.viewers.put(e.getPlayer().getUniqueId(), pair);
                
                if (Main.inventories.containsKey(e.getClickedBlock().getLocation())) {
                    e.getPlayer().openInventory(Main.inventories.get(e.getClickedBlock().getLocation()).getLeft());
                } else {
                    Inventory inv = Bukkit.createInventory(null, SHELF_SIZE, ChatColor.DARK_GRAY + "Book Shelf");
                    e.getPlayer().openInventory(inv);
                }
            } else {
                e.getPlayer().sendMessage(ChatColor.RED + "Only 1 player can view a bookshelf");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void event(BlockPlaceEvent e) {
        if (e.getBlockAgainst().getType().equals(Material.BOOKSHELF) && !e.getPlayer().isSneaking()) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    @SuppressWarnings("unused")
    public void event(BlockBreakEvent e) {
        if (e.getBlock().getType().equals(Material.BOOKSHELF)) {
            
            if (Main.inventories.containsKey(e.getBlock().getLocation())) {
                
                Location l = e.getBlock().getLocation();
    
                if (Main.viewers.values().stream().noneMatch(p -> p.getLeft().equals(l))) {
                    InventoryEvents.saveInventory(e.getPlayer().getInventory(), e.getPlayer());
                }
                
                if (Main.inventories.get(l).getRight() != null) {
                    ItemMeta meta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
                    if (meta != null) {
                        if (meta.hasDisplayName()) {
                            String name = meta.getDisplayName();
                            if (!Main.inventories.get(l).getRight().equals(name)) {
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                
                Inventory inv = Main.inventories.get(l).getLeft();
                Main.inventories.remove(l);
                
                l.setY(l.getY() + 0.5);
                
                for (ItemStack is : inv.getContents()) {
                    if (is == null) {
                        continue;
                    }
                    e.getPlayer().getWorld().dropItemNaturally(l, is);
                    
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void extend(BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks()) {
            if (b.getType().equals(Material.BOOKSHELF)) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused")
    public void retract(BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks()) {
            if (b.getType().equals(Material.BOOKSHELF)) {
                e.setCancelled(true);
            }
        }
    }
}