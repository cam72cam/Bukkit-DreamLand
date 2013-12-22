package me.cmesh.DreamLand.Listeners;

import me.cmesh.DreamLand.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.*;

public class DreamPlayerListener implements Listener {
	private DreamPlayer getPlayer(Player player) {
		return DreamLandPlugin.Instance.players.get(player);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		DreamLandPlugin.log.info("Player logged in " + event.getPlayer().getName());
		DreamLandPlugin.Instance.players.add(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerBedEnter(PlayerBedEnterEvent event)
	{
		getPlayer(event.getPlayer()).onBedEnterEvent(event);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerBedLeave(PlayerBedLeaveEvent event)
	{
		getPlayer(event.getPlayer()).onBedLeaveEvent(event);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		getPlayer(event.getPlayer()).onMoveEvent(event);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDamageEvent(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			DreamLandPlugin.Instance.players.get((Player)event.getEntity()).onDamageEvent(event);
		}
	}
}
