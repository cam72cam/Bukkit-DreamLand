package me.cmesh.DreamLand;

import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;

public class DreamLandEntityListener extends EntityListener
{
	public static DreamLand plugin;
	public static World dreamworld;

	public DreamLandEntityListener(DreamLand instance)
	{
		plugin = instance;
		dreamworld = plugin.getServer().getWorld(plugin.getServer().getWorlds().get(0).getName()+"_skylands");
	}

	public void onEntityDamage(EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			if(event.getEntity().getWorld() == dreamworld && plugin.dreamInvincible)
			{
				event.setCancelled(true);
	    		}
    		}
	}
}
