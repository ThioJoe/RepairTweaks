package com.kraby.repairtweaks.listeners;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import com.kraby.repairtweaks.RepairTweaks;

/**
 * Handles the "prior work" penalty for anvil repairs that don't add any new
 * enchantment (repairing with a raw material, or with an unenchanted copy of
 * the item). For those repairs it keeps the item's stored penalty from growing,
 * drops the penalty from the level cost of the repair itself, and lifts the
 * vanilla 40-level "Too Expensive!" cap so even a heavily penalised item can
 * always be repaired at its base cost. Adding an enchantment (an enchanted
 * book, or merging in an enchanted item) still raises and charges the penalty
 * and still becomes "Too Expensive!" past 40 levels, like in vanilla.
 */
public class RepairCostKeeperListener implements Listener {

    /**
     * Vanilla's anvil limit: an operation reaching this many levels shows
     * "Too Expensive!" and yields no result (outside creative mode).
     */
    private static final int VANILLA_MAX_REPAIR_COST = 40;

    /**
     * Vanilla decides "Too Expensive!" (and clears the result) before
     * PrepareAnvilEvent fires, so the cap must already be lifted when the
     * result is computed for penalised repairs to reach the event at all.
     * Vanilla behaviour for enchanting and renaming is restored in
     * {@link #keepCostOnRepair}.
     */
    @EventHandler
    public void liftCostCapOnOpen(InventoryOpenEvent e) {
        if (!RepairTweaks.singleton.config.isRepairDontIncreaseCost())
            return;

        if (e.getInventory() instanceof AnvilInventory)
            ((AnvilInventory) e.getInventory()).setMaximumRepairCost(Integer.MAX_VALUE);
    }

    @EventHandler
    public void keepCostOnRepair(PrepareAnvilEvent e) {
        if (!RepairTweaks.singleton.config.isRepairDontIncreaseCost())
            return;

        AnvilInventory inv = e.getInventory();

        // Covers anvils that were already open when the plugin (re)loaded;
        // takes effect from the next result computation on.
        inv.setMaximumRepairCost(Integer.MAX_VALUE);

        ItemStack firstItem = inv.getFirstItem();
        ItemStack secondItem = inv.getSecondItem();
        ItemStack result = e.getResult();
        // Invalid or impossible operations come through as an empty result -
        // null or an air stack, depending on the server implementation.
        boolean hasResult = result != null && !result.getType().isAir();

        if (isEmpty(firstItem))
            return;

        // Rename-only operation (empty second slot): with the cap lifted,
        // vanilla's own clamp of rename costs to 39 levels no longer engages,
        // so restore it.
        if (isEmpty(secondItem)) {
            if (hasResult && inv.getRepairCost() >= VANILLA_MAX_REPAIR_COST)
                inv.setRepairCost(VANILLA_MAX_REPAIR_COST - 1);
            return;
        }

        // Leave enchantments alone: if the second item carries any enchantments
        // (an enchanted book, or an enchanted item being merged in), a new
        // enchantment is being added, so let the prior work cost increase like
        // in vanilla - including the "Too Expensive!" limit the lifted cap
        // bypassed: past 40 levels the operation stays impossible outside
        // creative mode.
        ItemMeta secondItemMeta = secondItem.getItemMeta();
        if (secondItemMeta != null
            && (secondItemMeta.hasEnchants()
                || (secondItemMeta instanceof EnchantmentStorageMeta && ((EnchantmentStorageMeta) secondItemMeta).hasStoredEnchants()))) {
            if (hasResult
                && inv.getRepairCost() >= VANILLA_MAX_REPAIR_COST
                && e.getView().getPlayer().getGameMode() != GameMode.CREATIVE) {
                e.setResult(null);
            }
            return;
        }

        if (!hasResult
            || !(firstItem.getItemMeta() instanceof Repairable)
            || !(result.getItemMeta() instanceof Repairable)) {
            return;
        }

        int firstPriorWork = ((Repairable) firstItem.getItemMeta()).getRepairCost();

        // Keep the result's stored prior work penalty from growing.
        Repairable resultMeta = (Repairable) result.getItemMeta();
        resultMeta.setRepairCost(firstPriorWork);
        result.setItemMeta(resultMeta);
        e.setResult(result);

        // Drop the prior work penalty from this operation's level cost too, so a
        // basic repair is never charged for an already-accumulated penalty -
        // only the normal repair cost (e.g. the material sacrifice) is left.
        // Vanilla adds each input item's stored penalty to the level cost, so
        // subtracting them removes exactly the penalty portion. Never go below
        // 1: every real operation has a base cost of at least 1 level, and
        // vanilla refuses to hand over a result priced at 0.
        int secondPriorWork = (secondItem.getItemMeta() instanceof Repairable)
            ? ((Repairable) secondItem.getItemMeta()).getRepairCost()
            : 0;
        inv.setRepairCost(Math.max(1, inv.getRepairCost() - firstPriorWork - secondPriorWork));
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().isAir();
    }
}
