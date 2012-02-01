package me.cmesh.DreamLand;

import org.bukkit.World;
import org.bukkit.event.weather.*;
import org.bukkit.event.*;

public class DreamLandWeatherListener implements Listener
{
	public static DreamLand plugin;

	public DreamLandWeatherListener(DreamLand instance)
	{
		plugin = instance;
		
		if(plugin.options.weatherDisable)
		{
			Load(plugin.dream.getWorld());
	        plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	private void Load(World world) {
		if(world.hasStorm())
		{
			world.setStorm(false);
			world.setThundering(false);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)	
	public void onWeatherChange( WeatherChangeEvent event )
	{
		if(!plugin.options.weatherDisable)
		{
			return;
		}
		
		if(!event.isCancelled() && event.toWeatherState() && event.getWorld().equals(plugin.dream.getWorld()))
		{
			event.setCancelled( true );
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onThunderChange( ThunderChangeEvent event )
	{
		if(!plugin.options.weatherDisable)
		{
			return;
		}
		if( !event.isCancelled() && event.toThunderState() && event.getWorld().equals(plugin.dream.getWorld()) )
		{
			event.setCancelled( true );
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onLightningStrike( LightningStrikeEvent event )
	{
		if(!plugin.options.weatherDisable)
		{
			return;
		}
		if(!event.isCancelled() && event.getWorld().equals(plugin.dream.getWorld()) )
		{
			event.setCancelled( true );
		}
	}
}
