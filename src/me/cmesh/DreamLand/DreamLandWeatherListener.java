package me.cmesh.DreamLand;

import org.bukkit.World;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherListener;

public class DreamLandWeatherListener extends WeatherListener
{
	public static DreamLand plugin;

	public DreamLandWeatherListener(DreamLand instance)
	{
		plugin = instance;
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
}
