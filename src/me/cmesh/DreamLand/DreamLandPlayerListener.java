package me.cmesh.DreamLand;

import org.bukkit.block.BlockFace;
import org.bukkit.block.Block;
import java.io.OutputStreamWriter;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
//import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
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
					if (plugin.flyTool.contains("-1") || plugin.flyTool.contains(Integer.toString(event.getPlayer().getItemInHand().getTypeId())))
					{
						if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
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
		
		if (playerDreaming(player))
		{
			Boolean tick = tick();
			
			if(tick)
			{
				noWeather(player);
			}
			if (event.getTo().getY() < 0)
			{
				leaveDream(player);
			}
			if(plugin.DoubleSpawn.get(player.getName()) > 0)
			{
				plugin.DoubleSpawn.put(player.getName(), plugin.DoubleSpawn.get(player.getName()) - 1);
				player.teleport(player.getWorld().getSpawnLocation());
			}
			else
			{
				if(tick && playerInNightmare(player))
		    	{
		    		player.setFireTicks(3*30);
		    	}
			}
			if(plugin.morningReturn)
			{
				long time = loadLocation(player).getWorld().getTime();
				if(time >=0 && time <= 12000)
				{
					player.sendMessage("It is morning, WAKEUP!");
					leaveDream(player); 
				}
			}
		}
	}
	
	public void onPlayerBedEnter(PlayerBedEnterEvent event)
	{
		Player player = event.getPlayer();
		if (plugin.checkpermissions(player,"dreamland.goto",true) && !playerDreaming(player))
		{
			if ((plugin.attemptWait == 0 || getWait(player)) && new Random().nextInt(plugin.chance) == 0)
			{
				enterDream(player, event.getBed().getLocation(), new Random().nextInt(plugin.nightmareChance) == 0);
    		}
			else
			{
				if(getWait(player))
				{
					setAttemptTime(player);
				}
			}
    	}
    }
	
	public void onPlayerQuit(PlayerQuitEvent event)
	{
    	Player player = event.getPlayer();
    	
    	if (playerDreaming(player) && plugin.teleportOnQuit)
		{
    		leaveDream(player);
		}
	}

	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();
    	if (playerDreaming(player))
		{
			plugin.DoubleSpawn.put(player.getName(), 2);
		}
	}
	
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		Player player = event.getPlayer();
		plugin.Beds.put(player.getName(), loadLocation(player));
		plugin.DoubleSpawn.put(player.getName(), 0);
		plugin.Attempt.put(player.getName(), new Long(0));
		//other possible initializers
	}

	//helper functions
 
	public Boolean playerDreaming(Player player)
	{
		return playerInDreamLand(player) || (playerInNightmare(player) && plugin.nightmare);
	}
	
	private Boolean playerInDreamLand(Player player)
	{
		return player.getWorld().getName().equalsIgnoreCase(plugin.dreamWorld().getName());
	}

	private Boolean playerInNightmare(Player player)
	{
		return player.getWorld().getName().equalsIgnoreCase(plugin.nightmareWorld);
	}
	
	private void noWeather(Player player)
	{
		World world = player.getWorld();
		world.setStorm(false);
		world.setThundering(false);
		world.setWeatherDuration(0);
		//TODO have this happen less often
	}

    private void message(Player player)
    { 
    	File save = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "message.txt");
		if (!save.exists()) 
		{
			return;
		}
		
		try 
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));

			String inputLine = br.readLine();
			
			while(inputLine != null) 
			{
				player.sendMessage(inputLine);
				inputLine = br.readLine();
			}
			
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
    }

    private void enterDream(Player player, Location bed, Boolean nightmare)
    {
		saveLocation(player, bed);

		savePlayerHealth(player);
			
		if(plugin.seperateInv)
		{
			savePlayerInv(player, player.getWorld());
			if(!nightmare)
			{
				loadPlayerInv(player, plugin.dreamWorld());
			}
		}
		
		clearAttempt(player);
		
		Location loc = null; 
		if(nightmare)
		{
			loc = plugin.nightmareWorld().getSpawnLocation();
		}
		else
		{
			loc = plugin.dreamWorld().getSpawnLocation();
		}
		player.teleport(loc);
		plugin.DoubleSpawn.put(player.getName(), 1);
		
		if(plugin.message)
		{
			message(player);
		}
    	log.info(player.getName() + " went to Dream Land");
    	return;
    }
 
    private void leaveDream(Player player)
    {
    	player.setFireTicks(0);
    	Location loc = null;
		try
		{
			player.setFallDistance(0);
			loc = loadLocation(player);
			loc.getWorld().loadChunk(loc.getBlock().getChunk());
			player.setFallDistance(0);
			player.teleport(checkBedSpawnLoc(loc));
			player.setFallDistance(0);
		}
		catch (java.lang.NullPointerException e)
		{
			loc.getWorld().loadChunk(loc.getBlock().getChunk());
			loc = plugin.getServer().getWorlds().get(0).getSpawnLocation();
			player.setFallDistance(0);
			player.teleport(loc);
			player.setFallDistance(0);
		}
		if(plugin.seperateInv)
		{
			savePlayerInv(player, plugin.dreamWorld());
			player.getInventory().clear();
			loadPlayerInv(player, loc.getWorld());
		}
		log.info(player.getName() + " left DreamLand");
		loadPlayerHealth(player);
    }

	private Boolean tick()
	{
		int timeoutSeconds = 3; 
		if(plugin.tick + timeoutSeconds*30 <= plugin.getServer().getWorlds().get(0).getTime())
		{
			plugin.tick = plugin.getServer().getWorlds().get(0).getTime();
			return true;
		}
		return false;
	}

    
    //health
    private File playerHealthFile(Player player)
    {
    	File healthFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Health");
		if (!healthFolder.exists()) 
		{
			healthFolder.mkdir();
		}
		return new File(healthFolder + File.separator + player.getName());
    }
    
    private void savePlayerHealth(Player player)
    {
    	BufferedWriter bw;
		try 
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playerHealthFile(player))));
			bw.write(((Integer)player.getHealth()).toString());
			bw.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void loadPlayerHealth(Player player)
    {
    	File save = playerHealthFile(player);
		if (!save.exists()) 
		{
			return;
		}
		
		try 
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
			player.setHealth(Integer.parseInt(br.readLine()));
			player.sendMessage("Health:" + ((Integer)player.getHealth()).toString());
			br.close();
		}
		catch (IOException e) 
		{
			return;
		}
		catch (java.lang.NumberFormatException e)
		{
			return;
		}
		catch (java.lang.IllegalArgumentException e)
		{
			return;
		}
    }
    
    
    //wait time
	private void setAttemptTime(Player player)
	{
		plugin.Attempt.put(player.getName(), plugin.getServer().getWorlds().get(0).getTime());
	}
	
	private void clearAttempt(Player player)
	{
		plugin.Attempt.put(player.getName(), new Long(0));
	}
	
	private Boolean getWait(Player player)
	{
		Long time = plugin.getServer().getWorlds().get(0).getTime() - plugin.Attempt.get(player.getName());
		if(time >= plugin.attemptWait)
		{
			setAttemptTime(player);
			return true;
		}
   		else
   		{
   			player.sendMessage("Wait " + ((Long)((plugin.attemptWait - time)/30)).toString() + "s before trying again");
			return false;
   		}
	}

	
	//Inventory store/switcher
	private File playerInv(Player player, World world)
	{
		File invFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Inventories");
		if (!invFolder.exists()) 
		{
			invFolder.mkdir();
		}
		return new File(invFolder + File.separator + player.getName() + "." + world.getName());
	}
	
	private void savePlayerInv(Player player, World world)
	{
		BufferedWriter bw;
		try 
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playerInv(player, world))));
			
			ItemStack [] inv =player.getInventory().getContents();
			
			for(int i = 0; i<inv.length; i++)
			{
				ItemStack item = inv[i];
				String temp = "Empty";
				try
				{
					temp = i + " " + item.getTypeId() + " " + item.getAmount() + " "+ item.getDurability();
					bw.write(temp);
					bw.newLine();
				}
				catch (java.lang.NullPointerException e)
				{
					//log.info("Exception");
				}
			}
			bw.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private void stringToInv(Player player, List<String> inv)
	{
		player.getInventory().clear();
		
		for(String item : inv)
		{
			try
			{
				String [] split = item.split(" ", 4);
				
				int spot = Integer.parseInt(split[0]);
				int itemId = Integer.parseInt(split[1]);
				int ammount = Integer.parseInt(split[2]);
				short damage = (short)Integer.parseInt(split[3]);
				
				player.getInventory().setItem(spot, new ItemStack(itemId, ammount, damage));

			}
			catch (java.lang.NumberFormatException e)
			{
				player.sendMessage("There was an issue loading your inventory");
			}
		}
		//only way to get inv to update 
		//TODO Do this properly!
		player.updateInventory();
	}

	@SuppressWarnings("deprecation")
	private void loadPlayerInv(Player player, World world)
	{
		
		File save = playerInv(player, world);
		try 
		{
			if (!save.exists()) 
			{
				if(plugin.kit && world.getName() == plugin.dreamWorld().getName())
				{
					save = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "kit.txt");
					if (!save.exists()) 
					{
						return;
					}
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
					
					List<String> inv = new ArrayList<String>();
					String inputLine = br.readLine();
					
					int count =0;
					while (inputLine != null)
					{
						inv.add(count + " " + inputLine + " 0");
						inputLine = br.readLine();
						count++;
					}
					
					stringToInv(player, inv);
					return;
				}
				else
				{
					if(plugin.seperateInvInitial)
					{
						player.getInventory().clear();
						//only way to get inv to update 
						//TODO Do this properly!
						player.updateInventory();
					}
					return;
				}
			}
		
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
			
			List<String> inv = new ArrayList<String>();
			String inputLine = br.readLine();
			
			while (inputLine != null)
			{
				inv.add(inputLine);
				inputLine = br.readLine();
			}
			
			stringToInv(player, inv);
			return;
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		catch (java.lang.NumberFormatException e)
		{
			e.printStackTrace();
		}
	}
	
	
	//saves bed locations of players
	private Location loadLocation(Player player) 
	{
		if(plugin.Beds.containsKey(player.getName()))
		{
			return plugin.Beds.get(player.getName());
		}
		else
		{
			File save = getBedFile(player);
			if (save.exists()) 
			{
				try 
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
					
					String world = br.readLine();
					String inputLine = br.readLine();
					
					if (inputLine != null && world != null) 
					{
						String splits[] = inputLine.replace(',', '.').split(" ", 3);
						return new Location(plugin.getServer().getWorld(world), Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]));
					}
				}
				catch (IOException e) {log.info("There was an issue loading a player's bed location");}
				catch (java.lang.NumberFormatException e){log.info("There was an loading saving a player's bed location");}
			}
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
		catch (FileNotFoundException e){log.info("There was an issue saving a player's bed location");}
		catch (IOException e) {log.info("There was an issue saving a player's bed location");}
		
		plugin.Beds.put(player.getName(), location);
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


	public Location checkBedSpawnLoc(Location location)
	{
		Block block = location.getBlock();
		Block blockCheck = null;
		double spawnoffset = 0.5;
		
		if (block.getRelative(BlockFace.NORTH).getType() == Material.AIR) 
		{
			blockCheck = block.getRelative(BlockFace.NORTH);
			Location blockCheckLoc = blockCheck.getLocation();
			
			if (blockCheck.getRelative(BlockFace.UP).getType() == Material.AIR) 
			{
				blockCheckLoc.setX(blockCheckLoc.getX()-spawnoffset);
				
				return blockCheckLoc;
			}
		}
		else if (block.getRelative(BlockFace.EAST).getType() == Material.AIR) 
		{
			blockCheck = block.getRelative(BlockFace.EAST);
			Location blockCheckLoc = blockCheck.getLocation();
			
			if (blockCheck.getRelative(BlockFace.UP).getType() == Material.AIR) 
			{
				blockCheckLoc.setZ(blockCheckLoc.getZ()-spawnoffset);
				
				return blockCheckLoc;
			}
		}
		else if (block.getRelative(BlockFace.SOUTH).getType() == Material.AIR) 
		{
			blockCheck = block.getRelative(BlockFace.SOUTH);
			Location blockCheckLoc = blockCheck.getLocation();
			
			if (blockCheck.getRelative(BlockFace.UP).getType() == Material.AIR) 
			{
				blockCheckLoc.setX(blockCheckLoc.getX()+spawnoffset);
				
				return blockCheckLoc;
			}
		}
		else if (block.getRelative(BlockFace.WEST).getType() == Material.AIR) 
		{
			blockCheck = block.getRelative(BlockFace.WEST);
			Location blockCheckLoc = blockCheck.getLocation();
			
			if (blockCheck.getRelative(BlockFace.UP).getType() == Material.AIR) 
			{
				blockCheckLoc.setZ(blockCheckLoc.getZ()+spawnoffset);
				
				return blockCheckLoc;
			}
		}
		
		// Return default weird spawn
		location.setY(location.getY()+1.5);
		
		return location;
	}
}
