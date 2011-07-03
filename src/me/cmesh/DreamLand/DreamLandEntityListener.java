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
    		if(event.getEntity().getWorld() ==  plugin.getServer().getWorld(plugin.getServer().getWorlds().get(0).getName()+"_skylands") && plugin.dreamInvincible)
    		{
    				event.setCancelled(true);
    		}
    	}
    }
}