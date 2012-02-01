package me.cmesh.DreamLand;

import org.bukkit.event.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;

public class DreamLandEntityListener implements Listener
{
	public static DreamLand plugin;

	public DreamLandEntityListener(DreamLand instance)
	{
		plugin = instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.HIGH)	
	public void onCreatureSpawnEvent(CreatureSpawnEvent event)
	{
		if(event.getLocation().getWorld().equals(plugin.dream.getWorld()))
		{
			if(plugin.dream.MobChance == 0)
			{
				switch (event.getCreatureType())
				{
					case CHICKEN: case COW: case SHEEP: case PIG: case SQUID: case VILLAGER: return;
					default: event.setCancelled(true);
				}
			}
		}	
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			if(plugin.world(event.getEntity().getWorld()).Invincible)
			{
				event.setCancelled(true);
    		}
		}
	}
}
