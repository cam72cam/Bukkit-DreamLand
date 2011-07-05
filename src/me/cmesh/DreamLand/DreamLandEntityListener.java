package me.cmesh.DreamLand;

import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;

public class DreamLandEntityListener extends EntityListener
{
	public static DreamLand plugin;

	public DreamLandEntityListener(DreamLand instance)
	{
		plugin = instance;
	}

	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			if(playerInDreamLands(EntityDamageEvent event) && plugin.dreamInvincible)
			{
				event.setCancelled(true);
	    		}
    		}
	}
	private Boolean playerInDreamLands(EntityDamageEvent event)
	{
		return (plugin.getServer().getWorld(plugin.getServer().getWorlds().get(0).getName()+"_skylands") == event.getEntity().getWorld());
	}
}
