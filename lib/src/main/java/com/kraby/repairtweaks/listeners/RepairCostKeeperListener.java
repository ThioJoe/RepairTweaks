package com.kraby.repairtweaks.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import com.kraby.repairtweaks.RepairTweaks;

/**
 * Keeps an item's "prior work" penalty (its anvil repair cost) from increasing
 * when the item is repaired with a raw material (gems, ingots, etc.).
 * Applying enchantments or combining two items still increases the penalty just
 * like in vanilla.
 */
public class RepairCostKeeperListener implements Listener {

    @EventHandler
    public void keepCostOnRepair(PrepareAnvilEvent e) {
        if (!RepairTweaks.singleton.config.isRepairDontIncreaseCost())
            return;

        AnvilInventory inv = e.getInventory();
        ItemStack firstItem = inv.getFirstItem();
        ItemStack secondItem = inv.getSecondItem();

        if (!(
            firstItem != null
            && secondItem != null
            && e.getResult() != null
            && firstItem.getItemMeta() instanceof Repairable))
        {
            return;
        }

        // Only apply to material repairs: the second slot must be a repair
        // material (a different item than the one being repaired). This leaves
        // out combining two of the same item, which keeps its vanilla cost.
        if (secondItem.getType() == firstItem.getType()) {
            return;
        }

        // Leave enchantments (e.g. enchanted books) alone: those should still
        // increase the prior work cost like in vanilla.
        ItemMeta secondItemMeta = secondItem.getItemMeta();
        if (secondItemMeta.hasEnchants()
            || (secondItemMeta instanceof EnchantmentStorageMeta && ((EnchantmentStorageMeta) secondItemMeta).hasStoredEnchants())) {
            return;
        }

        ItemStack result = e.getResult();
        Repairable resultMeta = (Repairable) result.getItemMeta();
        resultMeta.setRepairCost(((Repairable) firstItem.getItemMeta()).getRepairCost());
        result.setItemMeta(resultMeta);
        e.setResult(result);
    }

}
