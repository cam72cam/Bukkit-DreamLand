package me.cmesh.DreamLand;


import org.bukkit.block.BlockFace;
//import org.bukkit.block.Block;
import java.io.OutputStreamWriter;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
//import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.util.Vector;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.util.logging.Logger;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.BufferedWriter;

public class DreamLandPlayerListener extends PlayerListener
{
	public static DreamLand plugin;
	public static final Logger log = Logger.getLogger("Minecraft");
	
    public DreamLandPlayerListener(DreamLand instance)
    {
        plugin = instance;
    }

    
    //Main Functions
    public void onPlayerPortal(PlayerPortalEvent event)
    {
    	if (playerInDreamLand(event.getPlayer()))
    	{
    		event.setCancelled(true);
    	}
    }
  
    public void onPlayerInteract(PlayerInteractEvent event)
    {
    	if (plugin.dreamFly)
    	{
    		Player player = event.getPlayer();
    		if (plugin.checkpermissions(player,"dreamland.fly",true))
    		{
    			if (playerInDreamLand(player))
    			{
    				if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
    				{
    		    		boolean tool = false;
    		    		if (plugin.flyTool.contains("-1"))
    		    		{
    		    			tool = true;
    		    		}
    		    		if (plugin.flyTool.contains(Integer.toString(event.getPlayer().getItemInHand().getTypeId())))
    		    		{
    		    			tool = true;
    		    		}

    		    		if (tool)
    					{
    		    			Vector dir = player.getLocation().getDirection().multiply(plugin.flySpeed);
    		    			dir.setY(0.75);
    		    			player.setVelocity(dir);
    			        	player.setFallDistance(0);
    					}
    				}
    			}
    		}
    	}
    }
    
    public void onPlayerMove(PlayerMoveEvent event)
    {
    	Player player = event.getPlayer();
    	if (plugin.portalExplode)
    	{
    		if (playerInDreamLand(player))
    		{
    			Location portal = event.getTo();
            	if (portal.getBlock().getTypeId() == 90)
            	{
            		portal.getWorld().createExplosion(portal.getBlock().getRelative(BlockFace.UP).getLocation(),5);
            	}
            	else if (portal.getBlock().getRelative(BlockFace.UP).getTypeId() == 90)
            	{
            		portal.getWorld().createExplosion(portal.getBlock().getRelative(BlockFace.UP).getLocation(),5);
            	}
    		}
    	}
    	if (playerInDreamLand(player))
    	{
    		noWeather(player);
    		if (event.getTo().getY() < 0)
    		{
    			try
    			{
	    			player.setFallDistance(0);
	    			Location loc = loadLocation(player);
	    			loc.setY(loc.getY()+1.5);
	    			
	    			player.setFallDistance(0);
	    			player.teleport(loc);
	    			player.setFallDistance(0);
    			}
    			catch (java.lang.NullPointerException e)
    			{
					Location loc = plugin.getServer().getWorlds().get(0).getSpawnLocation();
					player.setFallDistance(0);
					player.teleport(loc);
					player.setFallDistance(0);
    			}
    			log.info(player.getName() + " left DreamLand");
    		}
    		if(playerSpawn(player))
    		{
    			player.teleport(getSpawn());
    		}
    	}
    }
	
    public void onPlayerBedEnter(PlayerBedEnterEvent event)
    {
    	Player player = event.getPlayer();
    	if (plugin.checkpermissions(player,"dreamland.goto",true) && !getLock(event.getPlayer()))
    	{
    		if (new Random().nextInt(plugin.chance) == 0)
    		{
	    		createLock(player);
	    		
				saveLocation(player, event.getBed().getLocation());
				
				Location loc = getSpawn();
				
				try
				{
					player.teleport(loc);
					playerSetSpawn(player);
				}
				catch (java.lang.NullPointerException e)
				{
					loc = dreamWorld().getSpawnLocation();					
					player.teleport(loc);
					saveSpawn(player);
					playerSetSpawn(player);
				}
				
				removeLock(event.getPlayer());
				
		    	log.info(event.getPlayer().getName() + " went to Dream Land");
    		}
    	}
    }

    
    //helper functions
    public World dreamWorld()
    {
    	return plugin.getServer().getWorld(plugin.getServer().getWorlds().get(0).getName()+"_skylands");
    }

