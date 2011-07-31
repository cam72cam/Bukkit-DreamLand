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
			if(plugin.GetSetting(event.getEntity().getWorld()).Invincible)
			{
				event.setCancelled(true);
    		}
		}
	}
}
