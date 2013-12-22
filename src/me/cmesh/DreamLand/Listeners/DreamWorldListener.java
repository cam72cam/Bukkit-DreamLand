package me.cmesh.DreamLand.Listeners;

import me.cmesh.DreamLand.*;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.*;

public class DreamWorldListener implements Listener {
	@EventHandler
	public void onPortalCreateEvent(PortalCreateEvent event) {
		DreamWorld world = DreamLandPlugin.Instance.worlds.getWorld(event.getWorld());
		world.onPortalCreateEvent(event);
	}
	@EventHandler
	public void onWorldInit(WorldInitEvent event) {
		
	}
}
