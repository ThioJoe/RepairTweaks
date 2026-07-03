# RepairTweaks

Tweaks the anvil repair behaviour of Minecraft.

- **Repair cost** : by default, repairing an item in an anvil increases its "prior work" penalty each time, which quickly makes the item too expensive to repair or enchant further. This plugin stops repairs done with **raw materials** (gems, ingots, etc.) from increasing that penalty, so an item can be repaired with materials indefinitely. Applying **enchantments** (or combining two items) still increases the penalty like in vanilla.

## Commands

`/rtreload` : reload / regenerate the config file

## Config

See [config.yml](lib/src/main/resources/config.yml) for the config. The repair-cost tweak is enabled by default.
