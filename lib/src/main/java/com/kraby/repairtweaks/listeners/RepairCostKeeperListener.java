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
 * when nothing new is being enchanted onto it. Repairing with a raw material
 * (gems, ingots, etc.) or with an unenchanted copy of the item leaves the cost
 * untouched. Only adding an enchantment (an enchanted book, or merging in an
 * enchanted item) still increases the penalty, like in vanilla.
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

        // Leave enchantments alone: if the second item carries any enchantments
        // (an enchanted book, or an enchanted item being merged in), a new
        // enchantment is being added, so let the prior work cost increase like
        // in vanilla. Repairs with no enchantment involved keep their cost.
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
