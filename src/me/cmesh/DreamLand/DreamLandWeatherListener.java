package me.cmesh.DreamLand;

import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherListener;

public class DreamLandWeatherListener extends WeatherListener
{
	public static DreamLand plugin;

	public DreamLandWeatherListener(DreamLand instance)
	{
		plugin = instance;
	}
	
	public void onWeatherChange( WeatherChangeEvent event )
	{
		if( !event.isCancelled() && event.toWeatherState() && event.getWorld().equals(plugin.dream.getWorld()))
		{
			event.setCancelled( true );
		}
	}

	public void onThunderChange( ThunderChangeEvent event )
	{
		if( !event.isCancelled() && event.toThunderState() && event.getWorld().equals(plugin.dream.getWorld()) )
		{
			event.setCancelled( true );
		}
	}

	public void onLightningStrike( LightningStrikeEvent event )
	{
		if(!event.isCancelled() && event.getWorld().equals(plugin.dream.getWorld()) )
		{
			event.setCancelled( true );
		}
	}
}
