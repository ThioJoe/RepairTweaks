# RepairTweaks

Tweaks the anvil repair behaviour of Minecraft.

- **Repair cost** : by default, repairing an item in an anvil increases its "prior work" penalty each time, which quickly makes the item too expensive to repair or enchant further. This plugin stops **repairs** from increasing that penalty — whether you repair with a **raw material** (gems, ingots, etc.) or with an **unenchanted** copy of the item — so an item can be repaired indefinitely. Only adding a **new enchantment** (an enchanted book, or merging in an enchanted item) still increases the penalty, like in vanilla.

## Commands

`/rtreload` : reload / regenerate the config file

## Config

See [config.yml](lib/src/main/resources/config.yml) for the config. The repair-cost tweak is enabled by default.
