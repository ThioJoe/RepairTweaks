package com.kraby.repairtweaks.utils;

import org.bukkit.plugin.Plugin;

public class MainConfig extends ConfigAccessor {

	public MainConfig(Plugin plugin, String fileName) {
		super(plugin, fileName);
	}

	public boolean isRepairDontIncreaseCost () {
		return config.getBoolean("repair_dont_increase_cost", true);
	}

}