    public Boolean playerInDreamLand(Player player)
    {
    	return player.getWorld().getName().equalsIgnoreCase(dreamWorld().getName());
    }

    public void noWeather(Player player)
    {
    	World world = player.getWorld();
    	world.setStorm(false);
    	world.setThundering(false);
    	world.setWeatherDuration(0);
    	//TODO have this happen less often
    }
    
    //used to tp 2 extra times (accounts for chunk loading time)
	private File spawnFile(Player player)
	{
		File lockFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Spawning");
		if (!lockFolder.exists()) 
		{
			lockFolder.mkdir();
		}
		return new File(lockFolder + File.separator + player.getName());
	}
    
	private Boolean playerSpawn(Player player)
	{
		File file1 = new File(spawnFile(player) + ".1");
		File file2 = new File(spawnFile(player) + ".2");
		if((file2).exists())
		{
			file2.delete();
			return false;
		}
		if((file1).exists())
		{
			file1.delete();
			return true;
		}
		return false;
	}
	
	private void playerSetSpawn(Player player)
	{
		File file1 = new File(spawnFile(player) + ".1");
		File file2 = new File(spawnFile(player) + ".2");
		try
		{
			file1.createNewFile();
			file2.createNewFile();
		}
		catch (IOException e)
		{
			e.toString();
		}
	}

	
	//saves bed locations of players
	public Location loadLocation(Player player) 
	{
		File save = getBedFile(player);
		if (!save.exists()) 
		{
			return null;
		}
		
		try 
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
			
			String world = br.readLine();
			String inputLine = br.readLine();
			
			if (inputLine == null || world == null) 
			{
				return null;
			}
			
			String splits[] = inputLine.split(" ", 3);
			return new Location(plugin.getServer().getWorld(world), Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]));
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		catch (java.lang.NumberFormatException e)
		{
			return null;
		}

		return null;
	}
	
	public void saveLocation(Player player, Location location) 
	{
		BufferedWriter bw;
		try 
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getBedFile(player))));
			bw.write(player.getWorld().getName());
			bw.newLine();
			bw.write(String.format("%f %f %f", location.getX(), location.getY(), location.getZ()));
			bw.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private File getBedFile(Player player)
	{
		File bedFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "BedLocations");
		if (!bedFolder.exists()) 
		{
			bedFolder.mkdir();
		}
		return new File(bedFolder + File.separator + player.getName());
	}


	//used to prevent concurrent modification exceptions
	private File lockFile(Player player)
	{
		File lockFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Lock");
		if (!lockFolder.exists()) 
		{
			lockFolder.mkdir();
		}
		return new File(lockFolder + File.separator + player.getName() + ".lock");
	}
	
	private void createLock(Player player)
	{
		try
		{
			lockFile(player).createNewFile();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Boolean getLock(Player player)
	{
		return lockFile(player).exists();
	}
	
	private void removeLock(Player player)
	{
		lockFile(player).delete();
	}

	
	//used to save the spawn location of a skylands world
	public File spawnWorldFile(World world)
	{
		File spawnFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "WorldSpawn");
		if (!spawnFolder.exists()) 
		{
			spawnFolder.mkdir();
		}
		return new File(spawnFolder + File.separator + world.getName() + ".spawn");
	}
	
    public Location getSpawn() 
	{
		File save = spawnWorldFile(dreamWorld());
		if (!save.exists()) 
		{
			return null;
		}
		
		try 
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
			String inputLine = br.readLine();
			if (inputLine == null) 
			{
				return null;
			}
			String splits[] = inputLine.split(" ", 3);
			return new Location(dreamWorld(), Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]));
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		catch (java.lang.NumberFormatException e)
		{
			return null;
		}

		return null;
	}

    public void saveSpawn(Player player) 
	{
		if(plugin.checkpermissions(player,"dreamland.setdreamspawn",true) && playerInDreamLand(player) && player.getLocation().getY() > 0)
 		{
			Location location = player.getLocation();
			location.setY(location.getY() + 5);
			File save = spawnWorldFile(dreamWorld());
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(save)));
				bw.write(String.format("%f %f %f", location.getX(), location.getY(), location.getZ()));
				bw.close();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}   