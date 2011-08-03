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
	
	public void load(String world)
	{
		plugin.getConfiguration().load();
		
		String endpoint = "dreamland.worlds." + world + ".";
		World = plugin.getConfiguration().getString(endpoint + "name",World);
		PersistInventory = plugin.getConfiguration().getBoolean("dreamland.worlds.dream.persistInventory",PersistInventory);
		InitialInventoryClear = plugin.getConfiguration().getBoolean(endpoint + "clearInitialInventory", InitialInventoryClear);
		Invincible = plugin.getConfiguration().getBoolean(endpoint + "invincible", Invincible);
		Fly = plugin.getConfiguration().getBoolean(endpoint + "fly", Fly);
		Flaming = plugin.getConfiguration().getBoolean(endpoint + "flaming", Flaming);
		Kit = plugin.getConfiguration().getBoolean(endpoint + "kit", Kit);
		Chance = plugin.getConfiguration().getInt("dreamland.chance."+world,Chance);
		ReturnToBed = plugin.getConfiguration().getBoolean(endpoint + "returnToBed", ReturnToBed);
		
		MobChance = plugin.getConfiguration().getInt("dreamland.worlds.dream.mobChance",0);
		try
		{
			for(String node : plugin.getConfiguration().getKeys(endpoint +"mobs"))
			{
				if(plugin.getConfiguration().getBoolean("dreamland.worlds.dream.mobs."+node, true))
				{
					Mobs.add(node);
				}
			}
		}
		catch (java.lang.NullPointerException e){}
		
		plugin.getConfiguration().save();
		
	}
}
