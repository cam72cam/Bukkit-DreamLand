package me.cmesh.DreamLand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DreamLandPlayerSetting {
	
	public Player get()
	{
		return player;
	}

	private Location Bed = null;
	public Boolean Respawn = false;
	private Long Attempt = new Long(0);
	public Player player;
	
	public static DreamLand plugin;
	public static final Logger log = Logger.getLogger("Minecraft");
	
	public DreamLandPlayerSetting(DreamLand instance)
	{
		plugin = instance;
	}
	public DreamLandPlayerSetting Update(Player player)
	{
		this.player = player;
		return this;
	}
	
	public void enterDream(Location bed, Boolean nightmare)
	{
		setBed(bed);
		savePlayerHealth();
		
		DreamLandWorldSetting setting = plugin.dream;
		if(nightmare)
		{
			setting = plugin.nightmare;
		}
		
		Location loc = setting.GetWorld().getSpawnLocation();
		
		plugin.loadChunk(loc);
		
		savePlayerInv(player.getWorld());
		loadPlayerInv(loc.getWorld());
		
		player.teleport(loc);
		
		player.sendMessage("going to" + loc.toString());
		
		if(!plugin.message.isEmpty())
		{
			player.sendMessage(plugin.message);
		}
		log.info(player.getName() + " is dreaming");
		return;
	}
	public void leaveDream()
	{
		player.setFireTicks(0);
		Location loc = getBed();
		plugin.loadChunk(loc);

		savePlayerInv(player.getWorld());
		loadPlayerInv(loc.getWorld());


		player.setFallDistance(0);
		player.teleport(loc);
		player.setFallDistance(0);
		
		loadPlayerHealth();
		
		log.info(player.getName() + " woke up");
	}
	
	
	
	public Boolean getWait()
	{
		Long time = plugin.getServer().getWorlds().get(0).getTime() - Attempt;
		if(time >= plugin.attemptWait)
		{
			Attempt = plugin.getServer().getWorlds().get(0).getTime();
			return true;
		}
   		else
   		{
   			player.sendMessage("Wait " + ((Long)((plugin.attemptWait - time)/30)).toString() + "s before trying again");
			return false;
   		}
	}

	
	//health
	public File playerHealthFile()
	{
		File healthFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Health");
		if (!healthFolder.exists()) 
		{
			healthFolder.mkdir();
		}
		return new File(healthFolder + File.separator + player.getName());
	}
	
	public void savePlayerHealth()
	{
		BufferedWriter bw;
		try 
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playerHealthFile())));
			bw.write(((Integer)player.getHealth()).toString());
			bw.close();
		}
		catch (FileNotFoundException e) {}
		catch (IOException e) {}
	}
	
	public void loadPlayerHealth()
	{
		File save = playerHealthFile();
		if (save.exists()) 
		{
			try 
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
				player.setHealth(Integer.parseInt(br.readLine()));
				br.close();
			}
			catch (IOException e) {}
			catch (java.lang.NumberFormatException e){}
			catch (java.lang.IllegalArgumentException e){}
		}
		return;
	}
	
	//Inventory store/switcher
	public File playerInv(World world)
	{
		File invFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Inventories");
		if (!invFolder.exists()) 
		{
			invFolder.mkdir();
		}
		return new File(invFolder + File.separator + player.getName() + "." + world.getName());
	}
	
	public void savePlayerInv(World world)
	{
		if(plugin.GetSetting(world).PersistInventory)
		{
			BufferedWriter bw;
			try 
			{
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playerInv(world))));
				
				ItemStack [] inv =player.getInventory().getContents();
				for(int i = 0; i<inv.length; i++)
				{
					ItemStack item = inv[i];
					if(item != null)
					{
						String temp = i + " " + item.getTypeId() + " " + item.getAmount() + " "+ item.getDurability();
						bw.write(temp);
						bw.newLine();
					}
				}
				bw.close();
			}
			catch (FileNotFoundException e) {}
			catch (IOException e) {}
		}
	}

	public void stringToInv(List<String> inv)
	{
		for(String item : inv)
		{
			String [] split = item.split(" ", 4);
			int spot = Integer.parseInt(split[0]);
			int itemId = Integer.parseInt(split[1]);
			int ammount = Integer.parseInt(split[2]);
			short damage = (short)Integer.parseInt(split[3]);
			
			player.getInventory().setItem(spot, new ItemStack(itemId, ammount, damage));
		}
	}

	@SuppressWarnings("deprecation")
	public void loadPlayerInv(World world)
	{		
		File save = playerInv(world);
		try 
		{
			if (!save.exists()) 
			{
				if(plugin.GetSetting(world).InitialInventoryClear)
				{
					player.getInventory().clear();
					if(plugin.kit.size() != 0)//TODO make per world!
					{
						stringToInv(plugin.kit);
					}
					player.updateInventory();
				}
				return;
			}
			
			if(plugin.GetSetting(world).PersistInventory)
			{
				player.getInventory().clear();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
				List<String> inv = new ArrayList<String>();
				
				String inputLine = br.readLine();
				while (inputLine != null)
				{
					inv.add(inputLine);
					inputLine = br.readLine();
				}
				stringToInv(inv);
				
				player.updateInventory();
			}
			return;
		}
		catch (IOException e){}
		catch (java.lang.NumberFormatException e){}
		player.sendMessage("There was an issue loading your inventory");
	}
	
	//saves bed locations of players
	public Location getBed() 
	{
		if(Bed != null)
		{
			return Bed;
		}
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
					Location bed = new Location(plugin.getServer().getWorld(world), Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]));
					bed = checkSpawnLoc(bed);
					Bed = bed;
					return bed;
				}
			}
			catch (IOException e) {}
			catch (java.lang.NumberFormatException e){}
		}
		log.info("There was an issue loading a player's bed location");
		return plugin.getServer().getWorlds().get(0).getSpawnLocation();
	}
	
	public void setBed(Location location) 
	{
		BufferedWriter bw;
		try 
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getBedFile(player))));
			bw.write(player.getWorld().getName());
			bw.newLine();
			bw.write(String.format("%f %f %f", location.getX(), location.getY(), location.getZ()));
			bw.close();
			Bed = location;
			return;
		}
		catch (FileNotFoundException e){}
		catch (IOException e){}
		log.info("There was an issue saving "+player.getName()+"'s bed location");
		
	}
	
	public File getBedFile(Player player)
	{
		File bedFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "BedLocations");
		if (!bedFolder.exists()) 
		{
			bedFolder.mkdir();
		}
		return new File(bedFolder + File.separator + player.getName());
	}
	
	public void setAttempt(Long time)
	{
		Attempt = time;
	}
	
	private Location checkSpawnLoc(Location location)
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
	//helper functions
	public Boolean Dreaming()
	{
		return InDreamLand() || (InNightmare() && plugin.nightmare.Chance != 0);
	}
	
	private Boolean InDreamLand()
	{
		return player.getWorld().equals(plugin.dream.GetWorld());
	}

	private Boolean InNightmare()
	{
		return player.getWorld().equals(plugin.nightmare.GetWorld());
	}
 
}
