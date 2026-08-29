package com.kraby.repairtweaks.listeners;

import org.bukkit.GameMode;
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
 * Handles the "prior work" penalty for anvil operations that don't add any
 * new enchantment. Repairs (with a raw material, or an unenchanted copy of
 * the item) keep the item's stored penalty from growing and drop the penalty
 * from the level cost, so only the base repair cost is charged; plain renames
 * charge just their base cost of 1 level. The vanilla 40-level
 * "Too Expensive!" cap is lifted so even a heavily penalised item can always
 * be repaired or renamed. Adding an enchantment (an enchanted book, or an
 * enchanted item merged in) keeps full vanilla behaviour: it raises and
 * charges the penalty and still becomes "Too Expensive!" past 40 levels.
 */
public class RepairCostKeeperListener implements Listener {

    /**
     * Vanilla's anvil limit: an operation reaching this many levels shows
     * "Too Expensive!" and yields no result (outside creative mode).
     */
    private static final int VANILLA_MAX_REPAIR_COST = 40;

    /**
     * The cap set while the tweak is enabled. Vanilla applies its cap (and
     * clears the result) before this event fires, so the cap must already be
     * lifted when a penalised repair is computed; the vanilla limit is then
     * re-applied below to every operation that isn't a discounted repair. The
     * distinctive value also identifies anvils this listener has touched, so
     * the lift can be undone after the tweak is disabled by a config reload.
     */
    private static final int LIFTED_MAX_REPAIR_COST = Integer.MAX_VALUE;

    @EventHandler
    public void keepCostOnRepair(PrepareAnvilEvent e) {
        AnvilInventory inv = e.getInventory();
        boolean enabled = RepairTweaks.singleton.config.isRepairDontIncreaseCost();

        // Keep the anvil's cap in sync with the config. Changing it here is
        // too late for the computation this event describes, but is in effect
        // for every following one - the next slot change or rename keystroke
        // triggers a fresh computation.
        if (enabled)
            inv.setMaximumRepairCost(LIFTED_MAX_REPAIR_COST);
        else if (inv.getMaximumRepairCost() == LIFTED_MAX_REPAIR_COST)
            inv.setMaximumRepairCost(VANILLA_MAX_REPAIR_COST);

        ItemStack firstItem = inv.getFirstItem();
        ItemStack secondItem = inv.getSecondItem();
        ItemStack result = e.getResult();
        // Invalid or impossible operations come through as an empty result -
        // null or an air stack, depending on the server implementation.
        boolean hasResult = result != null && !result.getType().isAir();

        if (isEmpty(firstItem))
            return;

        // A new enchantment only ever comes from the second slot: an enchanted
        // item being merged in, or an enchanted book. (A non-empty item always
        // has a meta, so a null meta doubles as "second slot empty".)
        ItemMeta secondItemMeta = isEmpty(secondItem) ? null : secondItem.getItemMeta();
        boolean addsEnchantment = secondItemMeta != null
            && (secondItemMeta.hasEnchants()
                || (secondItemMeta instanceof EnchantmentStorageMeta storage
                    && storage.hasStoredEnchants()));

        // A repair that adds no enchantment: keep the result's stored penalty
        // from growing, and drop the penalty from this operation's level cost
        // so only the base cost (e.g. the material sacrifice) is charged.
        // Vanilla adds each input item's stored penalty to the level cost, so
        // subtracting them removes exactly the penalty portion. The floor of 1
        // matters because vanilla refuses to hand over a result priced at 0;
        // any real operation's base cost is at least 1 and far below the cap.
        if (enabled && hasResult && secondItemMeta != null && !addsEnchantment
            && firstItem.getItemMeta() instanceof Repairable firstMeta
            && result.getItemMeta() instanceof Repairable resultMeta)
        {
            resultMeta.setRepairCost(firstMeta.getRepairCost());
            result.setItemMeta(resultMeta);
            e.setResult(result);

            int priorWork = firstMeta.getRepairCost()
                + (secondItemMeta instanceof Repairable secondMeta ? secondMeta.getRepairCost() : 0);
            inv.setRepairCost(Math.max(1, inv.getRepairCost() - priorWork));
            return;
        }

        // Renames (empty second slot) don't charge the penalty either: a
        // rename's base cost is always exactly 1 level, so charge just that.
        // Vanilla already leaves the stored penalty unchanged for renames.
        if (enabled && hasResult && secondItemMeta == null) {
            inv.setRepairCost(1);
            return;
        }

        // Everything else is vanilla's business: re-apply the end state its
        // cap would have produced, since this computation may have run with
        // the cap lifted (including the first one after the tweak was
        // disabled). A rename clamps at 39 levels; any other operation at or
        // past the cap yields no result outside creative mode. When the
        // computation ran with the cap in place, vanilla has already done all
        // of this itself and nothing below changes anything.
        if (hasResult && inv.getRepairCost() >= VANILLA_MAX_REPAIR_COST) {
            if (secondItemMeta == null)
                inv.setRepairCost(VANILLA_MAX_REPAIR_COST - 1);
            else if (e.getView().getPlayer().getGameMode() != GameMode.CREATIVE)
                e.setResult(null);
        }
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().isAir();
    }
}
