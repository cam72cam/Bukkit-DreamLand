package me.cmesh.DreamLand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;

public class DreamLandWorld {
	public static DreamLand plugin;
	public String World = new String();
	public Boolean PersistInventory = false;
	public Boolean InitialInventoryClear = false;
	public Boolean Invincible = false;
	public Boolean Flaming = false;
	public Boolean Fly = false;
	public Integer Chance = 0;
	public Boolean Kit = false;
	public List<String> Mobs = new ArrayList<String>();
	public Integer MobChance = 0;
	public Boolean ReturnToBed = true;
	
	public DreamLandWorld(DreamLand instance)
	{
		plugin = instance;
	}
	public World getWorld()
	{
		return plugin.getServer().getWorld(World);
	}
}
