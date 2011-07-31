package me.cmesh.DreamLand;

import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

public class DreamLandPlayerListener extends PlayerListener
{
	private static DreamLand plugin;
	private static final Logger log = Logger.getLogger("Minecraft");
	
	public DreamLandPlayerListener(DreamLand instance)
	{
		plugin = instance;
	}

	//Main Functions
	public void onPlayerPortal(PlayerPortalEvent event)
	{
		event.setCancelled(plugin.getPlayer(event.getPlayer()).Dreaming());
	}
  
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (!plugin.anyoneCanGo && !plugin.checkPermissions(player,"dreamland.fly",true))
		{
			return;
		}
		if (!plugin.getSetting(player.getWorld()).Fly)
		{
			return;
		}
		if (plugin.flyTool != event.getPlayer().getItemInHand().getTypeId())
		{
			return;
		}
				
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			Vector dir = player.getLocation().getDirection().multiply(plugin.flySpeed);
			dir.setY(dir.getY()+0.60);
			player.setVelocity(dir);
			player.setFallDistance(0);
		}
	}
	
	public void onPlayerMove(PlayerMoveEvent event)
	{
		DreamLandPlayer player = plugin.getPlayer(event.getPlayer());
		DreamLandWorld world = plugin.getSetting(player.get().getWorld());
		
		if (player.Dreaming())
		{
			if (event.getTo().getY() < 0)
			{
				player.leaveDream();
				return;
			}
			if(plugin.morningReturn)
			{
				long time = player.getBed().getWorld().getTime();
				if(time >=0 && time <= 12000)
				{
					player.get().sendMessage("It is morning, WAKEUP!");
					player.leaveDream(); 
					return;
				}
			}
			if(world.Flaming)
			{
				player.get().setFireTicks(3*30);
			}
			if(new Random().nextInt(1000) < world.MobChance)
			{
				for(String mob : world.Mobs)
				{
					CreatureType ct = CreatureType.fromName(mob);
		            if (ct == null) continue;
		            int amount = new Random().nextInt(3);
		            for (int i = 0; i < amount; i++)
		            {
		            	Location loc = player.get().getLocation();
		            	loc.setX(loc.getX() + 10);
		                world.getWorld().spawnCreature(loc, ct);
		            	log.info("spawned " +mob);
		            }
				}
			}
		}
	}
	
	public void onPlayerBedEnter(PlayerBedEnterEvent event)
	{
		DreamLandPlayer player = plugin.getPlayer(event.getPlayer());
		
		if(player.Dreaming())
		{
			return;
		}
		
		if (plugin.anyoneCanGo || plugin.checkPermissions(player.get(),"dreamland.goto",true))
		{
			
			if ((plugin.attemptWait == 0 || player.getWait()) && new Random().nextInt(100) < plugin.dream.Chance)
			{
				event.setCancelled(true);
				
				Boolean nightmare = (plugin.nightmare.Chance != 0) && new Random().nextInt(100) < plugin.nightmare.Chance;
				
				player.enterDream(player.get().getLocation(),nightmare);
				player.setAttempt(new Long(0));
			}
			else
			{
				if(player.getWait())
				{
					player.setAttempt(plugin.getServer().getWorlds().get(0).getTime());
				}
			}
		}
	}
	
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		DreamLandPlayer player = plugin.getPlayer(event.getPlayer());
		if (player.Dreaming())
		{
			player.leaveDream();
		}
	}

	
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		DreamLandPlayer player = plugin.getPlayer(event.getPlayer());
		
		Location loc =  player.getBed();
		
		plugin.loadChunk(loc);
		event.setRespawnLocation(loc);
		
		player.savePlayerInv(player.get().getWorld());
		player.loadPlayerInv(loc.getWorld());
		
		log.info(player.get().getName() + " woke up");
		
	}

	
	public void onPlayerKick(PlayerKickEvent event)
	{
		//TODO make this only for when moving between worlds
		if(event.getReason().contains("moved too quickly")) 
		{
			event.setCancelled(true);
		}
	}
	
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		plugin.createPlayer(event.getPlayer());
	}

}
