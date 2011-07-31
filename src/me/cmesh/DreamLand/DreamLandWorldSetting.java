package me.cmesh.DreamLand;

import org.bukkit.World;

public class DreamLandWorldSetting {
	public static DreamLand plugin;
	public String World = new String();
	public Boolean PersistInventory = false;
	public Boolean InitialInventoryClear = false;
	public Boolean Invincible = false;
	public Boolean Flaming = false;
	public Boolean Fly = false;
	public Integer Chance = 0;
	public Boolean Kit = false;
	
	public DreamLandWorldSetting(DreamLand instance)
	{
		plugin = instance;
	}
	public World GetWorld()
	{
		return plugin.getServer().getWorld(World);
	}
}
