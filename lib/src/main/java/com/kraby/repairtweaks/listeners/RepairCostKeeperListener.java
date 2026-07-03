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
 * Handles the "prior work" penalty for anvil repairs that don't add any new
 * enchantment (repairing with a raw material, or with an unenchanted copy of
 * the item). For those repairs it both keeps the item's stored penalty from
 * growing and drops the penalty from the level cost of the repair itself, so an
 * already-penalised item can still be repaired cheaply. Adding an enchantment
 * (an enchanted book, or merging in an enchanted item) still raises and charges
 * the penalty, like in vanilla.
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

        int firstPriorWork = ((Repairable) firstItem.getItemMeta()).getRepairCost();

        // Keep the result's stored prior work penalty from growing.
        ItemStack result = e.getResult();
        Repairable resultMeta = (Repairable) result.getItemMeta();
        resultMeta.setRepairCost(firstPriorWork);
        result.setItemMeta(resultMeta);
        e.setResult(result);

        // Drop the prior work penalty from this operation's level cost too, so a
        // basic repair is never charged for an already-accumulated penalty -
        // only the normal repair cost (e.g. the material sacrifice) is left.
        // Vanilla adds each input item's stored penalty to the level cost, so
        // subtracting them removes exactly the penalty portion.
        int secondPriorWork = (secondItem.getItemMeta() instanceof Repairable)
            ? ((Repairable) secondItem.getItemMeta()).getRepairCost()
            : 0;
        inv.setRepairCost(Math.max(0, inv.getRepairCost() - firstPriorWork - secondPriorWork));
    }

}
