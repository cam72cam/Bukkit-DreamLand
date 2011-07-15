package me.cmesh.DreamLand;

import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerKickEvent;
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
	public void onPlayerKick(PlayerKickEvent event)
	{
		//TODO make this only for when moving between worlds
		if(event.getReason().contains("moved too quickly")) 
		{
			event.setCancelled(true);
		}
	}
	private Boolean playerInDreamLands(EntityDamageEvent event)
	{
		return (plugin.dreamWorld().equals(event.getEntity().getWorld()));
	}
}
