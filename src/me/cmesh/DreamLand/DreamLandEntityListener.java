package me.cmesh.DreamLand;

import org.bukkit.World;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
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
			if(playerInDreamLands(event) && plugin.dreamInvincible)
			{
				event.setCancelled(true);
    		}
		}
	}
	
	public void onWeatherChange(WeatherChangeEvent event)
	{
		World world = event.getWorld();
		if(world.equals(plugin.dreamWorld()))
		{
			event.setCancelled(true);
			world.setStorm(false);
			world.setThundering(false);
			world.setWeatherDuration(0);
		}
	}

	private Boolean playerInDreamLands(EntityDamageEvent event)
	{
		return (plugin.dreamWorld().equals(event.getEntity().getWorld()));
	}
}
