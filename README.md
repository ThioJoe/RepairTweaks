# RepairTweaks

Tweaks the anvil repair behaviour of Minecraft.

- **Repair cost** : by default, repairing an item in an anvil increases its "prior work" penalty each time, which quickly makes the item too expensive to repair or enchant further. This plugin frees **repairs** from that penalty — whether you repair with a **raw material** (gems, ingots, etc.) or with an **unenchanted** copy of the item, the repair neither increases the penalty nor charges any penalty already on the item, so it only costs the normal repair amount and an item can be repaired indefinitely. That includes items whose accumulated penalty would make vanilla refuse with "Too Expensive!" — repairs bypass that cap, since the penalty isn't charged anyway. Plain **renames** are freed from the penalty too: renaming an item always costs just 1 level. Only adding a **new enchantment** (an enchanted book, or merging in an enchanted item) still increases and charges the penalty and still hits the vanilla 40-level "Too Expensive!" limit, like in vanilla.

## Commands

`/rtreload` : reload / regenerate the config file

## Config

See [config.yml](lib/src/main/resources/config.yml) for the config. The repair-cost tweak is enabled by default.
