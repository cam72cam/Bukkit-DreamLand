package me.cmesh.DreamLand;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

public class DreamLandPlayerListener extends PlayerListener
{
	private static DreamLand plugin;
	
	public DreamLandPlayerListener(DreamLand instance)
	{
		plugin = instance;
	}

	public void onPlayerPortal(PlayerPortalEvent event)
	{
		event.setCancelled(plugin.player(event.getPlayer()).Dreaming());
	}
  
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		DreamLandPlayer player = plugin.player(event.getPlayer());
		Block block = event.getClickedBlock();
		
		if (player.hasPermission("dreamland.fly",true))
			if (plugin.world(player.getWorld()).Fly)
				if (plugin.options.flyTool == event.getPlayer().getItemInHand().getTypeId())
					if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
					{
						Vector dir = player.getLocation().getDirection().multiply(plugin.options.flySpeed);
						dir.setY(dir.getY()+0.60);
						player.self().setVelocity(dir);
						player.self().setFallDistance(0);
					}
		
		// Attach a SIGN_POST to a BED with a RightClick
		if (plugin.options.signedBed && player.hasPermission("dreamland.signbed", true))
			if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
				if(block.getType().equals(Material.SIGN_POST)) 
					if (block.getRelative(BlockFace.NORTH).getTypeId() == 26) 
					{
						block.setTypeId(68); //Change to Wall Sign
						block.setData((byte) 0x5); // Set Direction
					}
					else if (block.getRelative(BlockFace.EAST).getTypeId() == 26) 
					{
						block.setTypeId(68); //Change to Wall Sign
						block.setData((byte) 0x3); // Set Direction
					}
					else if (block.getRelative(BlockFace.SOUTH).getTypeId() == 26) 
					{
						block.setTypeId(68); //Change to Wall Sign
						block.setData((byte) 0x4); // Set Direction
					}
					else if (block.getRelative(BlockFace.WEST).getTypeId() == 26) 
					{
						block.setTypeId(68); //Change to Wall Sign
						block.setData((byte) 0x2); // Set Direction
					}
	}
	
	public void onPlayerMove(PlayerMoveEvent event)
	{
		DreamLandPlayer player = plugin.player(event.getPlayer());
		DreamLandWorld world = plugin.world(player.getWorld());
		
		if (player.Dreaming())
		{
			if (event.getTo().getY() < 0)
			{
				player.leaveDream();
				return;
			}
			if(plugin.options.morningReturn && !player.hasPermission("dreamland.nowakeup", false))
			{
				long time = player.getBedWorld().getTime();
				if(time >=0 && time <= 12000)
				{
					player.sendMessage("It is morning, WAKEUP!");
					player.leaveDream(); 
					return;
				}
			}
			if(world.Flaming)
			{
				player.self().setFireTicks(3*30);
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
		            	Location loc = player.getLocation();
		            	loc.setX(loc.getX() + 10);
		                world.getWorld().spawnCreature(loc, ct);
		            }
				}
			}
		}
	}
	
	public void onPlayerBedEnter(PlayerBedEnterEvent event)
	{
		DreamLandPlayer player = plugin.player(event.getPlayer());
		Block block = event.getBed();
		
		if(player.Dreaming()){return;}
		
		if (plugin.options.anyoneCanGo || player.hasPermission("dreamland.goto",plugin.options.anyoneCanGo))
		{	
			if(checkBedSigned(block))
			{				
				if ((plugin.options.attemptWait == 0 || player.getWait()) && new Random().nextInt(100) < plugin.dream.Chance)
				{
					event.setCancelled(true);
					
					Boolean nightmare = (plugin.nightmare.Chance != 0) && new Random().nextInt(100) < plugin.nightmare.Chance;
					
					player.enterDream(player.getLocation(),nightmare);
					
					player.setAttempt(new Long(0));
					return;
				}
				if(player.getWait())
				{
					player.setAttempt(player.getWorld().getTime());
				}
			}
		}
	}
	
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		DreamLandPlayer player = plugin.player(event.getPlayer());
		if (player.Dreaming())
		{
			player.leaveDream();
		}
		plugin.removePlayer(player);
	}
	
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if(plugin.player(event.getPlayer()).Dreaming())
		{
			event.setRespawnLocation(plugin.player(event.getPlayer()).respawn());
		}
	}
	
	public void onPlayerKick(PlayerKickEvent event)
	{
		if(event.getReason().contains("moved too quickly")) 
		{
			event.setCancelled(true);
		}
	}
	
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		plugin.player(event.getPlayer());
	}
	public boolean checkBedSigned(Block block)
	{
		// If option is false always true
		if(!plugin.options.signedBed)
		{
			return true;
		}
		
		// Setting BedBlock2
		BlockFace face = null;
		if (block.getRelative(BlockFace.NORTH).getTypeId() == 26)
			face = BlockFace.NORTH;
		if (block.getRelative(BlockFace.EAST).getTypeId() == 26)
			face = BlockFace.EAST;
		if (block.getRelative(BlockFace.SOUTH).getTypeId() == 26)
			face = BlockFace.SOUTH;
		if (block.getRelative(BlockFace.WEST).getTypeId() == 26)
			face = BlockFace.WEST;
		
		Block block2 = face == null ? null : block.getRelative(face);
		
		// Sign Check BedBlock
		if (block.getRelative(BlockFace.NORTH).getTypeId() == 68)
			if (block.getRelative(BlockFace.NORTH).getData() == ((byte) 0x4))
				return true;
		
		if (block.getRelative(BlockFace.EAST).getTypeId() == 68)
			if (block.getRelative(BlockFace.EAST).getData() == ((byte) 0x2))
				return true;
		
		if (block.getRelative(BlockFace.SOUTH).getTypeId() == 68)
			if (block.getRelative(BlockFace.SOUTH).getData() == ((byte) 0x5))
				return true;
		
		if (block.getRelative(BlockFace.WEST).getTypeId() == 68)
			if (block.getRelative(BlockFace.WEST).getData() == ((byte) 0x4))
				return true;

		// Sign Check BedBlock2		
		if (block2.getRelative(BlockFace.NORTH).getTypeId() == 68)
			if (block2.getRelative(BlockFace.NORTH).getData() == ((byte) 0x4))
				return true;
		
		if (block2.getRelative(BlockFace.EAST).getTypeId() == 68)
			if (block2.getRelative(BlockFace.EAST).getData() == ((byte) 0x2))
				return true;
		
		if (block2.getRelative(BlockFace.SOUTH).getTypeId() == 68)
			if (block2.getRelative(BlockFace.SOUTH).getData() == ((byte) 0x5))
				return true;
		
		if (block2.getRelative(BlockFace.WEST).getTypeId() == 68)
			if (block2.getRelative(BlockFace.WEST).getData() == ((byte) 0x3))
				return true;
		
		return false;
	}
}
